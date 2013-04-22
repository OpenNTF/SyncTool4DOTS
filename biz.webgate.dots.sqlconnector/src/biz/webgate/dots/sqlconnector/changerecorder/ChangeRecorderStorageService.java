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

import java.util.Date;

import lotus.domino.*;

public class ChangeRecorderStorageService {
	private static ChangeRecorderStorageService m_Service;

	private ChangeRecorderStorageService() {

	}

	public static ChangeRecorderStorageService getInstance() {
		if (m_Service == null) {
			m_Service = new ChangeRecorderStorageService();
		}
		return m_Service;
	}

	public void saveChangeRecorder(Database ndbRecord, ChangeRecorder recCurrent, Document docJob) {
		try {
			DateTime dtCurrent = ndbRecord.getParent().createDateTime(
					new Date());
			for (ChangeRecord recElement : recCurrent.getChanges()) {
				Document docRecord = ndbRecord.createDocument();
				docRecord.replaceItemValue("Form", "frmChangeLog");
				docRecord.replaceItemValue("JobIDT", recCurrent.getJobID());
				docRecord.replaceItemValue("TargetIDT",
						recCurrent.getDataTargetID());
				docRecord.replaceItemValue("PrimaryKeyT",
						recCurrent.getPrimaryKey());
				docRecord.replaceItemValue("ChangeDateDT", dtCurrent);
				docRecord.replaceItemValue("FieldNameT",
						recElement.getTargetField());
				docRecord.replaceItemValue("OldValueT",
						recElement.getOldValue());
				docRecord.replaceItemValue("NewValueT",
						recElement.getNewValue());
				docRecord.replaceItemValue("FieldTypeT",
						recElement.getFieldType());
				docRecord.makeResponse(docJob);
				docRecord.save(true, false, true);
				docRecord.recycle();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
