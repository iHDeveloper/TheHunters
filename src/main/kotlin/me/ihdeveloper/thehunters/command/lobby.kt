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

import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.Lobby
import me.ihdeveloper.thehunters.component.CommandComponent
import me.ihdeveloper.thehunters.component.ConfigurationComponent
import me.ihdeveloper.thehunters.component.PlayerCommandComponent
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.NumberFormatException

const val TYPE_COMMAND_LOBBY_FORCE_START: Short = 1001
const val TYPE_COMMAND_LOBBY_SET_SPAWN: Short = 1002

class LobbyForceStartCommand (
        private val lobby: Lobby
) : CommandComponent("forcestart") {

    override val type: Short = TYPE_COMMAND_LOBBY_FORCE_START

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (args!!.size == 1) {
            try {
                val countdown = Integer.valueOf(args[0])

                lobby.start(countdown * 20)
            } catch (e: NumberFormatException) {
                val message = "${COLOR_RED}The countdown is not valid number"
                sender!!.sendMessage(message)
            }
            return true
        }

        lobby.start()
        return true
    }

}

class LobbySetSpawnCommand (
        private val config: ConfigurationComponent
) : PlayerCommandComponent("setlobbyspawn") {

    override val type = TYPE_COMMAND_LOBBY_SET_SPAWN

    override fun onPlayerExecute(sender: GamePlayer, command: Command?, label: String?, args: Array<out String>?): Boolean {
        sender.entity.run {
            config.write("location", location)
            sendMessage("${COLOR_GREEN}Successfully set the lobby spawn for the game!")
        }
        return true
    }

}
