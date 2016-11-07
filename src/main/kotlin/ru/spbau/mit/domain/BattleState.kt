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
class BattleState : MutableOOState {
    private var agent: BattleAgent
    private var enemy: BattleEnemy
    private var bullets: MutableList<BattleBullet>

    constructor(agent: BattleAgent, enemy: BattleEnemy, bullets: MutableList<BattleBullet>) {
        this.agent = agent
        this.enemy = enemy
        this.bullets = bullets
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
        when (objectName.toString()) {
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
        // TODO: implement
        throw UnsupportedOperationException("not implemented")
    }

    override fun numObjects(): Int = 2 + bullets.size

    override fun `object`(objectName: String?): ObjectInstance {
        return when (objectName.toString()) {
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
        return when (className.toString()) {
            BattleAgent.Static.CLASS -> mutableListOf(agent)
            BattleAgent.Static.CLASS_ENEMY -> mutableListOf(enemy)
            BattleBullet.Static.CLASS -> ArrayList<ObjectInstance>(bullets)
            else -> throw UnknownClassException(className)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        // TODO: implement
        throw UnsupportedOperationException("not implemented")
    }

    override fun variableKeys(): MutableList<Any> = OOStateUtilities.flatStateKeys(this)

    override fun get(variableKey: Any?): Any = OOStateUtilities.get(this, variableKey)

    override fun copy(): State = BattleState(agent, enemy, bullets)

    override fun toString(): String = OOStateUtilities.ooStateToString(this)

    private fun touchBullets(): MutableList<BattleBullet> {
        bullets = ArrayList<BattleBullet>(bullets)
        return bullets
    }
}
