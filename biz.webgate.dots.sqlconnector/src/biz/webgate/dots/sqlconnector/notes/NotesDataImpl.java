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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import lotus.domino.Agent;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Item;
import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecord;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorder;

public class NotesDataImpl implements ITargetDataSet, ISourceDataSet {

	private Document m_docTarget;
	private boolean m_Dirty;
	private boolean m_New;
	private String m_PrimaryKey;
	private ChangeRecorder m_Recorder;
	private String m_LasField;

	private SimpleDateFormat m_sdfTESTER_DE = new SimpleDateFormat("dd.MM.yyyy");
	private SimpleDateFormat m_sdfTESTER_US = new SimpleDateFormat("MM/dd/yyyy");

	public NotesDataImpl(Document docTarget, boolean isNew, Object PrimaryKey) {
		super();
		m_docTarget = docTarget;
		m_New = isNew;
		m_Dirty = false;
		if (PrimaryKey != null) {
			m_PrimaryKey = PrimaryKey.toString();
		}

	}

	@Override
	public void setIntValue(String strField, int nValue)
			throws SQLConnectorException {
		try {
			if (m_docTarget.hasItem(strField)) {
				String strValue = getValueAsString(m_docTarget, strField);
				int nValueOld = m_docTarget.getItemValueInteger(strField);
				if (nValueOld != nValue
						|| ("".equals(strValue.trim()) && nValue == 0)) {
					m_docTarget.replaceItemValue(strField, nValue);
					recordChange(strField, "" + nValueOld, "" + nValue, 1);
					m_Dirty = true;
				}
			} else {
				m_docTarget.replaceItemValue(strField, nValue);
				recordChange(strField, "", "" + nValue, 1);
				m_Dirty = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ASSIGN,
					"Error during assign Value", e);
		}
	}

	private void recordChange(String strField, String strOldValue,
			String strNewValue, int nType) throws NotesException {
		if (m_Recorder != null) {
			ChangeRecord crCurrent = new ChangeRecord(strField, strOldValue,
					strNewValue, nType);
			m_Recorder.addChangeRecord(crCurrent);
		}
	}

	@Override
	public void setDblValue(String strField, double nValue)
			throws SQLConnectorException {
		try {
			if (m_docTarget.hasItem(strField)) {
				String strValue = getValueAsString(m_docTarget, strField);
				double nValueOld = m_docTarget.getItemValueDouble(strField);
				if (nValueOld != nValue
						|| ("".equals(strValue.trim()) && nValue == 0)) {
					m_docTarget.replaceItemValue(strField, nValue);
					recordChange(strField, "" + nValueOld, "" + nValue, 2);
					m_Dirty = true;
				}
			} else {
				m_docTarget.replaceItemValue(strField, nValue);
				recordChange(strField, "", "" + nValue, 2);
				m_Dirty = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ASSIGN,
					"Error during assign Value", e);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setDateValue(String strField, Date datValue)
			throws SQLConnectorException {
		try {

			DateTime dtNewValue = m_docTarget.getParentDatabase().getParent()
					.createDateTime(datValue);
			if (m_docTarget.hasItem(strField)) {
				String strValueTMP = m_docTarget.getItemValueString(strField);
				Vector vecDates = new Vector();
				try {
					vecDates = m_docTarget.getItemValueDateTimeArray(strField);
				} catch (Exception ex) {
					// nix machen)
				}
				if (vecDates.size() > 0 && vecDates.elementAt(0) != null
						&& vecDates.elementAt(0) instanceof DateTime) {
					DateTime dtFieldValue = (DateTime) vecDates.elementAt(0);
					if (dtNewValue.timeDifference(dtFieldValue) != 0) {
						m_docTarget.replaceItemValue(strField, dtNewValue);
						recordChange(strField, dtFieldValue.getLocalTime(),
								dtNewValue.getLocalTime(), 3);
						m_Dirty = true;
					}
				} else {
					m_docTarget.replaceItemValue(strField, dtNewValue);
					recordChange(strField, strValueTMP,
							dtNewValue.getLocalTime(), 3);
					m_Dirty = true;

				}
			} else {
				m_docTarget.replaceItemValue(strField, dtNewValue);
				recordChange(strField, "", dtNewValue.getLocalTime(), 3);
				m_Dirty = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ASSIGN,
					"Error during assign Value", e);
		}

	}

	@Override
	public void setStringValue(String strField, String strValue)
			throws SQLConnectorException {
		try {
			if (m_docTarget.hasItem(strField)) {
				String strResult = m_docTarget.getItemValueString(strField);
				if (strResult == null && strValue != null
						&& !"".equals(strValue)) {
					m_docTarget.replaceItemValue(strField, strValue);
					recordChange(strField, "", strValue, 4);
					m_Dirty = true;
				} else {
					if (!strResult.equals(strValue)) {
						m_docTarget.replaceItemValue(strField, strValue);
						recordChange(strField, strResult, strValue, 4);
						m_Dirty = true;
					}
				}
			} else {
				m_docTarget.replaceItemValue(strField, strValue);
				recordChange(strField, "", strValue, 4);
				m_Dirty = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ASSIGN,
					"Error during assign Value", e);
		}

	}

	@Override
	public boolean isDirty() {
		return m_Dirty;
	}

	@Override
	public void updateRecord() throws SQLConnectorException {
		try {
			m_docTarget.save(true, false, true);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_SAVE,
					"Error during save", ex);
		}
	}

	@Override
	public boolean isNew() {
		return m_New;
	}

	@Override
	public int getIntValue(String strField) throws SQLConnectorException {
		m_LasField = strField;
		try {
			return m_docTarget.getItemValueInteger(strField);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ACCESS,
					"Error in accesing field: " + strField, ex);
		}
	}

