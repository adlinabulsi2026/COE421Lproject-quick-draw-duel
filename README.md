# Wild West Quick-Draw Duel

COE 421L Computer Systems Engineering Lab, Final Project (Fall 2026).

Two players stand facing each other with their guns holstered. After a random 3 to 10 second countdown the computer signals DRAW. The first player to draw and hit the other player's IR target wins the round. The first player to 3 round wins is the fastest gun in the West.

## Folders

| Folder | Contents |
|---|---|
| `with_ui/` | Final version: Java game with the game window, sounds and simulation mode, plus the Arduino code |
| `without_ui/` | Earlier version: Java game with console output only, plus the Arduino code |
| `lib/` | `jssc-2.9.6.jar` (serial port library) |
| `screenshots/r` | Results recorded with the real guns on 21 Nov, replayed in the game window |

The Arduino code is the same in both versions (With UI, without UI). The same sketch runs on both guns; only `playerId` changes (1 or 2).

## Hardware (each gun)

| Part | LilyPad pin |
|---|---|
| XBee TX / RX | 10 / 11 |
| IR receiver (OUT) | A0 |
| IR emitter | A1 |
| Trigger button (to GND) | 4 |
| Holster sensor (DO) | 5 |
| Vibration motor | 9 |

Player 1's gun sends to the computer over ZigBee (receiver XBee in an XBee Explorer). Player 2's gun is connected to the computer by USB because one XBee transmitter stopped working (it uses the same message format).

## Running the game

1. Create a Java project in Eclipse and copy the `.java` files from `with_ui/java` (or `without_ui/java`) into its `src` folder. Both folders include `SerialPortHandle.java` from the lab (with `readLine()` and `writeByte()`).
2. Add `lib/jssc-2.9.6.jar` to the build path (Build Path → Configure Build Path → Libraries → Classpath then Add External JARs).
3. Upload `PlayerSender.ino` to both guns, with `playerId = 1` and `playerId = 2`.
4. In `Main.java`, set the XBee port (`"COM6"`) and player 2's USB port (`usbGunPort`, `"COM7"`).
5. Close the Arduino Serial Monitor, run `Main`, choose the game mode (1 = Duel, 2 = Reaction). In `with_ui`, answer `n` to simulation to use the real guns.

## Simulation mode (`with_ui` only)

Run `Main` and answer `y` to simulation. Buttons for both players (Holster, Draw, Shoot hit, Shoot miss) send the same lines the guns send.

## Messages

Gun to Java, one line each time a value changes: `player,holster,trigger,hit` (for example `1,0,1,0`).

Java to guns, one byte: `P` = send values, `D` = DRAW, `1` / `2` = round winner, `0` = tie.
