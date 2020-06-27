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

import me.ihdeveloper.thehunters.command.GameShoutCommand
import me.ihdeveloper.thehunters.command.GameSetSpawnCommand
import me.ihdeveloper.thehunters.component.ConfigurationComponent
import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.NoDamageComponent
import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TYPE_COUNTDOWN
import me.ihdeveloper.thehunters.component.TYPE_NO_DAMAGE
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.component.gameplay.EnderDragonComponent
import me.ihdeveloper.thehunters.component.gameplay.GameBroadcastComponent
import me.ihdeveloper.thehunters.component.gameplay.GameWarningComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterAchievementComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterChatComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterCompassComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterDeathComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterRespawnComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterScoreboardComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterShoutComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterSignalComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetAchievementComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetChatComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetDeathComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetDimensionComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetGetReadyComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetScoreboardComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetSignalComponent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.hunter.HunterRespawnEvent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetQuitEvent
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_GET_READY
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_INTRO
import me.ihdeveloper.thehunters.util.COUNTDOWN_THE_END
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.lang.StringBuilder
import java.util.UUID
import kotlin.random.Random

class Gameplay : GameObject(
        components = listOf(
                TargetChatComponent(),
                HunterChatComponent(),
                EnderDragonComponent(),
                GameBroadcastComponent()
        )
), Listener {

    companion object {
        lateinit var instance: Gameplay

        val hunters: Int get() = instance.hunters
    }

    private var target: UUID? = null
    private var hunters: Int = 0

    private val config = ConfigurationComponent("game")

    private val end = CountdownComponent(
            id = COUNTDOWN_THE_END,
            defaultStart = (20 * 60) * 60,
            onFinish = {
                Game.win()
            }
    )

    private val gettingReady = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_GET_READY,
            defaultStart = 20 * 60,
            onFinish = {
                Game.players.values.forEach { it.remove(TYPE_NO_DAMAGE) }

                Game.unlock()

                end.start()
            }
    )

    private val intro = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_INTRO,
            defaultStart = 20 * 5,
            onFinish = {
                remove(TYPE_COUNTDOWN)
                add(gettingReady)
                gettingReady.start()

                Game.players.values.forEach { it.add(NoDamageComponent(it, false)) }
            }
    )

    init {
        add(config)
        add(intro)
        add(GameSetSpawnCommand(config))
        add(GameShoutCommand())

        instance = this
    }

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())

        Game.lock()
        Game.resetTime()

        val random = Random(Game.count + 1)
        var found = false

        for (player in Game.players.values) {
            player.run {
                add(ScoreboardComponent(this))
                add(TitleComponent(this))
                add(VanishComponent(this))
            }

            if (!found && random.nextBoolean()) {
                found = true
                target = player.entity.uniqueId

                player.run {
                    add(TargetComponent(this))
                    add(TargetGetReadyComponent(this))
                    add(TargetDimensionComponent(this))
                    add(TargetScoreboardComponent(this))
                    add(TargetSignalComponent(this))
                    add(TargetAchievementComponent(this))
                    add(TargetDeathComponent(this))
                }

                continue
            }

            addHunter(player)
        }
    }

    override fun afterInit() {
        Bukkit.getPluginManager().callEvent(TargetJoinEvent(Game.players[target!!]))

        for (player in Game.players.values) {
            teleportToSpawn(player)

            if (player.uniqueId === target)
                continue

            Bukkit.getPluginManager().callEvent(HunterJoinEvent(player))
        }

        intro.start()
    }

    @EventHandler
    fun onGameJoin(event: GameJoinEvent) {
        addHunter(event.player, true)

        broadcast {
            append("${COLOR_GRAY}[")
            append("$COLOR_GREEN+")
            append("${COLOR_GRAY}]")
            append("$COLOR_BLUE [Hunter] ${event.player.entity.name}")
            append("$COLOR_YELLOW came to hunt the target!")
        }
    }

    @EventHandler
    fun onGameQuit(event: GameQuitEvent) {
        if (event.player.uniqueId === target) {
            Bukkit.getPluginManager().callEvent(TargetQuitEvent(event.player))

            broadcast {
                append("$COLOR_RED")
                append("[Target] ${event.player.entity.name}")
                append("$COLOR_YELLOW disconnected.")
            }

            Game.win()
            return
        }
        hunters--

        broadcast {
            append("$COLOR_BLUE")
            append("[Hunter] ${event.player.entity.name}")
            append("$COLOR_YELLOW disconnected.")
        }

        if (hunters > 0)
            return

        Game.lost()
    }

    @EventHandler
    fun onHunterRespawn(event: HunterRespawnEvent) = teleportToSpawn(event.hunter)

    private fun addHunter(player: GamePlayer, join: Boolean = false) {
        hunters++
        player.run {
            add(HunterComponent(this))
            add(HunterScoreboardComponent(this))
            add(HunterSignalComponent(this))
            add(HunterCompassComponent(this))
            add(HunterAchievementComponent(this))
            add(HunterShoutComponent(this))
            add(HunterDeathComponent(this))
            add(HunterRespawnComponent(this))
        }

        if (join) {
            player.add(GameWarningComponent(player))
            teleportToSpawn(player)
            Bukkit.getPluginManager().callEvent(HunterJoinEvent(player))
        }
    }

    private fun teleportToSpawn(player: GamePlayer) {
        player.entity.run {
            val location = config.read<Location>("location")

            if (location == null)
                Game.logger.warning("Game spawn location doesn't exist ( use /setgamespawn )")
            else
                teleport(location)
        }
    }

    private inline fun broadcast(block: StringBuilder.() -> Unit) {
        val message = StringBuilder().apply {
            block(this)
        }.toString()

        // Bukkit.broadcastMessage() is expensive since it broadcast with permission
        Game.players.values.forEach {
            it.entity.run {
                sendMessage(message)
            }
        }
        Bukkit.getConsoleSender().sendMessage(message)
    }

    override fun onDestroy() {
        GameJoinEvent.getHandlerList().unregister(this)
        GameQuitEvent.getHandlerList().unregister(this)
        HunterRespawnEvent.getHandlerList().unregister(this)

        intro.stop()
        gettingReady.stop()
    }

}
