package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.MutableOOState
import burlap.mdp.core.oo.state.OOStateUtilities
import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.oo.state.exceptions.UnknownClassException
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException
import burlap.mdp.core.state.MutableState
import burlap.mdp.core.state.State
import burlap.mdp.core.state.annotations.ShallowCopyState
import java.util.*

@ShallowCopyState
class BattleState(private var agent: BattleAgent,
                  private var enemy: BattleEnemy,
                  private var bullets: MutableList<BattleBullet>) : MutableOOState {

    companion object {
        const val BULLETS_COUNT = 2
    }

    override fun addObject(obj: ObjectInstance?): MutableOOState {
        when (obj) {
            is BattleAgent -> agent = obj
            is BattleEnemy -> enemy = obj
            is BattleBullet -> touchBullets().add(obj)
            null -> throw NullPointerException("Object must be not null!")
            else -> throw UnknownClassException(obj.className())
        }

        return this
    }

    override fun removeObject(objectName: String?): MutableOOState {
        when (objectName) {
            agent.name -> agent = agent.copy()
            enemy.name -> enemy = enemy.copy()
            else -> {
                val index = OOStateUtilities.objectIndexWithName(bullets, objectName)
                if (index != -1) {
                    touchBullets().removeAt(index)
                } else {
                    throw UnknownObjectException(objectName)
                }
            }
        }

        return this
    }

    override fun renameObject(objectName: String?, newName: String?): MutableOOState {
        when (objectName) {
            agent.name -> throw RuntimeException("You can't rename agent!")
            enemy.name -> throw RuntimeException("You can't rename enemy agent!")
            else -> {
                val index = OOStateUtilities.objectIndexWithName(bullets, objectName)
                if (index != -1) {
                    val bullet = bullets[index]
                    touchBullets().removeAt(index)
                    bullets.add(index, bullet.copyWithName(newName) as BattleBullet)
                } else {
                    throw UnknownObjectException(objectName)
                }
            }
        }

        throw UnsupportedOperationException("not implemented")
    }

    override fun numObjects(): Int = 2 + bullets.size

    override fun `object`(objectName: String?): ObjectInstance {
        return when (objectName) {
            agent.name -> agent
            enemy.name -> enemy
            else -> {
                val index = OOStateUtilities.objectIndexWithName(bullets, objectName)
                if (index != -1) {
                    bullets[index]
                } else {
                    throw UnknownObjectException(objectName)
                }
            }
        }
    }

    override fun objects(): MutableList<ObjectInstance> {
        val objs = ArrayList<ObjectInstance>(numObjects())
        objs.add(agent)
        objs.add(enemy)
        objs.addAll(bullets)
        return objs
    }

    override fun objectsOfClass(className: String?): MutableList<ObjectInstance> {
        return when (className) {
            BattleAgent.CLASS -> mutableListOf(agent)
            BattleAgent.CLASS_ENEMY -> mutableListOf(enemy)
            BattleBullet.CLASS -> ArrayList<ObjectInstance>(bullets)
            else -> throw UnknownClassException(className)
        }
    }

    override fun set(variableKey: Any?, variableValue: Any?): MutableState {
        val key = OOStateUtilities.generateKey(variableKey)

        when (key.obName) {
            agent.name -> touchAgent().set(key.obVarKey, variableValue)
            enemy.name -> touchEnemy().set(key.obVarKey, variableValue)
            else -> {
                val index = OOStateUtilities.objectIndexWithName(bullets, key.obName)
                if (index != -1) {
                    touchBullet(index).set(key.obVarKey, variableValue)
                } else {
                    throw UnknownObjectException(key.obName)
                }
            }
        }

        throw UnsupportedOperationException("not implemented")
    }

    override fun variableKeys(): MutableList<Any> = OOStateUtilities.flatStateKeys(this)

    override fun get(variableKey: Any?): Any = OOStateUtilities.get(this, variableKey)

    override fun copy(): State = BattleState(agent, enemy, bullets)

    override fun toString(): String = OOStateUtilities.ooStateToString(this)

    fun touchBullets(): MutableList<BattleBullet> {
        bullets = ArrayList<BattleBullet>(bullets)
        return bullets
    }

    fun touchBullet(index: Int): BattleBullet {
        val bullet = bullets[index].copy()
        touchBullets().removeAt(index)
        touchBullets().add(index, bullet)
        return bullet
    }

    fun touchAgent() : BattleAgent {
        agent = agent.copy()
        return agent
    }

    fun touchEnemy() : BattleEnemy {
        enemy = enemy.copy()
        return enemy
    }
}
