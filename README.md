# Sokoban4J

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot.png)

## Overview

This is an implementation of the puzzle game [Sokoban](https://en.wikipedia.org/wiki/Sokoban) in Java (using Swing).  It is fully playable from the keyboard, but truly intended for programmers who want to develop Sokoban-playing agents in Java.

What are the benefits of Sokoban4J over [JSoko](http://www.sokoban-online.de/jsoko.html)? The answer is simplicity; this project has no applet or installer, a simpler programming interface, and a much smaller code base.

Sokoban4J supports loading level files in the .sok format.  The Sokoban4J/levels/sokobano.de folder in this repository contains many .sok packages downloaded from [sokobano.de](http://sokobano.de/en/levels.php). Thank you for such a wonderful collection!

This project includes art created by 1001.com, downloaded from [OpenGameArt](http://opengameart.org/content/sokoban-pack) and used under [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/legalcode); thank you!

Sokoban4J was written by Jakub Gemrot of the Faculty of Mathematics and Physics at Charles University, with some fixes and enhancements by [Adam Dingle](https://ksvi.mff.cuni.cz/~dingle/).  It is licensed under [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/legalcode). Please retain a URL to the original [Sokoban4J repository](https://github.com/kefik/Sokoban4J) in your work.

## Building the game

This version of Sokoban4J works with Java 11, 12, or 13, and probably older Java versions as well.  It includes Maven project files, and you should easily be able to open it in any Java IDE such as Eclipse, IntelliJ, or Visual Studio Code.

## Playing the game

To play the game from the keyboard, run the Main class in the Sokoban4J/src/main/java/cz/sokoban4j subdirectory.  Edit the code in the main() method to specify the level(s) that you wish to play.  You can choose any of the levels in the Sokoban4J/levels subdirectory.

Use the arrow keys or the W/S/A/D keys to move.

## Writing an agent

You can write an agent using the [Sokoban API](https://ksvi.mff.cuni.cz/~dingle/2018/ai/sokoban_api.html).

Use Sokoban4J-Playground/src/main/java/MyAgent.java as a starting point.  This class contains a simple depth-first search implementation that you can delete and replace with your own solver.

Run the RunMyAgent class to run your agent on one or more levels.  Edit the code in main() to select the level(s) you want it to play.

To evaluate your agent, run Evaluate.java, which will run it on a series of levels in one or more level files (listed in the LEVELS array in that source file).

There are several simple agents in the Sokoban4J-Agents/src/main/java/cz/sokoban4j/agents directory.  You may wish to look at them, though they are all quite similar to the sample MyAgent.

## Project structure

**Sokoban4J**: the main project containing the simulator and visualizer of the game

**Sokoban4J-Agents**: sample artificial agents for Sokoban4J

**Sokoban4J-Playground**: meant for easily hopping on the train of Sokoban agent development

**Sokoban4J-Tournament**: code for assessing Java agents in batch mode from the console

## Notes

Here are some more detailed notes:

1. The code base includes four different game state representations.  The Board class is an object-oriented representation used by the simulator.  For state space searching, use BoardCompact, BoardSlim or BoardCompressed. Use StateCompressed or StateMinimal for representing of no-good states.

1. There are HumanAgent and ArtificialAgent stubs.  ArtificialAgent uses its own thread for thinking (so it will not hang the GUI). See DFSAgent from Sokoban4J-Agents project for an example of an artificial player.

1. It is possible to run headless simulations (which will have the same result as visualized simulations, only more quickly).

1. A level may include up to 6 different kinds of boxes (yellow, blue, gray, purple, red and black) and targets for specific box types. A brown target is a generic spot for any kind of box.

1. Large levels automatically scale down to fit the screen in order to be playable by humans.

1. Level play time may be limited (in milliseconds).

1. You may evaluate Sokoban4J agents using the Sokoban4J-Tournament subproject.  The tournament code runs each agent/level in a separate JVM, so it is possible to give an agent a specific amount of memory for solving each level.

1. You may find introductory tips for creating a Sokoban artificial player in this [report](http://pavel.klavik.cz/projekty/solver/solver.pdf) (courtesy of Pavel Klavík).

------------------------------------------------------------

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot2.png)

![alt tag](https://github.com/kefik/Sokoban4J/raw/master/Sokoban4J/screenshot3.png)

------------------------------------------------------------
