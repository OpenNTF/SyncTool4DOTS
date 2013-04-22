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

public class Constants {
	public final static int DATATYPE_INT = 1;
	public final static int DATATYPE_DOUBLE = 2;
	public final static int DATATYPE_DATE = 3;
	public final static int DATATYPE_STRING = 4;
	
	
	public final static int E_JOB_INIT = 1000;
	public final static int E_CONNECTOR_INIT = 1001;
	public final static int E_CONNECTOR_NOTFOUND = 1002;
	public final static int E_CONNECTOR_SOURCE_FAILED = 1100;
	public final static int E_CONNECTOR_SOURCE_EXEC = 1101;
	public final static int E_CONNECTOR_TARGET_FAILED = 1200;
	public final static int E_CONNECTOR_TARGET_KEY = 1201;


	public final static int E_VP_WASNULL = 2001;

	
	public final static int E_VP_DATEVALUE = 2101;
	public final static int E_VP_DATEVALUE_C = 2102;
	public final static int E_VP_DATEVALUE_NS = 2103;

	public final static int E_VP_DBLVALUE = 2201;
	public final static int E_VP_DBLVALUE_C = 2202;
	public final static int E_VP_DBLVALUE_NS = 2203;

	public final static int E_VP_INTVALUE = 2301;
	public final static int E_VP_INTVALUE_C = 2302;
	public final static int E_VP_INTVALUE_NS = 2303;

	public final static int E_VP_STRINGVALUE = 2401;
	public final static int E_VP_STRINGVALUE_C = 2402;
	public final static int E_VP_STRINGVALUE_NS = 2403;

	public final static int E_NOTES_DOC_SAVE = 3000;
	public final static int E_NOTES_DOC_ACCESS = 3001;
	public final static int E_NOTES_DOC_VALUE_ASSIGN = 3002;
	public final static int E_NOTES_DOC_VALUE_ACCESS = 3003;

	public final static int E_NOTES_AGENT_AFTER_EX = 3100;
	public final static int E_NOTES_AGENT_AFTER_EX_NOTFOUND = 3101;
	public final static int E_NOTES_AGENT_AFTER_CR = 3200;
	public final static int E_NOTES_AGENT_AFTER_CR_NOTFOUND = 3201;
	public final static int E_NOTES_AGENT_AFTER_UP = 3300;
	public final static int E_NOTES_AGENT_AFTER_UP_NOTFOUND = 3301;

	
	public final static int E_SQL_ROW_SAVE = 4000;
	public final static int E_SQL_ROW_ACCESS = 4001;
	public final static int E_SQL_ROW_VALUE_ASSIGN = 4002;
	public final static int E_SQL_ROW_VALUE_ACCESS = 4003;

}
