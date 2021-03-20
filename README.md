# The Hunters (`V1.0`) - [Spigot](https://www.spigotmc.org/resources/the-hunters.81065/)

A Minecraft mini-game has a target and 6 hunters.
The hunter's goal is to kill the target before it reaches the end and kills the ender dragon.

The game currently runs only in `Paper/Spigot/Craftbukkit 1.8.8` **only**.

> ⚠️ The game doesn't prevent the players from seeing `The End Credits` ⚠
>
> So I recommend for you to use **PaperSpigot `1.8.8`**
> 
> Also, you need to set `disable-end-credits` from the `paper.yml` to `true`

## Getting started
- Download the plugin from [Github Releases](https://github.com/iHDeveloper/TheHunters/releases) or [Spigot](https://www.spigotmc.org/resources/the-hunters.81065/)
- Run the server
- The game is in **lobby** state. Run `/setlobbyspawn` to set the lobby
- To switch to the **game** state. Run `/forcestart 10` to force starting the game in 10 seconds
- Run `/setgamespawn` to set the location where players are going to be teleported when the game starts
- Stop the server by doing `/stop`
- Re-run the server again
- The game will be ready to play! \o/

## Runs the game
(Installs **Spigot** only not **PaperSpigot** in the server environment)
- Run the server `./gradlew server`

## Compile the game
(Installs **Spigot** only not **PaperSpigot** in the server environment)
- Build the plugin `./gradlew build-plugin`

You will find the plugin in `server/plugins/The Hunters.jar` or `build/The Hunters-0.1.jar`.
