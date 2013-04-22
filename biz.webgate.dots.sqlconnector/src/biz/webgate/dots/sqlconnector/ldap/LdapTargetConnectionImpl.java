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

import java.util.Date;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import biz.webgate.dots.sqlconnector.ITargetConnection;
import biz.webgate.dots.sqlconnector.ITargetDataSet;
import biz.webgate.dots.sqlconnector.SQLConnectorException;

public class LdapTargetConnectionImpl implements ITargetConnection {

	private String m_URL;
	private String m_Class;
	private String m_UserName;
	private String m_Password;
	private InitialDirContext m_DirContext;
	private Exception m_LastException;
	private String m_SearchBase;
	private String m_SearchFilter;

	public LdapTargetConnectionImpl(String uRL, String class1, String userName,
			String password, String strSearchBase, String strSearchFilter) {
		super();
		m_URL = uRL;
		m_Class = class1;
		m_UserName = userName;
		m_Password = password;
		m_SearchBase = strSearchBase;
		m_SearchFilter = strSearchFilter;
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
	public ITargetDataSet findTargetSetByStringKey(String strKey)
			throws SQLConnectorException {
		SearchResult sr = doSearch(strKey);
		if (sr != null) {
			return new LdapTargetData(sr,m_DirContext);
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByIntKey(int nKey)
			throws SQLConnectorException {
		SearchResult sr = doSearch(""+nKey);
		if (sr != null) {
			return new LdapTargetData(sr,m_DirContext);
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByDblKey(double nKey)
			throws SQLConnectorException {
		SearchResult sr = doSearch(""+nKey);
		if (sr != null) {
			return new LdapTargetData(sr,m_DirContext);
		}
		return null;
	}

	@Override
	public ITargetDataSet findTargetSetByDateKey(Date datKey)
			throws SQLConnectorException {
		SearchResult sr = doSearch(""+datKey.toString());
		if (sr != null) {
			return new LdapTargetData(sr,m_DirContext);
		}
		return null;
	}

	@Override
	public String getTargetID() {
		return null;
	}

	@Override
	public void executeAfterExecutionJob(String strJobName)
			throws SQLConnectorException {
		// TODO Auto-generated method stub

	}

	@Override
	public Exception getLastException() {
		return m_LastException;
	}

	private SearchResult doSearch(String strKey) {
        try
        {
            String strSearchPattern = m_SearchFilter.replace("?", strKey);
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            //SERACH PATTERN DARF NICHT NULL SEIN
            if (strSearchPattern == null)
            {
                return null;
            }
            @SuppressWarnings("rawtypes")
			NamingEnumeration namEn = m_DirContext.search(m_SearchBase,
                    strSearchPattern,ctls);
            if (namEn.hasMoreElements())
            {
                SearchResult srCurrent = (SearchResult) namEn.nextElement();
                if (namEn.hasMoreElements())
                {
                    return null;
                }
                return srCurrent;
            }
            return null;
        } catch (Exception e)
        {
            m_LastException = e;
            return null;
        }
	}
	
}
