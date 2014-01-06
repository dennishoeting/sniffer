var map;
var umkreis;
var initialLocation;
var infowindow;
var locationSuccess;
var myOptions = {
	zoom : 14,
	mapTypeId : google.maps.MapTypeId.ROADMAP
};
/* Speichert alle Marker */
var allMarkersArray = [];
/* Speichert nur den Marker des jeweiligen Sniffs */
var singleMarker = new google.maps.Marker({
	position : new google.maps.LatLng(40.69847032728747, -73.9514422416687),
	map : null,
	icon : pawImage,
	title : title
});


/* Variablen für UserOnlineList */
var mapUserOnline;
var myOptions2 = {
		zoom : 14,
		mapTypeId : google.maps.MapTypeId.ROADMAP
};
var allUsersMarkers = [];


/*
 * Initialisierung
 */
function initializeGeoLocation() {
	locationSuccess = false;

	/*
	 * OnChange-Handler für Umkreis Auswahl
	 */
	var sniffRatioVar = document.getElementById('sniffRatioBox');
	sniffRatioVar.onchange = function() {
		allMarkersArray = [];
		showCircle(parseInt(this.value));
		ws.send("{" + 
					"\"type\": 2," + 
					"\"ratio\": " + this.value + 
				"}");
		ws.send("{" +
					"\"type\": 3" +
				"}");
	};

	/*
	 * OnChange-Handler für OnlineUsers
	 * Setzt den Center der Map aufs ausgewählte Objekt
	 */
	var onlineUsersVar = document.getElementById('usersOnlineList');
	var temp = document.userListForm.listSelect;
	onlineUsersVar.onchange = function() {
		// Dass man für so was in JS extra variablen braucht..
		var location = new google.maps.LatLng(JSON.parse(temp.options[temp.selectedIndex].value).lat,
				JSON.parse(temp.options[temp.selectedIndex].value).lng);
		mapUserOnline.setCenter(location);
		
		/*
		 * Setzen des userName
		 */
		if(JSON.parse(this.value).name == "Du") {
			document.chat.text.disabled = true;
			document.getElementById("chatUserName").innerHTML = "?";
		} else {
			document.chat.text.disabled = false;
			document.getElementById("chatUserName").innerHTML = JSON.parse(this.value).name;
		}
	};
	
	// Daten für die Lokalisierung
	var siberia = new google.maps.LatLng(60, 105);
	var newyork = new google.maps.LatLng(40.69847032728747, -73.9514422416687);
	var browserSupportFlag = new Boolean();
	var geocoder = new google.maps.Geocoder();
	infowindow = new google.maps.InfoWindow();

	map = new google.maps.Map(document.getElementById("mapCanvas"), myOptions);
	mapUserOnline = new google.maps.Map(document.getElementById("userOnlineMapCanvas"), myOptions);
	
	// Try W3C Geolocation method (Preferred)
	if (navigator.geolocation) {
		browserSupportFlag = true;
		navigator.geolocation.getCurrentPosition(function(position) {

			initialLocation = new google.maps.LatLng(position.coords.latitude,
					position.coords.longitude);

			map.setCenter(initialLocation);
			mapUserOnline.setCenter(initialLocation);
//			infowindow.setContent("Dein Standort.");
//			infowindow.setPosition(initialLocation);
//			infowindow.open(map);

			// Umkreis -> erstmal 1km
			showCircle(1000);

			// Adresse suchen/anzeigen
			if (geocoder) {
				geocoder.geocode({
					'latLng' : initialLocation
				}, function(results, status) {
					if (status == google.maps.GeocoderStatus.OK) {
						start(position.coords.latitude,
								position.coords.longitude,
								results[0].formatted_address);
					}
				});
			}

		}, function() {
			handleNoGeolocation(browserSupportFlag);
		});
	} else if (google.gears) {
		// Try Google Gears Geolocation
		browserSupportFlag = true;
		var geo = google.gears.factory.create('beta.geolocation');
		geo.getCurrentPosition(
				function(position) {
					initialLocation = new google.maps.LatLng(position.latitude,
							position.longitude);
					map.setCenter(initialLocation);
					start(position.latitude, position.longitude,
							"Gears-Adresse (TODO)");
				}, function() {
					handleNoGeolocation(browserSupportFlag);
				});
	} else {
		// Browser doesn't support Geolocation
		browserSupportFlag = false;
		handleNoGeolocation(browserSupportFlag);
	}
	
	/*
	 * Hilfsfunktion f�r Ausnahme
	 */
	function handleNoGeolocation(errorFlag) {
		if (errorFlag == true) {
			initialLocation = newyork;
			contentString = "Error: The Geolocation service failed.";
		} else {
			initialLocation = siberia;
			contentString = "Error: Your browser doesn't support geolocation. Are you in Siberia?";
		}
		map.setCenter(initialLocation);
		infowindow.setContent(contentString);
		infowindow.setPosition(initialLocation);
		infowindow.open(map);
	}
	
	/*
	 * Startet die Lokalisierung
	 */
	function start(lat, lng, adress) {
		document.getElementById("position").innerHTML = adress;
		document.getElementById("printPosition").innerHTML = adress;
		/*
		 * Sende Positionsdaten und Radius
		 */
		ws.send("{" + 
					"\"type\": 0," + 
					"\"lat\": " + lat + "," + 
					"\"lng\": " + lng + "," + 
					"\"positionString\": \"" + adress + "\"," + 
					"\"ratio\": " + document.getElementById("sniffRatioBox").value + 
				"}");

		/*
		 * Fordere Messages aus DB
		 */
		ws.send("{\"type\": " + 3 + "}");

		/*
		 * Es kann geschrieben werden
		 */
		locationSuccess = true;
	}
}

