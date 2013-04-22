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
import java.util.Date;

import biz.webgate.dots.sqlconnector.ITargetConnection;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;

import com.ibm.commons.jdbc.drivers.JDBCDriverLoader;

public class SQLTargetConnectionImpl implements ITargetConnection {

	private String m_UserName;
	private String m_Password;
	private String m_URL;
	private String m_Selection;
	private String m_DriverClass;

	private boolean m_CreateNew;
	private boolean m_SingleUpdate;
	private String m_KeyField;
	private String m_Table;

	private Connection m_Connection;
	private ResultSet m_RSCurrent;
	private Exception m_LastException;
	private String m_Key;

	public SQLTargetConnectionImpl(String userName, String password,
			String uRL, String selection, String driverClass,
			boolean createNew, String strKeyField, String strTable,
			boolean singleUpdate) {
		super();
		m_UserName = userName;
		m_Password = password;
		m_URL = uRL;
		m_Selection = selection;
		m_DriverClass = driverClass;
		m_CreateNew = createNew;
		m_KeyField = strKeyField;
		m_Table = strTable;
		m_SingleUpdate = singleUpdate;
	}

	@Override
	public boolean doConnect() {
		try {
			m_Connection = JDBCDriverLoader.createConnection(m_DriverClass,
					m_URL, m_UserName, m_Password);
			return true;
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ITargetDataSet findTargetSetByStringKey(String strKey)
			throws SQLConnectorException {
		m_Key = strKey;
		try {
			PreparedStatement psSelect = m_Connection
					.prepareStatement(m_Selection);
			psSelect.setString(1, strKey);
			m_RSCurrent = psSelect.executeQuery();
			SQLTargetData td = new SQLTargetData(m_RSCurrent, m_Connection,
					m_CreateNew, m_KeyField, strKey, m_Table, m_SingleUpdate);
			return td;
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByIntKey(int nKey)
			throws SQLConnectorException {
		m_Key = "" + nKey;
		try {
			PreparedStatement psSelect = m_Connection
					.prepareStatement(m_Selection);
			psSelect.setInt(1, nKey);
			m_RSCurrent = psSelect.executeQuery();
			SQLTargetData td = new SQLTargetData(m_RSCurrent, m_Connection,
					m_CreateNew, m_KeyField, new Integer(nKey), m_Table,
					m_SingleUpdate);
			return td;
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByDblKey(double nKey)
			throws SQLConnectorException {
		m_Key = "" + nKey;
		try {
			PreparedStatement psSelect = m_Connection
					.prepareStatement(m_Selection);
			psSelect.setDouble(1, nKey);
			m_RSCurrent = psSelect.executeQuery();
			SQLTargetData td = new SQLTargetData(m_RSCurrent, m_Connection,
					m_CreateNew, m_KeyField, new Double(nKey), m_Table,
					m_SingleUpdate);
			return td;
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByDateKey(Date datKey)
			throws SQLConnectorException {
		m_Key = "" + datKey;
		try {
			PreparedStatement psSelect = m_Connection
					.prepareStatement(m_Selection);
			psSelect.setDate(1, new java.sql.Date(datKey.getTime()));
			m_RSCurrent = psSelect.executeQuery();
			SQLTargetData td = new SQLTargetData(m_RSCurrent, m_Connection,
					m_CreateNew, m_KeyField, datKey, m_Table, m_SingleUpdate);
			return td;
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTargetID() {
		return m_Key;
	}

	@Override
	public void executeAfterExecutionJob(String strJobName)
			throws SQLConnectorException {
	}

	@Override
	public Exception getLastException() {
		return m_LastException;
	}

}
