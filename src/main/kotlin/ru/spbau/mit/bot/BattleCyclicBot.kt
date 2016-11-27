package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattlePhysicsParameters

class BattleCyclicBot : BattleBot {
    val actions: MutableList<String> = mutableListOf()

    fun addAction(action: String): BattleCyclicBot {
        actions.add(action)
        return this
    }

    fun addAction(action: String, count: Int): BattleCyclicBot {
        (1..count).forEach { actions.add(action) }
        return this
    }

    override fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String {
        if (actions.isEmpty()) {
            return BattleAgent.Companion.Action.SKIP
        }

        val action = actions.removeAt(0)
        actions.add(action)
        return action
    }
}