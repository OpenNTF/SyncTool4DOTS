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

package biz.webgate.dots.sqlconnector;

import java.util.Date;

public interface ISourceDataSet {

	public int getIntValue(String strField) throws SQLConnectorException;
	public double getDblValue(String strField) throws SQLConnectorException;
	public Date getDateValue(String strField) throws SQLConnectorException;
	public String getStringValue( String strField) throws SQLConnectorException;
	public boolean wasNull() throws SQLConnectorException;
}
