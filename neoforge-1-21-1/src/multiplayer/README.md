# Local two-client terminal test

Run each command in a separate terminal from `neoforge-1-21-1`:

```bat
gradlew.bat runMultiplayerServer --console=plain
gradlew.bat runMultiplayerClient1 --console=plain
gradlew.bat runMultiplayerClient2 --console=plain
```

Start the server first and wait for `Done` before starting the clients. On a fresh checkout,
read the Minecraft EULA and accept it in `run-multiplayer/server/eula.txt` before starting the server again.
EULA acceptance is deliberately not included in the committed presets.

The server listens only on `127.0.0.1:25575`. Offline authentication is for these local development
profiles only; do not change the bind address to expose it to a network. The two clients use
`MyotusTester1` and `MyotusTester2` with separate game directories and client configs. Their
960 x 640 windows connect automatically. AE2WTLib uses the module's normal development dependencies.
Equipment, crafting, and Curios modules are supplied by AppliedTerminalModules, not Myotus.
Use the addon's development setup for its feature tests, or install the addon and its required
dependencies in all three test profiles. Myotus itself no longer includes Curios.

`prepareMultiplayerTest` seeds missing settings from the presets but does not overwrite existing
settings or worlds. All runtime data stays in ignored `run-multiplayer/` directories.
Each game JVM is capped at 2 GiB. Type `stop` in the server terminal to save and stop it.

## Manual checks

- Open the same placed terminal from both clients. Install/remove addon module cards;
  check that both clients agree on card availability and that each player's slots remain isolated.
- Drag a panel on one client. Close/reopen its terminal and restart that client: its position should
  persist without changing the other client's position.
- Reset addon-registered module positions from Myotus's config tab; return to the existing terminal and verify
  automatic placement. Shrink/enlarge the window and verify saved positions are not overwritten.
- Check normal clicks, shift-click, curses, offhand-held wireless terminals, and overlapping panels.
- Equip a Curio that changes slot count: the terminal intentionally closes; reopening rebuilds the slots.
- Check the addon's module recipes and documentation.

Server and clients must always use the same mod set and terminal slot count.
