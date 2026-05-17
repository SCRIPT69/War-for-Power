# User Manual — War for Power

## 1. Requirements

- Java 24 or newer
- Maven 3.6 or newer

## 2. Starting the Game

Navigate to the root directory of the project and run:

```bash
mvn javafx:run
```

### Starting with Logging Enabled

To enable logging, add the `--enable-logging` argument:

```bash
mvn javafx:run -Djavafx.args="--enable-logging"
```

Log messages are printed to the console. Without this parameter, logging is disabled.

## 3. Main Menu

After launching, the main menu is shown with the following options:

- **2 Players / 3 Players / 4 Players** — select the number of players
- **Start Game** — start a new game
- **Continue** — resume a saved game (shown only if a save file exists)

## 4. Gameplay

The game is played in hot-seat mode — all players share one device and take turns one after another.

Each player has **60 seconds** per turn. When the time runs out, the turn ends automatically.

### Controls

The game is controlled entirely with the mouse.

**Select a unit** — click on a unit icon on the map. The selected unit is highlighted with a gold frame and available movement and attack tiles are shown.

**Move** — after selecting a unit, click on a gold-highlighted tile.

**Attack** — after selecting a unit, click on a red-highlighted tile containing an enemy unit, a city or an enemy base. A confirmation dialog will appear.

**Select two units** — hold Shift and click a second unit. Two units can be selected together only if they share at least one common target tile.

**Buy a unit** — click on your base. A unit purchase menu will appear showing available unit types and their prices.

**Capture a tile** — click on a tile where your unit is standing. A button to capture the tile for 50 coins will appear.

**End turn** — click the **End Turn** button in the top-right corner.

### HUD

The top panel displays:

- current player name and color
- current coin balance
- current round number
- remaining turn time countdown

### Battle

After initiating an attack, a battle overlay appears. The player clicks **Roll Attack** and **Roll Defense** in sequence to reveal dice results. After the result is shown, click **Exit** to return to the game, or **Reroll** in case of a draw.

## 5. Saving and Loading

The game is **saved automatically** after every turn end. The save file is located at `savegame.json` in the root directory of the project.

To resume a saved game, launch the application and click **Continue** in the main menu.

When the game ends (after round 15 or when only one player remains), the save file is deleted automatically.

## 6. End of Game

The game ends after 15 rounds or when only one player remains. An end-game screen is shown displaying:

- the winner or winners in case of a tie
- each player's score broken down into base points and territory points

Click **Main Menu** to return to the main menu.
