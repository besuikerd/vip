## Eyecall Video geleide systeem ##

Een applicatie om visueel beperkte personen te helpen


##Installatie

Vrijwilliger en VBP
Het programma wordt geleverd door middel van een APK (Android application package). Als deze geopend wordt op een android telefoon dan komt er vanzelf een bevestiging van de installatie. Voordat het mogelijk is om een APK van onbekende bronnen te installeren moet er eerst in de instellingen van de telefoon veranderd worden dat dit toegestaan is. Deze instelling staat onder Instellingen → Beveiliging → Onbekende bronnen. Als deze instelling aangevinkt is kan de applicatie geinstalleerd worden.

###Server
De server kan door middel van een opdrachtprompt opgestart worden. Via het volgende commando begint de server:

    java -jar server.jar [port]

waarbij [port] vervangen moet worden door een poortnummer waarnaar geluisterd wordt. In de applicatie is dit standaard poort 5000

##Gebruik

###Vrijwilliger
Bij het opstarten van de applicatie komt de gebruiker in het scherm met zijn opgeslagen gewenste en ongewenste locaties. Er zijn in dit scherm twee knoppen te gebruiken. De knop ‘Refresh’ laad de lijst met locaties opnieuw. Met de knop ‘Add location’ kan een gewenste of ongewenste locatie toegevoegd worden. Als er op die knop gedrukt wordt verschijnt er een kaart op het scherm. Een gebruiker kan dan navigeren naar de locatie die hij wil toevoegen. Als een[port] een vinger op het scherm gehouden wordt dan komt er een rode marker te staan die de locatie aangeeft die toegevoegd moet worden. Door middel van de ‘Save’ knop kan deze locatie worden opgeslagen. In dit scherm brengt de ‘Cancel’ knop de gebruiker terug naar de scherm met locaties.

Als een vrijwilliger een hulpverzoek ontvangt dan verschijnt er een scherm met een kaart van de locatie waar de hulpverzoek vandaan komt. Door middel van de ‘Accept’ knop kan het hulpverzoek geaccepteerd worden en met ‘Decline’ kan deze geweigerd worden.

Na het accepteren van een hulpverzoek komt de vrijwilliger in een hulpscherm. Er zijn twee tabs: een voor video en een voor de locatie. Door middel van swypen (met de vinger over het scherm vegen) kan gewisseld worden tussen deze twee tabs of door op de tabs te drukken. De knop ‘Disconnect’ verbreekt de verbinding.
VBP
In het hoofdscherm is er alleen een grote knop. Als er op deze knop gedrukt wordt dan wordt er een hulpverzoek verstuurd. Als deze door een vrijwilliger wordt geaccepteerd komt de vrijwilliger in het hulpscherm. Hierin kan de VBP de verbinding verbreken door middel van de ‘Disconnect’ knop.