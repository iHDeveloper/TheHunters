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
import me.ihdeveloper.thehunters.plugin
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import net.minecraft.server.v1_8_R3.IChatBaseComponent
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle
import org.bukkit.Achievement
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerAchievementAwardedEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.scoreboard.Scoreboard
import java.util.*

const val TYPE_SCOREBOARD: Short = 101
const val TYPE_NO_HUNGER: Short = 102
const val TYPE_DISABLE_ITEM_COLLECT: Short = 103
const val TYPE_DISABLE_ITEM_DROP: Short = 104
const val TYPE_NO_DAMAGE: Short = 105
const val TYPE_ADVENTURE: Short = 106
const val TYPE_DISABLE_BLOCK_PLACE: Short = 107
const val TYPE_DISABLE_BLOCK_BREAK: Short = 108
const val TYPE_TITLE: Short = 109
const val TYPE_NO_INTERACT: Short = 110
const val TYPE_CLEAR_INVENTORY: Short = 111
const val TYPE_VANISH: Short = 112
const val TYPE_ACHIEVEMENT: Short = 113

class ScoreboardComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_SCOREBOARD

    var scoreboard: Scoreboard? = null

    override fun onInit(gameObject: GamePlayer) {
        reset()
    }

    fun reset() {
        scoreboard = Bukkit.getScoreboardManager().newScoreboard
        gameObject.entity.scoreboard = scoreboard
    }

    override fun onDestroy(gameObject: GamePlayer) {
        gameObject.entity.scoreboard = Bukkit.getScoreboardManager().mainScoreboard

        scoreboard = null
    }

}

class NoHungerComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_NO_HUNGER

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.foodLevel = 20
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        FoodLevelChangeEvent.getHandlerList().unregister(this)
    }

}

class DisableItemCollectComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_ITEM_COLLECT

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onCollect(event: PlayerPickupItemEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerPickupItemEvent.getHandlerList().unregister(this)
    }

}

class DisableItemDropComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_ITEM_DROP

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerDropItemEvent.getHandlerList().unregister(this)
    }

}

class NoDamageComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_NO_DAMAGE

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onDamage(event: EntityDamageEvent) {
        if (event.entity.uniqueId !== gameObject.uniqueId)
            return

        event.isCancelled = true
    }

    @EventHandler
    private fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity.uniqueId !== gameObject.uniqueId
                && event.damager.uniqueId !== gameObject.uniqueId)
            return

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        EntityDamageEvent.getHandlerList().unregister(this)
    }

}

class AdventureComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_ADVENTURE

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.gameMode = GameMode.ADVENTURE
    }

    override fun onDestroy(gameObject: GamePlayer) {
        gameObject.entity.gameMode = GameMode.SURVIVAL
    }

}

class DisableBlockPlaceComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_BLOCK_PLACE

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onPlace(event: BlockPlaceEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        BlockPlaceEvent.getHandlerList().unregister(this)
    }

}

class DisableBlockBreakComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_BLOCK_BREAK

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onBreak(event: BlockBreakEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        BlockBreakEvent.getHandlerList().unregister(this)
    }

}

class TitleComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_TITLE

    override fun onInit(gameObject: GamePlayer) {}

    fun title(value: String) {
        val packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, toChatComponent(value))
        connection().sendPacket(packet)
    }

    fun subtitle(value: String) {
        val packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, toChatComponent(value))
        connection().sendPacket(packet)
    }

    fun time(fadeIn: Int, interval: Int, fadeOut: Int) {
        val packet = PacketPlayOutTitle(fadeIn, interval, fadeOut)
        connection().sendPacket(packet)
    }

    fun clear() {
        val packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, toChatComponent())
        connection().sendPacket(packet)
    }

    fun reset() {
        val packet = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, toChatComponent())
        connection().sendPacket(packet)
    }

    private fun toChatComponent(value: String = "") = IChatBaseComponent.ChatSerializer.a("""{"text":"$value"}""")
    private fun connection() = (gameObject.entity as CraftPlayer).handle.playerConnection

    override fun onDestroy(gameObject: GamePlayer) {}

}

class NoInteractComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_NO_INTERACT

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onInteract(event: PlayerInteractEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerInteractEvent.getHandlerList().unregister(this)
    }

}

class ClearInventoryComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_CLEAR_INVENTORY

    override fun onInit(gameObject: GamePlayer) {
        gameObject.entity.inventory.clear()
    }

    override fun onDestroy(gameObject: GamePlayer) {
    }

}

class VanishComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_VANISH

    // Any player on this map is hidden from gameObject ( aka our player )
    private val states = mutableMapOf<UUID, Boolean>()

    override fun onInit(gameObject: GamePlayer) {}

    fun show(player: GamePlayer) {
        if (player.uniqueId === gameObject.uniqueId)
            return

        if (states[player.uniqueId] == null)
            return

        states.remove(player.uniqueId)
        gameObject.entity.showPlayer(player.entity)
    }

    fun hide(player: GamePlayer) {
        if (player.uniqueId === gameObject.uniqueId)
            return

        val state = states[player.uniqueId]

        if (state != null)
            return

        states[player.uniqueId] = true
        gameObject.entity.hidePlayer(player.entity)
    }

    override fun onDestroy(gameObject: GamePlayer) {

        for (uniqueId in states.keys) {
            val player = Game.players[uniqueId] ?: continue

            gameObject.entity.showPlayer(player.entity)
        }
    }

}

open class AchievementComponent (
        override val gameObject: GamePlayer

) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_ACHIEVEMENT

    private var blocked = false

    override fun onInit(gameObject: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    fun onAwarding(event: PlayerAchievementAwardedEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId)
            return

        if (blocked) {
            event.isCancelled = true
            return
        }

        Bukkit.broadcastMessage(message(event.achievement))
    }

    fun accept() {
        blocked = false
    }

    fun block() {
        blocked = true
    }

    fun reset() {
        Achievement.values().forEach {
            gameObject.entity.removeAchievement(it)
        }
    }

    open fun message(achievement: Achievement): String {
        val builder = StringBuilder().run {
            append("$COLOR_GRAY")
            append(gameObject.entity.name)
            append("$COLOR_YELLOW ")
            append("got an achievement [ $COLOR_GOLD")
            append(achievement.name)
            append(" $COLOR_GRAY]")
        }
        return builder.toString()
    }

    override fun onDestroy(gameObject: GamePlayer) {
        PlayerAchievementAwardedEvent.getHandlerList().unregister(this)
    }

}
