package ru.spbau.mit.domain

import burlap.mdp.core.action.Action
import burlap.mdp.core.oo.OODomain
import burlap.mdp.core.oo.propositional.PropositionalFunction
import burlap.mdp.core.oo.state.OOState
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.RewardFunction

class BattleRewardFunction(domain: OODomain) : RewardFunction {
    companion object {
        private val SHOT_MAX_DELTA_ANGLE: Double = Math.PI / 12.0

        /**
         * Reward for different actions
         */
        private val DEFAULT_REWARD: Double = -1.0
        private val ANGLE_DELTA_REWARD: Double = 50.0 / Math.PI
        private val DISTANCE_DELTA_REWARD: Double = 0.2
        private val ENEMY_IS_DEAD_REWARD: Double = 1000.0
        private val ENEMY_INJURED_REWARD: Double = 150.0
        private val AGENT_INJURED_REWARD: Double = -100.0
        private val INACCURATE_SHOT_REWARD: Double = -20.0
        private val TOUCHING_OBSTACLE_REWARD: Double = -50.0
    }

    private val didFinish: PropositionalFunction = domain.propFunction(BattleDomain.ENEMY_IS_DEAD)

    private fun touchingObstacle(state: BattleState, newState: BattleState): Boolean {
        return state.agent.x == newState.agent.x && state.agent.y == newState.agent.y
    }

    private fun getAngle(from: BattleAgent, to: BattleAgent): Double {
        val angle = Math.atan2(to.y - from.y, to.x - from.x)
        val delta = Math.abs(angle - from.angle)
        return Math.min(delta, 2 * Math.PI - delta)
    }

    private fun inaccurateShot(state: BattleState): Boolean {
        return getAngle(state.agent, state.enemy) > SHOT_MAX_DELTA_ANGLE
    }

    private fun agentInjured(state: BattleState, newState: BattleState): Boolean = newState.agent.hp < state.agent.hp

    private fun enemyInjured(state: BattleState, newState: BattleState): Boolean = newState.enemy.hp < state.enemy.hp

    private fun getDistanceDelta(state: BattleState, newState: BattleState): Double {
        val distance = Math.sqrt(Math.pow(state.enemy.x - state.agent.x, 2.0) + Math.pow(state.enemy.y - state.agent.y, 2.0))
        val newDistance = Math.sqrt(Math.pow(newState.enemy.x - newState.agent.x, 2.0) + Math.pow(newState.enemy.y - newState.agent.y, 2.0))
        return newDistance - distance
    }

    private fun getAngleDelta(state: BattleState, newState: BattleState): Double {
        return getAngle(newState.agent, newState.enemy) - getAngle(state.agent, state.enemy)
    }

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

        if (state !is BattleState) {
            throw RuntimeException("State isn\'t BattleState!")
        }

        if (newState !is BattleState) {
            throw RuntimeException("State isn\'t BattleState!")
        }

        if (didFinish.someGroundingIsTrue(newState as OOState)) {
            return ENEMY_IS_DEAD_REWARD
        }

        var reward = DEFAULT_REWARD

        if (BattleAgent.Companion.Action.isMoving(action.actionName()) && touchingObstacle(state, newState)) {
            reward += TOUCHING_OBSTACLE_REWARD
        }

        if (BattleAgent.Companion.Action.isShooting(action.actionName()) && inaccurateShot(newState)) {
            reward += INACCURATE_SHOT_REWARD
        }

        if (agentInjured(state, newState)) {
            reward += AGENT_INJURED_REWARD
        }

        if (enemyInjured(state, newState)) {
            reward += ENEMY_INJURED_REWARD
        }

        reward -= getAngleDelta(state, newState) * ANGLE_DELTA_REWARD
        reward -= getDistanceDelta(state, newState) * DISTANCE_DELTA_REWARD

        return reward
    }
}
