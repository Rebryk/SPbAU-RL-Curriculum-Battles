package ru.spbau.mit.domain

import burlap.mdp.core.StateTransitionProb
import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.statemodel.FullStateModel

class BattleModel : FullStateModel {
    val physicsParameters: BattlePhysicsParameters

    constructor(physicsParameters: BattlePhysicsParameters) {
        this.physicsParameters = physicsParameters
    }

    override fun stateTransitions(state: State?, action: Action?): MutableList<StateTransitionProb> {
        return FullStateModel.Helper.deterministicTransition(this, state, action)
    }

    override fun sample(state: State?, action: Action?): State {
        if (state == null || action == null) {
            throw RuntimeException("State and action have to be not null!")
        }

        // TODO: implement

        throw UnsupportedOperationException("not implemented")
    }
}