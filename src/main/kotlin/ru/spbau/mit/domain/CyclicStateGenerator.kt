package ru.spbau.mit.domain

import burlap.mdp.auxiliary.StateGenerator
import burlap.mdp.core.state.State

class CyclicStateGenerator(private val model: BattleModel) : StateGenerator {
    private val states: MutableList<State> = mutableListOf()

    fun addState(state: State): CyclicStateGenerator {
        states.add(state)
        return this
    }

    override fun generateState(): State {
        if (states.isEmpty()) {
            throw RuntimeException("CyclicStateGenerator hasn\'t any states!")
        }

        model.nextStep()

        val state = states.removeAt(0)
        states.add(state)
        return state.copy()
    }
}
