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
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.GamePlayerEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownCancelEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownFinishEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_LOBBY
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard

const val TYPE_LOBBY_PLAYER: Short = 201
const val TYPE_LOBBY_SCOREBOARD: Short = 200

class LobbyComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_LOBBY_PLAYER

    override fun onInit(gameObject: GamePlayer) {
        val titleComponent = gameObject.get<TitleComponent>(TYPE_TITLE)
        titleComponent.title("$COLOR_YELLOW${COLOR_BOLD}The Hunters")
        titleComponent.subtitle("${COLOR_RED}Prove that you can't be hunted!")
        titleComponent.time(20, 40, 20)

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onCountdownTick(event: CountdownTickEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        val seconds = event.ticks / 20

        if (gameObject.entity.level == seconds || seconds > 60)
            return

        if (seconds == 60
            || seconds == 45
            || seconds == 30
            || seconds == 15
            || seconds == 10
            || (seconds in 1..5) )
            notifyPlayer(seconds)
        gameObject.entity.level = seconds
    }

    @EventHandler
    private fun onCountdownFinish(event: CountdownFinishEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        gameObject.entity.level = 0
    }

    @EventHandler
    private fun onCountdownCancel(event: CountdownCancelEvent) {
        if (event.id != COUNTDOWN_LOBBY)
            return

        gameObject.entity.level = 0
    }

    private fun notifyPlayer(seconds: Int) {
        val message = "${COLOR_RED}${seconds} ${COLOR_YELLOW}seconds to start"
        gameObject.get<TitleComponent>(TYPE_TITLE).reset()
        gameObject.get<TitleComponent>(TYPE_TITLE).subtitle(message)
        gameObject.get<TitleComponent>(TYPE_TITLE).time(10, 20, 10)
    }

    override fun onDestroy(gameObject: GamePlayer) {
        CountdownEvent.getHandlerList().unregister(this)
    }

}

class LobbyScoreboardComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_LOBBY_SCOREBOARD

    private var scoreboard: Scoreboard? = null
    private var sidebar: Objective? = null

    private var showTimeLeft = false
    private var timeLeftSeconds = 60
    private var timeLeftLastSeconds = 61
    private var timeLeftScore: Score? = null

    private var playersScore: Score? = null

    override fun onInit(gameObject: GamePlayer) {
        val component = gameObject.get<ScoreboardComponent>(TYPE_SCOREBOARD)
        scoreboard = component.scoreboard!!

        sidebar = scoreboard!!.getObjective(DisplaySlot.SIDEBAR)

        if (sidebar == null) {
            sidebar = scoreboard!!.registerNewObjective("sidebar", "dummy")
            sidebar!!.displaySlot = DisplaySlot.SIDEBAR
            sidebar!!.displayName = "${COLOR_YELLOW}${COLOR_BOLD}THE HUNTERS"
        }

        sidebar!!.getScore("$COLOR_BOLD$COLOR_WHITE").score = 4
        updateTimeLeft()
        sidebar!!.getScore("$COLOR_BOLD$COLOR_RED").score = 2

        updatePlayersCount()
        sidebar!!.getScore("$COLOR_BOLD$COLOR_GRAY").score = 0

        sidebar!!.getScore("${COLOR_YELLOW}By").score = -1
        sidebar!!.getScore("${COLOR_RED}iHDeveloper").score = -2

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    private fun updateTimeLeft() {
        if (!showTimeLeft)
            return

        if (timeLeftSeconds > 60)
            return

        if (timeLeftLastSeconds == timeLeftSeconds)
            return
        timeLeftLastSeconds = timeLeftSeconds

        if (timeLeftScore != null) {
            scoreboard!!.resetScores(timeLeftScore!!.entry)
            timeLeftScore = null
        }

        var secs = timeLeftSeconds
        var mins = secs / 60
        secs %= 60
        mins %= 60

        val builder = StringBuilder()
        builder.append("${COLOR_YELLOW}Time Left: ")
        builder.append("$COLOR_WHITE")
        if (mins <= 9) builder.append("0")
        builder.append(mins)
        builder.append("${COLOR_YELLOW}:")
        builder.append("$COLOR_WHITE")
        if (secs <= 9) builder.append("0")
        builder.append(secs)

        timeLeftScore = sidebar!!.getScore(builder.toString())
        timeLeftScore!!.score = 3
    }

    private fun updatePlayersCount() {
        if (playersScore != null) {
            scoreboard!!.resetScores(playersScore!!.entry)
            playersScore = null
        }

        val builder = StringBuilder()
        builder.append("${COLOR_YELLOW}Players: ")
        builder.append("$COLOR_GREEN${Game.count}")
        builder.append("$COLOR_GRAY/")
        builder.append("$COLOR_RED${Game.max}")
        playersScore = sidebar!!.getScore(builder.toString())
        playersScore!!.score = 1
    }

    @EventHandler
    private fun onJoin(event: GameJoinEvent) = updatePlayersCount()

    @EventHandler
    private fun onQuit(event: GameQuitEvent) = updatePlayersCount()

    @EventHandler
    private fun onCountdownStart(event: CountdownStartEvent) {
        if (event.id != COUNTDOWN_LOBBY) {
            return
        }

        showTimeLeft = true
    }

    @EventHandler
    private fun onCountdownTick(event: CountdownTickEvent) {
        if (event.id != COUNTDOWN_LOBBY) {
            return
        }

        timeLeftSeconds = event.ticks / 20
    }

    @EventHandler
    private fun onCountdownFinish(event: CountdownFinishEvent) {
        if (event.id != COUNTDOWN_LOBBY) {
            return
        }

        showTimeLeft = false
    }

    @EventHandler
    private fun onCountdownCancel(event: CountdownCancelEvent) {
        if (event.id != COUNTDOWN_LOBBY) {
            return
        }

        showTimeLeft = false
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onUpdate(event: CountdownEvent) {
        updateTimeLeft()
    }

    override fun onDestroy(gameObject: GamePlayer) {
        GamePlayerEvent.getHandlerList().unregister(this)
        CountdownEvent.getHandlerList().unregister(this)

        gameObject.get<ScoreboardComponent>(TYPE_SCOREBOARD).reset()

        scoreboard = null
        sidebar = null
        playersScore = null
        timeLeftScore = null
    }

}
