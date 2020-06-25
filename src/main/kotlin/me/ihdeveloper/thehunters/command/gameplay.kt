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

package me.ihdeveloper.thehunters.command

import me.ihdeveloper.thehunters.Game
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.component.ConfigurationComponent
import me.ihdeveloper.thehunters.component.PlayerCommandComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterShoutComponent
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_HUNTER
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_HUNTER_SHOUT
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_TARGET
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit
import org.bukkit.command.Command

const val TYPE_GAMEPLAY_COMMAND_SET_SPAWN: Short = 1101
const val TYPE_GAMEPLAY_COMMAND_SHOUT: Short = 1102

class GameSetSpawnCommand (
        private val config: ConfigurationComponent
) : PlayerCommandComponent("setgamespawn") {

    override val type = TYPE_GAMEPLAY_COMMAND_SET_SPAWN

    override fun onPlayerExecute(sender: GamePlayer, command: Command?, label: String?, args: Array<out String>?): Boolean {
        sender.entity.run {
            config.write("location", location)
            sendMessage("${COLOR_GREEN}Successfully set the game spawn!")
        }
        return true
    }

}

class GameShoutCommand : PlayerCommandComponent("shout") {

    override val type = TYPE_GAMEPLAY_COMMAND_SHOUT

    override fun onPlayerExecute(sender: GamePlayer, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (!sender.has(TYPE_GAMEPLAY_HUNTER)) {
            sender.entity.sendMessage("${COLOR_RED}Only hunters can execute this command!")
            return true
        }

        if (args!!.isEmpty()) {
            sender.entity.sendMessage("${COLOR_RED}Missing Arguments. Use /shout <message>")
            return true
        }

        var message = ""
        args!!.forEachIndexed { i, part ->
            if (i == 0) message = part
            else message += " $part"
        }

        val shout = sender.get<HunterShoutComponent>(TYPE_GAMEPLAY_HUNTER_SHOUT)

        if (!shout.can) {
            val seconds = shout.remaining

            sender.entity.sendMessage("${COLOR_YELLOW}You can shout in ${COLOR_RED}$seconds ${COLOR_YELLOW}seconds!")
            return true
        }

        val builder = StringBuilder().run {
            append("${COLOR_GOLD}[Shout] ")
            append("${COLOR_BLUE}[Hunter] ")
            append(sender.entity.name)
            append("${COLOR_WHITE}: $message")
        }

        val shoutMessage = builder.toString()

        Game.players.values.forEach {
            it.entity.sendMessage(shoutMessage)
        }
        Bukkit.getConsoleSender().sendMessage(shoutMessage)

        shout.shout()
        return true
    }

}
