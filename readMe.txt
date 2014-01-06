----------------------------------------------------------------------------------------
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ S N I F F E R: /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
----------------------------------------------------------------------------------------
                                  ,.+----.
                              ,*""         I
             ____           /         Mbp. dP          __
         ,o@QBBBBBbm,_     ; ,d       /~`QMP'\     _odBBBBbo,
         OBBROYALBBBBBBBBmOBBBM      |    |   |,*~dBBBBBBBBBBBb
         OBBBBBBBBBBqBBBBBBBBBP      |  0 |0 ,'  dBBBBBBBBBBBBBb
         "*BBBP"     ^OBBBP"          \__/  ~    OBBBBBBBBBBBBBP
     /                     :           __.       `0BBBBBBBBBBP|
                   .-.     `          ~,A.         `0BBBBBBBBP |
                .-(   `x__  `\        .B*Bb          `"?OOP~   ;
             .-(   `\/'   `.  `~--++*\{  `Pb           ,'     ,'
  /         (   `\_/'\__/  )     _    !`-' !`.       ,'     ,'
       /     `\_/'       ,'     L "+,_|    |-,`.__ ,'    _,','~~\.~~~\
                \        /\    / ',   "+.  |/ >   `~~~~~~  ,'    :/~  \
   /             `~===\/'  `\/'    "+.   >{  /\        __.,: |   ;    ;
          /           {         ,|    `\/  \/  `Y~~---~   `. `--:'~~~';
                       `\     ,&#|      .uuuuuuu \         `,   ;^`~~';
       /               ,d#b,.&###|     .UUUU.     `\     __.;~~' `~~~'
     /              ,d###########|         UUU:     Y~~~~
                ,d############i~            `UUU;   I
          ,d##################I        `UUUUUUU'    I     /   /
(#############################b.                    Im
   )############################b.      \           d#b
  (###############################b++g+++~#b      ,d##P
      }####################P~ ~Y####P    J#P`~~~T'
     (###################P'     `YP'    ;P'   ,'
       )###############P'        '    ,'    ,'    /
      (##############P'          __+~'   /##'
 /            Y####P'         ;-'     /###P
           /   Y#P'         /'      /&##P~
                |         /'      /&#P~        
     /          `\______/'\_____/'            
                                   /           (c) asciikunst.com 
                          /
             /


----------------------------------------------------------------------------------------
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ Unterstützung: /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
---------------------------------------------------------------------------------------- 
Bei erstmaligem Benutzen von Sniffer muss die GeoLocation erlaubt werden!

Sniffer wird für den Chrome-Browser empfohlen und wurde dafür optimiert.
Die Firefox4-Bete funktioniert ebenfalls.

Der Browser muss
	- Websockets unterstützen
	- GeoLocation unterstützen
			
----------------------------------------------------------------------------------------
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ DATENBANK: /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
----------------------------------------------------------------------------------------
Nötig ist eine PostgreSQL-Datenbank mit den Paketen "cube.sql" und "earth.sql".
Die Datenbankeinstellungen sind in database.DBConnection.java vorhanden:
	private static final String DB_DRIVER = "org.postgresql.Driver";
	private static final String DB_DATABASE = "sniffer";
	private static final String DB_SERVERNAME = "134.106.56.169";
	private static final String DB_PORT = "9002";
	private static final String DB_USERNAME = "user";
	private static final String DB_PASSWORD = "penis";
	
Die Datenbank sollte "sniffer" heißen.
Die Extension kann dann mit:

CREATE TABLE sniffertable
(
  id serial NOT NULL,
  nickname text NOT NULL,
  "text" text NOT NULL,
  "timestamp" timestamp with time zone DEFAULT now(),
  "location" text NOT NULL,
  lat double precision NOT NULL,
  lng double precision NOT NULL,
  CONSTRAINT pk PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE sniffertable OWNER TO postgres;

erstellt werden.

----------------------------------------------------------------------------------------
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ Websocket: /\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
----------------------------------------------------------------------------------------
Der Websocket ist in websocket.js einzustellen.
Zeile19			//ws = new WebSocket("ws://134.106.56.169:9144/Sniffer/");
Zeile20			ws = new WebSocket("ws://localhost:8080/Sniffer/");
Muss auf localhost stehen, wenn der lokale Websocket genutzt werden soll.


----------------------------------------------------------------------------------------
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\ lokaler Server:/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
----------------------------------------------------------------------------------------
Unter Eclipse wird ein lokaler Jettyv7.2-Server empfohlen.