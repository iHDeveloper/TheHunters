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

import me.ihdeveloper.thehunters.component.AdventureComponent
import me.ihdeveloper.thehunters.component.ClearInventoryComponent
import me.ihdeveloper.thehunters.component.ClearPotionEffectComponent
import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.DefeatedComponent
import me.ihdeveloper.thehunters.component.DisableBlockBreakComponent
import me.ihdeveloper.thehunters.component.DisableBlockPlaceComponent
import me.ihdeveloper.thehunters.component.DisableItemCollectComponent
import me.ihdeveloper.thehunters.component.DisableItemDropComponent
import me.ihdeveloper.thehunters.component.FlyComponent
import me.ihdeveloper.thehunters.component.NoDamageComponent
import me.ihdeveloper.thehunters.component.NoHungerComponent
import me.ihdeveloper.thehunters.component.NoInteractComponent
import me.ihdeveloper.thehunters.component.ResetHealthComponent
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VictoryComponent
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_HUNTER_SCOREBOARD
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_TARGET
import me.ihdeveloper.thehunters.component.gameplay.TYPE_GAMEPLAY_TARGET_SCOREBOARD
import me.ihdeveloper.thehunters.util.COUNTDOWN_RESTARTING

class TheEnd (
        val won: Boolean = false
) : GameObject() {

    private val countdown = CountdownComponent(
            id = COUNTDOWN_RESTARTING,
            defaultStart = 20 * 60,
            onFinish = {
                // TODO Restart the game
            }
    )

    override fun onInit() {
        Game.lock()

        Game.players.values.forEach {
            val target = it.has(TYPE_GAMEPLAY_TARGET)

            if (target)
                resetTarget(it)
            else
                resetHunter(it)

            it.run {
                add(TitleComponent(this))
                add(FlyComponent(this))
                add(AdventureComponent(this))
                add(NoHungerComponent(this))
                add(DisableItemCollectComponent(this))
                add(DisableItemDropComponent(this))
                add(NoDamageComponent(this))
                add(DisableBlockPlaceComponent(this))
                add(DisableBlockBreakComponent(this))
                add(NoInteractComponent(this))
                add(ClearInventoryComponent(this))
                add(ClearPotionEffectComponent(this))
                add(ResetHealthComponent(this))

                if (won) {
                    if (target)
                        it.add(DefeatedComponent(true, this))
                    else
                        it.add(VictoryComponent(false, this))
                } else {
                    if (target)
                        it.add(VictoryComponent(true, this))
                    else
                        it.add(DefeatedComponent(false, this))
                }
            }
        }

        countdown.start()
    }

    private fun resetTarget(player: GamePlayer) {
        player.run {
            reset(filter = {
                it.type == TYPE_GAMEPLAY_TARGET_SCOREBOARD
            })
        }
    }

    private fun resetHunter(player: GamePlayer) {
        player.run {
            reset(filter = {
                it.type == TYPE_GAMEPLAY_HUNTER_SCOREBOARD
            })
        }
    }

}
