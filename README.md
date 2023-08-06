# Timber-b1.7.3

## Timber
Timber-b1.7.3 is a nice plugin about cutting down trees. It was developed using [Project-Poseidon](https://github.com/RhysB/Project-Poseidon), a Craftbukkit fork for Minecraft beta 1.7.3 which implements more recent additions such as events. This was also for me an introduction to maven!

You can compile the plugin for yourself using maven.
```sh
mvn package
```

## How it works
Upon server startup, a configuration file is created under config/. You can change the maximum amount of log blocks to allow the tree to be cut down, and you can enable or disable auto replanting of saplings.
Use a axe while sneaking to cut down a tree! If the axe doesn't have enough durability to cut down the whole tree, then only the broken log block will drop.