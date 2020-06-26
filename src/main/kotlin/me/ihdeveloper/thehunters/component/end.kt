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

import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW

const val TYPE_END_VICTORY: Short = 901
const val TYPE_END_DEFEATED: Short = 902

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
                    append("Good hunt!")
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
