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
import java.sql.ResultSet;
import java.sql.Statement;

import com.ibm.commons.jdbc.drivers.JDBCDriverLoader;

import biz.webgate.dots.sqlconnector.ISourceConnection;
import biz.webgate.dots.sqlconnector.ISourceDataSet;

public class SQLSourceConnectionImpl implements ISourceConnection {

	private String m_UserName;
	private String m_Password;
	private String m_URL;
	private String m_Selection;
	private String m_DriverClass;

	private Connection m_Connection;
	private Statement m_Statement;
	private ResultSet m_RSCurrent;
	private Exception m_LastException;

	public SQLSourceConnectionImpl(String userName, String password, String uRL,
			String selection, String driverClass) {
		super();
		m_UserName = userName;
		m_Password = password;
		m_URL = uRL;
		m_Selection = selection;
		m_DriverClass = driverClass;
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
	public boolean execute() {
		try {
			m_Statement = m_Connection.createStatement();
			m_RSCurrent = m_Statement.executeQuery(m_Selection);
			return true;
		} catch (Exception e) {
			m_LastException = e;
			System.out.println("SELECTION: " + m_Selection);
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean hasMoreElements() {
		try {
			return m_RSCurrent.next();
		} catch (Exception e) {
			m_LastException = e;
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ISourceDataSet nextElement() {
		if (m_RSCurrent != null) {
			return new SQLSourceDate(m_RSCurrent);
		}
		return null;
	}

	public Exception getLastException() {
		return m_LastException;
	}

}
