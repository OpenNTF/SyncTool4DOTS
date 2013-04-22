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
package biz.webgate.dots.sqlconnector.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import biz.webgate.dots.sqlconnector.MainTask;

import lotus.domino.*;

public class JobScheduling {

	private static JobScheduling m_Scheduler;

	private JobScheduling() {

	}

	public static JobScheduling getInstance() {
		if (m_Scheduler == null) {
			m_Scheduler = new JobScheduling();
		}
		return m_Scheduler;
	}

	public boolean checkScheduling(Document docDefinition, View viwOpenSchedule) {
		try {
			String strJobIDT = docDefinition.getItemValueString("JobIDT");
			Date dtToday = new Date();

			Calendar calToday = GregorianCalendar.getInstance();
			calToday.setTime(dtToday);
			int nYear = calToday.get(Calendar.YEAR);
			int nMonth = calToday.get(Calendar.MONTH) + 1;
			int nDay = calToday.get(Calendar.DAY_OF_MONTH);
			int nHour = calToday.get(Calendar.HOUR_OF_DAY);
			// int nWeekDay = calToday.get(Calendar.DAY_OF_WEEK);
			if (docDefinition.hasItem("ScheduleT")) {
				if ("DAILY".equals(docDefinition
						.getItemValueString("ScheduleT"))) {
					DateTime dtCurrent = (DateTime) docDefinition
							.getItemValueDateTimeArray("ScheduleTimeDT")
							.elementAt(0);
					Date datCurrent = dtCurrent.toJavaDate();
					Calendar calCurrent = GregorianCalendar.getInstance();
					calCurrent.setTime(datCurrent);
					nHour = calCurrent.get(Calendar.HOUR_OF_DAY);
					int nMinute = calCurrent.get(Calendar.MINUTE);
					String strCheck = strJobIDT + "@@@" + nYear + "@@@"
							+ nMonth + "@@@" + nDay + "@@@" + nHour + "@@@"
							+ nMinute;
					Document docCheck = viwOpenSchedule.getDocumentByKey(
							strCheck, true);
					if (docCheck == null) {
						buildRequest(nYear, nMonth, nDay, nHour, nMinute,
								strJobIDT, docDefinition);
					}
					strCheck = strJobIDT + "@@@" + nYear + "@@@" + nMonth
							+ "@@@" + (nDay + 1) + "@@@" + nHour + "@@@"
							+ nMinute;
					docCheck = viwOpenSchedule.getDocumentByKey(strCheck, true);
					if (docCheck == null) {
						buildRequest(nYear, nMonth, nDay + 1, nHour, nMinute,
								strJobIDT, docDefinition);
					}
				}
				if ("HOURLY".equals(docDefinition
						.getItemValueString("ScheduleT"))) {
					int nMinute = docDefinition
							.getItemValueInteger("ScheduleMinuteN");
					String strCheck = strJobIDT + "@@@" + nYear + "@@@"
							+ nMonth + "@@@" + nDay + "@@@" + nHour + "@@@"
							+ nMinute;
					Document docCheck = viwOpenSchedule.getDocumentByKey(
							strCheck, true);
					if (docCheck == null) {
						buildRequest(nYear, nMonth, nDay, nHour, nMinute,
								strJobIDT, docDefinition);
					}
					strCheck = strJobIDT + "@@@" + nYear + "@@@" + nMonth
							+ "@@@" + nDay + "@@@" + (nHour + 1) + "@@@"
							+ nMinute;
					docCheck = viwOpenSchedule.getDocumentByKey(strCheck, true);
					if (docCheck == null) {
						buildRequest(nYear, nMonth, nDay, nHour + 1, nMinute,
								strJobIDT, docDefinition);
					}
				}
				if ("WEEKLY".equals(docDefinition
						.getItemValueString("ScheduleT"))) {

				}
			}
		} catch (Exception ex) {

		}

		return true;
	}

	private void buildRequest(int nYear, int nMonth, int nDay, int nHour,
			int nMinute, String strJobIDT, Document docJob) {

		try {
			MainTask.getInstance().doLog(
					MainTask.LOG_NORMAL,
					"Creating new Request for Job "
							+ docJob.getItemValueString("JobNameT"));
			Session sesCurrent = docJob.getParentDatabase().getParent();
			DateTime dtDate = sesCurrent.createDateTime(new Date());
			dtDate.setLocalDate(nYear, nMonth, nDay);
			DateTime dtTime = sesCurrent.createDateTime(new Date());
			dtTime.setLocalDate(nYear, nMonth, nDay);
			dtTime.setLocalTime(nHour, nMinute, 0, 0);
			Document docRequest = docJob.getParentDatabase().createDocument();
			docRequest.replaceItemValue("Form", "frmRequest");
			docRequest.replaceItemValue("JobIDT", strJobIDT);
			docRequest.replaceItemValue("ActiveT", "1");
			docRequest.replaceItemValue("ExecutionDateDT", dtDate);
			docRequest.replaceItemValue("ExecutionTimeDT", dtTime);
			docRequest.replaceItemValue("TypeT", "Job");
			docRequest.replaceItemValue("JobNameT",
					docJob.getItemValueString("JobNameT"));
			docRequest.save(true, false, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
