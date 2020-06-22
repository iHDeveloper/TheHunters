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

package me.ihdeveloper.thehunters.component.gameplay

import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.component.TYPE_TITLE
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.event.target.TargetDimensionEvent
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit

const val TYPE_GAMEPLAY_TARGET: Short = 310

class TargetComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_GAMEPLAY_TARGET

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.run {
            val h = 20.0 * 3
            maxHealth = h
            health = h
        }

        val role = "$COLOR_RED${COLOR_BOLD}Target"
        val goal = "${COLOR_YELLOW}Finish the game before you get killed!"

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title(role)
            subtitle(goal)
            time(5, 2 * 20, 5)
        }

        gameObject.entity.run {
            sendMessage("")
            sendMessage("${COLOR_YELLOW}You're $role")
            sendMessage(goal)
            sendMessage("")
        }

        Bukkit.getPluginManager().callEvent(TargetDimensionEvent(gameObject))
    }

    override fun onDestroy(gameObject: GamePlayer) {
        gameObject.entity.run {
            val h = 20.0
            health = h
            maxHealth = h
        }
    }

}