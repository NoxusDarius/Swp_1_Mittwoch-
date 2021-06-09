Was macht das Programm:<br>
Das Programm dient dazu aus einer JSON von Alphavantage die Schließungen der Aktie von jedem Tag auszulesen und diese in eine Datenbank abzuspeichern. Also vereinfacht gesagt wird eine Liste mit den jeweiligen close Werten plus das dazugehörige Datum abgespeichert. Diese Liste wird dann mithilfe von SQLite in eine Datenbank abgespeichert und ausgegeben.<br>
Am Anfang des Programmes wird eine Abfrage des gewünschten Unternehmens stattfinden. Diese beantwortet man mit der erwünschten Abkürzung:<br>1. Apple...AAPL <br> 2. Amazon...AMZN<br> 3. Tesla...TSLA<br>4. International Business Machines Corporation...IBM<br><br>Zum Starten benötigt man:<br>1.commons-io-2.7.jar <br>2.java.json.jar  <br> 3.sqlite-jdbx-3.32.3.2.jar<br>


Was muss man beachten wenn man eine Library einbindet?
Dieses Tutorial wird helfen:
https://www.jetbrains.com/help/idea/library.html#define-library
