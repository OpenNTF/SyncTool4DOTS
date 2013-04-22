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
package biz.webgate.dots.sqlconnector.definition.valueprocessor;

import java.util.Vector;

import lotus.domino.Document;
import lotus.domino.Session;
import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceDataSet;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.definition.IValueProcessor;

public class StringValueProcessor implements IValueProcessor {

	@SuppressWarnings("rawtypes")
	@Override
	public void processValue(ISourceDataSet isCurrent,
			ITargetDataSet itCurrent, String sourceField, String targetField,
			String strFormula, Session sesCurrent) throws SQLConnectorException {
		try {
			String strValue = isCurrent.getStringValue(sourceField);
			boolean blIsNull = isCurrent.wasNull();
			if (blIsNull) {
				itCurrent.setNULValue(targetField);
			} else {

				if (strFormula != null && !strFormula.equals("")) {
					String strFormulaTransform = strFormula.replace(
							"###VALUE###", strValue);
					Vector vecResult = sesCurrent.evaluate(strFormulaTransform);
					if (vecResult.firstElement() instanceof String) {
						strValue = ((String) vecResult.firstElement());
					} else {
						throw new SQLConnectorException(
								Constants.E_VP_STRINGVALUE_NS,
								"ResultType Error: " + strFormulaTransform
										+ " String expected.");
					}
				}
				itCurrent.setStringValue(targetField, strValue);
			}
		} catch (SQLConnectorException exSQL) {
			throw exSQL;
		} catch (Exception exCurrent) {
			throw new SQLConnectorException(Constants.E_VP_STRINGVALUE,
					"Unexpected Error:", exCurrent);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void computeValue(ITargetDataSet itCurrent, String targetField,
			String strFormula, Session sesCurrent, Document docContext)
			throws SQLConnectorException {
		try {
			Vector vecResult = sesCurrent.evaluate(strFormula);
			if (vecResult.firstElement() instanceof String) {
				String strValue = ((String) vecResult.firstElement());
				itCurrent.setStringValue(targetField, strValue);
			} else {
				throw new SQLConnectorException(Constants.E_VP_STRINGVALUE_NS,
						"ResultType Error: " + strFormula + " String expected.");
			}

		} catch (Exception e) {
			throw new SQLConnectorException(Constants.E_VP_STRINGVALUE_C,
					"Unexpected Error", e);
		}

	}
}
