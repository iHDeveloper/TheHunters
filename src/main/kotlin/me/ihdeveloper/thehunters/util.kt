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

import me.ihdeveloper.thehunters.util.COLOR_GRAY
import me.ihdeveloper.thehunters.util.COLOR_GREEN
import me.ihdeveloper.thehunters.util.COLOR_PURPLE
import me.ihdeveloper.thehunters.util.COLOR_RED

enum class Dimension (
        val displayName: String,
        private val checker: (String) -> Boolean
) {
    UNKNOWN("${COLOR_GRAY}Unknown", { false }),
    WORLD("${COLOR_GREEN}World", { it == "world" }),
    NETHER("${COLOR_RED}Nether", { it == "world_nether" }),
    THE_END("${COLOR_PURPLE}The End", { it == "world_the_end" });

    companion object {

        fun get(name: String): Dimension {
            return when (THE_END.check(name)) {
                true -> THE_END
                else -> when (NETHER.check(name)) {
                    true -> NETHER
                    else -> when (WORLD.check(name)) {
                        true -> WORLD
                        else -> UNKNOWN
                    }
                }
            }
        }

    }

    fun check(name: String) = checker(name)
}
