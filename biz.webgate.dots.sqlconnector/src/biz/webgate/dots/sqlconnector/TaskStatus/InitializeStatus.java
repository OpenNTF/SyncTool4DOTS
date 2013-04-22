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
package biz.webgate.dots.sqlconnector.TaskStatus;

import lotus.domino.Session;
import lotus.domino.Database;
import lotus.domino.View;
import biz.webgate.dots.sqlconnector.MainTask;

public class InitializeStatus implements ITaskStatus {

	@Override
	public ITaskStatus doRunOnce(MainTask mtCurrent) {
		return checkInitialization(mtCurrent);
	}

	private ITaskStatus checkInitialization(MainTask mtCurrent) {
		try {
			Session sesCurrent = mtCurrent.getSession();
			String strLogLevel = sesCurrent.getEnvironmentString(
					MainTask.INI_LOG, true);
			String strTargetPath = sesCurrent.getEnvironmentString(
					MainTask.INI_DB, true);
			if (strLogLevel != null && !strLogLevel.equals("")) {
				try {
					mtCurrent.setLogLevel(Integer.parseInt(strLogLevel));
				} catch (Exception e) {
					mtCurrent.logException(e);
					mtCurrent.setLogLevel(MainTask.LOG_NORMAL);
					mtCurrent.doLog(MainTask.LOG_NORMAL, "Loglevel set to 0");
				}
			}
			mtCurrent.doLog(MainTask.LOG_NORMAL,
					"Log Level is: " + mtCurrent.getLogLevel());
			Database ndbJobs = null;
			if (strTargetPath != null && !strTargetPath.equals("")) {
				ndbJobs = sesCurrent.getDatabase("", strTargetPath);
			}
			if (ndbJobs == null || !ndbJobs.isOpen()) {
				mtCurrent.setNdbJobs(null);
				mtCurrent.doLog(MainTask.LOG_NORMAL, "Database "
						+ strTargetPath + "not found!");
				return this;
			}
			mtCurrent.setNdbJobs(ndbJobs);
			View viwAdHoc = mtCurrent.getNdbJobs().getView(MainTask.VIEW_ADHOC);
			View viwRegular = mtCurrent.getNdbJobs()
					.getView(MainTask.VIEW_JOBS);
			View viwScheduling = mtCurrent.getNdbJobs().getView(
					MainTask.VIEW_SCHEDULING);
			View viwJob2S = mtCurrent.getNdbJobs().getView(MainTask.VIEW_JD2S);
			if (viwAdHoc == null) {
				mtCurrent.doLog(MainTask.LOG_NORMAL, "View "
						+ MainTask.VIEW_ADHOC + " not found!");
				return this;
			}
			if (viwRegular == null) {
				mtCurrent.doLog(MainTask.LOG_NORMAL, "View"
						+ MainTask.VIEW_JOBS + "not found!");
				return this;
			}
			if (viwJob2S == null) {
				mtCurrent.doLog(MainTask.LOG_NORMAL, "View"
						+ MainTask.VIEW_JD2S + "not found!");
				return this;
			}
			if (viwScheduling == null) {
				mtCurrent.doLog(MainTask.LOG_NORMAL, "View"
						+ MainTask.VIEW_SCHEDULING + "not found!");
				return this;
			}

			mtCurrent.setViwAdHoc(viwAdHoc);
			mtCurrent.setViwRegular(viwRegular);
			mtCurrent.setViwJobDefinition2Schedule(viwJob2S);
			mtCurrent.setViwSchedule(viwScheduling);
			mtCurrent.setLogINIValue(strLogLevel);
			mtCurrent.setDatabasePath(strTargetPath);
			return new WaitForRequestStatus();

		} catch (Exception e) {
			mtCurrent.logException(e);
		}
		return this;
	}

	@Override
	public ITaskStatus doRunAddhoc(MainTask mtCurrent) {
		return this;
	}

	@Override
	public ITaskStatus doRunRegular(MainTask mtCurrent) {
		return checkInitialization(mtCurrent);
	}

	@Override
	public String getStatusDescription() {
		return "Initializing access to configuration DB:";
	}

	@Override
	public ITaskStatus doScheduling(MainTask mtCurrent) {
		return this;
	}

}
