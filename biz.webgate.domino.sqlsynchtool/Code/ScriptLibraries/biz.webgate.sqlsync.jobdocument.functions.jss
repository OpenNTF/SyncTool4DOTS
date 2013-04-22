function getTypeString( nValue ) {
	switch (nValue) {
	case "1": 
		return "Integer";
	case "2": 
		return "Double";
	case "3": 
		return "Date";
	case "4": 
		return "String";
	
	}
	return "Unknown";
}

function calcValuesSave() {
	docJob.replaceItemValue("SourceConnectionIDT", @DbLookup("","plSourceConnection",docJob.getItemValueString("SourceConnectionNameT"), "conIDT"));
	docJob.replaceItemValue("TargetConnectionIDT", @DbLookup("","plTargetConnection",docJob.getItemValueString("TargetConnectionNameT"), "conIDT"));
	if(docJob.getItemValueString("JobIDT") == "") {
		docJob.replaceItemValue("JobIDT",@Unique());
	}

}