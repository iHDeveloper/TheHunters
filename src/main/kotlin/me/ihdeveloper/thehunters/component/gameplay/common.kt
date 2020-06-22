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
import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TYPE_SCOREBOARD
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_GET_READY
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_INTRO
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.lang.StringBuilder

abstract class GameplayScoreboardComponent : GameComponentOf<GamePlayer>(), Listener {

    abstract override val gameObject: GamePlayer

    abstract override val type: Short

    abstract val target: Boolean

    protected var scoreboard: Scoreboard? = null

    protected var sidebar: Objective? = null

    protected var hunters: Team? = null
    protected var targets: Team? = null

    private var gameEventHeader: Score? = null

    private var gameEventName: Score? = null

    private var gameEventTimer: Score? = null
    private var lastGameEventSeconds = -1

    override fun onInit(gameObject: GamePlayer) {
        scoreboard = gameObject.get<ScoreboardComponent>(TYPE_SCOREBOARD).scoreboard

        targets = scoreboard!!.getTeam("targets")
        if (targets == null) {
            targets = scoreboard!!.registerNewTeam("targets")
            targets!!.prefix = "${COLOR_RED}[Target] "
            targets!!.nameTagVisibility = NameTagVisibility.ALWAYS
            targets!!.setAllowFriendlyFire(false)
            targets!!.setCanSeeFriendlyInvisibles(false)

            if (!target) targets!!.suffix = " $COLOR_YELLOW${COLOR_BOLD}KILL!!"
        }

        hunters = scoreboard!!.getTeam("hunters")
        if (hunters == null) {
            hunters = scoreboard!!.registerNewTeam("hunters")
            hunters!!.prefix = "${COLOR_BLUE}[Hunter] "
            hunters!!.nameTagVisibility = NameTagVisibility.HIDE_FOR_OTHER_TEAMS
            hunters!!.setCanSeeFriendlyInvisibles(false)

            if (target) hunters!!.suffix = " $COLOR_YELLOW${COLOR_BOLD}KILL!!"
        }

        sidebar = scoreboard!!.getObjective(DisplaySlot.SIDEBAR)
        if (sidebar == null) {
            sidebar = scoreboard!!.registerNewObjective("stats", "dummy")
            sidebar!!.displaySlot = DisplaySlot.SIDEBAR
            sidebar!!.displayName = "$COLOR_YELLOW${COLOR_BOLD}THE HUNTERS"
        }

        sidebar!!.run {
            val name = if (target) {
                "${COLOR_RED}Target"
            } else {
                "${COLOR_BLUE}Hunter"
            }

            getScore("${COLOR_YELLOW}Role: $name").score = 9
        }

        sidebar!!.getScore("$COLOR_BOLD$COLOR_RED").score = 8

        gameEventHeader = sidebar!!.getScore("${COLOR_YELLOW}Game Event ")
        gameEventHeader!!.run { score = 7 }

        updateGameEvent(null, null)

        sidebar!!.getScore("$COLOR_BOLD$COLOR_BLUE").score = 4

        sidebar!!.getScore("${COLOR_YELLOW}By").score = -1
        sidebar!!.getScore("${COLOR_YELLOW}-$COLOR_RED iHDeveloper").score = -2

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    private fun updateGameEvent(name: String?, seconds: Int?) {
        updateGameEventName(name)
        updateGameEventTimer(seconds)
    }

    private fun updateGameEventName(name: String?) {
        if (gameEventName != null) {
            scoreboard!!.resetScores(gameEventName!!.entry)
        }

        if (name == null) {
            gameEventName = null
            return
        }

        gameEventName = sidebar!!.getScore("${COLOR_YELLOW}- $name")
        gameEventName!!.score = 6
    }

    private fun updateGameEventTimer(seconds: Int?) {
        if (seconds == null) {
            gameEventTimer = null
            return
        }

        if (lastGameEventSeconds == seconds) {
            return
        }
        lastGameEventSeconds = seconds

        if (gameEventTimer != null) {
            scoreboard!!.resetScores(gameEventTimer!!.entry)
        }

        val mins = (seconds / 60) % 60
        val secs = seconds % 60

        val builder = StringBuilder().run {
            append("${COLOR_YELLOW}- $COLOR_WHITE")
            if (mins <= 9) append("0")
            append(mins)
            append("${COLOR_GRAY}:$COLOR_WHITE")
            if (secs <= 9) append("0")
            append(secs)
        }

        gameEventTimer = sidebar!!.getScore(builder.toString())
        gameEventTimer!!.score = 5
    }

    @EventHandler
    fun onGameEventStart(event: CountdownStartEvent) {
        val name = when (event.id) {
            COUNTDOWN_GAMEPLAY_INTRO -> "${COLOR_BLUE}Intro"
            COUNTDOWN_GAMEPLAY_GET_READY ->"${COLOR_GOLD}Getting Ready"
            else -> "${COLOR_GRAY}Unknown"
        }

        updateGameEvent(name, event.ticks / 20)
    }

    @EventHandler
    fun onGameEventTick(event: CountdownTickEvent) {
        updateGameEventTimer(event.ticks / 20)
    }


    override fun onDestroy(gameObject: GamePlayer) {
        CountdownEvent.getHandlerList().unregister(this)

        gameEventHeader = null
        gameEventName = null
        gameEventTimer = null

        sidebar!!.unregister()
        sidebar = null

        scoreboard = null
    }

}
