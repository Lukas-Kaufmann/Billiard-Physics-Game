# Billiard Game

Starten mit ``.\gradlew run``

### Funktionale Besonderheiten

- Wenn der weiße Ball versenkt wird, wird dieser nicht an den letzten Platz gesetzt, sondern wieder auf die Anfangsposition. 
- Ein einfacher Klick zählt bereits als Turn => es muss nicht unbedingt
ein Ball getroffen werden um einen Spielerwechsel auszulösen.
- Wenn es ein Foul gibt aber trotzdem Bälle versenkt wurden, bekommt der Spieler der das Foul verursacht
trotzdem die Punkte für das Versenken aber auch den Negativpunkt für das Foul.

Ansonsten sind die Regeln wie in der Angabe implementiert.

### Design decisions

Es wurden größtenteils nicht die EventListener-interfaces aus dem Code-Skelet verwendet.
Der State zwischen Physics und Game wird stattdesen vom Game selber gelesen (über Getter) und 
dann der interne State vom Game angepasst.

Dabei hat die Game-Klasse einen eigenen loop, wird von JavaFx's render-loop angestossen (Game implementiert FrameListener)
