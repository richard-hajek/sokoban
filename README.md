# Sokoban4J

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot.png)

## Overview

This is an implementation of the puzzle game [Sokoban](https://en.wikipedia.org/wiki/Sokoban) in Java (using Swing).  It is fully playable from the keyboard, but truly intended for programmers who want to develop Sokoban-playing agents in Java.

Sokoban4J supports loading level files in the .sok format.  The levels folder in this repository contains many .sok packages downloaded from [sokobano.de](http://sokobano.de/en/levels.php). Thank you for such a wonderful collection!

This project includes art created by 1001.com, downloaded from [OpenGameArt](http://opengameart.org/content/sokoban-pack) and used under [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/legalcode); thank you!

Sokoban4J was originally written by Jakub Gemrot of the Faculty of Mathematics and Physics at Charles University.  [Adam Dingle](https://ksvi.mff.cuni.cz/~dingle/) has continued its development.  It is licensed under [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/legalcode). Please retain a URL to the original [Sokoban4J repository](https://github.com/kefik/Sokoban4J) in your work.

## Building the game

This version of Sokoban4J works with Java 11 or higher, and possibly older Java versions as well.  It includes a Maven project file, and you should easily be able to open it in any Java IDE such as Eclipse, IntelliJ, or Visual Studio Code.

## Playing the game

To play the game from the keyboard on Linux or macOS, run

```
$ ./sokoban
```

Or, on Windows:

```
> .\sokoban
```
By default, the game plays a set of 10 relatively easy levels, found in `levels/easy.sok`.  You can use the -levelset option to chooose a different level set.  Various other options are available; type './sokoban -help' to see them.

Use the arrow keys or the W/S/A/D keys to move.  Press Z to undo your last move.

## Writing an agent

You can write an agent using the [Sokoban API](doc/sokoban_api.html).

Use src/MyAgent.java as a starting point.  This class contains a simple depth-first search implementation that you can delete and replace with your own solver.

Run the RunMyAgent class to run your agent on one or more levels.  Edit the code in main() to select the level(s) you want it to play.

To evaluate your agent, run Evaluate.java, which will run it on a series of levels in one or more level files (listed in the LEVELS array in that source file).

There are several simple agents in the src/cz/sokoban4j/agents directory.  You may wish to look at them, though they are all quite similar to the sample MyAgent.

## Notes

Here are some more detailed notes:

1. The code base includes four different game state representations.  The Board class is an object-oriented representation used by the simulator.  For state space searching, use BoardCompact, BoardSlim or BoardCompressed. Use StateCompressed or StateMinimal for representing of no-good states.

1. There are HumanAgent and ArtificialAgent stubs.  ArtificialAgent uses its own thread for thinking (so it will not hang the GUI). See DFSAgent from Sokoban4J-Agents project for an example of an artificial player.

1. It is possible to run headless simulations (which will have the same result as visualized simulations, only more quickly).

1. A level may include up to 6 different kinds of boxes (yellow, blue, gray, purple, red and black) and targets for specific box types. A brown target is a generic spot for any kind of box.

1. Large levels automatically scale down to fit the screen in order to be playable by humans.

1. Level play time may be limited (in milliseconds).

1. You may evaluate Sokoban4J agents using the SokobanTournamentConsole class.  The tournament code runs each agent/level in a separate JVM, so it is possible to give an agent a specific amount of memory for solving each level.

1. You may find introductory tips for creating a Sokoban artificial player in this [report](http://pavel.klavik.cz/projekty/solver/solver.pdf) (courtesy of Pavel Klavík).

------------------------------------------------------------

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot2.png)

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot3.png)

------------------------------------------------------------
