/** Author: Tony McGuckin, IBM **/

DISPLAY_ALL_DOCUMENTS		= 0;
DISPLAY_BY_MOST_RECENT 		= 1;
DISPLAY_BY_AUTHOR			= 2;
DISPLAY_BY_TAG				= 3;
DISPLAY_MY_DOCUMENTS		= 4;
DISPLAY_AUTHOR_PROFILE 		= 5;
DISPLAY_TOPIC_THREAD 		= 6;
DISPLAY_SETTINGS 			= 7;		// WGC MBA: added
DISPLAY_BY_AREA				= 8;		// WGC MBA: added
DISPLAY_BY_MOST_FOLLOWED	= 9;		// WGC MBA: added
DISPLAY_BY_MOST_DOWNLOADED	= 10;		// WGC MBA: added

DEFAULT_ROW_COUNT			= 25;

//-----------
function init() {	// setup sessionScope defaults...
//-----------	

	initSession();
	
} // end init

//------------------
function initSession() {	// setup sessionScope defaults...
//------------------	

	// we need to do this EVERY TIME
	if(sessionScope.effectiveUserName !== session.getEffectiveUserName()) {

		// (re)cache current user name and ACL info (in case user login credetials change from anonymous to a real login)

		var _request		= facesContext.getExternalContext().getRequest();
		var _cookies		= _request.getHeader("Cookie"); //dnt
		var _canonicalName	= session.getEffectiveUserName();
		var _nn:NotesName	= session.createName(_canonicalName);
										
		sessionScope.isSessionAuth			= @Contains(_cookies, "DomAuthSessId") === 1 ? true : false; //dnt
		sessionScope.effectiveUserName		= _canonicalName;
		sessionScope.commonUserName			= _nn.getCommon();
		sessionScope.abbreviatedUserName	= _nn.getAbbreviated();
		sessionScope.isAnonymous			= sessionScope.effectiveUserName === "Anonymous"; //dnt
		sessionScope.aclPrivileges			= database.queryAccessPrivileges(sessionScope.effectiveUserName);
		
		//WGC MBA: check for User Profile, create if not existing
		var _wiw = session.getCurrentDatabase().getProfileDocument("frmCloudConfig", "config").getItemValueString("AppNPSocialNetT");
		var _user:NotesName = session.createName(@UserName());
		var _socialid = session.getDatabase(null, _wiw).getView("vwLkupProfiles").getDocumentByKey(@UserName()).getItemValueString("socialid");
		var _db:NotesDatabase = session.getCurrentDatabase();
		var _doc:NotesDocument = _db.getView("luPersonalProfiles").getDocumentByKey(_user.getAbbreviated(), true);
		if (_doc == null) {
			_doc = _db.createDocument();
			_doc.replaceItemValue("Form", "frmPersonalProfile");
			_doc.replaceItemValue("socialid", _socialid);	
			_doc.replaceItemValue("FromCN", _user.getCommon());
		}
		_doc.computeWithForm(false, false);
		_doc.save(true,false,true);
	}
	
	// everything else needs to be done only ONCE, so if we're already initialized, get out.
	if(sessionScope.initApp === true) {
		return;
	}

	sessionScope.path = facesContext.getExternalContext().getRequestContextPath();
					
	// remember that we're initialized
	sessionScope.initApp = true;
		
} // end initSession

//---------------------
function setPageHistory(linkID) {	// remember the current page name and title for the "back to XZY" link
//---------------------

	if (linkID == "area") {
		sessionScope.historyPageName	= "/xpByArea.xsp";
		sessionScope.historyPageTitle	= "";		
	} else {
		sessionScope.historyPageName	= view.getPageName();
		sessionScope.historyPageTitle	= getComponent(linkID).getTitle();
	}

} // end setPageHistory

//---------------------
function setDisplayFormType(displayFormType) {	// new topic or response? possible values 1 || 2
//---------------------
	sessionScope.displayFormType = displayFormType;

} // end setDisplayFormType


//---------------------
function getDisplayFormType(){ // return the curent form type 1==new topic, 2==response
//---------------------
	return sessionScope.displayFormType;
	
} // end getDisplayFormType



/* ******************** Additions by WGC *********************/

