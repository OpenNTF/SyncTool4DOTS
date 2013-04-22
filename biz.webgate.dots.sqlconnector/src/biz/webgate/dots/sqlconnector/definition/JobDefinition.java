/*
 * © Copyright WebGate Consulting AG, 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package biz.webgate.dots.sqlconnector.definition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceConnection;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.ITargetConnection;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorder;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorderStorageService;

public class JobDefinition {

	private ISourceConnection m_SourceConnection;
	private ITargetConnection m_TargetConnection;
	private TreeMap<Integer, FieldMapping> m_FieldMappingList;
	private int m_Sequence = 0;
	private String m_KeyField;
	private int m_KeyType;
	private boolean m_ChangeLog = false;
	private String m_JobID;
	private String m_JobAfterCreation;
	private String m_JobAfterUpdate;
	private String m_JobAfterExecution;

	public JobDefinition(ISourceConnection sourceConnection,
			ITargetConnection targetConnection, String strKeyField,
			int nKeyType, String strJobID, String strJobAfterCreation,
			String strJobAfterUpdate, String strJobAfterExecution) {
		super();
		m_SourceConnection = sourceConnection;
		m_TargetConnection = targetConnection;
		m_KeyField = strKeyField;
		m_KeyType = nKeyType;
		m_FieldMappingList = new TreeMap<Integer, FieldMapping>();
		m_JobID = strJobID;
		m_JobAfterCreation = strJobAfterCreation;
		m_JobAfterUpdate = strJobAfterUpdate;
		m_JobAfterExecution = strJobAfterExecution;
	}

	public void addFieldMapping(FieldMapping flMap) {
		m_FieldMappingList.put(new Integer(m_Sequence), flMap);
		m_Sequence++;
	}

	public void processJob(Session sesCurrent, Document docJob) {
		long nNew = 0;
		long nModified = 0;
		long nTotal = 0;
		SimpleDateFormat sdfCurrent = new SimpleDateFormat("HH:mm:ss");
		RichTextItem rtReport = null;
		try {
			rtReport = docJob.createRichTextItem("ReportRT");
			reportMsg(sdfCurrent, rtReport, "Connect to Source");
			if (!m_SourceConnection.doConnect()) {
				throw new SQLConnectorException(
						Constants.E_CONNECTOR_SOURCE_FAILED,
						"SourceConnection failed.", m_SourceConnection.getLastException());
			}
			reportMsg(sdfCurrent, rtReport, "Connect to Target");
			if (!m_TargetConnection.doConnect()) {
				throw new SQLConnectorException(
						Constants.E_CONNECTOR_TARGET_FAILED,
						"TargetConnection failed.", m_TargetConnection.getLastException());
			}
			reportMsg(sdfCurrent, rtReport, "Execute Source Selection");
			if (!m_SourceConnection.execute()) {
				throw new SQLConnectorException(
						Constants.E_CONNECTOR_SOURCE_EXEC,
						"Source.execute() failed.", m_SourceConnection.getLastException());
			}
			reportMsg(sdfCurrent, rtReport, "Start processing all records.");
			docJob.replaceItemValue("JobIsRunningT", "1");
			docJob.save(true, false, true);
			while (m_SourceConnection.hasMoreElements()) {
				try {
					ISourceDataSet sdsCurrent = m_SourceConnection
							.nextElement();
					ITargetDataSet tdsCurrent = null;
					nTotal++;
					switch (m_KeyType) {
					case 1:
						tdsCurrent = m_TargetConnection
								.findTargetSetByIntKey(sdsCurrent
										.getIntValue(m_KeyField));
						break;
					case 2:
						tdsCurrent = m_TargetConnection
								.findTargetSetByDblKey(sdsCurrent
										.getDblValue(m_KeyField));
						break;
					case 3:
						tdsCurrent = m_TargetConnection
								.findTargetSetByDateKey(sdsCurrent
										.getDateValue(m_KeyField));
						break;
					case 4:
						tdsCurrent = m_TargetConnection
								.findTargetSetByStringKey(sdsCurrent
										.getStringValue(m_KeyField));
						break;
					}
					if (tdsCurrent == null) {
						throw new SQLConnectorException(
								Constants.E_CONNECTOR_TARGET_KEY,
								"Finding a Target with KeyField " + m_KeyField
										+ " failed");
					}
					ChangeRecorder crCurrent = null;
					if (m_ChangeLog) {
						crCurrent = new ChangeRecorder(
								tdsCurrent.getPrimaryKeyAsString(), m_JobID,
								m_TargetConnection.getTargetID());
						tdsCurrent.setChangeRecorder(crCurrent);
					}
					Iterator<FieldMapping> itFM = m_FieldMappingList.values()
							.iterator();
					while (itFM.hasNext()) {
						FieldMapping flCurrent = itFM.next();
						flCurrent.processValues(sdsCurrent, tdsCurrent,
								sesCurrent);
					}
					if (tdsCurrent.isNew()) {
						nNew++;
						tdsCurrent.updateRecord();
						try {
							if (m_JobAfterCreation != null
									&& !m_JobAfterCreation.equals("")) {
								tdsCurrent
										.executeAfterCreateJob(m_JobAfterCreation);
							}
						} catch (Exception e) {
							writeException(e, docJob);
						}
					}
					if (tdsCurrent.isDirty()) {
						if (!tdsCurrent.isNew()) {
							tdsCurrent.updateRecord();
						}
						if (m_ChangeLog) {
							if (crCurrent != null) {
								try {
									ChangeRecorderStorageService.getInstance()
											.saveChangeRecorder(
													docJob.getParentDatabase(),
													crCurrent, docJob);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
						if (!tdsCurrent.isNew()) {
							try {
								if (m_JobAfterUpdate != null
										&& !m_JobAfterUpdate.equals("")) {
									tdsCurrent
											.executeAfterCreateJob(m_JobAfterUpdate);
								}
							} catch (Exception e) {
								writeException(e, docJob);
							}

						}
						nModified++;
					}
					// Alle 10 Dokumente einen Zwischentand.
					if (nTotal % 10 == 0) {
						writeSummary(docJob, nNew, nModified, nTotal, false);
						docJob.save(true,false,true);
					}
				} catch (Exception e) {
					reportMsg(sdfCurrent, rtReport,
							"Exception: " + e.getLocalizedMessage());
					writeException(e, docJob);
				}
			}

			try {
				if (m_JobAfterExecution != null
						&& !m_JobAfterExecution.equals("")) {
					m_TargetConnection
							.executeAfterExecutionJob(m_JobAfterExecution);
				}
			} catch (Exception e) {
				writeException(e, docJob);
			}

			reportMsg(sdfCurrent, rtReport, "Processing done!");
			reportMsg(sdfCurrent, rtReport, "New: " + nNew + " / Modified: "
					+ nModified + " / Total: " + nTotal);
			docJob.replaceItemValue("JobIsRunningT", "");

		} catch (Exception e) {
			reportMsg(sdfCurrent, rtReport,
					"Exception: " + e.getLocalizedMessage());
			writeException(e, docJob);
		}
		writeSummary(docJob, nNew, nModified, nTotal, true);

	}

	private void reportMsg(SimpleDateFormat sdfCurrent, RichTextItem rtReport,
			String strMsg) {
		try {
			rtReport.appendText(sdfCurrent.format(new Date()) + " " + strMsg);
			rtReport.addNewLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void writeSummary(Document docJob, long nNew, long nModifed,
			long nTotal, boolean finished) {
		try {
			docJob.replaceItemValue("NewRecordsN", nNew);
			docJob.replaceItemValue("ModifiedRecordsN", nModifed);
			docJob.replaceItemValue("TotalRecordsN", nTotal);
			if (finished) {
				docJob.replaceItemValue(
						"JobFinishedDT",
						docJob.getParentDatabase().getParent()
								.createDateTime(new Date()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeException(Exception exCurrent, Document docJob) {
		Document docEx;
		try {
			docEx = docJob.getParentDatabase().createDocument();
			docEx.replaceItemValue("form", "frmException");
			docEx.replaceItemValue("MessageT", exCurrent.getMessage());
			docEx.replaceItemValue("JobIDT", m_JobID);
			if (exCurrent instanceof SQLConnectorException) {
				docEx.replaceItemValue("EventIDN",
						((SQLConnectorException) exCurrent).getEventNr());
			}
			docEx.makeResponse(docJob);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exCurrent.printStackTrace(pw);
			RichTextItem rtCurrent = docEx.createRichTextItem("BodyRT");
			rtCurrent.appendText(sw.toString());
			docEx.save(true, false, true);
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}

	public boolean isChangeLog() {
		return m_ChangeLog;
	}

	public void setChangeLog(boolean changeLog) {
		m_ChangeLog = changeLog;
	}
}
