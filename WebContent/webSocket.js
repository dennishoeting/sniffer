var numberOfNewMessages = 0;
var ws;
document.ready(init());
var idArray;
var count;

/*
 * Initialisiert das WebSocket
 * 
 * Auto-Disconnect nach 5min
 */
function init() {	
	count = 0;
	
	/*
	 * Speicherung des webSocket
	 * 
	 */
//	ws = new WebSocket("ws://134.106.56.169:9144/Sniffer/");
	ws = new WebSocket("ws://localhost:8080/Sniffer/");
	
	/*
	 * �berschreiben der onopen-Funktionalit�t:
	 * 		Statustext �ndern
	 */
	ws.onopen = function(event) {
		$('#status').text(" ");
		
		/*
		 * Cookie lesen und Name an server senden
		 */
		var nameAusCookie;
		if(document.cookie != "") {
			nameAusCookie = document.cookie.substring(5, document.cookie.length);
			document.writeForm.name.value = nameAusCookie;
			sendNameToServer();
		}
		
		/*
		 * Initiale Initialisierungsinitialisierung
		 */
		initializeGeoLocation();
		
		/*
		 * wiederholte Standortaktualisierung (150000)
		 */
		loopProcedure.Timer(150000, 
							Infinity, 
							function error() {
								alert("Position konnte dieses Mal nicht erkannt werden.");
							});
	};
	
	/*
	 * �berschreiben der onmessage-Funktionalit�t:
	 * 		Popup anzeigen
	 */
	ws.onmessage = function(event) {	
		/*
		 * JSon parsen
		 */
		var parsed = JSON.parse(event.data);
		
		/*
		 * Alle Sniffs weg!
		 */
		if(parsed.type == 0) {
			clearSniffs();
			count=0;
			return;
		}
		
		
		/*
		 * Neue Nachricht
		 */
		if(parsed.type == 1) {
			count++;
			
			//idArray
			if(idArray) {
				var tempArray = new Array(idArray.length + 1);
				for(var i=0; i<idArray.length; i++) {
					tempArray[i] = idArray[i];
				}
				tempArray[idArray.length] = parsed.id;
				idArray = tempArray;
			} else {
				idArray = new Array(1);
				idArray[0] = parsed.id;
			}
			
			/*
			 * Bereich sichtbar machen / hochz�hlen
			 */
			if(parsed.yourOwn == false) {
				numberOfNewMessages++;
				
				var en = "";
				if(numberOfNewMessages > 1)
					en = "en";
				var newMessagesString = numberOfNewMessages + " neue Nachricht" + en;
				document.getElementById('newSmell').innerHTML = newMessagesString;
				if(numberOfNewMessages > 0)
					document.getElementById('newSmell').style.visibility = "visible";
			}
			
			addMarker(parsed.lat, parsed.lng, parsed.name);
			addSniff(parsed.id, parsed.name, parsed.text, parsed.time, parsed.date, parsed.distance, parsed.yourOwn, parsed.lat, parsed.lng, parsed.position);
			
			return;
		} else
		
		/*
		 * gibt es noch weitere Nachrichten? "more"-Anzeige anzeigen/ausblenden
		 */
		if(parsed.type == 2) {
			if(parsed.more) 
				document.getElementById("moreSniffs").style.visibility = "visible";
			else
				document.getElementById("moreSniffs").style.visibility = "hidden";
			
			return;
		} else
		
		/*
		 * Die weiteren Messages kommen an
		 */
		if(parsed.type == 3) {
			count++;
			
			document.getElementById("sniffs").innerHTML = document.getElementById("sniffs").innerHTML + 
			"<div class='sniff' id="+parsed.id+">"+
				"<span class='name'>"+parsed.name+"</span>"+
				"<span class='zeit'>"+parsed.time+" - "+parsed.date+"</span><br />"+
				"<span class='location'>"+parsed.position+"</span>"+
				"<span class='entfernung'>"+parsed.distance+"m entfernt</span><br />"+
				"<span class='hidden'><a onclick='javascript:openPopUpWithSingleMarker(\""+parsed.id+"\");'>Kartenansicht</a></span>"+
				"<span class='nachricht'>"+parsed.text+"</span><br />"+
				"<div id='sniffDiv"+parsed.id+"' style='visibility:hidden'>"+parsed.name+"</div>"+
				"<div id='lat"+parsed.id+"' style='display:none'>"+parsed.lat+"</div>"+
				"<div id='lng"+parsed.id+"' style='display:none'>"+parsed.lng+"</div>"+
			"</div>";
			
			document.getElementById("printSniffs").innerHTML = document.getElementById("printSniffs").innerHTML +
			"<div class='printSniff'>" +
				"<div><b>" + parsed.name + "</b> am <b>" + parsed.date + "</b> um <b>" + parsed.time + "</b></div>" +
				"<div>Position: <b>" + parsed.position + "</b>, <b>" + parsed.distance + "m</b> entfernt</div>" +
				"<div>" + parsed.text + "</div>" +
			"</div>";
			
			return;
		} else
		
		/*
		 * Neue Position
		 */
		if(parsed.type == 4) {
			if(!parsed.result) {
				initializeGeoLocation();
				alert("position wurde aktualisiert");
			}
		} else
		
		/*
		 * UserOnline aktualisiert
		 */
		if(parsed.type == 5) {
			document.getElementById("usersOnline").innerHTML = parsed.value + " Nutzer online";
		} else
		
		/*
		 * Userdaten empfangen
		 */
		if(parsed.type == 6) {	
			if(parsed.you == "true") {
				
				var hundeImage = 'img/hund.gif';
				// Marker setzen:
				allMarkersArray.push(new google.maps.Marker({
					position : new google.maps.LatLng(parsed.lat, parsed.lng),
					map : mapUserOnline,
					icon : hundeImage,
					title : parsed.name
				}));
				
			} else {
				
				var pawImage = 'img/paw.gif';
				// Marker setzen:
				allMarkersArray.push(new google.maps.Marker({
					position : new google.maps.LatLng(parsed.lat, parsed.lng),
					map : mapUserOnline,
					icon : pawImage,
					title : parsed.name
				}));
				
			}
			
			var content = parsed.name + " (" + parsed.distance + "m)";
			var newOption = new Option(content, event.data);
			document.userListForm.listSelect[parsed.id] = newOption;
		} else
		
		if(parsed.type == 7) {
			alert("Private Nachricht von " + parsed.from + ":\n" + parsed.message);
		}
	};
	
	/*
	 * �berschreiben der onclose-Funktionalit�t
	 * 		Status �ndern
	 */
	ws.onclose = function(event) {
		$('#status').text("Sniffer-WebSocket verbinden..");
	};
	
	function addSniff(id, name, text, time, date, distance, yourOwn, lat, lng, position) {
		
		if(yourOwn) {
			document.getElementById("sniffs").innerHTML = 
				"<div class='sniff' id="+id+">"+	
					"<span class='name'>"+name+"</span>"+
					"<span class='zeit'>"+time+" - "+date+"</span><br />"+
					"<span class='location'>"+position+"</span>"+
					"<span class='entfernung'>"+distance+"m entfernt</span><br />"+
					"<span class='hidden'><a onclick='javascript:openPopUpWithSingleMarker(\""+id+"\");'>Kartenansicht</a></span>"+
					"<span class='nachricht'>"+text+"</span><br />"+
					"<div id='sniffDiv"+id+"' style='visibility:hidden'>"+name+"</div>"+
					"<div id='lat"+id+"' style='display:none'>"+lat+"</div>"+
					"<div id='lng"+id+"' style='display:none'>"+lng+"</div>"+
				"</div>"+
				document.getElementById("sniffs").innerHTML;
			
				document.getElementById("printSniffs").innerHTML = 
					"<div class='printSniff'>" +
						"<div><b>" + name + "</b> am <b>" + date + "</b> um <b>" + time + "</b></div>" +
						"<div>Position: <b>" + position + "</b>, <b>" + distance + "m</b> entfernt</div>" +
						"<div>" + text + "</div>" +
					"</div>" +
					document.getElementById("printSniffs").innerHTML;
				
				document.getElementById("noSniffSniff").style.display = "none";
		} else {
			document.getElementById("sniffs").innerHTML = 
				"<div class='sniff' id="+id+" style='display:none'>"+	
					"<span class='name'>"+name+"</span>"+
					"<span class='zeit'>"+time+" - "+date+"</span><br />"+
					"<span class='location'>"+position+"</span>"+
					"<span class='entfernung'><b>"+distance+" </b>m entfernt</span><br />"+
					"<span class='hidden'><a onclick='javascript:openPopUpWithSingleMarker(\""+id+"\");'>Kartenansicht</a></span>"+
					"<span class='nachricht'>"+text+"</span><br />"+
					"<div id='sniffDiv"+id+"' style='visibility:hidden'>"+name+"</div>"+
					"<div id='lat"+id+"' style='display:none'>"+lat+"</div>"+
					"<div id='lng"+id+"' style='display:none'>"+lng+"</div>"+
				"</div>"+
				document.getElementById("sniffs").innerHTML;
			
				document.getElementById("printSniffs").innerHTML = 
				"<div class='printSniff'>" +
					"<div><b>" + name + "</b> am <b>" + date + "</b> um <b>" + time + "</b></div>" +
					"<div>Position: <b>" + position + "</b>, <b>" + distance + "m</b> entfernt</div>" +
					"<div>" + text + "</div>" +
				"</div>" + 
				document.getElementById("printSniffs").innerHTML;
		}
	}
}


