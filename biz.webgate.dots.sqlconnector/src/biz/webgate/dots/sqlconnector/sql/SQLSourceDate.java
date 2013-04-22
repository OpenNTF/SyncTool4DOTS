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

import java.sql.ResultSet;
import java.util.Date;

import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;

public class SQLSourceDate implements ISourceDataSet {

	private ResultSet m_RSCurrent;
	private String m_LastField;

	public SQLSourceDate(ResultSet rSCurrent) {
		super();
		m_RSCurrent = rSCurrent;
	}

	@Override
	public int getIntValue(String strField) throws SQLConnectorException {
		m_LastField = strField;
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			return m_RSCurrent.getInt(nColumn);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}
	}

	@Override
	public double getDblValue(String strField) throws SQLConnectorException {
		m_LastField = strField;
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			return m_RSCurrent.getDouble(nColumn);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}
	}

	@Override
	public Date getDateValue(String strField) throws SQLConnectorException {
		m_LastField = strField;
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			return m_RSCurrent.getDate(nColumn);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}
	}

	@Override
	public String getStringValue(String strField) throws SQLConnectorException {
		m_LastField = strField;
		try {
			int nColumn = m_RSCurrent.findColumn(strField);
			return m_RSCurrent.getString(nColumn);
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_SQL_ROW_VALUE_ACCESS,
					"Error in accesing column " + strField, ex);
		}
	}

	@Override
	public boolean wasNull() throws SQLConnectorException {
		try {
			return m_RSCurrent.wasNull();
		} catch (Exception e) {
			throw new SQLConnectorException(Constants.E_VP_WASNULL,
					"Error in check wasNull on Field:  " + m_LastField, e);
		}
	}
}
