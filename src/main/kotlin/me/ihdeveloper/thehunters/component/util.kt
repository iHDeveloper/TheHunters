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

package me.ihdeveloper.thehunters.component

import me.ihdeveloper.thehunters.Game
import me.ihdeveloper.thehunters.GameComponent
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownCancelEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownFinishEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.NotFoundCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.scheduler.BukkitTask
import java.io.File

const val TYPE_CONFIGURATION: Short = 1
const val TYPE_COUNTDOWN: Short = 2

class ConfigurationComponent (
        private val name: String,
        private val lazyLoad: Boolean = false
) : GameComponent {

    override val type = TYPE_CONFIGURATION

    private val file: File get() { return File(plugin().dataFolder, "${name}.yml") }
    private val config: YamlConfiguration = YamlConfiguration()

    override fun init() {
        Game.logger.info("Loading configuration [${name}]...")

        if (!lazyLoad)
            load()
    }

    fun load() {
        if (!file.exists()) {
            Game.logger.warning("Configuration [lobby] doesn't exist - Creating empty one...")
            file.createNewFile()
        }

        config.load(file)
    }

    fun <T : Any> read(key: String): T? {
        val value = config.get(key) ?: return null
        return value as T
    }

    fun <T> write(key: String, value: T) {
        config.set(key, value)
    }

    fun <T> writeDefault(key: String, value: T) {
        config.addDefault(key, value)
    }

    override fun destroy() {
        Game.logger.info("Saving configuration [${name}]...")
        config.save(file)
    }

}

class CountdownComponent (
        val id: Byte,
        var defaultStart: Int,
        val onFinish: () -> Unit = { }
) : GameComponent, Runnable {

    override val type = TYPE_COUNTDOWN

    var started = false

    private var ticksRemaining = defaultStart

    private var task: BukkitTask? = null

    override fun init() {
    }

    fun start() {
        if (started || ticksRemaining <= 0)
            return

        stop(false)

        started = true

        task = Bukkit.getScheduler().runTaskTimer(plugin(), this, 0L, 1L)
        Bukkit.getPluginManager().callEvent(CountdownStartEvent(id, ticksRemaining))
    }

    fun reset() {
        ticksRemaining = defaultStart
    }

    fun stop(broadcast: Boolean = true) {
        if (task != null) {
            task!!.cancel()
            task = null
        }

        started = false

        if (broadcast) {
            Bukkit.getPluginManager().callEvent(CountdownCancelEvent(id))
        }
    }

    override fun run() {
        ticksRemaining--

        Bukkit.getPluginManager().callEvent(CountdownTickEvent(id, ticksRemaining))

        if (ticksRemaining <= 0) {
            Bukkit.getPluginManager().callEvent(CountdownFinishEvent(id))

            onFinish()

            stop(false)
        }
    }

    override fun destroy() {
        stop(false)
    }

}

abstract class CommandComponent (
        open val name: String
) : GameComponent, CommandExecutor {

    abstract override val type: Short

    override fun init() {
        plugin().getCommand(name).executor = this
    }

    override fun destroy() {
        plugin().getCommand(name).executor = NotFoundCommand()
    }

    abstract override fun onCommand(
            sender: CommandSender?,
            command: Command?,
            label: String?,
            args: Array<out String>?
    ): Boolean
}

abstract class ChatComponent : GameComponent, Listener {

    abstract override val type: Short

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        val sender = Game.players[event.player.uniqueId] ?: error("Failed to find the player game object from the game")

        if (!allow(sender)) {
            return
        }

        val message = build(sender, event.message)

        // Bukkit.broadcastMessage() is expensive since it broadcast with permission
        // And checking the permission can be expensive operation in chat events
        toWho(sender).forEach {
            it.entity.run {
                sendMessage(message)
            }
        }
        Bukkit.getConsoleSender().sendMessage(message)

        // We don't want to format every message in the game
        // Since String.format is expensive string operation
        // We prefer not to use it.
        event.isCancelled = true
    }

    override fun destroy() {
        AsyncPlayerChatEvent.getHandlerList().unregister(this)
    }

    open fun allow(sender: GamePlayer): Boolean = true
    abstract fun build(sender: GamePlayer, message: String): String
    open fun toWho(sender: GamePlayer): Collection<GamePlayer> = Game.players.values
}

abstract class PlayerCommandComponent (
        name: String
) : CommandComponent(name) {

    abstract override val type: Short

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender!!.run {
                sendMessage("${COLOR_RED}The player only can execute this command!")
            }
            return true
        }
        val player = Game.players[sender.uniqueId]
        return onPlayerExecute(player!!, command, label, args)
    }

    abstract fun onPlayerExecute(sender: GamePlayer, command: Command?, label: String?, args: Array<out String>?): Boolean
}

abstract class BroadcastComponent (
        val id: Byte,
        val filter: (Int) -> Boolean = { true }
) : GameComponent, Listener {

    abstract override val type: Short

    abstract val onStart: StringBuilder.(Int) -> Unit
    abstract val onSecond: StringBuilder.(Int) -> Unit
    abstract val onFinish: StringBuilder.() -> Unit

    private var lastSeconds: Int = -1

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun start(event: CountdownStartEvent) {
        if (event.id != id)
            return

        broadcast {
            onStart(this, event.ticks / 20)
        }
    }

    @EventHandler
    fun tick(event: CountdownTickEvent) {
        if (event.id != id)
            return

        val seconds = event.ticks / 20

        if (lastSeconds == seconds)
            return
        lastSeconds = seconds

        if (!filter(seconds))
            return

        broadcast {
            onSecond(this, seconds)
        }
    }

    @EventHandler
    fun finish(event: CountdownFinishEvent) {
        if (event.id != id)
            return

        broadcast {
            onFinish(this)
        }
    }

    private inline fun broadcast(block: java.lang.StringBuilder.() -> Unit) {
        val message = java.lang.StringBuilder().apply {
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

    override fun destroy() {
        CountdownEvent.getHandlerList().unregister(this)
    }
}
