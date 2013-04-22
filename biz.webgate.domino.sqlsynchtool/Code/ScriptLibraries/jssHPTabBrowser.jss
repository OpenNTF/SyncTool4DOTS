function TabElementProperty ( strName, valValue ) {
	var m_Name = strName;
	var m_Value = valValue;
	
	this.getName = function () {
		return m_Name;
	}
	this.getValue = function () {
		return m_Value;
	}
}

function TabElement (strName, strPlaceTitle, strURL) {
	var m_Name = strName;
	var m_PlaceTitle = strPlaceTitle;
	var m_URL = strURL;
	var m_Active = true;
	var m_Properties = new Array();
	
	this.getName = function () {
		return m_Name;
	}
	this.setName = function (strName) {
		m_Name = strName;
	}
	this.getPlaceTitle = function () {
		return m_PlaceTitle;
	}
	this.getURL = function() {
		return m_URL;
	}
	this.isActive = function () {
		return m_Active;
	}
	this.setActive = function (isActive) {
		m_Active = isActive;
	}
	this.isEqual = function (oTab) {
		if (oTab.getName() != m_Name) 
			return false;
		if (oTab.getURL() != m_URL) 
			return false;
		return true;
	}
	this.addProperty = function (oProperty) {
		m_Properties.push( oProperty);
	}
	this.getPropertyByName = function ( strName ) {
		for (var nCounter = 0; nCounter < m_Properties.length; nCounter++ ) {
			if (m_Properties[nCounter].getName() == strName) {
				return m_Properties[nCounter];
			}
		}
		return null;
	}
}

function getTabBrowser() {
	if (sessionScope.containsKey("navTabBrowser")) {
		return sessionScope.get("navTabBrowser");
	}
	else
	{
		var nTB = new TabBrowser();
		//Construct First Tab
		var nTabElement = new TabElement(@DbTitle(), "Meine Notizen", "xpHome.xsp");
		var oProp = new TabElementProperty("contentNav", 1);
		nTabElement.addProperty( oProp);
		nTB.initTab( nTabElement);
		sessionScope.put("navTabBrowser", nTB);
		return nTB;
	}
}

function TabBrowser() {
	var m_Tabs = new Array();
	var m_ActiveTab;
	this.getTabs = function () {
		return m_Tabs;
	}

	this.initTab = function (tabInit) {
		m_Tabs = new Array();
		tabInit.setActive(true);
		m_Tabs.push(tabInit);
		m_ActiveTab = tabInit;
	}
	this.addTab = function(tabTest) {
		for (var nCounter =0; nCounter < m_Tabs.length; nCounter++ ) {
			var tabCurrent = m_Tabs[nCounter];
			if (tabCurrent.isEqual(tabTest)) {
				m_Tabs = m_Tabs.splice(nCounter,1,tabTest);
				this.activateTab( nCounter );
				return;
			}
		}
		m_Tabs.push( tabTest);
		this.activateTab (m_Tabs.length -1);
	}
	
	this.modifyTab = function (nNumber, strName) {
		m_Tabs[nNumber].setName(strName);
	}
	
	this.activateTab = function (nNumber) {
		var nTab = null;
		for (var nCounter =0; nCounter < m_Tabs.length; nCounter++ ) {
			if (nCounter == nNumber) {
				m_Tabs[nCounter].setActive(true);
				nTab = m_Tabs[nCounter];
			} else {
				m_Tabs[nCounter].setActive(false);
			}
		}
		if (nTab != null) {
			m_ActiveTab = nTab;
			context.redirectToPage(nTab.getURL());			
		}
	}
	
	this.removeTab = function ( nNumber) {
		var m_TabsTemp = new Array();
		m_TabsTemp = m_Tabs.splice(nNumber,1);
		m_Tabs = m_TabsTemp;
		if (nNumber == 0) {this.activateTab(0);} else {this.activateTab(nNumber -1);}
	}
	
	this.getActiveTabNumber = function () {
		for (var nCounter =0; nCounter < m_Tabs.length; nCounter++ ) {
			if (m_Tabs[nCounter].isActive()) {
			 return nCounter;
			}
			this.activateTab(0);
			return 0;
		}
	}
	this.getActiveTab = function() {
		return m_ActiveTab;
	}
}