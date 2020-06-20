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

package me.ihdeveloper.thehunters.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class CountdownEvent (
        open val id: Byte
) : Event() {

    companion object {
        @JvmStatic private lateinit var handlerList: HandlerList

        @JvmStatic fun getHandlerList() = handlerList
    }

    init {
        handlerList = HandlerList()
    }

    override fun getHandlers() = handlerList

}

class CountdownTickEvent (
        override val id: Byte,
        val tick: Int
): CountdownEvent (id)

class CountdownStartEvent (
        override val id: Byte
) : CountdownEvent (id)

class CountdownFinishEvent (
        override val id: Byte
) : CountdownEvent (id)

class CountdownCancelEvent (
        override val id: Byte
) : CountdownEvent (id)

