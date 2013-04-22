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
package biz.webgate.dots.sqlconnector.definition;

import biz.webgate.dots.sqlconnector.Constants;
import biz.webgate.dots.sqlconnector.ISourceConnection;
import biz.webgate.dots.sqlconnector.ITargetConnection;
import biz.webgate.dots.sqlconnector.SQLConnectorException;
import biz.webgate.dots.sqlconnector.ldap.LdapSourceConnectionImpl;
import biz.webgate.dots.sqlconnector.ldap.LdapTargetConnectionImpl;
import biz.webgate.dots.sqlconnector.notes.NotesConnectorImpl;
import biz.webgate.dots.sqlconnector.sql.SQLSourceConnectionImpl;
import biz.webgate.dots.sqlconnector.sql.SQLTargetConnectionImpl;
import lotus.domino.*;

public class JobBuilderService {
	private static JobBuilderService m_Service;

	private JobBuilderService() {
	}

	public static JobBuilderService getInstance() {
		if (m_Service == null) {
			m_Service = new JobBuilderService();
		}
		return m_Service;
	}

	public JobDefinition buildJobDefinition(Document docRequest)
			throws SQLConnectorException {
		JobDefinition jbRC = null;
		try {
			Database ndbDefinition = docRequest.getParentDatabase();
			View viwJobDef = ndbDefinition.getView("LUPJobByID");
			Document docDefinition = viwJobDef.getDocumentByKey(docRequest
					.getItemValue("JobIDT"));
			View viwConnections = ndbDefinition.getView("LUPConnectionsByID");

			jbRC = new JobDefinition(getSourceConnection(viwConnections,
					docDefinition.getItemValueString("SourceConnectionIDT")),
					getTargetConnection(viwConnections, docDefinition
							.getItemValueString("TargetConnectionIDT")),
					docDefinition.getItemValueString("KeyFieldT"),
					Integer.parseInt(docDefinition
							.getItemValueString("KeyFieldTypeT")),
					docDefinition.getItemValueString("JobIDT"),
					docDefinition.getItemValueString("JobAfterCreateT"),
					docDefinition.getItemValueString("JobAfterUpdateT"),
					docDefinition.getItemValueString("JobAfterExecutionT"));
			if ("1".equals(docDefinition.getItemValueString("ChangeLogT"))) {
				jbRC.setChangeLog(true);
			}
			View viwFieldMapping = ndbDefinition.getView("LUPFieldMapping");
			ViewEntryCollection nvcCurrent = viwFieldMapping
					.getAllEntriesByKey(docDefinition
							.getItemValueString("JobIDT"));
			ViewEntry nveNext = nvcCurrent.getFirstEntry();
			while (nveNext != null) {
				ViewEntry nveProcess = nveNext;
				nveNext = nvcCurrent.getNextEntry(nveNext);

				Document docFM = nveProcess.getDocument();

				FieldMapping fmCurrent = new FieldMapping(
						docFM.getItemValueString("SourceFieldT"),
						docFM.getItemValueString("TargetFieldT"),
						docFM.getItemValueString("TransformFormulaT"),
						Integer.parseInt(docFM
								.getItemValueString("TargetTypeT")));

				if ("1".equals(docFM.getItemValueString("updateOnlyT"))) {
					fmCurrent.setOnUpdateOnly(true);
				}
				if ("1".equals(docFM.getItemValueString("createOnlyT"))) {
					fmCurrent.setOnCreateOnly(true);
				}
				if ("1".equals(docFM.getItemValueString("computeOnlyT"))) {
					fmCurrent.setComputeOnly(true);
				}

				jbRC.addFieldMapping(fmCurrent);
				docFM.recycle();
				nveProcess.recycle();
			}
			nvcCurrent.recycle();
			viwFieldMapping.recycle();
			viwConnections.recycle();
			viwJobDef.recycle();
			docDefinition.recycle();
		} catch (Exception e) {
			throw new SQLConnectorException(Constants.E_JOB_INIT,
					"Error during Job Initialization.", e);
		}

		return jbRC;
	}

