Estructura:

	src/ -> codi font

	builds/ -> jars

	doc/ -> documentació


Detalls d'execució

El codi és compatible amb java-7 i java-8. Però si s'utilitza el java 7 runtime
el moviment dels objectes del joc és més lent i poden apareixer artefactes
visuals. Per tant, és recomanable utilitzar el java-8 per executar el jar.
A builds/ hi han dos carpetes java7/ i java8/ on hi han un jar compilat amb
java7 i amb java8 respectivament.

La resolucó de la finestra està definida al mateix codi a la linia 162 de
Joc.java. Aquesta és de 1024 x 768. Per tant, és recomanable tenir un monitor
amb suficient resolució perque hi càpiga la finestra del joc.

Instruccions d'ús

El Joc et permet controlar una Nau de color verd mitjançant els següents controls:
   W: impulsar cap endavant
   A: rotar cap a l'esquerra
   D: rotar cap a la dreta
   Espai: disparar rajos làser

Quan es perden tres vides mostra el missatge "Game Over". Per sortir es pot tancar
la finestra o premer la tecla ESC.




