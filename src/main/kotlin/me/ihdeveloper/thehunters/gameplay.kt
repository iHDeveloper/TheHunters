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

import me.ihdeveloper.thehunters.component.CountdownComponent
import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TYPE_COUNTDOWN
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.component.VanishComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterScoreboardComponent
import me.ihdeveloper.thehunters.component.gameplay.HunterSignalComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetDimensionComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetGetReadyComponent
import me.ihdeveloper.thehunters.component.gameplay.TargetScoreboardComponent
import me.ihdeveloper.thehunters.event.hunter.HunterJoinEvent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_GET_READY
import me.ihdeveloper.thehunters.util.COUNTDOWN_GAMEPLAY_INTRO
import org.bukkit.Bukkit
import java.util.UUID
import kotlin.random.Random

class Gameplay : GameObject() {

    companion object {
        lateinit var instance: Gameplay

        val hunters: Int get() = instance.hunters
    }

    private var target: UUID? = null
    private var hunters: Int = 0

    private val countdown = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_GET_READY,
            defaultStart = 20 * 60
    )

    private val intro = CountdownComponent(
            id = COUNTDOWN_GAMEPLAY_INTRO,
            defaultStart = 20 * 5,
            onFinish = {
                remove(TYPE_COUNTDOWN)
                add(countdown)
                countdown.start()
            }
    )

    init {
        add(intro)

        instance = this
    }

    override fun onInit() {
        Game.lock()

        val random = Random(Game.count + 1)
        var found = false

        for (player in Game.players.values) {
            player.add(ScoreboardComponent(player))
            player.add(TitleComponent(player))
            player.add(VanishComponent(player))

            if (!found && random.nextBoolean()) {
                found = true
                target = player.entity.uniqueId

                player.add(TargetComponent(player))
                player.add(TargetGetReadyComponent(player))
                player.add(TargetDimensionComponent(player))
                player.add(TargetScoreboardComponent(player))

                continue
            }

            hunters++
            player.add(HunterComponent(player))
            player.add(HunterScoreboardComponent(player))
            player.add(HunterSignalComponent(player))
        }

        Bukkit.getPluginManager().callEvent(TargetJoinEvent(Game.players[target!!]))

        for (player in Game.players.values) {
            if (player.uniqueId === target)
                continue

            Bukkit.getPluginManager().callEvent(HunterJoinEvent(player))
        }

        intro.start()
    }

    override fun onDestroy() {
        intro.stop()
        countdown.stop()
    }

}