/*
 * Guckt nach, ob sich die Position ge�ndert hat und reagiert entsprechend
 */
function checkPositionChanged() {
	var newLat, newLng;
	
	// W3C
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			function(position) {
				newLat = position.coords.latitude;
				newLng = position.coords.longitude;

				sendToServer();
			},
			function() {
				locFail();
			}
		);
	} else
	// Gears
	if (google.gears) {
		var geo = google.gears.factory.create('beta.geolocation');
		geo.getCurrentPosition(
			function(position) {
				newLat = position.latitude;
				newLng = position.longitude;

				sendToServer();
			}, 
			function() {
				locFail();
			}
		);
	} 
	// nix
	else {
		alert("geolocation-fail");
		locFail();
		sendToServer();
	}
	
	/*
	 * Bei Fehlschlag, gleiche Werte nehmen
	 */
	function locFail() {
		newLat = initialLocation.lat();
		newLng = initialLocation.lng();
		sendToServer();
	}
	
	/*
	 * Bei Erfolg
	 */
	function sendToServer() {
		ws.send("{" +
					"type: 5, " +
					"distance: 50," +
					"oldLat: " + initialLocation.lat() + "," +
					"oldLng: " + initialLocation.lng() + "," +
					"newLat: " + newLat + "," +
					"newLng: " + newLng +
				"}");
	}
}

/*
 * Zeigt den Umkreis an
 */
function showCircle(radiusInMeter) {
	//Zoom �ndern
	var zoomValue;
	if(radiusInMeter == 100)
		zoomValue = 17;
	else if(radiusInMeter == 500)
		zoomValue = 15;
	else if(radiusInMeter == 1000)
		zoomValue = 14;
	else if(radiusInMeter == 2500)
			zoomValue = 13;
	else if(radiusInMeter == 10000)
		zoomValue = 11;
	else if(radiusInMeter == 50000)
		zoomValue = 8;
	else if(radiusInMeter == 500000)
		zoomValue = 5;
	else 
		zoomValue = 1;
	myOptions = {
			zoom: zoomValue,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
	
	if (radiusInMeter > 0) {
		map = new google.maps.Map(document.getElementById("mapCanvas"),
				myOptions);
		new google.maps.Circle({
			center : initialLocation,
			map : map,
			fillOpacity : 0.5,
			fillColor : "#000000",
			radius : radiusInMeter
		});
	} else {
		map = new google.maps.Map(document.getElementById("mapCanvas"),
				myOptions);
	}

	var hundeImage = 'img/hund.gif';
	new google.maps.Marker({
		position : initialLocation,
		map : map,
		icon : hundeImage,
		title : "Das bist du!"});
	
	map.setCenter(initialLocation);
}


function addMarker(lat, lng, title) {
	var pawImage = 'img/paw.gif';
	allMarkersArray.push(new google.maps.Marker({
		position : new google.maps.LatLng(lat, lng),
		map : null,
		icon : pawImage,
		title : title
	}));
}

function showAllMarkersInMap() {
	// SingleMarker von der Map nehmen
	if(singleMarker) {
		singleMarker.setMap(null);
	}
	
	// Alle Marker auf die Map setzen
	if(allMarkersArray) {
		for(i in allMarkersArray) {
	    	allMarkersArray[i].setMap(map);
	    }
	}
}

function showSingleMarkerInMap(lat, lng, title) {
	// Alle anderen Marker von der Map nehmen
	// Alle Marker auf die Map setzen
	if(allMarkersArray) {
		for(i in allMarkersArray) {
	    	allMarkersArray[i].setMap(null);
	    }	
	}
	if(singleMarker) {
		singleMarker.setMap(null);
	}
	// SingleMarker erstellen und anzeigen
	var pawImage = 'img/paw.gif';
	singleMarker = new google.maps.Marker({
		position : new google.maps.LatLng(lat, lng),
		map : map,
		icon : pawImage,
		title : title
	});
}