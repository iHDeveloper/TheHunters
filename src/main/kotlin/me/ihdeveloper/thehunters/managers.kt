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
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerListPingEvent
import java.util.UUID

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

        Bukkit.getPluginManager().callEvent(GameQuitEvent(players[uniqueId]!!))

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

class WorldsManager : GameObject() {

    override fun onInit() {
        Bukkit.getWorlds().forEach {
            it.isAutoSave = false
        }
    }

    fun resetTime() {
        Bukkit.getWorlds().forEach {
            it.time = 1000
            it.weatherDuration = 0
            it.isThundering = false
        }
    }

}

class LoginManager : GameObject(), Listener {

    var lock = false

    private val motd = listOf<String>(
            "${COLOR_GOLD}⇾ $COLOR_GRAY${COLOR_BOLD}The Hunters",
            "${COLOR_GOLD}⇾ ${COLOR_RED}${COLOR_BOLD}Prove that you can't be hunted"
    )

    private val full = "${COLOR_YELLOW}The game is full!"

    private val locked = "${COLOR_YELLOW}The game has already started!"

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
