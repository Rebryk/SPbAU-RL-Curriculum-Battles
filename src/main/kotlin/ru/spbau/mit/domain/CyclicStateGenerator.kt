package ru.spbau.mit.domain

import burlap.mdp.auxiliary.StateGenerator
import burlap.mdp.core.state.State

class CyclicStateGenerator : StateGenerator {
    private val states: MutableList<State> = mutableListOf()

    fun addState(state: State): CyclicStateGenerator {
        states.add(state)
        return this
    }

    override fun generateState(): State {
        if (states.isEmpty()) {
            throw RuntimeException("CyclicStateGenerator hasn\'t any states!")
        }

        val state = states.removeAt(0)
        states.add(state)
        return state.copy()
    }
}