/*
 * Nachricht senden
 * 		Test auf Integrit�t
 */
function sendMessage() {
	if(locationSuccess) {
		if(document.writeForm.name.value != ""
			&& document.writeForm.text.value != "") {
				var name = document.writeForm.name.value;
				var text = document.writeForm.text.value;

				// Text prüfen:
				if(testForTooLongWords(35, text)) { // Enthält zu lange Wörter
					
					alert("Der Text enthält zu lange Wörter!");
					
				} else { // passt so
					
					// Textfeld leeren
					document.writeForm.text.value = "";
					
					// Text vorbereiten
					text = prepareString(text);
					
					//Senden
					ws.send("{" +
							"\"type\": 1," +
							"\"name\": \"" + name + "\"," +
							"\"text\": \"" + text + "\"" +
							"}");
					
					//Counter zur�cksetzen
					document.getElementById("counter").innerHTML = "150";
					
					//Cookie setzen und Name an server
					if(document.cookie == "" || document.cookie.substring(0, 3) != name) {
						document.cookie = "name="+name;
						sendNameToServer();
					}
				}
			}
	} else {
		alert("Ihre Position konnte (noch) nicht festgestellt werden. Absenden der Nachricht somit nicht m&ouml;glich.");
	}
}

/*
 * bereitet den Text für das JSON Object vor
 */
