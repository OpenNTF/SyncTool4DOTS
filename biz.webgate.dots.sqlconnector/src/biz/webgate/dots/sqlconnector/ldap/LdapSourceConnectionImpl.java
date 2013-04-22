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
package biz.webgate.dots.sqlconnector.ldap;

import java.util.Hashtable;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import biz.webgate.dots.sqlconnector.ISourceConnection;
import biz.webgate.dots.sqlconnector.ISourceDataSet;

public class LdapSourceConnectionImpl implements ISourceConnection {

	private String m_URL;
	private String m_Class;
	private String m_UserName;
	private String m_Password;
	private InitialDirContext m_DirContext;
	private Exception m_LastException;

	public LdapSourceConnectionImpl(String uRL, String class1, String userName,
			String password) {
		super();
		m_URL = uRL;
		m_Class = class1;
		m_UserName = userName;
		m_Password = password;

	}

	@Override
	public boolean doConnect() {
		try {
			Hashtable<String, String> hsEnv = new Hashtable<String, String>();
			hsEnv.put(DirContext.PROVIDER_URL, m_URL);
			hsEnv.put(DirContext.SECURITY_AUTHENTICATION, "simple");
			hsEnv.put(DirContext.INITIAL_CONTEXT_FACTORY, m_Class);
			hsEnv.put(DirContext.SECURITY_PRINCIPAL, m_UserName);
			hsEnv.put(DirContext.SECURITY_CREDENTIALS, m_Password);
			m_DirContext = new InitialDirContext(hsEnv);
		} catch (Exception e) {
			m_LastException = e;
			return false;
		}
		return true;
	}

	@Override
	public boolean execute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasMoreElements() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ISourceDataSet nextElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exception getLastException() {
		return m_LastException;
	}

}
