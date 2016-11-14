package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattlePhysicsParameters

interface BattleBot {
    /**
     * Predicts next action to perform
     * @param state current state of the environment
     * @param physicsParameters physics parameters of the environment
     * @return action name
     */
    fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String
}
