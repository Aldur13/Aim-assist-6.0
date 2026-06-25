# Installation

## Requirements

| Requirement | Version |
|---|---|
| Java | 21 or newer |
| Minecraft | 26.x |
| Fabric Loader | 0.16.0 or newer |
| Fabric API | any build for 26.x |

## Steps

1. **Install Fabric Loader**
   Download the Fabric Installer from [fabricmc.net](https://fabricmc.net/use/installer/) and run it for your Minecraft 26.x version.

2. **Download Fabric API**
   Get the latest Fabric API jar for 26.x from [Modrinth](https://modrinth.com/mod/fabric-api) and place it in `.minecraft/mods/`.

3. **Add the mod**
   Place `aim-assist-1.0.0.jar` in `.minecraft/mods/`.

4. **Launch**
   Open the Minecraft Launcher, select the Fabric profile, and start the game.

## Building from Source

```bash
git clone https://github.com/Aldur13/Aim-assist-6.0.git
cd Aim-assist-6.0
./gradlew build
# Output: build/libs/aim-assist-1.0.0.jar
```

If you are targeting a specific 26.x patch, update `gradle.properties`:

```properties
minecraft_version=26.X   # your target version
yarn_mappings=26.X+build.Y
fabric_version=X.Y.Z+26.X
```

Check current values at [meta.fabricmc.net](https://meta.fabricmc.net/).

## Uninstallation

Remove `aim-assist-1.0.0.jar` from `.minecraft/mods/`. The config file at `.minecraft/config/aimassist.json` can also be deleted safely.