//@WebDbName - by Michael Gollmick
//http://blog.gollmick.de/mgoblog.nsf/40f2c735481f54dd80256d650047636c/0687660450ad65bdc125754a008357ce?OpenDocument
function @WebDbName() {
        try {
                if (typeof this.name === 'undefined') {
                        var path = database.getFilePath();
                        var re = new RegExp("\\\\", "g");
                        path = path.replace(re, "/");
                        var arr = path.split("/");
                        for (var a = 0; a < arr.length; a++) {
                                arr[a] = escape(arr[a]);
                        }
                        this.name = arr.join("/");
                }
        } catch (e) {
        }
        return this.name;
}

// convert format of names fields
var Converters = {  
  abbreviate: {
    // separator: String or RegExp
    getAsObject: function( value ){
      try {        
        return @Name("[Canonicalize]", value);
      } catch( e ){ /* Exception handling */ }
    },
    getAsString: function( value ){
      try {
        return @Name("[Abbreviate]", value);
      } catch( e ){ /* Exception handling */ }
    }
  }
}

//follow / stop following a topic or area
function follow(what, direction) {
	//get User Profile
	var _user = session.createName(@UserName());
	var _db = session.getCurrentDatabase();
	var _doc = _db.getView("luPersonalProfiles").getDocumentByKey(_user.getAbbreviated(), true);
	
	//prepare entry in socialnet
	var _docTemp: NotesDocument = _db.createDocument();
	_docTemp.replaceItemValue("socialID", _doc.getItemValueString("socialid"));
	_docTemp.replaceItemValue("appID", applicationScope.appID);
	
	if (what == "topic") {
		//follow topic
		//add main topic docunid to subscription list
		var _id = context.getUrl().getParameter("documentId");
		var _docDisc = _db.getDocumentByUNID(_id);
		
		var _itm:NotesItem = _doc.getFirstItem("Subscriptions");
		_docTemp.replaceItemValue("followID", _docDisc.getItemValueString("AreaID") + "\\" + _docDisc.getItemValueString("followID"));
		
		if (direction == 1) {
			//add subscription
			if (_itm == null) {
				_doc.replaceItemValue("Subscriptions", _docDisc.getItemValueString("followID"));			
			} else {
				_itm.appendToTextList(_docDisc.getItemValueString("followID"));	
			}
			_docTemp.replaceItemValue("action", "add");		
		} else {
			//remove subscription
			var _list = @Replace(_itm.getValues(), _docDisc.getItemValue("followID"), "")
			_doc.replaceItemValue("Subscriptions", _list);
			_docTemp.replaceItemValue("action", "remove");
		}
	} else {
		//follow area		
		var _itm:NotesItem = _doc.getFirstItem("SubscriptionsArea");
		_docTemp.replaceItemValue("followID", sessionScope.areaName);
		if (direction == 1) {
			//add subscription
			if (_itm == null) {
				_doc.replaceItemValue("SubscriptionsArea", sessionScope.areaName);			
			} else {
				_itm.appendToTextList(sessionScope.areaName);	
			}
			_docTemp.replaceItemValue("action", "add");		
		} else {
			//remove subscription
			var _list = @Replace(_itm.getValues(), sessionScope.areaName, "")
			_doc.replaceItemValue("SubscriptionsArea", _list);
			_docTemp.replaceItemValue("action", "remove");
		}
	}
	
	_doc.computeWithForm(false, false);
	_doc.save(true);
	
	//save agent parameter doc
	_docTemp.save();
	var agnt:NotesAgent = session.getCurrentDatabase().getAgent("(updateSocialNet)");
	//agnt.runOnServer(_docTemp.getNoteID());
	 
	if (what == "topic") {
		//update posting documents
		agnt = session.getCurrentDatabase().getAgent("(updateFollowers)");
		agnt.runOnServer(dominoDoc.getNoteID()); //dominoDoc is the topic to be followed
	}
}

//debug function. Da alert im SSJS nicht geht, schicke ich mir ein Mail mit der Ausgabe
function debugMail(wert) {
	var db:NotesDatabase = session.getCurrentDatabase();
	var memo:NotesDocument = db.createDocument();
	memo.replaceItemValue("Form", "Memo");
	memo.replaceItemValue("SendTo", "Marco Baumann/WGC/CH");
	memo.replaceItemValue("Subject", wert);
	memo.send(false);
}