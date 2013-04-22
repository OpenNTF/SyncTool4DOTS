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
package biz.webgate.dots.sqlconnector.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecord;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorder;

public class SQLTargetData implements ITargetDataSet {

	private ResultSet m_RSCurrent;
	private Connection m_Connection;
	private boolean m_CreateNew;
	private String m_KeyField;
	private Object m_KeyObject;
	private String m_Table;
	private HashMap<String, Object> m_UpdateList;
	private boolean m_isNew;
	private ChangeRecorder m_CR;
	private boolean m_SingleUpdate;

	private SimpleDateFormat m_sdfTESTER = new SimpleDateFormat("dd.MM.yyyy");
	private DecimalFormat m_decFormat = new DecimalFormat("0.000000000000");

	public SQLTargetData(ResultSet resultSet, Connection connection,
			boolean createNew, String strKeyField, Object objKey,
			String strTable, boolean singleUpdate) {
		super();
		m_RSCurrent = resultSet;
		m_Connection = connection;
		m_CreateNew = createNew;
		m_UpdateList = new HashMap<String, Object>();
		m_KeyField = strKeyField;
		m_KeyObject = objKey;
		m_Table = strTable;
		m_SingleUpdate = singleUpdate;
		try {
			if (!m_RSCurrent.next()) {
				m_isNew = true;
			} else {
				m_isNew = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setIntValue(String strField, int nValue)
			throws SQLConnectorException {
		if (m_isNew && !m_CreateNew) {
			return;
		}

		if (m_isNew && m_CreateNew) {
			m_UpdateList.put(strField, new Integer(nValue));
			recordChange(strField, "", "" + nValue, 1);
			return;
		}
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			int nValueOrig = m_RSCurrent.getInt(nColumn);
			if (nValue != nValueOrig) {
				m_UpdateList.put(strField, new Integer(nValue));
				recordChange(strField, "" + nValueOrig, "" + nValue, 1);
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}

	}

	@Override
	public void setDblValue(String strField, double nValue)
			throws SQLConnectorException {
		if (m_isNew && !m_CreateNew) {
			return;
		}

		if (m_isNew && m_CreateNew) {
			m_UpdateList.put(strField, new Double(nValue));
			recordChange(strField, "", "" + nValue, 2);
			return;
		}
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			double nValueOrig = m_RSCurrent.getDouble(nColumn);
			if (!m_decFormat.format(nValue).equals(
					m_decFormat.format(nValueOrig))) {
				m_UpdateList.put(strField, new Double(nValue));
				recordChange(strField, "" + nValueOrig, "" + nValue, 2);
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}

	}

	@Override
	public void setDateValue(String strField, Date datValue)
			throws SQLConnectorException {
		if (m_isNew && !m_CreateNew) {
			return;
		}

		if (m_isNew && m_CreateNew) {
			m_UpdateList.put(strField, datValue);
			recordChange(strField, "", "" + datValue, 3);

			return;
		}
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			Date nValueOrig = m_RSCurrent.getDate(nColumn);
			if (nValueOrig == null && datValue != null) {
				m_UpdateList.put(strField, datValue);
				recordChange(strField, "" + nValueOrig,
						m_sdfTESTER.format(datValue), 1);
				return;
			}
			if (!m_sdfTESTER.format(nValueOrig).equals(
					m_sdfTESTER.format(datValue))) {
				m_UpdateList.put(strField, datValue);
				recordChange(strField, "" + m_sdfTESTER.format(nValueOrig),
						m_sdfTESTER.format(datValue), 1);

			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}

	}

	@Override
	public void setStringValue(String strField, String strValue)
			throws SQLConnectorException {
		if (m_isNew && !m_CreateNew) {
			return;
		}
		if (m_isNew && m_CreateNew) {
			m_UpdateList.put(strField, strValue);
			recordChange(strField, "", strValue, 4);

			return;
		}
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			String strValueOrig = m_RSCurrent.getString(nColumn);
			if (!strValueOrig.equals(strValue)) {
				m_UpdateList.put(strField, strValue);
				recordChange(strField, strValueOrig, strValue, 1);
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}

	}

	@Override
	public void setNULValue(String strField) throws SQLConnectorException {
	}

	@Override
	public boolean isDirty() {
		return (m_UpdateList.size() > 0);
	}

	@Override
	public void updateRecord() throws SQLConnectorException {
		if (m_UpdateList.size() > 0) {
			if (m_SingleUpdate) {
				StringBuffer sbStatement = new StringBuffer();
				if (!m_isNew) {
					sbStatement.append("UPDATE " + m_Table + " SET ");
					StringBuffer sbAllFields = new StringBuffer();
					for (String strField : m_UpdateList.keySet()) {
						sbAllFields.append(strField + " = ?, ");
					}
					sbStatement.append(sbAllFields.substring(0,
							sbAllFields.length() - 2));
					sbStatement.append(" WHERE " + m_KeyField + " = ?");
				} else {
					sbStatement.append("INSERT INTO " + m_Table);
					StringBuffer sbAllFields = new StringBuffer();
					StringBuffer sbAllPH = new StringBuffer();
					for (String strField : m_UpdateList.keySet()) {
						sbAllFields.append(strField + ", ");
						sbAllPH.append("?, ");
					}
					sbAllFields.append(m_KeyField);
					sbAllPH.append("?");
					sbStatement.append(" (" + sbAllFields.toString() + ") ");
					sbStatement.append("VALUES (" + sbAllFields.toString()
							+ ") ");
				}
				try {
					PreparedStatement ps = m_Connection
							.prepareStatement(sbStatement.toString());
					int nCounter = 1;
					for (String strField : m_UpdateList.keySet()) {
						Object obj = m_UpdateList.get(strField);
						processValue2Statement(ps, nCounter, obj);
						nCounter++;
					}
					processValue2Statement(ps, nCounter, m_KeyObject);
					ps.executeUpdate();
				} catch (Exception e) {
					throw new SQLConnectorException(Constants.E_SQL_ROW_SAVE,
							"Error during Update/create", e);
				}
			} else {
				if (!m_isNew) {
					for (String strField : m_UpdateList.keySet()) {

						String strSQL = "UPDATE " + m_Table + " SET "
								+ strField + " = ? WHERE " + m_KeyField
								+ " = ?";

						try {
							PreparedStatement ps = m_Connection
									.prepareStatement(strSQL);
							processValue2Statement(ps, 1,
									m_UpdateList.get(strField));
							processValue2Statement(ps, 2, m_KeyObject);
							ps.executeUpdate();
						} catch (Exception e) {
							throw new SQLConnectorException(
									Constants.E_SQL_ROW_SAVE,
									"Error during Update/create", e);
						}
					}
				}
			}
		}
	}

	private void processValue2Statement(PreparedStatement ps, int nCounter,
			Object obj) throws SQLException {
		if (obj instanceof Integer) {
			ps.setInt(nCounter, ((Integer) obj).intValue());
		}
		if (obj instanceof Double) {
			ps.setDouble(nCounter, ((Double) obj).doubleValue());
		}
		if (obj instanceof Date) {
			ps.setDate(nCounter, new java.sql.Date(((Date) obj).getTime()));
		}
		if (obj instanceof String) {
			ps.setString(nCounter, (String) obj);
		}
	}

	@Override
	public boolean isNew() {
		return m_isNew;
	}

	@Override
	public String getPrimaryKeyAsString() {
		return m_KeyObject.toString();
	}

	@Override
	public void setChangeRecorder(ChangeRecorder cr) {
		m_CR = cr;

	}

	@Override
	public void executeAfterUpdateJob(String strJobName)
			throws SQLConnectorException {
	}

	@Override
	public void executeAfterCreateJob(String strJobName)
			throws SQLConnectorException {
	}

	private void recordChange(String strField, String strOldValue,
			String strNewValue, int nType) {
		if (m_CR != null) {
			ChangeRecord crCurrent = new ChangeRecord(strField, strOldValue,
					strNewValue, nType);
			m_CR.addChangeRecord(crCurrent);
		}
	}

}
