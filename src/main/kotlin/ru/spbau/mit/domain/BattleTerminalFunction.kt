package ru.spbau.mit.domain

import burlap.mdp.core.TerminalFunction
import burlap.mdp.core.state.State

class BattleTerminalFunction : TerminalFunction {
    override fun isTerminal(state: State?): Boolean {
        if (state == null) {
            throw NullPointerException("State is null!")
        }

        if (state !is BattleState) {
            throw RuntimeException("State isn\'t BattleState!")
        }

        return state.agent.hp == 0 || state.enemy.hp == 0
    }
}