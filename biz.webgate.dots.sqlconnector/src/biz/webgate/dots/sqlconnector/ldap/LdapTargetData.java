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
package biz.webgate.dots.sqlconnector.ldap;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.MainTask;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecord;
import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorder;

public class LdapTargetData implements ITargetDataSet {

	private SearchResult m_Data;
	private DirContext m_DirContext;
	private HashMap<String, Object> m_UpdateList = new HashMap<String, Object>();
	private ChangeRecorder m_CR;
	private boolean m_New = false;

	private SimpleDateFormat m_sdfTESTER = new SimpleDateFormat("dd.MM.yyyy");
	private DecimalFormat m_decFormat = new DecimalFormat("0.000000000000");

	@Override
	public void setIntValue(String strField, int nValue)
			throws SQLConnectorException {
		if (m_New) {
			return;
		}
		try {
			Attribute attrCurrent = m_Data.getAttributes().get(strField);
			int nValueOrig = ((Integer) attrCurrent.get()).intValue();
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
		if (m_New) {
			return;
		}
		try {
			Attribute attrCurrent = m_Data.getAttributes().get(strField);
			double nValueOrig = ((Double) attrCurrent.get()).doubleValue();
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
		if (m_New) {
			return;
		}
		try {
			Attribute attrCurrent = m_Data.getAttributes().get(strField);
			Date datValueOrig = (Date) attrCurrent.get();
			if (datValueOrig == null && datValue != null) {
				m_UpdateList.put(strField, datValue);
				recordChange(strField, "" + datValueOrig,
						m_sdfTESTER.format(datValue), 1);
				return;
			}
			if (!m_sdfTESTER.format(datValueOrig).equals(
					m_sdfTESTER.format(datValue))) {
				m_UpdateList.put(strField, datValue);
				recordChange(strField, "" + m_sdfTESTER.format(datValueOrig),
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
		if (m_New) {
			return;
		}
		try {
			Attribute attrCurrent = m_Data.getAttributes().get(strField);
			if (attrCurrent == null) {
				m_UpdateList.put(strField, strValue);
				recordChange(strField, "", strValue, Constants.DATATYPE_STRING);
				return;
			} 
			String strValueOrig = (String) attrCurrent.get();
			if (strValueOrig == null && strValue != null && !"".equals(strValue)) {
				m_UpdateList.put(strField, strValue);
				recordChange(strField, "", strValue, Constants.DATATYPE_STRING);
				return;				
			}
			if (!strValueOrig.equals(strValue)) {
				m_UpdateList.put(strField, strValue);
				recordChange(strField, strValueOrig, strValue, Constants.DATATYPE_STRING);
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
		return m_UpdateList.size() > 0;
	}

	@Override
	public void updateRecord() throws SQLConnectorException {

		if (m_UpdateList.size() == 0)
			return;
		ModificationItem[] modItems = new ModificationItem[m_UpdateList.size()];
		int nCounter = -1;
		for (String strKey : m_UpdateList.keySet()) {
			nCounter++;
			modItems[nCounter] = new ModificationItem(
					DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(strKey,
							m_UpdateList.get(strKey)));
		}
		try {

			for (int nCounter2 = 0; nCounter2 < modItems.length; nCounter2++) {

				ModificationItem[] modItems2 = new ModificationItem[1];

				modItems2[0] = modItems[nCounter2];
				try {
					m_DirContext.modifyAttributes(getPrimaryKeyAsString(),
							modItems2);
				} catch (Exception eIN) {
					eIN.printStackTrace();
				}
			}

		} catch (Exception e) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_SAVE,
					"Error during Update/create", e);
		}

	}

	@Override
	public boolean isNew() {
		return m_New;
	}

	@Override
	public String getPrimaryKeyAsString() {
		if (m_Data != null) {
			return m_Data.getName();
		}
		return "";
	}

	@Override
	public void setChangeRecorder(ChangeRecorder cr) {
		m_CR = cr;

	}

	@Override
	public void executeAfterUpdateJob(String strJobName)
			throws SQLConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeAfterCreateJob(String strJobName)
			throws SQLConnectorException {
		// TODO Auto-generated method stub

	}

	private void recordChange(String strField, String strOldValue,
			String strNewValue, int nType) {
		if (m_CR != null) {
			ChangeRecord crCurrent = new ChangeRecord(strField, strOldValue,
					strNewValue, nType);
			m_CR.addChangeRecord(crCurrent);
		}
	}

	public LdapTargetData(SearchResult data, DirContext dirContext) {
		super();
		m_Data = data;
		m_DirContext = dirContext;
		try {
			NamingEnumeration<String> nID = m_Data.getAttributes().getIDs();
			while (nID.hasMore()) {
				MainTask.getInstance().doLog(MainTask.LOG_INFO,
						"Attribute: " + nID.nextElement());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
