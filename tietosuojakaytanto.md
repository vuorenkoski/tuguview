# Tugurium - Tietosuojakäytäntö

Tugurium serveri ja applikaatio on ilmainen ja vapaasti jaettava sovellus, jolla voidaan kerätä ja visualisoida erilaisten sensoreiden, kytkimien ja kameroiden dataa. Tuguriumin lähdekoodi on vapaasti saatavilla ja käytettävissä: [https://github.com/vuorenkoski/tugurium](https://github.com/vuorenkoski/tugurium)

## Mobiiliapplikaatio

Applikaatio tallentaa puhelimeen vain serverin nimen, käyttäjätunnuksen ja serverin tuottaman autentikoinnissa käytettävän tokenin. Applikaatio ei kerää tai tallenna käyttäjästä tai laitteesta mitään muuta tietoa. Nämä tiedot säilyy sovelluksessa kunnes käyttäjä kirjautuu ulos, siitä huolimatta että sovellus suljettaisiin ilman uloskirjautumista. Uloskirjautumisen yhteydessä applikaation muistiin jää vain edellisen serverin osoite.

Applikaatio lähettää ja vastaanottaa dataa vain ja ainoastaan käyttäjän määrittelemälle serverille. Tietojen lähettäminen rajoittuu käyttätunnukseen, salasanaan, mahdolliseen mittaustiedon manuaaliseen lähettämiseen sekä kytkimien asennon muuttamiseen.

Serverille ei mobiiliapllikaatiosta lähetetä mitään henkilötietoa käyttäjätunnuksen lisäksi. Tieto kulkee salattujen https ja wss yhteyksien kautta.

## Serveri

Tugurium-serveri tallentaa sille lähetettyä sensoreiden tietoa. Kameroiden kuvista ja kytkimien asennoista tallennettuna on vain viimeisin. Data säilytetään tieturvallisesti sql-tietokannassa. Serverin käyttö on mahdollista vain admin-käyttäjän luomien käyttäjätunnusten haltijoille. Käyttäjän asentama serveri lähettää kerättyä tietoa vain ja ainostaan kirjautuneille käyttäjille selaimen ja mobiiliapplikaation kautta.

Serverin admin -käyttäjä päättää mitä tietoja serveri kerää. Konfigurointi tapahtuu serverin asennuksen yhteydessä sekä myöhemmin selain-käyttöliittymän kautta. Vain admin käyttäjä voi muuttaa konfiguraatiota.
