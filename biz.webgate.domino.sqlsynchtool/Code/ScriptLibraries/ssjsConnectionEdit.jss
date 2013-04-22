function setConnectionType( docCurrent ) {
	var strType = docCurrent.getItemValueString("conTypeT");
	if (strType == null || strType == "") {
		strType = facesContext.getExternalContext().getRequest().getParameter("TYPE");
	}
	if (strType == "SQL") {
		requestScope.put("isSQL", "1");
		requestScope.put("isNotes", "0");
	} else {
		requestScope.put("isSQL", "0");
		requestScope.put("isNotes", "1");
		
	}
}