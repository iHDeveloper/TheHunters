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

import me.ihdeveloper.thehunters.Game
import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.Gameplay
import me.ihdeveloper.thehunters.component.AchievementComponent
import me.ihdeveloper.thehunters.component.ChatComponent
import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.TYPE_TITLE
import me.ihdeveloper.thehunters.component.TYPE_VANISH
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.event.CountdownEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownFinishEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownStartEvent
import me.ihdeveloper.thehunters.event.countdown.CountdownTickEvent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.hunter.HunterQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetDimensionEvent
import me.ihdeveloper.thehunters.event.target.TargetKillEvent
import me.ihdeveloper.thehunters.event.target.TargetLostEvent
import me.ihdeveloper.thehunters.event.target.TargetRecoverEvent
import me.ihdeveloper.thehunters.event.target.TargetSignalEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_GET_READY
import org.bukkit.Achievement
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Team

const val TYPE_GAMEPLAY_TARGET: Short = 310
const val TYPE_GAMEPLAY_TARGET_GET_READY: Short = 311
const val TYPE_GAMEPLAY_TARGET_DIMENSION: Short = 312
const val TYPE_GAMEPLAY_TARGET_SCOREBOARD: Short = 313
const val TYPE_GAMEPLAY_TARGET_SIGNAL: Short = 314
const val TYPE_GAMEPLAY_TARGET_CHAT: Short = 315
const val TYPE_GAMEPLAY_TARGET_DEATH: Short = 316

class TargetComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener, Runnable {

    override val type = TYPE_GAMEPLAY_TARGET

    var kills = 0
    var surviveTime = 0

    private var taskId: Int = -1

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
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onKill(event: TargetKillEvent) = kills++

    internal fun startSurviveTimer() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin(), this, 0, 20).taskId
    }

    override fun run() {
        surviveTime += 20
        gameObject.get<TargetScoreboardComponent>(TYPE_GAMEPLAY_TARGET_SCOREBOARD).updateSurviveTime()
    }

    override fun onDestroy(gameObject: GamePlayer) {
        if (taskId != -1)
            Bukkit.getScheduler().cancelTask(taskId)

        TargetKillEvent.getHandlerList().unregister(this)

        gameObject.entity.run {
            val h = 20.0
            health = h
            maxHealth = h
        }
    }

}

class TargetGetReadyComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_GAMEPLAY_TARGET_GET_READY

    private var lastSeconds: Int = 100

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onStart(event: CountdownStartEvent) {
        if (event.id != COUNTDOWN_GAMEPLAY_GET_READY)
            return

        Bukkit.getPluginManager().callEvent(TargetLostEvent(gameObject))

        gameObject.get<VanishComponent>(TYPE_VANISH).run {
            for (player in Game.players.values) {
                hide(player)
            }
        }

        val seconds = event.ticks / 20

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_RED${COLOR_BOLD}Get ready!")
            subtitle("${COLOR_YELLOW}You have $COLOR_RED$seconds$COLOR_YELLOW seconds left")
            time(5, 20, 5)
        }

        gameObject.entity.run {
            val builder = StringBuilder().run {
                append("$COLOR_RED${COLOR_BOLD}GET READY!")
                append(' ')
                append("${COLOR_YELLOW}You have $COLOR_RED$seconds$COLOR_YELLOW seconds to get ready!")
            }
            sendMessage(" ")
            sendMessage(builder.toString())

            inventory.addItem(ItemStack(Material.STONE_SWORD, 1))
            inventory.addItem(ItemStack(Material.COOKED_BEEF, 10))

            val speedEffect = PotionEffect(PotionEffectType.SPEED, 60 * 20, 1, false)
            addPotionEffect(speedEffect)
        }
    }

    @EventHandler
    private fun onTick(event: CountdownTickEvent) {
        if (event.id != COUNTDOWN_GAMEPLAY_GET_READY)
            return

        notify(event.ticks / 20)
    }

    @EventHandler
    private fun onFinish(event: CountdownFinishEvent) {
        if (event.id != COUNTDOWN_GAMEPLAY_GET_READY)
            return

        Bukkit.getPluginManager().callEvent(TargetRecoverEvent(gameObject))

        gameObject.get<VanishComponent>(TYPE_VANISH).run {
            for (player in Game.players.values) {
                show(player)
            }
        }

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("${COLOR_YELLOW}Time is up!")
            subtitle("${COLOR_RED}Be careful from the hunters!")
        }

        gameObject.get<TargetComponent>(TYPE_GAMEPLAY_TARGET).startSurviveTimer()
    }

    private fun notify(seconds: Int) {
        if (lastSeconds == seconds)
            return

        lastSeconds = seconds

        if (seconds != 45
                && seconds != 30
                && seconds != 15
                && seconds != 10
                && seconds !in 1..5)
            return

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            subtitle("$COLOR_RED$seconds$COLOR_YELLOW seconds remaining")
            time(5, 10, 5)
        }

        gameObject.entity.run {
            val message = "${COLOR_YELLOW}You have $COLOR_RED$seconds$COLOR_YELLOW seconds to get ready!"
            sendMessage(message)
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {
        CountdownEvent.getHandlerList().unregister(this)
    }

}

class TargetDimensionComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_GAMEPLAY_TARGET_DIMENSION

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onWorldChange(event: PlayerChangedWorldEvent) {
        val event = TargetDimensionEvent(gameObject)
        event.run {
            val message = "${COLOR_YELLOW}You're in ${dimension.displayName}${COLOR_YELLOW}."
            gameObject.entity.sendMessage(message)
        }

        Bukkit.getPluginManager().callEvent(event)
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerChangedWorldEvent.getHandlerList().unregister(this)
    }

}

class TargetScoreboardComponent (
        override val gameObject: GamePlayer
) : GameScoreboardComponent(), Listener {

    override val target = true

    override val type = TYPE_GAMEPLAY_TARGET_SCOREBOARD

    private var huntersScore: Score? = null
    private var killsScore: Score? = null

    private var surviveTimeTeam: Team? = null

    private var lastHuntersCount: Int = -1
    private var lastKillsCount: Int = -1

    private var lastSurviveTime: Int = 0

    override fun onInit(gameObject: GamePlayer) {
        super.onInit(gameObject)

        targets!!.addEntry(gameObject.entity.name)

        updateHuntersCount()
        updateKillsCount()
        updateSurviveTime()

        sidebar!!.getScore("$COLOR_BOLD$COLOR_YELLOW").score = 1

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    private fun updateHuntersCount() {
        val count = Gameplay.hunters
        if (lastHuntersCount == count)
            return
        lastHuntersCount = count

        if (huntersScore != null) {
            scoreboard!!.resetScores(huntersScore!!.entry)
        }

        huntersScore = sidebar!!.getScore("${COLOR_YELLOW}Hunters Left:$COLOR_WHITE $count")
        huntersScore!!.score = 3
    }

    private fun updateKillsCount() {
        val count = gameObject.get<TargetComponent>(TYPE_GAMEPLAY_TARGET).kills
        if (lastKillsCount == count)
            return
        lastKillsCount = count

        if (killsScore != null) {
            scoreboard!!.resetScores(killsScore!!.entry)
        }

        killsScore = sidebar!!.getScore("${COLOR_YELLOW}Kills:$COLOR_WHITE $count")
        killsScore!!.score = 2
    }

    internal fun updateSurviveTime() {
        val ticks = gameObject.get<TargetComponent>(TYPE_GAMEPLAY_TARGET).surviveTime

        if (lastSurviveTime == ticks)
            return

        lastSurviveTime = ticks

        if (surviveTimeTeam == null) {
            surviveTimeTeam = scoreboard!!.getEntryTeam("@survive-time") ?: scoreboard!!.registerNewTeam("@survive-time")
            surviveTimeTeam!!.addEntry("§0§1 ")

            sidebar!!.getScore("§0§0 ").score = 1
            sidebar!!.getScore("§0§1 ").score = 1

            surviveTimeTeam!!.prefix = "§eSurvive Time:"
        }

        var seconds = ticks / 20
        var minutes = seconds / 60
        seconds %= 60
        minutes %= 60

        surviveTimeTeam!!.suffix = "§f${if (minutes <= 9) "0${minutes}" else minutes}§e:§f${if (seconds <= 9) "0${seconds}" else seconds}"
    }

    @EventHandler
    private fun onHunterJoin(event: HunterJoinEvent) {
        updateHuntersCount()
        hunters!!.addEntry(event.hunter.entity.name)
    }

    @EventHandler
    private fun onHunterQuit(event: HunterQuitEvent) {
        updateHuntersCount()
        hunters!!.removeEntry(event.hunter.entity.name)
    }

    @EventHandler
    fun onKill(event: TargetKillEvent) = updateKillsCount()

    override fun onDestroy(gameObject: GamePlayer) {
        HunterJoinEvent.getHandlerList().unregister(this)
        HunterQuitEvent.getHandlerList().unregister(this)
        TargetKillEvent.getHandlerList().unregister(this)

        huntersScore = null
        killsScore = null

        super.onDestroy(gameObject)
    }

}

class TargetSignalComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener, Runnable {

    override val type = TYPE_GAMEPLAY_TARGET_SIGNAL

    private var task: BukkitTask? = null

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    override fun run() {
        val message = "${COLOR_RED}A signal has been sent to the hunters!"

        gameObject.entity.sendMessage(message)

        val event = TargetSignalEvent(gameObject, gameObject.entity.location)
        Bukkit.getPluginManager().callEvent(event)
    }

    @EventHandler
    fun onRecover(event: TargetRecoverEvent) {
        task = Bukkit.getScheduler().runTaskTimer(plugin(), this, 0L, 30 * 20L)

        val message = "${COLOR_YELLOW}The hunters are going to receive a signal from you every$COLOR_RED 30$COLOR_YELLOW seconds!"

        gameObject.entity.sendMessage(message)
    }

    override fun onDestroy(gameObject: GamePlayer) {
        if (task != null) {
            task!!.cancel()
            task = null
        }

        TargetRecoverEvent.getHandlerList().unregister(this)
    }

}

class TargetAchievementComponent (
        gameObject: GamePlayer
): AchievementComponent(gameObject) {

    override fun message(achievement: Achievement): String {
        val builder = StringBuilder().run {
            append("${COLOR_RED}[Target] ")
            append(gameObject.entity.name)
            append("$COLOR_YELLOW ")
            append("is moving forward towards winning by getting")
            append("$COLOR_GOLD ${name(achievement)}")
            append("${COLOR_YELLOW}.")
        }
        return builder.toString()
    }

}

class TargetChatComponent : ChatComponent() {

    override val type = TYPE_GAMEPLAY_TARGET_CHAT

    override fun allow(sender: GamePlayer): Boolean {
        return sender.has(TYPE_GAMEPLAY_TARGET)
    }

    override fun build(sender: GamePlayer, message: String): String {
        val builder = StringBuilder().run {
            append("$COLOR_RED")
            append("[TARGET] ${sender.entity.name}")
            append("$COLOR_WHITE")
            append(": $message")
        }
        return builder.toString()
    }

}

class TargetDeathComponent (
        override val gameObject: GamePlayer
) : GameDeathComponent() {

    override val type = TYPE_GAMEPLAY_TARGET_DEATH

    override val name get() = "$COLOR_RED[Target] ${gameObject.entity.name}"

    override fun byPlayer(killer: GamePlayer, event: EntityDamageByEntityEvent) {
        broadcast {
            append("$COLOR_YELLOW was killed by")
            append("$COLOR_BLUE [Hunter] ${killer.entity.name}")
        }
    }

    override fun afterDeath() {
        Game.win()
    }

}
