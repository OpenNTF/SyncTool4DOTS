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

import java.util.ArrayList;

public class ChangeRecorder {

	private String m_PrimaryKey;
	private String m_JobID;
	private String m_DataTargetID;
	private boolean m_hasChanges;
	private ArrayList<ChangeRecord> m_Changes = new ArrayList<ChangeRecord>();

	public String getPrimaryKey() {
		return m_PrimaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		m_PrimaryKey = primaryKey;
	}

	public String getJobID() {
		return m_JobID;
	}

	public void setJobID(String jobID) {
		m_JobID = jobID;
	}

	public String getDataTargetID() {
		return m_DataTargetID;
	}

	public void setDataTargetID(String dataTargetID) {
		m_DataTargetID = dataTargetID;
	}

	public boolean isHasChanges() {
		return m_hasChanges;
	}

	public ArrayList<ChangeRecord> getChanges() {
		return m_Changes;
	}

	public void addChangeRecord(ChangeRecord rec) {
		m_hasChanges = true;
		m_Changes.add(rec);
	}

	public ChangeRecorder(String primaryKey, String jobID, String dataTargetID) {
		super();
		m_PrimaryKey = primaryKey;
		m_JobID = jobID;
		m_DataTargetID = dataTargetID;
	}
}