	@Override
	public double getDblValue(String strField) throws SQLConnectorException {
		m_LasField = strField;
		try {
			return m_docTarget.getItemValueDouble(strField);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ACCESS,
					"Error in accesing field: " + strField, ex);
		}
	}

	@Override
	public Date getDateValue(String strField) throws SQLConnectorException {
		m_LasField = strField;
		try {
			Date dtRC = null;
			Item itmCurrent = m_docTarget.getFirstItem(strField);
			if (itmCurrent.getType() == Item.TEXT) {
				String strDate = itmCurrent.getText();
				if (strDate.contains(".")) {
					dtRC = m_sdfTESTER_DE.parse(strDate);
				} else {
					dtRC = m_sdfTESTER_US.parse(strDate);
				}
			} else if (itmCurrent.getType() == Item.DATETIMES) {
				dtRC = itmCurrent.getDateTimeValue().toJavaDate();
			} else {
				throw new SQLConnectorException(
						Constants.E_NOTES_DOC_VALUE_ACCESS,
						"Error in accesing field: " + strField
								+ " is no date type", null);

			}
			return dtRC;
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ACCESS,
					"Error in accesing field: " + strField, ex);
		}
	}

	@Override
	public String getStringValue(String strField) throws SQLConnectorException {
		m_LasField = strField;
		try {
			return m_docTarget.getItemValueString(strField);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ACCESS,
					"Error in accesing field: " + strField, ex);
		}
	}

	@Override
	public String getPrimaryKeyAsString() {
		return m_PrimaryKey;
	}

	@Override
	public void setChangeRecorder(ChangeRecorder cr) {
		m_Recorder = cr;

	}

	public Document getDocTarget() {
		return m_docTarget;
	}

	@Override
	public void executeAfterUpdateJob(String strJobName)
			throws SQLConnectorException {
		try {
			Agent agtCurrent = m_docTarget.getParentDatabase().getAgent(
					strJobName);
			if (agtCurrent == null) {
				throw new SQLConnectorException(
						Constants.E_NOTES_AGENT_AFTER_UP_NOTFOUND, "Agent: "
								+ strJobName + " not found.");
			}
			agtCurrent.runOnServer(m_docTarget.getNoteID());
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_AGENT_AFTER_UP,
					"Unkown Error during executeAfterUpdateJob: " + strJobName,
					ex);
		}

	}

	@Override
	public void executeAfterCreateJob(String strJobName)
			throws SQLConnectorException {
		try {
			Agent agtCurrent = m_docTarget.getParentDatabase().getAgent(
					strJobName);
			if (agtCurrent == null) {
				throw new SQLConnectorException(
						Constants.E_NOTES_AGENT_AFTER_CR_NOTFOUND, "Agent: "
								+ strJobName + " not found.");
			}
			agtCurrent.runOnServer(m_docTarget.getNoteID());
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_NOTES_AGENT_AFTER_CR,
					"Unkown Error during executeAfterCreateJob: " + strJobName,
					ex);
		}

	}

	@Override
	public boolean wasNull() throws SQLConnectorException {
		try {
			return !m_docTarget.hasItem(m_LasField);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_VP_WASNULL,
					"Unkown Error during check of: " + m_LasField, ex);
		}
	}

	@Override
	public void setNULValue(String strField) throws SQLConnectorException {
		try {
			if (m_docTarget.hasItem(strField)) {
				String strOldValue = m_docTarget.getItemValueString(strField);
				if (!"".equals(strOldValue)) {
					m_docTarget.hasItem(strField);
					recordChange(strField, "" + strOldValue, "NULL (\"\")", 1);
					m_Dirty = true;
				}
			} else {
				// ACHTUNG das ist KEIN CHANGE
				m_docTarget.replaceItemValue(strField, "");
				m_Dirty = true;
			}
		} catch (Exception e) {
			throw new SQLConnectorException(Constants.E_NOTES_DOC_VALUE_ASSIGN,
					"Error during assign NULL value.", e);
		}

	}

	private String getValueAsString(Document docCurrent, String strField) {
		String strRC = "";
		try {
			if (docCurrent.hasItem(strField)) {
				String strFormula = "@Implode(@Text(" + strField + "))";
				@SuppressWarnings("rawtypes")
				Vector vecValue = docCurrent.getParentDatabase().getParent()
						.evaluate(strFormula, docCurrent);
				strRC = (String) vecValue.elementAt(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strRC;
	}
}
