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

import me.ihdeveloper.thehunters.command.LobbyForceStartCommand
import me.ihdeveloper.thehunters.component.NoHungerComponent
import me.ihdeveloper.thehunters.component.AdventureComponent
import me.ihdeveloper.thehunters.component.ClearInventoryComponent
import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.DisableBlockBreakComponent
import me.ihdeveloper.thehunters.component.DisableBlockPlaceComponent
import me.ihdeveloper.thehunters.component.DisableItemCollectComponent
import me.ihdeveloper.thehunters.component.DisableItemDropComponent
import me.ihdeveloper.thehunters.component.LobbyChatComponent
import me.ihdeveloper.thehunters.component.LobbyComponent
import me.ihdeveloper.thehunters.component.LobbyScoreboardComponent
import me.ihdeveloper.thehunters.component.NoDamageComponent
import me.ihdeveloper.thehunters.component.NoInteractComponent
import me.ihdeveloper.thehunters.component.TYPE_TITLE
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.GamePlayerEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownCancelEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownFinishEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_LOBBY
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Lobby : GameObject(
        components = listOf(LobbyChatComponent())
), Listener {

    init {
        add(LobbyForceStartCommand(this))
    }

    private val countdown = CountdownComponent(
            id = COUNTDOWN_LOBBY,
            defaultStart = 20 * 30,
            onFinish = {
                Game.start()
            }
    )

    private var lastSeconds = 70

    init {
        add(countdown)
    }

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onJoin(event: GameJoinEvent) {
        val player = event.player
        player.add(AdventureComponent(player))
        player.add(NoHungerComponent(player))
        player.add(DisableItemCollectComponent(player))
        player.add(DisableItemDropComponent(player))
        player.add(NoDamageComponent(player))
        player.add(DisableBlockPlaceComponent(player))
        player.add(DisableBlockBreakComponent(player))
        player.add(NoInteractComponent(player))
        player.add(ClearInventoryComponent(player))

        player.add(LobbyComponent(player))
        player.add(LobbyScoreboardComponent(player))

        val message = StringBuilder()
        message.append("${COLOR_YELLOW}[")
        message.append("${COLOR_GREEN}${Game.count}")
        message.append("${COLOR_GRAY}/")
        message.append("${COLOR_RED}${Game.max}")
        message.append("${COLOR_YELLOW}] ")
        message.append("${COLOR_GRAY}${player.entity.name}")
        message.append("$COLOR_GOLD joined the game!")
        Bukkit.broadcastMessage(message.toString())

        if (Game.count == Game.max) {
            start()
        }
    }

    @EventHandler
    private fun onQuit(event: GameQuitEvent) {
        val player = event.player

        val message = StringBuilder()
        message.append("${COLOR_YELLOW}[")
        message.append("${COLOR_RED}-")
        message.append("${COLOR_YELLOW}] ")
        message.append("${COLOR_GRAY}${player.entity.name}")
        message.append("$COLOR_GOLD left from the game.")
        Bukkit.broadcastMessage(message.toString())

        if (countdown.started) {
            countdown.stop()
            countdown.reset()
        }
    }

    @EventHandler
    private fun onStart(event: CountdownStartEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        val message = "${COLOR_YELLOW}Game is starting..."
        Bukkit.broadcastMessage(message)
    }

    @EventHandler
    private fun onTick(event: CountdownTickEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        val seconds = event.ticks / 20
        if (lastSeconds == seconds)
            return
        lastSeconds = seconds

        if (seconds == 60
            || seconds == 45
            || seconds == 30
            || seconds == 15
            || seconds == 10
            || (seconds in 1..5) ) {
            val builder = StringBuilder()
            builder.append("${COLOR_YELLOW}Game starting in ")
            builder.append("$COLOR_RED")
            builder.append(seconds)
            builder.append(" ${COLOR_YELLOW}seconds")

            Bukkit.broadcastMessage(builder.toString())
        }
    }

    @EventHandler
    private fun onCancel(event: CountdownCancelEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        val message = "${COLOR_YELLOW}Countdown cancelled!"
        Bukkit.broadcastMessage(message)
    }

    override fun onDestroy() {
        GamePlayerEvent.getHandlerList().unregister(this)
        CountdownEvent.getHandlerList().unregister(this)

        for (player in Game.players.values) {
            player.reset()
        }
    }

    fun start() {
        countdown.start()
    }

}
