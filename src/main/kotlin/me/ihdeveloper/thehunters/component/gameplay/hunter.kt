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
import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.component.AchievementComponent
import me.ihdeveloper.thehunters.component.ChatComponent
import me.ihdeveloper.thehunters.component.SpectatorComponent
import me.ihdeveloper.thehunters.component.TYPE_SPECTATOR
import me.ihdeveloper.thehunters.component.TYPE_TITLE
import me.ihdeveloper.thehunters.component.TYPE_VANISH
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.event.hunter.HunterDeathEvent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.hunter.HunterQuitEvent
import me.ihdeveloper.thehunters.event.hunter.HunterRespawnEvent
import me.ihdeveloper.thehunters.event.target.TargetDimensionEvent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetKillEvent
import me.ihdeveloper.thehunters.event.target.TargetLostEvent
import me.ihdeveloper.thehunters.event.target.TargetQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetRecoverEvent
import me.ihdeveloper.thehunters.event.target.TargetSignalEvent
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_BLUE
import me.ihdeveloper.thehunters.util.COLOR_BOLD
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_WHITE
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Achievement
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Score

const val TYPE_GAMEPLAY_HUNTER: Short = 350
const val TYPE_GAMEPLAY_HUNTER_SCOREBOARD: Short = 351
const val TYPE_GAMEPLAY_HUNTER_SIGNAL: Short = 352
const val TYPE_GAMEPLAY_HUNTER_COMPASS: Short = 353
const val TYPE_GAMEPLAY_HUNTER_CHAT: Short = 354
const val TYPE_GAMEPLAY_HUNTER_SHOUT: Short = 355
const val TYPE_GAMEPLAY_HUNTER_DEATH: Short = 356
const val TYPE_GAMEPLAY_HUNTER_RESPAWN: Short = 357

private const val COMPASS_SLOT = 8
private const val SHOUT_COOLDOWN = 60
private const val RESPAWN_COOLDOWN = 5

class HunterComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_GAMEPLAY_HUNTER

    var deaths = 0

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

            addSpawnItems()
        }

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onRespawn(event: HunterRespawnEvent) {
        if (event.hunter.uniqueId !== gameObject.uniqueId)
            return

        gameObject.entity.addSpawnItems()
    }

    @EventHandler
    fun onDeath(event: HunterDeathEvent) {
        if (event.hunter.uniqueId !== gameObject.uniqueId)
            return

        deaths++
    }

    override fun onDestroy(gameObject: GamePlayer) {
        HunterDeathEvent.getHandlerList().unregister(this)
        HunterRespawnEvent.getHandlerList().unregister(this)
    }

    private fun Player.addSpawnItems() {
        inventory.addItem(ItemStack(Material.WOOD_SWORD, 1))
        inventory.addItem(ItemStack(Material.COOKED_BEEF, 10))
    }
}

