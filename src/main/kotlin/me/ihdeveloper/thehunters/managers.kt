/*
 * MIT License
 *
 * Copyright (c) 2020 iHDeveloper ( Hamza Aljuaid )
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.ihdeveloper.thehunters

import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.util.Vector
import java.util.*

class PlayersManager : GameObject(), Listener {

    val players = mutableMapOf<UUID, GamePlayer>()
    var count = 0

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = GamePlayer(event.player)
        player.add(ScoreboardComponent(player))
        player.add(TitleComponent(player))

        player.init()

        players[player.uniqueId] = player

        count++

        Bukkit.getPluginManager().callEvent(GameJoinEvent(player))

        event.joinMessage = null
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val uniqueId = event.player.uniqueId
        val gameObject = players[uniqueId]!!

        Bukkit.getPluginManager().callEvent(GameQuitEvent(gameObject))
        gameObject.destroy()

        players.remove(uniqueId)

        count--

        event.quitMessage = null
    }

    override fun onDestroy() {
        for (player in players.values) {
            player.destroy()
        }

        PlayerQuitEvent.getHandlerList().unregister(this)

        players.clear()
    }

}

class WorldsManager : GameObject(), Listener {

    var name: String? = null

    var normal: String? = null
    var nether: String? = null
    var theEnd: String? = null

    /* Developer Options */
    var disableCopyWorlds: Boolean = false

    var worldNormal: World? = null
    private var worldNether: World? = null
    private var worldTheEnd: World? = null

    fun start() {
        Bukkit.getPluginManager().registerEvents(this, plugin())

        if (disableCopyWorlds) {
            Game.logger.warning("WORLDS ARE NOT BEING COPIED!! This means the worlds aren't being reset after game ends.")
            Game.logger.warning("Disable the option 'disable-copy-worlds' in config.yml to hide this warning")
            worldNormal = Bukkit.getWorld(normal)
            worldNether = Bukkit.getWorld(nether)
            worldTheEnd = Bukkit.getWorld(theEnd)
        } else {
            worldNormal = load(name!!, normal!!)
            worldNether = load("${name}_nether", nether!!)
            worldTheEnd = load("${name}_the_end", theEnd!!)
        }
    }

    fun resetTime() {
        Bukkit.getWorlds().forEach {
            it.time = 1000
            it.weatherDuration = 0
            it.isThundering = false
        }
    }

    @EventHandler
    @Suppress("UNUSED")
    fun onPortalTeleport(event: PlayerPortalEvent) {
        if (event.cause !== PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
            && event.cause !== PlayerTeleportEvent.TeleportCause.END_PORTAL)
            return

        val source = event.from.world
        when (source.environment) {
            World.Environment.NORMAL -> {
                if (event.cause === PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                    event.run {
                        useTravelAgent(true)
                        portalTravelAgent.canCreatePortal = true
                    }

                    /* When the player is teleporting to the nether */
                    val portalLocation = event.from.clone().apply {
                        world = worldNether
                        x = blockX / 8.0
                        z = blockZ / 8.0
                    }

                    event.to = event.run {
                        portalTravelAgent.findOrCreate(portalLocation)
                    }
                } else if (event.cause === PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                    /* Obsidian platform location (aka spawn location) */
                    val obsidianPlatformLocation = Location(worldTheEnd, 100.0, 49.0, 0.0).apply {
                        /* Sets the direction to WEST */
                        direction = Vector(-1, 0, 0)
                    }

                    /* Build the obsidian platform */
                    for (x in 98..102) {
                        for (z in -2..2) {
                            worldTheEnd!!.getBlockAt(x, 48, z).type = Material.OBSIDIAN

                            /* Make sure the player spawns in clear area */
                            for (y in 49..51) {
                                val block = worldTheEnd!!.getBlockAt(x, y, z)

                                if (block.type !== Material.AIR) {
                                    block.type = Material.AIR
                                }
                            }
                        }
                    }

                    /* Sets the location to the obsidian platform spawn location */
                    event.to = obsidianPlatformLocation
                }
            }
            World.Environment.NETHER -> {
                val portalLocation = event.from.clone().apply {
                    world = worldNormal
                    x = blockX * 8.0
                    z = blockZ * 8.0
                }

                event.to = event.run {
                    portalTravelAgent.findOrCreate(portalLocation)
                }
            }
            World.Environment.THE_END -> {
                event.to = worldNormal!!.spawnLocation
            }
            null -> {
                /* That's going to be a weird error to encounter */
                error("A weird has just occurred (aka unexpected error)")
            }
        }
    }

    private fun load(name: String, source: String): World {
        val world = Bukkit.getWorld(source) ?: error("We couldn't copy world '$source' because it doesn't exist!")

        Game.logger.info("Loading world/${name}...")

        val creator = WorldCreator(name).apply {
            copy(world)
        }

        return Bukkit.createWorld(creator).apply {
            isAutoSave = false
            time = 1000
            weatherDuration = 0
            isThundering = false
        }
    }

    private fun unload(world: World) {
        Game.logger.info("Deleting world/${world.name}...")
        Bukkit.unloadWorld(world, false)
        world.worldFolder.deleteRecursively()
    }

    override fun onDestroy() {

        if (!disableCopyWorlds) {
            unload(worldNormal!!)
            unload(worldNether!!)
            unload(worldTheEnd!!)
        }

        PlayerPortalEvent.getHandlerList().unregister(this)
    }

}

class LoginManager : GameObject(), Listener {

    var lock = false

    private val motd = listOf<String>(
            "${COLOR_GOLD}⇾ $COLOR_GRAY${COLOR_BOLD}The Hunters",
            "${COLOR_GOLD}⇾ ${COLOR_RED}${COLOR_BOLD}Prove that you can't be hunted"
    )

    private val full = "${COLOR_YELLOW}The game is full!"

    private val locked = "${COLOR_YELLOW}The game is getting started! Try again later to join it."

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onPing(event: ServerListPingEvent) {
        event.motd = "${motd[0]}\n${motd[1]}"
    }

    @EventHandler
    private fun onLogin(event: PlayerLoginEvent) {
        if (lock) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, locked)
            return
        }

        if (Game.count >= Game.max) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, full)
            return
        }

        event.allow()
    }

    override fun onDestroy() {
        ServerListPingEvent.getHandlerList().unregister(this)
        PlayerLoginEvent.getHandlerList().unregister(this)
    }

}
