Collection of mods for game [Space Haven](https://bugbyte.fi/spacehaven/).

# List of mods

* **MoreRefugeeActions** Addition interactions with refugees. Recruit, limited schedule management and more. **Status**: playable.

* **InfiniteTech** Repetitive tech concept **Status**: nothing but design document.

* **DebugUtils** Some debug tools/cheats to make mods debug easier for internal usage.

# Building from sources

## Prerequirements

1. You need a Java Development Kit (JDK) and [Maven](https://maven.apache.org) build tool installed.

2. Space Heaven jar needs to be added to your local maven repository:

`mvn install:install-file -DartifactId=spacehaven -Dpackaging=jar -Dversion=1.0.0 -DgroupId=fi.bugbyte -Dfile="path_to_space_haven/spacehaven.jar" -DgeneratePom=true`

3. [Mod loader](https://github.com/Spacehaven-modding-tools/spacehaven-modloader)

## Compilation

Each mod is separated java project. Run `mvn package` in mod subdirectory to compile it.

Output files will be in `target/{mod_name}` subdirectory.

# License

See [LICENSE](LICENSE).