class HunterScoreboardComponent (
        override val gameObject: GamePlayer
) : GameScoreboardComponent(), Listener {

    override val target = false

    override val type = TYPE_GAMEPLAY_HUNTER_SCOREBOARD

    private var dimensionScore: Score? = null
    private var lastDimension: Dimension = Dimension.UNKNOWN

    private var deathsScore: Score? = null
    private var lastDeathsCount = -1

    override fun onInit(gameObject: GamePlayer) {
        super.onInit(gameObject)

        Game.players.values.forEach {
            if (it.has(TYPE_GAMEPLAY_TARGET)) {
                targets!!.addEntry(it.entity.name)
                updateTargetDimension(Dimension.get(it.entity.world), true)
            }
            else
                hunters!!.addEntry(it.entity.name)
        }
        hunters!!.addEntry(gameObject.entity.name)

        updateTargetDimension(Dimension.UNKNOWN, true)
        updateDeathsCount(0)

        sidebar!!.getScore("$COLOR_BOLD$COLOR_YELLOW").score = 1

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onTargetJoin(event: TargetJoinEvent) {
        targets!!.addEntry(event.target.entity.name)
    }

    @EventHandler
    fun onTargetQuit(event: TargetQuitEvent) {
        targets!!.removeEntry(event.target.entity.name)
    }

    @EventHandler
    fun onTargetDimension(event: TargetDimensionEvent) = updateTargetDimension(event.dimension)

    @EventHandler
    fun onTargetLost(@Suppress("UNUSED_PARAMETER") event: TargetLostEvent) = updateTargetDimension(Dimension.UNKNOWN)

    @EventHandler
    fun onTargetRecover(event: TargetRecoverEvent) = updateTargetDimension(event.dimension)

    @EventHandler
    fun onHunterJoin(event: HunterJoinEvent) {
        hunters!!.addEntry(event.hunter.entity.name)
    }

    @EventHandler
    fun onHunterQuit(event: HunterQuitEvent) {
        hunters!!.removeEntry(event.hunter.entity.name)
    }

    @EventHandler
    fun onHunterDeath(event: HunterDeathEvent) {
        gameObject.run {
            if (event.hunter.uniqueId !== uniqueId)
                return

            updateDeathsCount(get<HunterComponent>(TYPE_GAMEPLAY_HUNTER).deaths)
        }
    }

    private fun updateTargetDimension(dimension: Dimension, force: Boolean = false) {
        if (lastDimension == dimension && !force)
            return
        lastDimension = dimension

        if (dimensionScore != null) {
            scoreboard!!.resetScores(dimensionScore!!.entry)
        }

        dimensionScore = sidebar!!.getScore("${COLOR_YELLOW}Target Dimension: ${dimension.displayName}")
        dimensionScore!!.score = 3
    }

    private fun updateDeathsCount(deaths: Int) {
        if (lastDeathsCount == deaths)
            return
        lastDeathsCount = deaths

        if (deathsScore != null) {
            scoreboard!!.resetScores(deathsScore!!.entry)
        }

        deathsScore = sidebar!!.getScore("${COLOR_YELLOW}Deaths: $COLOR_WHITE$deaths")
        deathsScore!!.score = 2
    }

    override fun onDestroy(gameObject: GamePlayer) {
        TargetJoinEvent.getHandlerList().unregister(this)
        TargetDimensionEvent.getHandlerList().unregister(this)
        TargetLostEvent.getHandlerList().unregister(this)
        HunterJoinEvent.getHandlerList().unregister(this)
        HunterQuitEvent.getHandlerList().unregister(this)
        HunterDeathEvent.getHandlerList()

        dimensionScore = null
        deathsScore = null

        super.onDestroy(gameObject)
    }

}

class HunterSignalComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_GAMEPLAY_HUNTER_SIGNAL

    private var died = false

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

        gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
    }

    @EventHandler
    fun onTargetDimensionChanged(event: TargetDimensionEvent) {
        val hunterDimension = Dimension.get(gameObject.entity.world)
        updateDimension(event.dimension, hunterDimension, event.target.entity.location)
    }

    private fun updateDimension(target: Dimension, hunter: Dimension, location: Location?) {
        if (target == hunter) {
            gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).found(location!!)
            return
        }

        gameObject.get<TitleComponent>(TYPE_TITLE).run {
            reset()
            title("$COLOR_RED${COLOR_BOLD}Target Lost!")
            subtitle("${COLOR_YELLOW}The target is in ${target.displayName}")
            time(5, 15, 5)
        }

        gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
    }

    @EventHandler
    fun signal(event: TargetSignalEvent) {
        val targetDimension = Dimension.get(event.target.entity.world)
        val ourDimension = Dimension.get(gameObject.entity.world)

        if (targetDimension != ourDimension) {
            gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
            return
        }

        if (!died) {
            val message = "${COLOR_YELLOW}You received a signal from the target!"
            gameObject.entity.sendMessage(message)

            gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).found(event.location)
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

        val location = event.target.entity.location
        gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).found(location)
    }

    @EventHandler
    fun respawn(event: HunterRespawnEvent) {
        if (event.hunter.uniqueId !== gameObject.uniqueId)
            return

        died = false

        gameObject.run {
            get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
        }
    }

    @EventHandler
    fun death(event: HunterDeathEvent) {
        if (event.hunter.uniqueId !== gameObject.uniqueId)
            return

        died = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        TargetDimensionEvent.getHandlerList().unregister(this)
        TargetLostEvent.getHandlerList().unregister(this)
        TargetSignalEvent.getHandlerList().unregister(this)
        TargetRecoverEvent.getHandlerList().unregister(this)
        HunterRespawnEvent.getHandlerList().unregister(this)

        gameObject.entity.compassTarget = gameObject.entity.location
        gameObject.entity.inventory.setItem(COMPASS_SLOT, ItemStack(Material.AIR))
    }

}

class HunterCompassComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    companion object {
        private val empty = ItemStack(Material.STAINED_GLASS_PANE, 1, 14)
        private val compass = ItemStack(Material.COMPASS, 1)

        private var initialized = false

        private fun initialize() {
            if (initialized)
                return

            empty.run {
                val meta = itemMeta
                meta.run {
                    displayName = "$COLOR_RED${COLOR_BOLD}No Signal!"
                    lore = listOf(
                            "",
                            "${COLOR_GRAY}The tracking device is not receiving",
                            "${COLOR_GRAY}any signal from the target."
                    )
                }
                itemMeta = meta
            }

            compass.run {
                val meta = itemMeta
                meta.run {
                    displayName = "$COLOR_YELLOW${COLOR_BOLD}Tracking Device"
                    lore = listOf(
                            "",
                            "${COLOR_GRAY}The tracking device is receiving signals",
                            "${COLOR_GRAY}on the whereabouts of the target."
                    )
                }
                itemMeta = meta
            }

            initialized = true
        }
    }

    init {
        initialize()
    }

    override val type = TYPE_GAMEPLAY_HUNTER_COMPASS

    override fun onInit(gameObject: GamePlayer) {
        lost()

        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked.uniqueId !== gameObject.uniqueId)
            return

        if (event.inventory.type !== InventoryType.PLAYER)
            return

        if (event.slot != COMPASS_SLOT)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId)
            return

        if (event.player.inventory.heldItemSlot != COMPASS_SLOT)
            return

        event.isCancelled = true
    }

    fun lost() {
        gameObject.entity.inventory.run {
            setItem(COMPASS_SLOT, empty)
        }
    }

    fun found(location: Location) {
        gameObject.entity.run {
            compassTarget = location

            inventory.run {
                setItem(COMPASS_SLOT, compass)
            }
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerDropItemEvent.getHandlerList().unregister(this)

        gameObject.entity.run {
            compassTarget = location
            inventory.setItem(COMPASS_SLOT, ItemStack(Material.AIR))
        }
    }

}

class HunterAchievementComponent (
        gameObject: GamePlayer
) : AchievementComponent(gameObject) {

    override fun message(achievement: Achievement): String {
        val builder = StringBuilder().run {
            append("$COLOR_BLUE")
            append(gameObject.entity.name)
            append("$COLOR_YELLOW ")
            append("just got achievement")
            append("$COLOR_GOLD ${name(achievement)}")
            append("${COLOR_YELLOW}.")
        }
        return builder.toString()
    }

}

class HunterChatComponent : ChatComponent() {

    override val type = TYPE_GAMEPLAY_HUNTER_CHAT

    override fun allow(sender: GamePlayer): Boolean {
        return sender.has(TYPE_GAMEPLAY_HUNTER)
    }

    override fun build(sender: GamePlayer, message: String): String {
        val builder = StringBuilder().run {
            append("$COLOR_BLUE")
            append(sender.entity.name)
            append("$COLOR_WHITE")
            append(": $message")
        }
        return builder.toString()
    }

    override fun toWho(sender: GamePlayer): Collection<GamePlayer> {
        return Game.players.values.filter {
            it.has(TYPE_GAMEPLAY_HUNTER)
        }
    }

}

class HunterShoutComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Runnable {

    override val type = TYPE_GAMEPLAY_HUNTER_SHOUT

    private var seconds: Int = -1

    val can: Boolean get() { return seconds == -1 }
    val remaining: Int get() { return seconds }

    private var task: BukkitTask? = null

    override fun onInit(gameObject: GamePlayer) {}

    fun shout() {
        seconds = SHOUT_COOLDOWN

        task = Bukkit.getScheduler().runTaskTimer(plugin(), this, 0L, 20L)
    }

    private fun stop() {
        if (task != null) {
            task!!.cancel()
            task = null
        }
    }

    override fun run() {
        seconds--

        if (seconds <= 0) {
            seconds = -1
            stop()
        }
    }

    override fun onDestroy(gameObject: GamePlayer) {
        stop()
    }

}

class HunterDeathComponent (
        override val gameObject: GamePlayer
) : GameDeathComponent() {

    override val type = TYPE_GAMEPLAY_HUNTER_DEATH

    override val name: String get() = "${COLOR_BLUE}[Hunter] ${gameObject.entity.name}"

    override fun byPlayer(killer: GamePlayer, event: EntityDamageByEntityEvent) {
        if (killer.has(TYPE_GAMEPLAY_TARGET))
            Bukkit.getPluginManager().callEvent(TargetKillEvent(killer, gameObject))

        broadcast {
            append("$COLOR_YELLOW was killed by")
            if (killer.has(TYPE_GAMEPLAY_HUNTER)) {
                append("$COLOR_BLUE ${killer.entity.name}")
            } else {
                append("$COLOR_RED$COLOR_BOLD ${ChatColor.UNDERLINE}Target")
            }
        }
    }

    override fun beforeDeath() {
        Bukkit.getPluginManager().callEvent(HunterDeathEvent(gameObject))
    }

}

class HunterRespawnComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener, Runnable {

    override val type = TYPE_GAMEPLAY_HUNTER_RESPAWN

    private var remaining = RESPAWN_COOLDOWN
    private var task: BukkitTask? = null

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onDie(event: HunterDeathEvent) {
        gameObject.run {
            if (event.hunter.uniqueId !== uniqueId)
                return

            remove(TYPE_GAMEPLAY_HUNTER_COMPASS)

            entity.run {
                closeInventory()

                inventory.forEach {
                    if (it != null)
                        world.dropItemNaturally(location, it)
                }
            }

            add(SpectatorComponent(this))
            start()
        }

    }

    override fun run() {
        if (remaining <= 0) {
            stop()
            respawn()
            return
        }

        gameObject.run {
            get<TitleComponent>(TYPE_TITLE).run {
                reset()
                title("$COLOR_RED${COLOR_BOLD}You died!")
                subtitle(StringBuilder().apply {
                    append("$COLOR_YELLOW")
                    append("Respawning in")
                    append("$COLOR_RED $remaining")
                    append("$COLOR_YELLOW seconds")
                }.toString())
                time(5, 20, 5)
            }
        }

        remaining--
    }

    private fun start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin(), this, 0L, 20L)
    }

    private fun stop() {
        if (task != null) {
            task!!.cancel()
            task = null
        }
    }

    private fun respawn() {
        gameObject.run {
            remove(TYPE_SPECTATOR)

            entity.run {
                val h = 20.0
                health = h
                maxHealth = h
            }

            add(HunterCompassComponent(this))

            get<TitleComponent>(TYPE_TITLE).run {
                reset()
                title("$COLOR_GREEN${COLOR_BOLD}You respawned!")
                subtitle("${COLOR_YELLOW}Try to get the target again!")
                time(5, 20, 5)
            }
        }

        Bukkit.getPluginManager().callEvent(HunterRespawnEvent(gameObject))
    }

    override fun onDestroy(gameObject: GamePlayer) {
        HunterDeathEvent.getHandlerList().unregister(this)

        stop()
    }

}
