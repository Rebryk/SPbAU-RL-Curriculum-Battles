package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattlePhysicsParameters

class BattleBotEmpty : BattleBot {
    override fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String {
        return BattleAgent.Companion.Action.SKIP
    }
}
