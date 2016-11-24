package ru.spbau.mit.domain

import burlap.mdp.core.action.Action
import burlap.mdp.core.oo.OODomain
import burlap.mdp.core.oo.propositional.PropositionalFunction
import burlap.mdp.core.oo.state.OOState
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.RewardFunction

class BattleRewardFunction(domain: OODomain) : RewardFunction {
    private val goalReward: Double = 1000.0
    private val defaultReward: Double = -1.0

    private val didFinish: PropositionalFunction = domain.propFunction(BattleDomain.ENEMY_IS_DEAD)

    override fun reward(state: State?, action: Action?, newState: State?): Double {
        if (state == null) {
            throw NullPointerException("State is null!")
        }

        if (action == null) {
            throw NullPointerException("Action is null!")
        }

        if (newState == null) {
            throw NullPointerException("NewState is null!")
        }

        if (didFinish.someGroundingIsTrue(newState as OOState)) {
            return goalReward
        }

        val x1 = state.get("agent:X") as Double
        val y1 = state.get("agent:Y") as Double
        val x2 = state.get("enemy:X") as Double
        val y2 = state.get("enemy:Y") as Double
        val distance = Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0)

        val newX1 = newState.get("agent:X") as Double
        val newY1 = newState.get("agent:Y") as Double
        val newX2 = newState.get("enemy:X") as Double
        val newY2 = newState.get("enemy:Y") as Double
        val newDistance = Math.pow(newX1 - newX2, 2.0) + Math.pow(newY1 - newY2, 2.0)

        return defaultReward + if (newDistance < distance) 2 else -1
    }
}
