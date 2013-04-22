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

public class SQLConnectorException extends Exception {



	
	private String m_Message;
	private int m_EventNr;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SQLConnectorException(int Event, String strMessage) {
		super(strMessage);
		m_Message = strMessage;
		m_EventNr = Event;
	}

	public SQLConnectorException(int Event, String strMessage,
			Throwable trCurrent) {
		super(strMessage, trCurrent);
		m_Message = strMessage;
		m_EventNr = Event;
	}

	public String getMessage() {
		return m_Message;
	}

	public int getEventNr() {
		return m_EventNr;
	}

}