function prepareString(text) {

	// HTML Tags ersetzen
	text = text.replace(/</g, "&lt;");
	text = text.replace(/>/g, "&gt;");

	// alle \n ersetzen
	text = text.replace(/\n/g, "<br />");

	// alle " ersetzen
	text = text.replace(/\"/g, "&quot");

	return text;
}

/*
 * Pürft ob Wörter zu lang sind
 * false wenn nicht zu lang
 * true wenn zu lang
 */
function testForTooLongWords(maxWordLength, text) {
	if(text.length - maxWordLength <= 0) {
		return false;
	}
	var index = 0;

	while(index < (text.length - maxWordLength)) {
		
		// Leerzeichen suchen
		var tmpIndex = text.indexOf(" ", index);
		
		//Es gibt weitere Leerzeichen
		if(tmpIndex != -1) {
			// Ist String dazwischen zu lang und enthält keinen Link?
			if(((tmpIndex - index) > maxWordLength)
					&&
					(text.substring(index, index+7).indexOf("http://") == -1)) {
				return true;
			}
		} else { // Es gibt keine weiteren Leerzeichen
			
			// Ist der String zu lang?
			if((text.length - index) > maxWordLength) {
				// Enhält der String keinen Link?
				if(text.substring(index, index+7).indexOf("http://") == -1) {
					return true;
				} 
			} 
			return false;
		}
		
		index = (tmpIndex + 1);
		
	}
	return false;
}