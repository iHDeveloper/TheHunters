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
import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_DARK_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_PURPLE
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_RESTARTING
import org.bukkit.Bukkit

const val TYPE_END_VICTORY: Short = 901
const val TYPE_END_DEFEATED: Short = 902
const val TYPE_END_BROADCAST: Short = 903
const val TYPE_END_CHAT: Short = 904

class VictoryComponent (
        val target: Boolean,
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_END_VICTORY

    override fun onInit(gameObject: GamePlayer) {
        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_GREEN${COLOR_BOLD}You won!")
            subtitle(StringBuilder().apply {
                append("$COLOR_YELLOW")
                append("$COLOR_BOLD")
                if (target)
                    append("You proved that you can't be hunted!")
                else
                    append("Good hunt! You defeated the target \\o/")
            }.toString())
            time(10, 60, 10)
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {}
}

class DefeatedComponent(
        val target: Boolean,
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_END_DEFEATED

    override fun onInit(gameObject: GamePlayer) {
        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_RED${COLOR_BOLD}You lost!")
            subtitle(StringBuilder().apply {
                append("$COLOR_YELLOW")
                append("$COLOR_BOLD")
                if (target)
                    append("You can be hunted!")
                else
                    append("Better luck! next time.")
            }.toString())
            time(10, 60, 10)
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {}

}

class TheEndBroadcastComponent : BroadcastComponent(
        id = COUNTDOWN_RESTARTING,
        filter = {
            it == 45 || it == 30 || it == 15 || it == 10 || it in 1..5
        }
) {

    override val type = TYPE_END_BROADCAST

    override val onStart: StringBuilder.(Int) -> Unit = {
        append("$COLOR_YELLOW")
        append("The game is about to restart in")
        append("$COLOR_RED $it")
        append("$COLOR_YELLOW seconds")
    }

    override val onSecond: StringBuilder.(Int) -> Unit = {
        append("$COLOR_YELLOW")
        append("Game is restarting in")
        append("$COLOR_RED $it")
        append("$COLOR_YELLOW seconds")
    }

    override val onFinish: StringBuilder.() -> Unit = {
        append("$COLOR_YELLOW")
        append("Game is restarting...")
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

}

class TheEndChatComponent : ChatComponent() {

    override val type = TYPE_END_CHAT

    override fun build(sender: GamePlayer, message: String): String {
        return StringBuilder().apply {
            append("${COLOR_DARK_GRAY}[")
            append("${COLOR_PURPLE}The End")
            append("${COLOR_DARK_GRAY}]")
            append("$COLOR_GRAY ")
            append(sender.entity.name)
            append("$COLOR_WHITE:$COLOR_GRAY")
            append(' ')
            append(message)
        }.toString()
    }

}
