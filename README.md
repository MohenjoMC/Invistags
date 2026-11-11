[![Invistags](https://modfolio.creeperkatze.dev/modrinth/project/invistags)](https://modrinth.com/mod/invistags)

# InvisTags by CoderSoft

A **client-side Fabric mod** that forces nametags, player skins, armour and held items of invisible players to render, and raises a toast with the player's name and head when an invisible player comes near. All features are toggleable at runtime via the `/invis` command.

> **Stable 1.3.x.** Gear reveal passed unit tests and a two-player multiplayer test over the network. The invisible-player toast replaces the removed vanished-player flag.

Built for **Minecraft 1.21.11**, **26.1.2**, **26.2**, and **26.3**.

## Features

- **Nametag visibility** — Forces nametags to render above invisible players regardless of render distance or status effects.
- **Skin visibility** — Renders the player body/skin at full opacity instead of the ghostly semi-transparent effect.
- **Gear reveal** — Restores armour and held items on invisible players when a server strips them, using the last gear your client saw. Vanilla itself keeps gear visible; servers that send empty equipment packets are the target here.
- **Invisible-player toast** — Shows a toast with the player's name and head the first time an invisible player comes within 64 blocks. Each invisibility is announced once; the alert re-arms when the player becomes visible or leaves the world.
- **Sneaking fix** — Keeps a forced nametag visible through blocks while the invisible player is crouching.
- **Invisible mob support** — Optional reveal for invisible mobs' bodies, plus nametags for custom-named invisible mobs (off by default).
- **State persistence** — **Remember state** is on by default, so the last toggles are restored on launch. Turn it off to start with everything disabled every time.
- **Keybinds** — `I` toggles skin + nametags + gear together, `O` opens the config screen.
- **Config GUI** — Cloth Config screen, reachable through Mod Menu or `/invis config`.
- **Runtime toggling** — All features can be enabled/disabled mid-game without restarting.

## Requirements

| Dependency | 1.21.11 | 26.1.2 | 26.2 | 26.3 |
|---|---|---|---|---|
| Minecraft | `1.21.11` | `26.1.2` | `26.2` | `26.3` |
| Fabric Loader | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` | `>=0.19.3` |
| Fabric API | `0.141.4+1.21.11` | `0.152.1+26.1.2` | `0.152.2+26.2` | `0.164.0+26.3` |
| Java (JDK) | `21` | `25` | `25` | `25` |

**Cloth Config is required** for the config screen (`21.11.153` on 1.21.11, `26.2.155` on 26.2, `26.3.159` on 26.3). **Mod Menu is optional** — install it to reach the config screen from the Mods list; the `O` keybind and `/invis config` work without it.

## Version Branches

| Branch | MC Version | Notes |
|---|---|---|
| `main` | 1.21.11 | Render-state-based, Fabric Loom |
| `26.1` | 26.1.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26` | 26.2 | Avatar API, unobfuscated mappings, Fabric Loom |
| `26.3` | 26.3 | Avatar API, SDL keyboard input, unobfuscated mappings, Fabric Loom |

## Building from Source

### 1. Clone the Repository

```bash
git clone https://github.com/MohenjoMC/Invistags.git
cd Invistags
```

### 2. Switch to the Desired Version Branch

```bash
git checkout main     # MC 1.21.11  (default)
git checkout 26.1     # MC 26.1.2
git checkout 26       # MC 26.2
git checkout 26.3     # MC 26.3
```

### 3. Build the Mod

**Linux / macOS:**
```bash
./gradlew build
```

**Windows:**
```cmd
gradlew.bat build
```

The first build will download Gradle, Minecraft assets, and all dependencies.

### 4. Locate the Build Artifact

```
build/libs/invistags-1.3.0.jar
```

## Installation

### 1. Install Fabric Loader

Download and run the Fabric installer from [fabricmc.net/use](https://fabricmc.net/use). Select your target Minecraft version and **Client**.

### 2. Install Fabric API

Download the matching Fabric API version from [Modrinth](https://modrinth.com/mod/fabric-api) or [CurseForge](https://curseforge.com/minecraft/mc-mods/fabric-api).

### 3. Install InvisTags

Copy the built JAR to your Minecraft `mods` folder:

**Linux / macOS:**
```bash
cp build/libs/invistags-1.3.0.jar ~/.minecraft/mods/
```

**Windows:**
```cmd
copy build\libs\invistags-1.3.0.jar %appdata%\.minecraft\mods\
```

Your `mods` folder should contain at minimum:

```
mods/
├── fabric-api-<version>.jar
└── invistags-1.3.0.jar
```

### 4. Launch

Start the Fabric profile in the Minecraft launcher. Verify the mod loaded by checking the Mods menu — look for **Invistags**.

## Usage

Use the `/invis` command in-game:

| Command | Effect |
|---|---|
| `/invis` | Shows current toggle state of all features |
| `/invis config` | Opens the Cloth Config screen |
| `/invis skin true` | Enables rendering of invisible players' skins at full opacity |
| `/invis skin false` | Disables skin override (reverts to vanilla behavior) |
| `/invis nametag true` | Enables rendering of invisible players' nametags |
| `/invis nametag false` | Disables nametag override (reverts to vanilla behavior) |
| `/invis mobs true` | Reveals invisible mobs (nametags only for custom-named mobs) |
| `/invis mobs false` | Disables mob reveal (reverts to vanilla behavior) |
| `/invis gear true` | Restores last-seen armour and held items on invisible players |
| `/invis gear false` | Stops restoring gear (vanilla behaviour) |
| `/invis toast true` | Shows a name-and-head toast when an invisible player comes within 64 blocks |
| `/invis toast false` | Stops showing invisible-player toasts |
| `/invis remember true` | Restores the last toggles on the next launch |
| `/invis remember false` | Starts with all features disabled on the next launch |

### Keybinds

| Default key | Action |
|---|---|
| `I` | Toggle skin + nametag + gear reveal together |
| `O` | Open the InvisTags config screen |

Both keybinds can be changed in **Options → Controls → Key Binds**.

Features start **disabled** on the first launch. **Remember state** is on by default, so later launches restore the last toggles. Turn it off in the config screen, or run `/invis remember false`, to start disabled every time.

### Gear reveal limits

Vanilla invisibility hides only the player model; armour and held items stay visible. Servers that want invisible players to look completely empty send equipment packets with empty stacks instead.

A client cannot invent gear it has never received. InvisTags therefore remembers the six equipment slots while a player is visible and re-applies the remembered stacks for the render pass while that player is invisible. This works for gear you have seen since joining the server. It cannot recover gear the server never sent, and it cannot see gear swapped while the player stays invisible if the server also strips the update.

### Invisible-player toast

Every client tick, players in the current world are checked for the invisibility effect and their distance to you. The first time an invisible player is within 64 blocks, a system toast appears with that player's name and skin head.

- A player is announced once per invisibility episode. Becoming visible or leaving the world forgets them, so a later invisibility triggers a fresh toast.
- Walking out of and back into range does not repeat the toast for the same episode.
- Your own player and spectators are skipped.
- The head comes from the tab-list skin; if the server does not list the player, the default skin is used.
- The toast is independent of the skin/nametag/gear reveals; you can use it without forcing the model to render.


## API Differences Between Versions

The 26.x branches use a significantly different rendering API (unobfuscated Mojang mappings):

| 1.21.x | 26.x |
|---|---|
| `PlayerEntityRenderer` | `AvatarRenderer` |
| `PlayerEntityRenderState` | `AvatarRenderState` |
| `OrderedRenderCommandQueue` | `SubmitNodeCollector` |
| `render()` | `submit()` |
| `renderLabelIfPresent()` | `submitNameDisplay()` |
| `hasLabel()` | `shouldShowName()` |
| `ClientCommandManager` | `ClientCommands` |
| `state.invisible` | `state.isInvisible` / `state.isInvisibleToPlayer` |
| `state.sneaking` | `state.isCrouching` |
| `CameraRenderState` (top-level) | `state.level.CameraRenderState` |
| `PlayerEntityRenderer#updateRenderState` | `AvatarRenderer#extractRenderState` |
| `getEquippedStack` / `equipStack` | `getItemBySlot` / `setItemSlot` |
| `EntityRenderState.displayName` | `EntityRenderState.nameTag` |

## Project Structure

```
src/
├── main/                                      # mapping-independent policy, shared by all version branches
│   ├── java/org/codersoft/cleaspfabric/
│   │   ├── Cleaspfabric.java              — Mod entrypoint (ModInitializer)
│   │   ├── ModConfig.java                 — Toggle -> boolean store + JSON persistence
│   │   ├── NametagPolicy.java             — Should this label render? Should its pose be corrected?
│   │   ├── ToastEpisodes.java             — Toast episode state machine (announce once per invisibility)
│   │   └── Toggle.java                    — Single declaration of a toggle (command, config key, default, labels)
│   └── resources/
│       ├── fabric.mod.json                 — Mod metadata
│       ├── cleaspfabric.mixins.json        — Main source set mixins (reserved)
│       └── assets/invistags/icon.png       — Mod icon
└── client/                                    # Minecraft/presentation adapters, the only part a version branch rewrites
    ├── java/org/codersoft/cleaspfabric/
    │   ├── client/
    │   │   ├── CleaspfabricClient.java     — Client entrypoint, /invis command, keybinds
    │   │   ├── CleaspfabricDataGenerator.java
    │   │   ├── GearReveal.java             — Last-seen gear cache + render-state decoration
    │   │   ├── InvistagsConfigScreen.java  — Shared Cloth Config screen
    │   │   ├── InvisiblePlayerNotifier.java— Per-tick proximity tracking + toasts
    │   │   ├── InvisiblePlayerToast.java   — System toast with a player-head icon
    │   │   ├── ModMenuIntegration.java     — Mod Menu entrypoint
    │   │   └── PlayerRoster.java           — Which players the reveal/alert rules may consider
    │   └── mixin/client/
    │       ├── EntityRendererMixin.java    — Forces hasLabel() to true via NametagPolicy
    │       ├── InvisibilityRenderMixin.java— Restores skin opacity
    │       ├── LabelSneakMixin.java        — Adds the through-block label copy while sneaking
    │       └── PlayerGearMixin.java        — Fills render-state gear from the cache
    └── resources/
        ├── assets/invistags/lang/en_us.json — Keybind/config strings
        └── cleaspfabric.client.mixins.json — Client mixin registration

src/gametest/                                — Optional client gametest harness
```

## Development

Run the headless unit tests (toggle catalogue, config persistence and toast episodes, no Minecraft launch required):

```bash
./gradlew test
```

The tests also guard the toggle catalogue — unique command and config names, and both translation keys present for every toggle — and the toast episode rules, so a new toggle or a changed episode rule fails the build rather than the game.

Run the client gametest (boots a real singleplayer world, applies every mixin, opens the config screen and runs `/invis`):

```bash
./gradlew runClientGameTest
```

Screenshots are written to `build/run/clientGameTest/screenshots/`.

The harness gives a world 1200 ticks to load, so run it on a real display (the test box's X session works). A software-rendered Xvfb server on the same hardware is not fast enough and times out before the test body.

### Two-player gametest over the network

A second client test starts a dedicated server on `0.0.0.0:25565` with offline auth and waits for a named player to join:

```bash
INVISTAGS_LAN_BOT=Siftbot ./gradlew runClientGameTest
```

Start that player first — a second client or a bot such as siftbot — pointed at an address this host answers on. The test gives them diamond gear while visible, turns them invisible, then strips the gear server-side and screenshots the result with gear reveal on and off. Without `INVISTAGS_LAN_BOT` the test returns immediately, so the normal run needs no second client. Fabric's network synchronizer is disabled for the run (external clients are unsynchronized traffic), which is why the bot may join before the in-process client finishes connecting.

### Architecture

Each user-facing toggle is declared once in `Toggle`: command name, JSON config key, default value, status and feedback labels, and the translation key suffixes. The `/invis` command tree, the Cloth Config screen and the status dump are all built by looping over `Toggle.values()`, so a new toggle is one enum constant plus its translations. `ModConfig` only stores a value per toggle and reads/writes `invistags.json`; it does not know what any toggle does. Rendering behaviour lives in the mixins under `mixin/client/`, which read the toggles they need. The unit tests loop over the same catalogue, so every toggle must round-trip through the config file and have the translations the screen and command expect.

`src/main` holds the policy that never touches a renderer: the toggle catalogue, config persistence, `NametagPolicy` and `ToastEpisodes`. `src/client` holds the Minecraft adapters — the entrypoint, the notifier, the gear cache, `PlayerRoster` and the mixins. The version branches port the policy verbatim and rewrite only the client side; see `docs/adr/0001-shared-policy-across-version-branches.md`.

Open the project in IntelliJ IDEA or Eclipse:

```bash
# IntelliJ: File → Open → select project folder
# Eclipse: File → Import → Gradle → Existing Gradle Project
```

To generate an IntelliJ IDEA project from scratch:

```bash
./gradlew idea
```

## License

All Rights Reserved. See [LICENSE.txt](LICENSE.txt).

Copyright (c) 2026 CoderSoft.
