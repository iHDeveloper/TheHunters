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
import me.ihdeveloper.thehunters.component.DeathComponent
import me.ihdeveloper.thehunters.component.TYPE_TITLE
import me.ihdeveloper.thehunters.component.TYPE_VANISH
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.event.hunter.HunterDeathEvent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.hunter.HunterQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetDimensionEvent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetLostEvent
import me.ihdeveloper.thehunters.event.target.TargetQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetRecoverEvent
import me.ihdeveloper.thehunters.event.target.TargetSignalEvent
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
import org.bukkit.Achievement
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
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

private const val COMPASS_SLOT = 8
private const val SHOUT_COOLDOWN = 60

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

class HunterScoreboardComponent (
        override val gameObject: GamePlayer
) : GameplayScoreboardComponent(), Listener {

    override val target = false

    override val type = TYPE_GAMEPLAY_HUNTER_SCOREBOARD

    private var dimensionScore: Score? = null
    private var lastDimension: Dimension = Dimension.UNKNOWN

    override fun onInit(gameObject: GamePlayer) {
        super.onInit(gameObject)

        hunters!!.addEntry(gameObject.entity.name)

        updateTargetDimension(Dimension.UNKNOWN, true)

        sidebar!!.getScore("$COLOR_BOLD$COLOR_YELLOW").score = 2

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
    fun onTargetLost(event: TargetLostEvent) = updateTargetDimension(Dimension.UNKNOWN)

    @EventHandler
    fun onTargetRecover(event: TargetRecoverEvent) {
        val dimension = Dimension.get(event.target.entity.world.name)
        updateTargetDimension(dimension)
    }

    @EventHandler
    fun onHunterJoin(event: HunterJoinEvent) {
        hunters!!.addEntry(event.hunter.entity.name)
    }

    @EventHandler
    fun onHunterQuit(event: HunterQuitEvent) {
        hunters!!.removeEntry(event.hunter.entity.name)
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

    override fun onDestroy(gameObject: GamePlayer) {
        TargetJoinEvent.getHandlerList().unregister(this)
        TargetDimensionEvent.getHandlerList().unregister(this)
        TargetLostEvent.getHandlerList().unregister(this)
        HunterJoinEvent.getHandlerList().unregister(this)
        HunterQuitEvent.getHandlerList().unregister(this)

        dimensionScore = null

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

        gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
    }

    @EventHandler
    fun onTargetDimensionChanged(event: TargetDimensionEvent) {
        val hunterDimension = Dimension.get(gameObject.entity.world.name)
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
        val targetDimension = Dimension.get(event.target.entity.world.name)
        val ourDimension = Dimension.get(gameObject.entity.world.name)

        if (targetDimension != ourDimension) {
            gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).lost()
            return
        }

        val message = "${COLOR_YELLOW}You received a signal from the target!"
        gameObject.entity.sendMessage(message)

        gameObject.get<HunterCompassComponent>(TYPE_GAMEPLAY_HUNTER_COMPASS).found(event.location)
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

    override fun onDestroy(gameObject: GamePlayer) {
        TargetDimensionEvent.getHandlerList().unregister(this)
        TargetLostEvent.getHandlerList().unregister(this)
        TargetSignalEvent.getHandlerList().unregister(this)
        TargetRecoverEvent.getHandlerList().unregister(this)

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
            append("just got achievement ")
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
        seconds = 60

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
) : DeathComponent() {

    override val type = TYPE_GAMEPLAY_HUNTER_DEATH

    private val name: String get() = "${COLOR_BLUE}[Hunter] ${gameObject.entity.name}"

    override fun byPlayer(killer: GamePlayer, event: EntityDamageByEntityEvent) {
        broadcast {
            append("$COLOR_YELLOW was killed by")
            if (killer.has(TYPE_GAMEPLAY_HUNTER)) {
                append("$COLOR_BLUE ${killer.entity.name}")
            } else {
                append("$COLOR_RED$COLOR_BOLD ${ChatColor.UNDERLINE}Target")
            }
        }
    }

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

    override fun onDeath() {
        Bukkit.getPluginManager().callEvent(HunterDeathEvent(gameObject))
    }

    private inline fun broadcast(block: StringBuilder.() -> Unit) {
        val builder = StringBuilder().apply {
            append(name)
            block(this)
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

}
