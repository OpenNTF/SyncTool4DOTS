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
package biz.webgate.dots.sqlconnector.changerecorder;

import biz.webgate.dots.sqlconnector.Constants;

public class ChangeRecord {
	private String m_TargetField;
	private String m_OldValue;
	private String m_NewValue;
	private int m_Type;

	public String getTargetField() {
		return m_TargetField;
	}

	public void setTargetField(String targetField) {
		m_TargetField = targetField;
	}

	public String getOldValue() {
		return m_OldValue;
	}

	public void setOldValue(String oldValue) {
		m_OldValue = oldValue;
	}

	public String getNewValue() {
		return m_NewValue;
	}

	public void setNewValue(String newValue) {
		m_NewValue = newValue;
	}

	public ChangeRecord(String targetField, String oldValue, String newValue,
			int type) {
		super();
		m_TargetField = targetField;
		m_OldValue = oldValue;
		m_NewValue = newValue;
		m_Type = type;
	}

	public String getFieldType() {
		switch (m_Type) {
		case Constants.DATATYPE_INT:
			return "Integer";
		case Constants.DATATYPE_DOUBLE:
			return "Double";
		case Constants.DATATYPE_DATE:
			return "Date";
		case Constants.DATATYPE_STRING:
			return "String";
		}
		return "Undefined";
	}
}
