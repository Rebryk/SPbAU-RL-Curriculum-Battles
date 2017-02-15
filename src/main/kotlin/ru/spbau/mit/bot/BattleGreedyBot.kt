package ru.spbau.mit.bot

import burlap.mdp.core.state.State
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattleBullet
import ru.spbau.mit.domain.BattleModel.Companion.hitsTarget
import ru.spbau.mit.domain.BattleModel.Companion.move
import ru.spbau.mit.domain.BattlePhysicsParameters
import ru.spbau.mit.domain.BattleState
import java.awt.geom.Line2D
import java.awt.geom.Point2D

class BattleGreedyBot : BattleBot {
    companion object {
        private const val MIN_ANGLE_DELTA = Math.PI / 12 + Math.PI / 50
        private const val VIEW_RANGE = 100
        private const val BULLET_VIEW_DISTANCE = 6
    }

    private fun getBulletTrajectory(bullet: BattleBullet): Line2D.Double {
        val angle = Math.atan2(bullet.speedY, bullet.speedX)
        val x = Math.cos(angle) * BULLET_VIEW_DISTANCE
        val y = Math.sin(angle) * BULLET_VIEW_DISTANCE
        return Line2D.Double(bullet.x, bullet.y, bullet.x + x, bullet.y + y)
    }

    private fun getDangerBullet(state: BattleState, physicsParameters: BattlePhysicsParameters): BattleBullet? {
        return state.agentBullets.filter {
            hitsTarget(getBulletTrajectory(it), Point2D.Double(state.enemy.x, state.enemy.y), physicsParameters)
        }.firstOrNull()
    }

    private fun moveBot(agent: BattleAgent, actionName: String, physicsParameters: BattlePhysicsParameters) {
        when (actionName) {
            BattleAgent.Companion.Action.GO_FORWARD -> move(agent, agent.angle, physicsParameters, 1.2)
            BattleAgent.Companion.Action.GO_BACKWARD -> move(agent, agent.angle + Math.PI, physicsParameters)
            BattleAgent.Companion.Action.GO_LEFT -> move(agent, agent.angle + Math.PI / 2.0, physicsParameters)
            BattleAgent.Companion.Action.GO_RIGHT -> move(agent, agent.angle - Math.PI / 2.0, physicsParameters)
        }
    }

    private fun tryToAvoidBullet(bullet: BattleBullet, state: BattleState, physicsParameters: BattlePhysicsParameters): String {
        val trajectory = getBulletTrajectory(bullet)

        return listOf(BattleAgent.Companion.Action.GO_FORWARD,
                BattleAgent.Companion.Action.GO_BACKWARD,
                BattleAgent.Companion.Action.GO_LEFT,
                BattleAgent.Companion.Action.GO_RIGHT,
                BattleAgent.Companion.Action.SKIP)
                .map {
                    val bot = state.enemy.copy()
                    moveBot(bot, it, physicsParameters)
                    Pair(it, trajectory.ptSegDistSq(bot.x, bot.y))
                }
                .sortedByDescending {
                    it.second
                }
                .first()
                .first
    }

    private fun reduceAngleDelta(angle: Double): String {
        if (angle > 0) {
            return if (angle < Math.PI) BattleAgent.Companion.Action.TURN_LEFT else BattleAgent.Companion.Action.TURN_RIGHT
        }

        return if (Math.abs(angle) < Math.PI) BattleAgent.Companion.Action.TURN_RIGHT else BattleAgent.Companion.Action.TURN_LEFT
    }

    private fun getAngleToAgent(state: BattleState): Double {
        return Math.atan2(state.agent.y - state.enemy.y, state.agent.x - state.enemy.x)
    }

    private fun getAngleDelta(state: BattleState): Double {
        return getAngleToAgent(state) - state.enemy.angle
    }

    private fun getDistanceTo(state: BattleState, x: Double, y: Double): Double {
        return Math.sqrt(Math.pow(y - state.enemy.y, 2.0) + Math.pow(x - state.enemy.x, 2.0))
    }

    private fun getDistanceToAgent(state: BattleState): Double {
        return getDistanceTo(state, state.agent.x, state.agent.y)
    }

    override fun nextAction(state: State, physicsParameters: BattlePhysicsParameters): String {
        if (state !is BattleState) {
            throw IllegalStateException("Illegal state type!")
        }

        getDangerBullet(state, physicsParameters)?.let {
            return tryToAvoidBullet(it, state, physicsParameters)
        }

        val angleDelta = getAngleDelta(state)
        if (Math.abs(angleDelta) >= MIN_ANGLE_DELTA) {
            return reduceAngleDelta(angleDelta)
        }

        val distanceToAgent = getDistanceToAgent(state)
        if (distanceToAgent < VIEW_RANGE) {
            if (state.enemy.cooldown == 0) {
                return BattleAgent.Companion.Action.SHOOT
            }
        }

        return BattleAgent.Companion.Action.SKIP
    }
}