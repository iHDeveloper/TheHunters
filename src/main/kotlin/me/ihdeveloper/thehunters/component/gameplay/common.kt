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

import me.ihdeveloper.thehunters.Dimension
import me.ihdeveloper.thehunters.Game
import me.ihdeveloper.thehunters.GameComponent
import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.component.BroadcastComponent
import me.ihdeveloper.thehunters.component.DeathComponent
import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TYPE_SCOREBOARD
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_CYAN
import me.ihdeveloper.thehunters.util.COLOR_DARK_PURPLE
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_PURPLE
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_GET_READY
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_INTRO
import me.ihdeveloper.thehunters.util.COUNTDOWN_RESTARTING
import me.ihdeveloper.thehunters.util.COUNTDOWN_THE_END
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.scoreboard.Criterias
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.NameTagVisibility
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team


const val TYPE_GAMEPLAY_ENDER_DRAGON: Short = 300
const val TYPE_GAMEPLAY_BROADCAST: Short = 301
const val TYPE_GAMEPLAY_WARNING: Short = 302

abstract class GameScoreboardComponent : GameComponentOf<GamePlayer>(), Listener {

    abstract override val gameObject: GamePlayer

    abstract override val type: Short

    abstract val target: Boolean

    protected var scoreboard: Scoreboard? = null

    protected var sidebar: Objective? = null
    private var belowName: Objective? = null
    private var playerList: Objective? = null

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

        belowName = scoreboard!!.getObjective(DisplaySlot.BELOW_NAME)
        if (belowName == null) {
            belowName = scoreboard!!.registerNewObjective("hearts", Criterias.HEALTH)
            belowName!!.displaySlot = DisplaySlot.BELOW_NAME
            belowName!!.displayName = "${COLOR_RED}♥"
        }

        playerList = scoreboard!!.getObjective(DisplaySlot.PLAYER_LIST)
        if (playerList == null) {
            playerList = scoreboard!!.registerNewObjective("hearts_in_list", "dummy")
            playerList!!.displaySlot = DisplaySlot.PLAYER_LIST
        }
        Game.players.values.forEach {
            playerList!!.getScore(it.entity.name).score = it.entity.health.toInt()
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
            COUNTDOWN_GAMEPLAY_GET_READY -> "${COLOR_GOLD}Getting Ready"
            COUNTDOWN_THE_END -> "${COLOR_PURPLE}The End"
            COUNTDOWN_RESTARTING -> "${COLOR_RED}Restarting"
            else -> "${COLOR_GRAY}Unknown"
        }

        updateGameEvent(name, event.ticks / 20)
    }

    @EventHandler
    fun onGameEventTick(event: CountdownTickEvent) {
        updateGameEventTimer(event.ticks / 20)
    }

    @EventHandler
    fun onJoin(event: GameJoinEvent) {
        playerList!!.getScore(event.player.entity.name).score = event.player.entity.health.toInt()
    }

    @EventHandler
    fun onQuit(event: GameQuitEvent) {
        scoreboard!!.resetScores(event.player.entity.name)
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if (event.entityType !== EntityType.PLAYER)
            return

        playerList!!.getScore(event.entity.name).score = ((event.entity as Player).health).toInt()
    }


    override fun onDestroy(gameObject: GamePlayer) {
        GameJoinEvent.getHandlerList().unregister(this)
        GameQuitEvent.getHandlerList().unregister(this)
        CountdownEvent.getHandlerList().unregister(this)
        EntityDamageEvent.getHandlerList().unregister(this)

        gameEventHeader = null
        gameEventName = null
        gameEventTimer = null

        hunters!!.unregister()
        hunters = null

        targets!!.unregister()
        targets = null

        sidebar!!.unregister()
        sidebar = null

        scoreboard = null
    }

}

abstract class GameDeathComponent : DeathComponent() {

    abstract override val gameObject: GamePlayer

    abstract override val type: Short

    protected abstract val name: String

