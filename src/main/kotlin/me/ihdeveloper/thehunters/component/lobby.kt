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
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.GamePlayerEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard

const val TYPE_LOBBY_SCOREBOARD: Short = 200

class LobbyScoreboardComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_LOBBY_SCOREBOARD

    private var scoreboard: Scoreboard? = null
    private var sidebar: Objective? = null

    private var playersScore: Score? = null

    override fun onInit(gameObject: GamePlayer) {
        val component = gameObject.get<ScoreboardComponent>(TYPE_SCOREBOARD)
        scoreboard = component.scoreboard!!

        sidebar = scoreboard!!.getObjective(DisplaySlot.SIDEBAR)

        if (sidebar == null) {
            sidebar = scoreboard!!.registerNewObjective("sidebar", "dummy")
        }

        updatePlayersCount()
        sidebar!!.getScore("$COLOR_BOLD$COLOR_GRAY").score = 0

        sidebar!!.getScore("${COLOR_YELLOW}By").score = -1
        sidebar!!.getScore("${COLOR_RED}iHDeveloper").score = -2

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    private fun updatePlayersCount() {
        if (playersScore != null) {
            scoreboard!!.resetScores(playersScore!!.entry)
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

    override fun onDestroy(gameObject: GamePlayer) {
        GamePlayerEvent.getHandlerList().unregister(this)

        gameObject.get<ScoreboardComponent>(TYPE_SCOREBOARD).reset()

        scoreboard = null
        sidebar = null
        playersScore = null
    }

}
