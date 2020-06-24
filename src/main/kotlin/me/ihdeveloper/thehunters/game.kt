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

import net.minecraft.server.v1_8_R3.DedicatedServer
import org.bukkit.Bukkit
import java.util.UUID

private val playersManager = PlayersManager()
private val loginManager = LoginManager()
private val lobby = Lobby()
private val gameplay = Gameplay()

private const val name = "TheHunters"
private val components = listOf<GameComponent>()
private val children = listOf(
        loginManager,
        WorldsManager(),
        playersManager,
        lobby
)

class Game : GameInstance (name, components, children) {

        init {
               // Disable announcing any achievements since we are going to override them
               DedicatedServer.getServer().propertyManager.setProperty("announce-player-achievements", false)

               instance = this
        }

        companion object {
                lateinit var instance: Game

                val players: Map<UUID, GamePlayer> get() = playersManager.players
                val count: Int get() = playersManager.count
                val max: Int get() = Bukkit.getMaxPlayers()

                fun start() {
                        lobby.destroy()
                        instance.add(gameplay)
                }

                fun lock() {
                        loginManager.lock = true
                }
        }

}
