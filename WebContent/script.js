/*
 * Druckversion 
 */
var printVersion = false;
function switchPrintVersion() {
	if(printVersion == true) {
		document.getElementById("printImg").src = "img/printOn.png";
		document.getElementById("styles").href = "styles.css";
		printVersion = false;
	} else {
		var jetzt = new Date();
		document.getElementById("printDatum").innerHTML = jetzt.getDate() + "." + jetzt.getMonth()+1 + "." + jetzt.getFullYear();
		document.getElementById("printZeit").innerHTML = jetzt.getHours() + ":" + jetzt.getMinutes() + " Uhr";
		
		document.getElementById("printImg").src = "img/printOff.png";
		document.getElementById("styles").href = "stylesPrint.css";
		printVersion = true;
	}
}

/*
 * Schleife, alle 2,5min
 */
function loopProcedure() {
	checkPositionChanged();
}

/*
 * ---------------- POP UP Globale Variablen
 */
var openedPopUp;
var map;
var mapBackground;
var mapFrame;
var mapClose;

/*
 * Open Popup
 */
function openPopUp(handle) {	
	if (typeof openedPopUp === "undefined") {
		mapBackground = document.createElement("div");
		mapBackground.setAttribute("id", "mapBackground");
		mapFrame = document.createElement("div");
		mapFrame.setAttribute("id", "mapFrame");
		mapClose = document.createElement("div");
		mapClose.setAttribute("id", "mapClose");
		map = document.getElementById("mapCanvas");
		mapClose.innerHTML = "<img src='close.jpg'>";
		document.body.insertBefore(mapBackground, map);
		document.body.insertBefore(mapFrame, map);
		document.body.insertBefore(mapClose, map);
		if (window.addEventListener) {
			mapBackground.addEventListener("click", openPopUp, false);
			mapClose.addEventListener("click", openPopUp, false);
		} else {
			mapBackground.attachEvent("onclick", openPopUp);
			mapClose.attachEvent("onclick", openPopUp);
		}
		openedPopUp = "";
	}
	if (openedPopUp == "") {
		mapBackground.style.visibility = "visible";
		mapFrame.style.visibility = "visible";
		mapClose.style.visibility = "visible";
		document.getElementById(handle).style.visibility = "visible";
		openedPopUp = handle;
	} else {
		mapBackground.style.visibility = "hidden";
		mapFrame.style.visibility = "hidden";
		mapClose.style.visibility = "hidden";
		document.getElementById(openedPopUp).style.visibility = "hidden";
		openedPopUp = "";
	}
	
	if(handle == "mapCanvas") {
		document.getElementById("mapFrame").style.marginLeft = "-212px";
		document.getElementById("mapFrame").style.width = "424px";
		document.getElementById("mapClose").style.marginLeft ="201px"; 
	} else if(handle == "userOnlineCanvas") {
		document.getElementById("mapFrame").style.marginLeft = "-312px";
		document.getElementById("mapFrame").style.width = "624px";
		document.getElementById("mapClose").style.marginLeft ="301px"; 
	}
}

/* Öffnet das PopUp mit allen Sniffs drauf */
function openPopUpWithAllMarkers(handle) {
	showCircle(parseInt(document.getElementById('sniffRatioBox').value));
	showAllMarkersInMap();
	openPopUp(handle);
}

/* Öffnet das PopUp mit dem angegebenem Sniff als Marker drauf */
function openPopUpWithSingleMarker(id) {
	showCircle(parseInt(document.getElementById('sniffRatioBox').value));
	showSingleMarkerInMap(document.getElementById("lat" +id).innerHTML, document.getElementById("lng" +id).innerHTML, document.getElementById("sniffDiv" +id).innerHTML);
	openPopUp("mapCanvas");
}

/* ----------- hidden Sniffs visiblen! -------- */
function makeVisible() {
	for(var i=0; i<idArray.length; i++) {
		document.getElementById(idArray[i]).style.display = "";
	}
	document.getElementById('newSmell').style.visibility = "hidden";
	numberOfNewMessages = 0;
	idArray = null;
	
	document.getElementById("noSniffSniff").style.display = "none";
}

/* ---- Sniffs weg */
function clearSniffs() {
	document.getElementById("sniffs").innerHTML = "<div class='sniff' id='noSniffSniff'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>Bisher nichts gesnifft</i></div>";
	document.getElementById("printSniffs").innerHTML = "<legend>Sniffs</legend>";
}

/* ---- mehr Sniffs anzeigen */
function showMoreSniffs() {
	ws.send("{\"type\": 4, \"offset\": " + count + "}");
}

/* userOnline anfragen */
function getUsersOnline() {
	// Alte Marker löschen
	if(allUsersMarkers) {
		for(i in allUsersMarkers) {
			allUsersMarkers[i].setMap(null);
	    }	
		allUsersMarkers = [];
	}
	openPopUp("userOnlineCanvas");
	ws.send("{\"type\": 7}");
}

/*
 * neuer Name in Cookie und an Server
 */
function sendNameToServer() {	
	/*
	 * Print-Version aktualisieren
	 */
	document.getElementById("printName").innerHTML = document.writeForm.name.value;
	
	/*
	 * in Cookie
	 */
	if(document.cookie == "" || document.cookie.substring(0, 3) != document.writeForm.name.value) {
		document.cookie = "name="+document.writeForm.name.value;
	}
	
	/*
	 * an server
	 */
	if(document.writeForm.name.value != "") {
		ws.send("{" +
				"\"type\": 6," +
				"\"name\": " + document.writeForm.name.value +
			"}");
	}
}

/*
 * Timer-Funktion (Aufruf f�r bla(): bla.Timer(interval, calls / Infinity, onend : function);
 */
Function.prototype.Timer = function(interval, calls, onend) {
	var count = 0;
	var payloadFunction = this;
	var startTime = new Date();
	var callbackFunction = function() {
		return payloadFunction(startTime, count);
	};
	var endFunction = function() {
		if (onend) {
			onend(startTime, count, calls);
		}
	};
	var timerFunction = function() {
		count++;
		if (count < calls && callbackFunction() != false) {
			window.setTimeout(timerFunction, interval);
		} else {
			endFunction();
		}
	};
	timerFunction();
};

/*
 * ---------- CharsLeftCounter ----------
 * Zaehlt die restlichen Zeichen
 */
function textLeftCounter() {
	// trimmen, falls zu lang
	if (document.writeForm.text.value.length > 150)
		document.writeForm.text.value = document.writeForm.text.value
				.substring(0, 150);
	// sonst ausgeben
	else
		document.getElementById('counter').innerHTML = (150 - document.writeForm.text.value.length);
}


/*
 * CHAT
 */
document.userListForm.listSelect.onchange = function() {
	if(JSON.parse(this.value).name == "Du") {
		document.chat.text.disabled = true;
		document.getElementById("chatUserName").innerHTML = "?";
	} else {
		document.chat.text.disabled = false;
		document.getElementById("chatUserName").innerHTML = JSON.parse(this.value).name;
	}
};

function sendChat() {
	var temp = document.userListForm.listSelect;
	if(document.chat.text.value != "" && document.chat.text.disabled == false) {
		ws.send("{" +
					"\"type\":" + 8 + "," +
					"\"hash\":" + JSON.parse(temp.options[temp.selectedIndex].value).hash + "," +
					"\"message\":\"" + document.chat.text.value + "\"" +
				"}");
		document.chat.text.value = "";
	}
}