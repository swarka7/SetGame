# Set Card Game

Multithreaded Java implementation of the Set card game with Swing UI and configurable AI opponents.

## Requirements
- JDK 8+  
- Maven 3+

## Build & Run
- Tests: `mvn test`
- Run (exec plugin): `mvn exec:java -Dexec.mainClass=bguspl.set.Main`
- Run built jar (after `mvn package`): `java -jar target/Set_Card_Game-1.0-SNAPSHOT.jar`

## Docker
- Build image: `docker build -t set-game .`
- Run headless: `docker run --rm set-game`
- Run with UI (needs X server, example Linux/WSL):
  ```
  xhost +local:docker
  docker run --rm \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    set-game java -Djava.awt.headless=false -jar Set_Card_Game-1.0-SNAPSHOT.jar
  ```
- Override config: mount your file `-v /path/to/config.properties:/app/config.properties`
- Persist logs: `-v /path/to/logs:/app/logs`

## Configuration
Edit `src/main/resources/config.properties` (or provide a `config.properties` next to the jar) to control:
- Players: `HumanPlayers`, `ComputerPlayers`
- AI difficulty: `ComputerLevels` (comma-separated per bot: `easy`, `medium`, `hard`)
- Timers and penalties: `TurnTimeoutSeconds`, `PointFreezeSeconds`, `PenaltyFreezeSeconds`
- Grid: `Rows`, `Columns`
- Keybindings: `PlayerKeys1`, `PlayerKeys2` (scan codes for each slot)
- UI names: `PlayerNames`

## AI Difficulty
- `easy`: random card taps, slow reaction.
- `medium`: finds real sets on the table (uses game set logic) with moderate reaction; falls back to random if none available.
- `hard`: always hunts the best available set with fast taps.

## Controls (defaults)
- Player 1: `Q W E R / A S D F / Z X C V`
- Player 2: `U I O P / J K L ; / M , . /`

## Logging
Timestamped logs are written to `logs/`. Adjust log level/format via `config.properties` (`LogLevel`, `LogFormat`).
