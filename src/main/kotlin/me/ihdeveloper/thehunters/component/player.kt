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

import me.ihdeveloper.thehunters.GameComponentOf
import me.ihdeveloper.thehunters.GamePlayer
import me.ihdeveloper.thehunters.plugin
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.scoreboard.Scoreboard

const val TYPE_SCOREBOARD: Short = 101
const val TYPE_NO_HUNGER: Short = 102
const val TYPE_DISABLE_ITEM_COLLECT: Short = 103
const val TYPE_DISABLE_ITEM_DROP: Short = 104
const val TYPE_NO_DAMAGE: Short = 105
const val TYPE_SPECTATE: Short = 106

class ScoreboardComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_SCOREBOARD

    var scoreboard: Scoreboard? = null

    override fun onInit(gameObject: GamePlayer) {
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

    override fun onInit(player: GamePlayer) {
        player.entity.foodLevel = 20
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(player: GamePlayer) {
        FoodLevelChangeEvent.getHandlerList().unregister(this)
    }

}

class DisableItemCollectComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_ITEM_COLLECT

    override fun onInit(player: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onCollect(event: PlayerPickupItemEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(player: GamePlayer) {
        PlayerPickupItemEvent.getHandlerList().unregister(this)
    }

}

class DisableItemDropComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_DISABLE_ITEM_DROP

    override fun onInit(player: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onDrop(event: PlayerDropItemEvent) {
        if (event.player.uniqueId !== gameObject.uniqueId) {
            return
        }

        event.isCancelled = true
    }

    override fun onDestroy(player: GamePlayer) {
        PlayerDropItemEvent.getHandlerList().unregister(this)
    }

}

class NoDamageComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>(), Listener {

    override val type = TYPE_NO_DAMAGE

    override fun onInit(player: GamePlayer) {
        Bukkit.getPluginManager().registerEvents(this, plugin())
    }

    @EventHandler
    private fun onDamage(event: EntityDamageEvent) {
        if (event.entity.uniqueId !== gameObject.uniqueId)
            return

        event.isCancelled = true
    }

    override fun onDestroy(gameObject: GamePlayer) {
        EntityDamageEvent.getHandlerList().unregister(this)
    }

}

class SpectateComponent (
        override val gameObject: GamePlayer
) : GameComponentOf<GamePlayer>() {

    override val type = TYPE_SPECTATE

    override fun onInit(player: GamePlayer) {
        player.entity.gameMode = GameMode.SPECTATOR
    }

    override fun onDestroy(player: GamePlayer) {
        player.entity.gameMode = GameMode.ADVENTURE
    }

}


