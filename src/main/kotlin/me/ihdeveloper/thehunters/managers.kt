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
import me.ihdeveloper.thehunters.event.GameJoinEvent
import me.ihdeveloper.thehunters.event.GameQuitEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class PlayersManager : GameObject(), Listener {

    private val players = mutableMapOf<UUID, GamePlayer>()

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = GamePlayer(event.player)
        player.add(ScoreboardComponent(player))

        player.init()

        players[player.uniqueId] = player

        Bukkit.getPluginManager().callEvent(GameJoinEvent(player))

        event.joinMessage = null
    }

    @EventHandler
    private fun onQuit(event: PlayerQuitEvent) {
        val uniqueId = event.player.uniqueId

        Bukkit.getPluginManager().callEvent(GameQuitEvent(players[uniqueId]!!))

        players.remove(uniqueId)

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
        for (world in Bukkit.getWorlds()) {
            world.isAutoSave = false
        }
    }

}