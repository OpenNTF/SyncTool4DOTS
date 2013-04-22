function mOver(chapter){
	//alert(document.getElementById(chapter).className);
		document.getElementById(chapter).className= "m_over";
//		alert(document.getElementById(chapter).className);
	}
	function mOut(chapter){

		document.getElementById(chapter).className= "m_out";
	}


function loadChildSectionInline(doctype, subtype, divid, key) { // this one is called from a solution document.. results will display inline in the right-hand box
	var udiv = document.getElementById(divid); 
	if (udiv.style.display=="none") {
		udiv.style.display=""; 
		//var ai = new AJAXInteraction( basepath + "/ajax-childs?OpenAgent&action=getsection&solutionkey=" + key + "&divid="+divid+"&doctype=" + doctype + "&subtype="+subtype+"&timestamp="+ getCacheStamp() ,  handleHttpResponseGeneric );
		ai.doGet();
	} else {
		udiv.style.display="none"; 
	}
	
}
function getCacheStamp() {
	Stamp = new Date(); // fix for IE cache problem: add timestamp to url on reload
	return Stamp.getDay()+""+Stamp.getHours()+Stamp.getMinutes()+Stamp.getSeconds();
}


/*
function toggle( panID ) {	
	if (panID == 1)
	{ 
		document.getElementById("liByArea").className="lotusSelected";		
		document.getElementById("liMyDocuments").className="";
	}
	else if (panID == 2)
	{ 
		document.getElementById("liByArea").className="";
		document.getElementById("liMyDocuments").className="lotusSelected";
	}	
}*/