    override fun byEntity(killer: Entity, event: EntityDamageByEntityEvent) {
        broadcast {
            if (killer.type === EntityType.FIREBALL) {
                append("$COLOR_YELLOW was fire balled!")
                return
            }

            append("$COLOR_YELLOW was killed by")

            append(when(killer.type) {
                EntityType.ARROW -> "$COLOR_RED Arrow"
                EntityType.BLAZE -> "$COLOR_GOLD Blaze"
                EntityType.CAVE_SPIDER -> "$COLOR_DARK_PURPLE Cave Spider"
                EntityType.ENDER_DRAGON -> "$COLOR_DARK_PURPLE Ender Dragon"
                EntityType.ENDERMAN -> "$COLOR_DARK_PURPLE Enderman"
                EntityType.FALLING_BLOCK -> "$COLOR_CYAN Falling Block"
                EntityType.GHAST -> "$COLOR_GRAY Ghost"
                EntityType.GUARDIAN -> "$COLOR_CYAN Guardian"
                EntityType.IRON_GOLEM -> "$COLOR_WHITE Iron Golem"
                EntityType.MAGMA_CUBE -> "$COLOR_RED Magma Cube"
                EntityType.SILVERFISH -> "$COLOR_GRAY Silverfish"
                EntityType.SKELETON -> "$COLOR_GRAY Skeleton"
                EntityType.SPIDER -> "$COLOR_PURPLE Spider"
                EntityType.PIG_ZOMBIE -> "$COLOR_PURPLE Pig Zombie"
                EntityType.WITHER_SKULL -> "$COLOR_RED Wither Skull"
                EntityType.WOLF -> "$COLOR_GRAY Wolf"
                EntityType.ZOMBIE -> "$COLOR_GREEN Zombie"
                else -> "$COLOR_GRAY Unknown"
            })
        }
    }

    override fun byEntityExplosion(entity: Entity) {
        broadcast {
            append("$COLOR_YELLOW blew up")

            append(when (entity.type) {
                EntityType.CREEPER -> " by$COLOR_GREEN Creeper"
                EntityType.PRIMED_TNT -> " by$COLOR_RED TNT"
                EntityType.MINECART_TNT -> " by$COLOR_RED Minecart$COLOR_YELLOW with$COLOR_RED TNT"
                else -> "!"
            })
        }
    }

    override fun byBlock(killer: Block, event: EntityDamageByBlockEvent) {
        broadcast {
            append("$COLOR_YELLOW was killed by a block aka")

            // FIX ME: Give another name instead of the enum name
            append("$COLOR_RED ${killer.type.name}")
        }
    }

    override fun byBlockExplosion(killer: Block) {
        broadcast {
            append("$COLOR_YELLOW blew up")
        }
    }

    override fun byContact() {
        // FIX ME: This death reason should be improved and checked if it can happen at all

        broadcast {
            append("$COLOR_YELLOW contacted the wrong entity")
        }
    }

    override fun byDrowning() {
        broadcast {
            append("$COLOR_YELLOW drowned")
        }
    }

    override fun byFalling() {
        broadcast {
            append("$COLOR_YELLOW fell from high place")
        }
    }

    override fun byFire() {
        broadcast {
            append("$COLOR_YELLOW fired away")
        }
    }

    override fun byLava() {
        broadcast {
            append("$COLOR_YELLOW tried to swim in")
            append("$COLOR_RED Lava")
        }
    }

    override fun byLighting() {
        broadcast {
            append("$COLOR_YELLOW lighted away")
        }
    }

    override fun byMagic() {
        broadcast {
            append("$COLOR_YELLOW was killed by")
            append("$COLOR_PURPLE Magic")
        }
    }

    override fun byMelting() {
        // FIX ME: This death reason should be improved and checked if it can happen at all

        broadcast {
            append("$COLOR_YELLOW melted to")
            append("$COLOR_RED Death")
        }
    }

    override fun byPoison() {
        // FIX ME: This death reason should be improved and checked if it can happen at all

        broadcast {
            append("$COLOR_YELLOW poisoned to")
            append("$COLOR_RED Death")
        }
    }

