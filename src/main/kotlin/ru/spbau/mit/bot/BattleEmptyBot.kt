package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattlePhysicsParameters

/**
 * Simple bot which doesn't do anything
 */
class BattleEmptyBot : BattleBot {
    override fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String {
        return BattleAgent.Companion.Action.SKIP
    }
}
