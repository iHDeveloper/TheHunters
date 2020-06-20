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

interface GameComponent {
    val type: Short

    fun init()
    fun destroy()
}

abstract class GameComponentOf<T : GameObject> (
    val gameObject: T
) : GameComponent {

    abstract override val type: Short

    override fun init() = onInit(gameObject)
    override fun destroy() = onDestroy(gameObject)

    abstract fun onInit(gameObject: T)
    abstract fun onDestroy(gameObject: T)
}

open class GameObject (
        components: List<GameComponent>,
        children: List<GameObject>
) {
    private val components = mutableMapOf<Short, GameComponent>()
    private val children = mutableListOf<GameObject>()

    private var initialized = false

    init {
        for (component in components)
            add(component)

        for (child in children)
            add(child)
    }

    fun <T : GameComponent> get(type: Short) : T {
        return components[type] as T
    }

    fun add(component: GameComponent) {
        components[component.type] = component

        if (initialized)
            component.init()
    }

    fun remove(type: Short) {
        components.remove(type)
    }

    fun <T : GameObject> get(index: Int) : T {
        return children[index] as T
    }

    fun add(child: GameObject) {
        children.add(child)

        if (initialized)
            child.init()
    }

    fun removeAllChildren() {
        children.clear()
    }

    fun init() {
        onInit()

        for (component in components.values)
            component.init()

        for (child in children)
            child.init()

        initialized = true
    }

    fun destroy() {
        onDestroy()

        for (component in components.values)
            component.destroy()

        components.clear()

        for (child in children)
            child.destroy()

        children.clear()

        initialized = false
    }

    open fun onInit() {}
    open fun onDestroy() {}
}
