package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattlePhysicsParameters

interface BattleBot {
    fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String
}
