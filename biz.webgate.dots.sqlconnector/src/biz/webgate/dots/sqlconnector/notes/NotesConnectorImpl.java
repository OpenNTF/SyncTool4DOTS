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
package biz.webgate.dots.sqlconnector.notes;

import java.util.Date;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Session;
import lotus.domino.View;
import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceConnection;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.ITargetConnection;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.MainTask;
import biz.webgate.dots.sqlconnector.SQLConnectorException;

public class NotesConnectorImpl implements ITargetConnection, ISourceConnection {

	private Session m_Session;
	private String m_Server;
	private String m_Path;
	private String m_ViewName;
	private Database m_ndbTarget;
	private View m_View;
	private String m_FormName;

	private Document m_docNext;
	private Document m_docProcess;
	private Exception m_LastException;

	public NotesConnectorImpl(Session session, String server, String path,
			String viewName, String formName) {
		super();
		m_Session = session;
		m_Server = server;
		m_Path = path;
		m_ViewName = viewName;
		m_FormName = formName;
	}

	@Override
	public boolean doConnect() {
		try {
			m_ndbTarget = m_Session.getDatabase(m_Server, m_Path);
			if (!m_ndbTarget.isOpen()) {
				m_ndbTarget.open();
			}
			if (!m_ndbTarget.isOpen()) {
				MainTask.getInstance().doLog(MainTask.LOG_NORMAL, "Error in opening "+ m_Server +"!!"+ m_Path);
				return false;
			}
			m_View = m_ndbTarget.getView(m_ViewName);
		} catch (Exception ex) {
			m_LastException = ex;
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public ITargetDataSet findTargetSetByStringKey(String strKey)
			throws SQLConnectorException {
		try {
			Document docResult = m_View.getDocumentByKey(strKey);
			if (docResult != null) {
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, false, strKey);
				return tdsRC;
			} else {
				docResult = m_ndbTarget.createDocument();
				docResult.replaceItemValue("Form", m_FormName);
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, true, strKey);
				return tdsRC;
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_ACCESS,
					"Error in getting documents", ex);
		}
	}

	@Override
	public ITargetDataSet findTargetSetByIntKey(int nKey)
			throws SQLConnectorException {
		try {
			Document docResult = m_View.getDocumentByKey(nKey);
			if (docResult != null) {
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, false, ""+nKey);
				return tdsRC;
			} else {
				docResult = m_ndbTarget.createDocument();
				docResult.replaceItemValue("Form", m_FormName);
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, true, ""+nKey);
				return tdsRC;
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_ACCESS,
					"Error in getting documents", ex);
		}
	}

	@Override
	public ITargetDataSet findTargetSetByDblKey(double nKey)
			throws SQLConnectorException {
		try {
			Document docResult = m_View.getDocumentByKey(nKey);
			if (docResult != null) {
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, false,""+nKey);
				return tdsRC;
			} else {
				docResult = m_ndbTarget.createDocument();
				docResult.replaceItemValue("Form", m_FormName);
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, true, ""+nKey);
				return tdsRC;
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_ACCESS,
					"Error in getting documents", ex);
		}
	}

	@Override
	public ITargetDataSet findTargetSetByDateKey(Date datKey)
			throws SQLConnectorException {
		try {
			Document docResult = m_View.getDocumentByKey(datKey);
			if (docResult != null) {
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, false, datKey);
				return tdsRC;
			} else {
				docResult = m_ndbTarget.createDocument();
				docResult.replaceItemValue("Form", m_FormName);
				ITargetDataSet tdsRC = new NotesDataImpl(docResult, true, datKey);
				return tdsRC;
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_ACCESS,
					"Error in getting documents", ex);
		}
	}

	@Override
	public boolean execute() {
		try {
			m_docProcess = null;
			m_docNext = m_View.getFirstDocument();
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean hasMoreElements() {
		if (m_docNext == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public ISourceDataSet nextElement() {
		try {
			if (m_docProcess != null) {
				m_docProcess.recycle();
			}
			m_docProcess = m_docNext;
			if (m_docProcess != null) {
				m_docNext = m_View.getNextDocument(m_docProcess);
				ISourceDataSet itRC = new NotesDataImpl(m_docProcess, false, null);
				return itRC;
			} else {
				return null;
			}
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTargetID() {
		return m_Server + "!!" + m_ndbTarget + "/" + m_ViewName + "/?Form="
				+ m_FormName;
	}

	@Override
	public void executeAfterExecutionJob(String strJobName)
			throws SQLConnectorException {
		try {
			Agent agtCurrent = m_ndbTarget.getAgent(strJobName);
			if (agtCurrent == null) {
				throw new SQLConnectorException(
						Constants.E_NOTES_AGENT_AFTER_EX_NOTFOUND, "Agent: "
								+ strJobName + " not found.");
			}
			agtCurrent.runOnServer();
		} catch (Exception ex) {
			m_LastException = ex;
			throw new SQLConnectorException(Constants.E_NOTES_AGENT_AFTER_EX,
					"Unkown Error during executeAfterExectionJob: "
							+ strJobName, ex);
		}

	}

	@Override
	public Exception getLastException() {
		return m_LastException;
	}

}
