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
import me.ihdeveloper.thehunters.component.TYPE_VANISH
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetLostEvent
import me.ihdeveloper.thehunters.event.target.TargetRecoverEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

const val TYPE_GAMEPLAY_HUNTER: Short = 350
const val TYPE_GAMEPLAY_HUNTER_SCOREBOARD: Short = 351
const val TYPE_GAMEPLAY_HUNTER_SIGNAL: Short = 352

class HunterComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_GAMEPLAY_HUNTER

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.run {
            val h = 20.0
            health = h
            maxHealth = h
        }

        val role = "$COLOR_BLUE${COLOR_BOLD}Hunter"
        val goal = "${COLOR_YELLOW}Kill the target before it kills the ender dragon!"

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
    }

    override fun onDestroy(gameObject: GamePlayer) {
    }

}

class HunterScoreboard (
        override val gameObject: GamePlayer
) : GameplayScoreboardComponent(), Listener {

    override val target = false

    override val type = TYPE_GAMEPLAY_HUNTER_SCOREBOARD

    override fun onInit(gameObject: GamePlayer) {
        super.onInit(gameObject)

        hunters!!.addEntry(gameObject.entity.name)

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onTargetJoin(event: TargetJoinEvent) {
        targets!!.addEntry(event.target.entity.name)
    }

    override fun onDestroy(gameObject: GamePlayer) {
        TargetJoinEvent.getHandlerList().unregister(this)

        super.onDestroy(gameObject)
    }

}

class HunterSignalComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_GAMEPLAY_HUNTER_SIGNAL

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun lost(event: TargetLostEvent) {
        val msg = "${COLOR_YELLOW}We lost the signal of the target."

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_RED${COLOR_BOLD}Target Lost!")
            subtitle(msg)
            time(5, 15, 5)
        }

        gameObject.entity.sendMessage(msg)

        gameObject.get<VanishComponent>(TYPE_VANISH).run {
            hide(event.target)
        }
    }

    @EventHandler
    fun recover(event: TargetRecoverEvent) {
        val msg = "${COLOR_YELLOW}We recovered the signal of the target!"

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_GOLD${COLOR_BOLD}Target Found!")
            subtitle(msg)
            time(5, 15, 5)
        }

        gameObject.get<VanishComponent>(TYPE_VANISH).show(event.target)
    }

    override fun onDestroy(gameObject: GamePlayer) {
        TargetLostEvent.getHandlerList().unregister(this)
        TargetRecoverEvent.getHandlerList().unregister(this)
    }

}