	public ITargetConnection getTargetConnection(View viwConnections,
			String strID) throws SQLConnectorException {
		ITargetConnection tcRC = null;
		try {
			Document docConnection = viwConnections.getDocumentByKey(strID);
			if (docConnection == null) {
				throw new SQLConnectorException(Constants.E_CONNECTOR_NOTFOUND,
						"Document for Connector ID: " + strID + " not found!");
			}

			if (docConnection.getItemValueString("conTypeT").equals("SQL")) {
				tcRC = new SQLTargetConnectionImpl(
						docConnection.getItemValueString("conSQLUsernameT"),
						docConnection.getItemValueString("conSQLPasswordT"),
						docConnection.getItemValueString("conSQLDBURLT"),
						docConnection.getItemValueString("conSQLSelectionT"),
						docConnection.getItemValueString("conSQLDRIVERT"),
						"1".equals(docConnection
								.getItemValueString("conSQLCreateNewT")),
						docConnection.getItemValueString("conSQLKeyFieldT"),
						docConnection.getItemValueString("conSQLTableT"),
						"1".equals(docConnection
								.getItemValueString("conSQLSingleUpdateT")));
			}else if (docConnection.getItemValueString("conTypeT").equals("LDAP")) {
				tcRC = new LdapTargetConnectionImpl(
						docConnection.getItemValueString("conLDAPURLT"),
						docConnection.getItemValueString("conLDAPClass"),
						docConnection.getItemValueString("conLDAPUserName"),
						docConnection.getItemValueString("conLDAPPassword"),
						docConnection.getItemValueString("conLDAPSearchBase"),
						docConnection.getItemValueString("conLDAPSearchFilter"));
			} else {
				tcRC = new NotesConnectorImpl(viwConnections.getParent()
						.getParent(),
						docConnection.getItemValueString("conNotesServerT"),
						docConnection.getItemValueString("conNotesDBT"),
						docConnection.getItemValueString("conNotesViewT"),
						docConnection.getItemValueString("conNotesFormT"));
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_CONNECTOR_INIT,
					"Error during Initializing of Connector ID: " + strID, ex);
		}
		return tcRC;
	}

	public ISourceConnection getSourceConnection(View viwConnections,
			String strID) throws SQLConnectorException {
		ISourceConnection scRC = null;
		try {
			Document docConnection = viwConnections.getDocumentByKey(strID);
			if (docConnection == null) {
				throw new SQLConnectorException(Constants.E_CONNECTOR_NOTFOUND,
						"Document for Connector ID: " + strID + " not found!");
			}
			if (docConnection.getItemValueString("conTypeT").equals("SQL")) {
				scRC = new SQLSourceConnectionImpl(
						docConnection.getItemValueString("conSQLUsernameT"),
						docConnection.getItemValueString("conSQLPasswordT"),
						docConnection.getItemValueString("conSQLDBURLT"),
						docConnection.getItemValueString("conSQLSelectionT"),
						docConnection.getItemValueString("conSQLDRIVERT"));
			}else if (docConnection.getItemValueString("conTypeT").equals("LDAP")) {
				scRC = new LdapSourceConnectionImpl(
						docConnection.getItemValueString("conLDAPURLT"),
						docConnection.getItemValueString("conLDAPClass"),
						docConnection.getItemValueString("conLDAPUserName"),
						docConnection.getItemValueString("conLDAPPassword"));
			} else {
				scRC = new NotesConnectorImpl(viwConnections.getParent()
						.getParent(),
						docConnection.getItemValueString("conNotesServerT"),
						docConnection.getItemValueString("conNotesDBT"),
						docConnection.getItemValueString("conNotesViewT"),
						docConnection.getItemValueString("conNotesFormT"));
			}
		} catch (Exception ex) {
			throw new SQLConnectorException(Constants.E_CONNECTOR_INIT,
					"Error during Initializing of Connector ID: " + strID, ex);
		}
		return scRC;
	}
}
