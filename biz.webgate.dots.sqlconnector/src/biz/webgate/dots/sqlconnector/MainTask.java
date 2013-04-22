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

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.View;

import org.eclipse.core.runtime.IProgressMonitor;

import biz.webgate.dots.sqlconnector.TaskStatus.ITaskStatus;
import biz.webgate.dots.sqlconnector.TaskStatus.InitializeStatus;

import com.ibm.dots.annotation.HungPossibleAfter;
import com.ibm.dots.annotation.Run;
import com.ibm.dots.annotation.RunEvery;
import com.ibm.dots.annotation.RunOnStart;
import com.ibm.dots.task.AbstractServerTask;
import com.ibm.dots.task.RunWhen;
import com.ibm.dots.task.RunWhen.RunUnit;

public class MainTask extends AbstractServerTask {

	public static int LOG_NONE = 0;
	public static int LOG_NORMAL = 1;
	public static int LOG_INFO = 2;
	public static int LOG_DEBUG = 3;
	public static int LOG_ALL = 4;

	public static String INI_LOG = "WGCSQLDebug";
	public static String INI_DB = "WGCSQLDB";

	public static String VIEW_JOBS = "LUPJobsSchedule";
	public static String VIEW_ADHOC = "LUPAdhoc";
	public static String VIEW_JD2S = "LUPJobDefinition2Schedule";
	public static String VIEW_SCHEDULING = "LUPScheduling";

	private int m_LogLevel = 1;
	private String m_LogINIValue = "";
	private View m_viwAdHoc;
	private View m_viwRegular;
	private View m_viwSchedule;
	private View m_viwJobDefinition2Schedule;
	
	private Database m_ndbJobs;
	private static MainTask m_MainTask;
	private String m_DatabasePath = "";
	private ITaskStatus m_Status = new InitializeStatus();

	public MainTask() {
	}

	@Override
	public void dispose() throws NotesException {
		if (m_viwAdHoc != null) {
			m_viwAdHoc.recycle();
		}
		if (m_viwRegular != null) {
			m_viwRegular.recycle();
		}
		if (m_ndbJobs != null) {
			m_ndbJobs.recycle();
		}
		logMessage("SQL Connector shutdown");
	}

	public static MainTask getInstance() {
		if (m_MainTask == null) {
			m_MainTask = new MainTask();
		}
		return m_MainTask;
	}

	@Override
	public void run(RunWhen arg0, String[] arg1, IProgressMonitor arg2)
			throws NotesException {
	}

	@RunOnStart
	public void runOnStart(IProgressMonitor monitor) {
		m_MainTask = this;
		logMessage("SQL Connector startup.");
		m_Status = m_Status.doRunOnce(this);

	}

	@RunEvery(every = 1, unit = RunUnit.second)
	@HungPossibleAfter(timeInMinutes = 1)
	public void runAdHocRequests(IProgressMonitor monitor) {
		m_Status = m_Status.doRunAddhoc(this);
	}

	@RunEvery(every = 1, unit = RunUnit.minute)
	public void runRegularRequests(IProgressMonitor monitor) {
		m_Status = m_Status.doRunRegular(this);
	}

	@RunEvery(every = 30, unit = RunUnit.minute)
	public void runScheduling(IProgressMonitor monitor) {
		m_Status = m_Status.doScheduling(this);
	}

	@Run(id = "status")
	public void runStatus(String[] args, IProgressMonitor monitor) {
		logMessage("SqlConnector Status: " + m_Status.getStatusDescription());
	}

	public void doLog(int nLevel, String strMessage) {
		if ((nLevel + 1) < m_LogLevel) {
			logMessage(strMessage);
		}
	}

	public int getLogLevel() {
		return m_LogLevel;
	}

	public void setLogLevel(int logLevel) {
		m_LogLevel = logLevel;
	}

	public View getViwAdHoc() {
		return m_viwAdHoc;
	}

	public void setViwAdHoc(View viwAdHoc) {
		m_viwAdHoc = viwAdHoc;
	}

	public View getViwRegular() {
		return m_viwRegular;
	}

	public void setViwRegular(View viwRegular) {
		m_viwRegular = viwRegular;
	}

	public Database getNdbJobs() {
		return m_ndbJobs;
	}

	public void setNdbJobs(Database ndbJobs) {
		m_ndbJobs = ndbJobs;
	}

	public ITaskStatus getStatus() {
		return m_Status;
	}

	public String getDatabasePath() {
		return m_DatabasePath;
	}

	public void setDatabasePath(String databasePath) {
		m_DatabasePath = databasePath;
	}

	public boolean hasINIChange() {
		try {
			String strNewLogValue = getSession().getEnvironmentString(INI_LOG,
					true);
			String strNewPathValue = getSession().getEnvironmentString(INI_DB,
					true);
			if (!m_LogINIValue.equals(strNewLogValue)) {
				return true;
			}
			if (!m_DatabasePath.equals(strNewPathValue)) {
				return true;
			}
		} catch (Exception e) {
			logException(e);
		}
		return false;
	}
	/*
	public ITaskStatus checkNotesInitialization(ITaskStatus taskCurrent) {
		try {
			Session sesCurrent = getSession();
			Database ndbJobs = null;

			ndbJobs = sesCurrent.getDatabase("", m_DatabasePath);
			if (ndbJobs == null || !ndbJobs.isOpen()) {
				setNdbJobs(null);
				doLog(MainTask.LOG_NORMAL, "Database " + m_DatabasePath
						+ "not found!");
				m_DatabasePath = "";
				return new InitializeStatus();
			}
			setNdbJobs(ndbJobs);
			View viwAdHoc = getNdbJobs().getView(VIEW_ADHOC);
			View viwRegular = getNdbJobs().getView(VIEW_JOBS);
			if (viwAdHoc == null) {
				doLog(MainTask.LOG_NORMAL, "View " + VIEW_ADHOC + " not found!");
				return new InitializeStatus();
			}
			if (viwRegular == null) {
				doLog(MainTask.LOG_NORMAL, "View " + VIEW_JOBS + " not found!");
				return new InitializeStatus();
			}
			setViwAdHoc(viwAdHoc);
			setViwRegular(viwRegular);
			// setStatus(new WaitForRequestStatus());

		} catch (Exception e) {
			logException(e);
		}
		return taskCurrent;
	}
	*/
	public String getLogINIValue() {
		return m_LogINIValue;
	}

	public void setLogINIValue(String logINIValue) {
		m_LogINIValue = logINIValue;
	}

	public View getViwSchedule() {
		return m_viwSchedule;
	}

	public void setViwSchedule(View viwSchedule) {
		m_viwSchedule = viwSchedule;
	}

	public View getViwJobDefinition2Schedule() {
		return m_viwJobDefinition2Schedule;
	}

	public void setViwJobDefinition2Schedule(View viwJobDefinition2Schedule) {
		m_viwJobDefinition2Schedule = viwJobDefinition2Schedule;
	}

}
