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

import java.util.Date;

import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;

public class LdapSourceDataSet implements ISourceDataSet {

	@Override
	public int getIntValue(String strField) throws SQLConnectorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDblValue(String strField) throws SQLConnectorException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getDateValue(String strField) throws SQLConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringValue(String strField) throws SQLConnectorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean wasNull() throws SQLConnectorException {
		// TODO Auto-generated method stub
		return false;
	}

}
