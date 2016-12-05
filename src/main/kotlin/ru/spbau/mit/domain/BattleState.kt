package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.OOState
import burlap.mdp.core.oo.state.OOStateUtilities
import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.oo.state.exceptions.UnknownClassException
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException
import burlap.mdp.core.state.State
import burlap.mdp.core.state.annotations.ShallowCopyState


@ShallowCopyState
class BattleState(var agent: BattleAgent,
                  var enemy: BattleEnemy,
                  var agentBullets: MutableList<BattleBullet> = mutableListOf(),
                  var enemyBullets: MutableList<BattleBullet> = mutableListOf()) : OOState {

    constructor() : this(BattleAgent(), BattleEnemy())

    init {
        while (agentBullets.size < BULLETS_COUNT) {
            agentBullets.add(BattleBullet())
        }

        while (enemyBullets.size < BULLETS_COUNT) {
            enemyBullets.add(BattleBullet())
        }
    }

    companion object {
        const val BULLETS_COUNT = 2
    }

    override fun numObjects(): Int = 2 * (BULLETS_COUNT + 1)

    override fun `object`(objectName: String): ObjectInstance {
        return when (objectName) {
            agent.name -> agent
            enemy.name -> enemy
            else -> {
                getBullet(agentBullets, objectName)?.let { return it }
                getBullet(enemyBullets, objectName)?.let { return it }
                throw UnknownObjectException(objectName)
            }
        }
    }

    override fun objects(): MutableList<ObjectInstance> {
        return listOf(listOf(agent, enemy), agentBullets, enemyBullets).flatten().toMutableList()
    }

    override fun objectsOfClass(className: String?): MutableList<ObjectInstance> {
        return when (className) {
            BattleAgent.CLASS -> mutableListOf(agent)
            BattleAgent.CLASS_ENEMY -> mutableListOf(enemy)
            BattleBullet.CLASS -> listOf(agentBullets, enemyBullets).flatten().toMutableList()
            else -> throw UnknownClassException(className)
        }
    }

    override fun variableKeys(): MutableList<Any> = OOStateUtilities.flatStateKeys(this)

    override fun get(variableKey: Any?): Any = OOStateUtilities.get(this, variableKey)

    override fun copy(): State = BattleState(agent, enemy, agentBullets, enemyBullets)

    override fun toString(): String = OOStateUtilities.ooStateToString(this)

    fun touchAgentBullets(): MutableList<BattleBullet> {
        agentBullets = agentBullets.map { it.copy() }.toMutableList()
        return agentBullets
    }

    fun touchEnemyBullets(): MutableList<BattleBullet> {
        enemyBullets = enemyBullets.map { it.copy() }.toMutableList()
        return enemyBullets
    }

    fun getBulletsFor(agent: BattleAgent): MutableList<BattleBullet> {
        if (agent.className() == BattleAgent.CLASS) {
            return agentBullets
        }

        return enemyBullets
    }

    fun touchAgent(): BattleAgent {
        agent = agent.copy()
        return agent
    }

    fun touchEnemy(): BattleEnemy {
        enemy = enemy.copy()
        return enemy
    }

    private fun getBullet(bullets: List<BattleBullet>, name: String): BattleBullet? {
        return bullets.getOrNull(OOStateUtilities.objectIndexWithName(bullets, name))
    }
}
