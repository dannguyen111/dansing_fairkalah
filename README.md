# Dansing FairKalah

An advanced AI Mancala player featuring a custom heuristic evaluation function derived from game data analysis. This bot supports standard Mancala as well as **FairKalah**, a variant designed to eliminate the first-move advantage.

## Features

* **Advanced AI:** Uses Iterative Deepening Alpha-Beta Pruning.
* **Custom Heuristics:** Evaluation function tuned via linear regression on Maia Chess-style game data.
* **FairKalah Support:** Play on any of the 254 distinct FairKalah board configurations.
* **GUI & CLI:** Play via a graphical interface or the command line.

## Installation & How to Play

Do you want to try to beat our bot? Here are ways you can try & play against our player.

### Option 1: Graphical Interface (Recommended)
1.  Ensure you have [Java Installed](https://www.java.com/en/download/).
2.  Go to the **[Releases](../../releases)** page of this repository.
3.  Download `dansing_fairkalah.jar`.
4.  Double-click the file to launch.
5.  **Settings:**
    * **Board Index:** Enter a number between `0` and `253` to select a specific FairKalah board (or `-1` for random).
    * **Side:** Choose to go First (Player 1) or Second (Player 2).


https://github.com/user-attachments/assets/b0af8c9d-2fc4-408e-89a7-75e30f4474a3


### Option 2: Build from Source
1.  Clone this repository.
2.  Compile the Java files:
    ```bash
    javac *.java
    ```
3.  Run the GUI:
    ```bash
    java MancalaGUI
    ```
4.  Run the Tournament/CLI mode:
    ```bash
    java Mancala
    ```

## The Strategy

The `dansing2MancalaPlayer` utilizes a weighted evaluation function:
`Utility = w1 * ScoreDiff + w2 * Mobility + w3 * CapturableStones ...`

These weights were derived by analyzing game states using linear regression (see `fairkalah-utility.ipynb`), allowing the bot to value strategic elements like "Free Moves" and "Vulnerable Stones" accurately.

## Credits
* **Base Engine:** Todd W. Neller
* **AI Implementation:** Dan Nguyen, Rajwat Singh
