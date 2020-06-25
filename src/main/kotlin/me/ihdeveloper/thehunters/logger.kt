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

import me.ihdeveloper.thehunters.util.COLOR_AQUA
import me.ihdeveloper.thehunters.util.COLOR_GOLD
import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_RED
import me.ihdeveloper.thehunters.util.COLOR_YELLOW
import org.bukkit.Bukkit
import org.bukkit.ChatColor

class GameLogger (
        private val name: String
) {

    fun info(message: String) = print(COLOR_AQUA, "INFO", message)
    fun warning(message: String) = print(COLOR_YELLOW, "WARN", message)
    fun error(message: String) = print(COLOR_RED, "ERR", message)
    fun debug(message: String) = print(COLOR_GOLD, "DEBUG", message)

    private fun print(color: ChatColor, prefix: String, message: String) {
        val builder = StringBuilder().run {
            append("[${name}] ")
            append("[")
            append("$color$prefix${COLOR_GRAY}")
            append("] ")
            append("$color")
            append(message)
        }

        Bukkit.getConsoleSender().sendMessage(builder.toString())
    }

}
