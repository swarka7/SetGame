# Set Card Game (Multithreaded Java)

Multithreaded Java implementation of the Set card game with a Swing UI, human + AI players, and configurable rules. Players race to claim sets of three cards where each feature is all the same or all different.

## Highlights
- Concurrency-focused design with a dealer thread, per-player threads, and separate AI threads; set submissions are coordinated via a blocking queue.
- AI difficulty levels with timing and selection strategies (random, legal-set search, best-set choice).
- Configurable gameplay (players, grid size, timers, penalties, key bindings, hints).
- Swing UI with live tokens, countdown timer, scores, and winner announcement.
- Maven build, unit tests (JUnit 5 + Mockito), and optional Docker runtime.

## Architecture
- `bguspl.set.ex.Dealer`: manages the deck, timer, reshuffles, and validates submitted sets.
- `bguspl.set.ex.Player`: handles input queue, token selection, and score/penalty flow; AI uses strategy implementations.
- `bguspl.set.ex.Table` and `bguspl.set.ex.TableView`: shared table state and immutable snapshots for AI planning.
- `bguspl.set.UserInterfaceSwing`: UI panels for the grid, timer, and player stats.

## Tech Stack
- Java 8, Swing, Maven
- JUnit 5, Mockito
- Docker (optional)

## Requirements
- JDK 8+
- Maven 3+

## Build & Run
- Tests: `mvn test`
- Run (exec plugin): `mvn exec:java -Dexec.mainClass=bguspl.set.Main`
- Run built jar (after `mvn package`): `java -jar target/Set_Card_Game-1.0-SNAPSHOT.jar`

## Configuration
Edit `src/main/resources/config.properties` (or provide a `config.properties` next to the jar) to control:
- Players: `HumanPlayers`, `ComputerPlayers`, `ComputerLevels`
- Timers and penalties: `TurnTimeoutSeconds`, `PointFreezeSeconds`, `PenaltyFreezeSeconds`
- Grid size: `Rows`, `Columns`
- Key bindings: `PlayerKeys1`, `PlayerKeys2` (scan codes)
- UI names and sizing: `PlayerNames`, `CellWidth`, `CellHeight`

## AI Difficulty
- `easy`: random card taps with longer delays.
- `medium`: searches for legal sets, falls back to random picks.
- `hard`: scans all sets, prefers a deterministic "best" set, and taps quickly.

## Controls (defaults)
- Player 1: `Q W E R / A S D F / Z X C V`
- Player 2: `U I O P / J K L ; / M , . /`

## Logging
Timestamped logs are written to `logs/`. Adjust log level/format via `config.properties` (`LogLevel`, `LogFormat`).

## Docker
- Build image: `docker build -t set-game .`
- Run headless: `docker run --rm set-game`
- Run with UI (needs X server, example Linux/WSL):
  ```sh
  xhost +local:docker
  docker run --rm \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    set-game java -Djava.awt.headless=false -jar Set_Card_Game-1.0-SNAPSHOT.jar
  ```
- Override config: mount your file `-v /path/to/config.properties:/app/config.properties`
- Persist logs: `-v /path/to/logs:/app/logs`
