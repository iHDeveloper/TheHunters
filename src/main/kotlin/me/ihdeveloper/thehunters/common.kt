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

import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin

interface GameComponent {
    val type: Short

    fun init()
    fun destroy()
}

abstract class GameComponentOf<T : GameObject> : GameComponent {
    abstract val gameObject: T

    abstract override val type: Short

    override fun init() = onInit(gameObject)
    override fun destroy() = onDestroy(gameObject)

    abstract fun onInit(gameObject: T)
    abstract fun onDestroy(gameObject: T)
}

open class GameObject (
        components: List<GameComponent> = listOf<GameComponent>(),
        children: List<GameObject> = listOf<GameObject>()
) {
    var parent: GameObject? = null

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

    fun has(type: Short) = components.containsKey(type)

    fun remove(type: Short) {
        components[type]!!.destroy()
        components.remove(type)
    }

    fun <T : GameObject> get(index: Int) : T {
        return children[index] as T
    }

    fun add(child: GameObject) {
        children.add(child)
        child.parent = this

        if (initialized)
            child.init()
    }

    fun removeAllChildren() {
        for (child in children) {
            child.parent = null
        }

        children.clear()
    }

    fun reset() {
        for (component in components.values) {
            component.destroy()
        }

        removeAllChildren()

        components.clear()
    }

    fun init() {
        if (initialized)
            return

        onInit()

        for (component in components.values)
            component.init()

        for (child in children)
            child.init()

        initialized = true
    }

    fun destroy() {
        if (!initialized)
            return

        onDestroy()

        for (component in components.values)
            component.destroy()

        components.clear()

        for (child in children)
            child.destroy()

        children.clear()

        if (parent != null)
            parent = null

        initialized = false
    }

    open fun onInit() {}
    open fun onDestroy() {}
}

open class GameEntity<T : Entity> (
        val entity: T,
        components: List<GameComponent> = listOf<GameComponent>(),
        children: List<GameObject> = listOf<GameObject>()
) : GameObject(
        components,
        children
) {

    override fun onInit() {
        onInit(entity)
    }

    override fun onDestroy() {
        onDestroy(entity)

        entity.remove()
    }

    open fun onInit(entity: T) {}
    open fun onDestroy(entity: T) {}
}

open class GameInstance (
        name: String,
        val components: List<GameComponent>,
        val children: List<GameObject>
) : GameObject(
        components = components,
        children = children
) {
    protected val logger = GameLogger(name)

    override fun onInit() {
        logger.info("Initializing...")
    }

    override fun onDestroy() {
        logger.info("Destroying...")
    }

}

open class GameEntryPoint<T : GameInstance> (
        private val instance: T
) : JavaPlugin() {

    override fun onEnable() {
        instance.init()
    }

    override fun onDisable() {
        instance.destroy()
    }

}
