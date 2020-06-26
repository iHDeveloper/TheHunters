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

package me.ihdeveloper.thehunters

import me.ihdeveloper.thehunters.command.GameShoutCommand
import me.ihdeveloper.thehunters.command.GameSetSpawnCommand
import me.ihdeveloper.thehunters.component.ConfigurationComponent
import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.DeathComponent
import me.ihdeveloper.thehunters.component.NoDamageComponent
import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TYPE_COUNTDOWN
import me.ihdeveloper.thehunters.component.TYPE_NO_DAMAGE
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterAchievementComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterChatComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterCompassComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterDeathComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterRespawnComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterScoreboardComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterShoutComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterSignalComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetAchievementComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetChatComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetDeathComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetDimensionComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetGetReadyComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetScoreboardComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetSignalComponent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.hunter.HunterRespawnEvent
import me.ihdeveloper.thehunters.event.player.GameJoinEvent
import me.ihdeveloper.thehunters.event.player.GameQuitEvent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetQuitEvent
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
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import java.util.UUID
import kotlin.random.Random

class Gameplay : GameObject(
        components = listOf(
                TargetChatComponent(),
                HunterChatComponent()
        )
), Listener {

    companion object {
        lateinit var instance: Gameplay

        val hunters: Int get() = instance.hunters
    }

    private var target: UUID? = null
    private var hunters: Int = 0

    private val config = ConfigurationComponent("game")

    private val countdown = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_GET_READY,
            defaultStart = 20 * 60,
            onFinish = {
                Game.players.values.forEach { it.remove(TYPE_NO_DAMAGE) }

                Game.unlock()
            }
    )

    private val intro = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_INTRO,
            defaultStart = 20 * 5,
            onFinish = {
                remove(TYPE_COUNTDOWN)
                add(countdown)
                countdown.start()

                Game.players.values.forEach { it.add(NoDamageComponent(it, false)) }
            }
    )

    init {
        add(config)
        add(intro)
        add(GameSetSpawnCommand(config))
        add(GameShoutCommand())

        instance = this
    }

    override fun onInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin())

        Game.lock()
        Game.resetTime()

        val random = Random(Game.count + 1)
        var found = false

        for (player in Game.players.values) {
            player.run {
                add(ScoreboardComponent(this))
                add(TitleComponent(this))
                add(VanishComponent(this))
            }

            if (!found && random.nextBoolean()) {
                found = true
                target = player.entity.uniqueId

                player.run {
                    add(TargetComponent(this))
                    add(TargetGetReadyComponent(this))
                    add(TargetDimensionComponent(this))
                    add(TargetScoreboardComponent(this))
                    add(TargetSignalComponent(this))
                    add(TargetAchievementComponent(this))
                    add(TargetDeathComponent(this))
                }

                continue
            }

            addHunter(player)
        }
    }

    override fun afterInit() {
        Bukkit.getPluginManager().callEvent(TargetJoinEvent(Game.players[target!!]))

        for (player in Game.players.values) {
            teleportToSpawn(player)

            if (player.uniqueId === target)
                continue

            Bukkit.getPluginManager().callEvent(HunterJoinEvent(player))
        }

        intro.start()
    }

    @EventHandler
    fun onGameJoin(event: GameJoinEvent) = addHunter(event.player, true)

    @EventHandler
    fun onGameQuit(event: GameQuitEvent) {
        if (event.player.uniqueId === target) {
            Bukkit.getPluginManager().callEvent(TargetQuitEvent(event.player))

            Game.win()
            return
        }
        hunters--

        if (hunters > 0)
            return

        Game.lost()
    }

    @EventHandler
    fun onHunterRespawn(event: HunterRespawnEvent) = teleportToSpawn(event.hunter)

    private fun addHunter(player: GamePlayer, join: Boolean = false) {
        hunters++
        player.run {
            add(HunterComponent(this))
            add(HunterScoreboardComponent(this))
            add(HunterSignalComponent(this))
            add(HunterCompassComponent(this))
            add(HunterAchievementComponent(this))
            add(HunterShoutComponent(this))
            add(HunterDeathComponent(this))
            add(HunterRespawnComponent(this))
        }

        if (join) {
            teleportToSpawn(player)
            Bukkit.getPluginManager().callEvent(HunterJoinEvent(player))
        }
    }

    private fun teleportToSpawn(player: GamePlayer) {
        player.entity.run {
            val location = config.read<Location>("location")

            if (location == null)
                Game.logger.warning("Game spawn location doesn't exist ( use /setgamespawn )")
            else
                teleport(location)
        }
    }

    override fun onDestroy() {
        GameJoinEvent.getHandlerList().unregister(this)
        GameQuitEvent.getHandlerList().unregister(this)
        HunterRespawnEvent.getHandlerList().unregister(this)

        intro.stop()
        countdown.stop()
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
