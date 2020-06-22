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

import me.ihdeveloper.thehunters.GameComponent
import me.ihdeveloper.thehunters.event.countdown.CountdownCancelEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownFinishEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.NotFoundCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitTask
import java.io.File

const val TYPE_CONFIGURATION: Short = 1
const val TYPE_COUNTDOWN: Short = 2
const val TYPE_COMMAND: Short = 3

class ConfigurationComponent (
        name: String
) : GameComponent {

    override val type = TYPE_CONFIGURATION

    private val file: File = File(plugin().dataFolder, "${name}.yml")
    private val config: YamlConfiguration = YamlConfiguration()

    override fun init() {
        config.load(file)
    }

    fun <T> read(key: String): T {
        return config.get(key) as T
    }

    fun <T> write(key: String, value: T) {
        config.set(key, value)
    }

    override fun destroy() {
        config.save(file)
    }

}

class CountdownComponent (
        val id: Byte,
        val defaultStart: Int,
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

        started = true

        stop(false)

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
        val name: String
) : GameComponent, CommandExecutor {

    override val type = TYPE_COMMAND

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
