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

import biz.webgate.dots.sqlconnector.changerecorder.ChangeRecorder;

public interface ITargetDataSet {

	public void setIntValue(String strField, int nValue)
			throws SQLConnectorException;

	public void setDblValue(String strField, double nValue)
			throws SQLConnectorException;

	public void setDateValue(String strField, Date datValue)
			throws SQLConnectorException;

	public void setStringValue(String strField, String strValue)
			throws SQLConnectorException;

	public void setNULValue(String strField) throws SQLConnectorException;

	public boolean isDirty();

	public void updateRecord() throws SQLConnectorException;

	public boolean isNew();

	public String getPrimaryKeyAsString();

	public void setChangeRecorder(ChangeRecorder cr);

	public void executeAfterUpdateJob(String strJobName)
			throws SQLConnectorException;

	public void executeAfterCreateJob(String strJobName)
			throws SQLConnectorException;
}
