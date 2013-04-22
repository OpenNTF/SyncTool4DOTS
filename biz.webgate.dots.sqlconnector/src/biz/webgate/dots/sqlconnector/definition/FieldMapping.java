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
package biz.webgate.dots.sqlconnector.definition;

import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.definition.valueprocessor.DateValueProcessor;
import biz.webgate.dots.sqlconnector.definition.valueprocessor.DoubleValueProcessor;
import biz.webgate.dots.sqlconnector.definition.valueprocessor.IntegerValueProcessor;
import biz.webgate.dots.sqlconnector.definition.valueprocessor.StringValueProcessor;
import biz.webgate.dots.sqlconnector.notes.NotesDataImpl;
import lotus.domino.Session;

public class FieldMapping {
	private String m_SourceField;
	private String m_TargetField;
	private String m_TransformFormula;
	private IValueProcessor m_ValueProcessor;
	private boolean m_OnCreateOnly;
	private boolean m_OnUpdateOnly;
	private boolean m_ComputeOnly;

	public FieldMapping(String SourceField, String TargetField,
			String TransformFormula, int TargetType) {
		super();
		this.m_SourceField = SourceField;
		this.m_TargetField = TargetField;
		this.m_TransformFormula = TransformFormula;
		switch (TargetType) {
		case Constants.DATATYPE_INT:
			m_ValueProcessor = new IntegerValueProcessor();
			break;
		case Constants.DATATYPE_DOUBLE:
			m_ValueProcessor = new DoubleValueProcessor();
			break;
		case Constants.DATATYPE_DATE:
			m_ValueProcessor = new DateValueProcessor();
			break;
		case Constants.DATATYPE_STRING:
			m_ValueProcessor = new StringValueProcessor();
			break;
		}
	}

	public void processValues(ISourceDataSet sourceData,
			ITargetDataSet targetData, Session sesCurrent)
			throws SQLConnectorException {
		//CREATE CHECK
		if (m_OnCreateOnly && !targetData.isNew()) {
			return;
		}
		//UPDATE ONLY CHECK
		if (m_OnUpdateOnly && targetData.isNew()) {
			return;
		}
		if (m_ComputeOnly) {
			if (targetData instanceof NotesDataImpl) {
				m_ValueProcessor.computeValue(targetData, m_TargetField,
						m_TransformFormula, sesCurrent,
						((NotesDataImpl) targetData).getDocTarget());
			}
		} else {
			m_ValueProcessor.processValue(sourceData, targetData,
					m_SourceField, m_TargetField, m_TransformFormula,
					sesCurrent);
		}
	}

	public boolean isOnCreateOnly() {
		return m_OnCreateOnly;
	}

	public void setOnCreateOnly(boolean onCreateOnly) {
		m_OnCreateOnly = onCreateOnly;
	}

	public boolean isOnUpdateOnly() {
		return m_OnUpdateOnly;
	}

	public void setOnUpdateOnly(boolean onUpdateOnly) {
		m_OnUpdateOnly = onUpdateOnly;
	}

	public boolean isComputeOnly() {
		return m_ComputeOnly;
	}

	public void setComputeOnly(boolean computeOnly) {
		m_ComputeOnly = computeOnly;
	}
}