    override fun byStarvation() {
        broadcast {
            append("$COLOR_YELLOW starved to")
            append("$COLOR_RED Death")
        }
    }

    override fun bySuffocation() {
        broadcast {
            append("$COLOR_YELLOW suffocated in")
            append("$COLOR_GRAY Wall")
        }
    }

    override fun bySuicide() {
        broadcast {
            append("$COLOR_YELLOW died")
        }
    }

    override fun byThorns() {
        broadcast {
            append("$COLOR_YELLOW was finished by")
            append("$COLOR_GREEN Cactus")
        }
    }

    override fun byVoid() {
        broadcast {
            append("$COLOR_YELLOW fell into the void")
        }
    }

    override fun byWither() {
        broadcast {
            append("$COLOR_YELLOW was killed by")
            append("$COLOR_PURPLE Wither")
        }
    }

    override fun unknown(event: EntityDamageEvent) {
        broadcast {
            append("$COLOR_YELLOW died")
        }
    }

    protected inline fun broadcast(block: StringBuilder.() -> Unit) {
        val builder = StringBuilder().apply {
            append(name)
            block(this)

            val dimension = Dimension.get(gameObject.entity.world)

            if (dimension != Dimension.WORLD)
                append("$COLOR_YELLOW in ${dimension.displayName}")
        }

        val message = builder.toString()

        // Bukkit.broadcastMessage() is expensive since it broadcast with permission
        Game.players.values.forEach {
            it.entity.run {
                sendMessage(message)
            }
        }
        Bukkit.getConsoleSender().sendMessage(message)
    }

    abstract override fun byPlayer(killer: GamePlayer, event: EntityDamageByEntityEvent)
}

class EnderDragonComponent : GameComponent, Listener {

    override val type = TYPE_GAMEPLAY_ENDER_DRAGON

    override fun init() {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onEnderDragonDeath(event: EntityDeathEvent) {
        if (event.entity.type !== EntityType.ENDER_DRAGON)
            return

        Game.lost()
    }

    override fun destroy() {
        EntityDeathEvent.getHandlerList().unregister(this)
    }

}

class GameBroadcastComponent : BroadcastComponent(
        id = COUNTDOWN_THE_END,
        filter = {
            val min = 60

               it == 45 * min  || it == 30 * min
            || it == 15 * min || it == 10 * min
            || it == 5 * min  || it == 3 * min
            || it == 2 * min  || it == 1 * min
            || it == 45       || it == 30
            || it == 15       || it == 10
            || it in 1..5
        }
) {

    override val type = TYPE_GAMEPLAY_BROADCAST

    override val onStart: StringBuilder.(Int) -> Unit = {
        append("$COLOR_YELLOW")
        append("The target has")
        append("$COLOR_RED ${it/60}")
        append("$COLOR_YELLOW minutes to finish the game!")
    }

    override val onSecond: StringBuilder.(Int) -> Unit = {
        append("$COLOR_YELLOW")
        append("The target has")
        if (it >= 60) {
            append("$COLOR_RED $${it / 60}")
            append("$COLOR_YELLOW minutes left to win!")
        } else {
            append("$COLOR_RED $it")
            append("$COLOR_YELLOW seconds left to win!")
        }
    }

    override val onFinish: StringBuilder.() -> Unit = {
        append("$COLOR_YELLOW")
        append("The target failed to finish the game!")
    }

}

class GameWarningComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    companion object {
        val header = StringBuilder().apply {
            append("$COLOR_RED")
            append("This game is under development. So, you may expect bugs on it!")
        }.toString()

        val description = StringBuilder().apply {
            append("$COLOR_RED")
            append("Please report any issue with the game in")
        }.toString()

        val link = StringBuilder().apply {
            append("$COLOR_YELLOW")
            append("https://github.com/iHDeveloper/TheHunters")
        }.toString()
    }

    override val type = TYPE_GAMEPLAY_WARNING

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.run {
            sendMessage(arrayOf(
                    "\n",
                    header,
                    "\n",
                    description,
                    link,
                    "\n"
            ))
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {}

}
