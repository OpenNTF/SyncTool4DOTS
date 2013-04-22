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

import java.util.Date;

import biz.webgate.dots.sqlconnector.MainTask;
import biz.webgate.dots.sqlconnector.definition.JobBuilderService;
import biz.webgate.dots.sqlconnector.definition.JobDefinition;
import biz.webgate.dots.sqlconnector.scheduler.JobScheduling;
import lotus.domino.*;

public class WaitForRequestStatus implements ITaskStatus {

	private String m_Message = "Waiting for Requests";

	@Override
	public ITaskStatus doRunOnce(MainTask mtCurrent) {
		return this;
	}

	@Override
	public ITaskStatus doRunAddhoc(MainTask mtCurrent) {
		m_Message = "doRungAddhoc";
		if (mtCurrent.hasINIChange()) {
			mtCurrent.doLog(MainTask.LOG_NORMAL,
					"INI Values of the Configuration has changed. Reinit....");
			m_Message = "Waiting for Requests";
			return new InitializeStatus();
		}
		return this;
	}

	@Override
	public ITaskStatus doRunRegular(MainTask mtCurrent) {
		m_Message = "doRungRegular";

		if (mtCurrent.hasINIChange()) {
			mtCurrent.doLog(MainTask.LOG_NORMAL,
					"INI Values of the Configuration has changed. Reinit....");
			m_Message = "Waiting for Requests";
			return new InitializeStatus();
		}
		Document docRequest = null;
		try {
			if (mtCurrent.getViwRegular() == null) {
				m_Message = "Waiting for Requests";
				// return mtCurrent.checkNotesInitialization(this);
				return new InitializeStatus();

			}
			mtCurrent.getViwRegular().refresh();
			docRequest = mtCurrent.getViwRegular().getFirstDocument();
			if (docRequest != null) {
				m_Message = "Building new JobDefinition()";
				JobDefinition jbCurrent = JobBuilderService.getInstance()
						.buildJobDefinition(docRequest);
				Date datNow = new Date();
				m_Message = "Processing Job";
				jbCurrent.processJob(mtCurrent.getSession(), docRequest);
				docRequest.replaceItemValue("ActiveT", "2");
				docRequest.replaceItemValue("StartDT", mtCurrent.getSession()
						.createDateTime(datNow));
				docRequest.replaceItemValue("ProcessedDT", mtCurrent
						.getSession().createDateTime(new Date()));
				docRequest.save(true, false, true);
				m_Message = "Waiting for Requests";
			}
		} catch (Exception e) {
			m_Message = "Waiting for Requests";
			e.printStackTrace();
			try {
				if (docRequest != null) {
					docRequest.replaceItemValue("ActiveT", "9999");
					docRequest.save(true, false, true);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		m_Message = "Waiting for Requests";
		return this;
	}

	@Override
	public String getStatusDescription() {
		return m_Message;
	}

	@Override
	public ITaskStatus doScheduling(MainTask mtCurrent) {
		m_Message = "doScheduling";

		if (mtCurrent.hasINIChange()) {
			mtCurrent.doLog(MainTask.LOG_NORMAL,
					"INI Values of the Configuration has changed. Reinit....");
			m_Message = "Waiting for Requests";
			return new InitializeStatus();
		}
		mtCurrent.doLog(MainTask.LOG_NORMAL, "Check Scheduling of JobDefinitions");
		Document docRequest = null;
		try {
			if (mtCurrent.getViwJobDefinition2Schedule() == null
					|| mtCurrent.getViwSchedule() == null) {
				mtCurrent.doLog(MainTask.LOG_NORMAL,
						"Views are NULL. Reinit....");
				m_Message = "Waiting for Requests";
				// return mtCurrent.checkNotesInitialization(this);
				return new InitializeStatus();
			}
			docRequest = mtCurrent.getViwJobDefinition2Schedule()
					.getFirstDocument();
			while (docRequest != null) {
				Document docCheck = docRequest;
				docRequest = mtCurrent.getViwJobDefinition2Schedule()
						.getNextDocument(docRequest);
				m_Message = "Check JobDefinition";
				JobScheduling.getInstance().checkScheduling(docCheck,
						mtCurrent.getViwSchedule());
				docCheck.recycle();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

}
