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

import me.ihdeveloper.thehunters.component.ScoreboardComponent
import me.ihdeveloper.thehunters.component.TargetComponent
import me.ihdeveloper.thehunters.component.TitleComponent
import me.ihdeveloper.thehunters.event.target.TargetJoinEvent
import org.bukkit.Bukkit
import java.util.UUID
import kotlin.random.Random

class Gameplay : GameObject() {

    private var target: UUID? = null

    override fun onInit() {
        Game.lock()

        val random = Random(Game.count + 1)
        var found = false

        for (player in Game.players.values) {
            player.add(ScoreboardComponent(player))
            player.add(TitleComponent(player))

            if (!found && random.nextBoolean()) {
                found = true
                target = player.entity.uniqueId

                player.add(TargetComponent(player))

                continue
            }

            // TODO Add Hunter Component
        }

        Bukkit.getPluginManager().callEvent(TargetJoinEvent(Game.players[target!!]))
    }

    override fun onDestroy() {
    }

}
