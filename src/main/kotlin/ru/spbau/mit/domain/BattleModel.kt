package ru.spbau.mit.domain

import burlap.mdp.core.StateTransitionProb
import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.statemodel.FullStateModel
import ru.spbau.mit.bot.BattleBot
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

class BattleModel(val physicsParameters: BattlePhysicsParameters, val bot: BattleBot) : FullStateModel {
    override fun stateTransitions(state: State?, action: Action?): MutableList<StateTransitionProb> {
        return FullStateModel.Helper.deterministicTransition(this, state, action)
    }

    override fun sample(state: State?, action: Action?): State {
        if (state == null || action == null) {
            throw RuntimeException("State and action have to be not null!")
        }

        val newState = state.copy() as BattleState

        val agent = newState.touchAgent()
        val enemy = newState.touchEnemy()
        val bullets = newState.touchBullets()

        performAction(agent, action.actionName(), bullets)
        performAction(enemy, bot.nextAction(state, physicsParameters), bullets)

        agent.cooldown = Math.max(0, agent.cooldown - 1)
        enemy.cooldown = Math.max(0, enemy.cooldown - 1)

        processBullets(newState.touchBullets(), agent, enemy)

        return newState
    }

    /**
     * Applies agent action to the current state
     * @param agent agent which performs action
     * @param actionName name of the action
     */
    private fun performAction(agent: BattleAgent, actionName: String, bullets: MutableList<BattleBullet>) {
        when (actionName) {
            BattleAgent.Companion.Action.TURN_LEFT      -> rotate(agent, physicsParameters.agent.rotationAngle)
            BattleAgent.Companion.Action.TURN_RIGHT     -> rotate(agent, -physicsParameters.agent.rotationAngle)
            BattleAgent.Companion.Action.GO_FORWARD     -> move(agent, agent.angle + Math.PI / 2, 1.2)
            BattleAgent.Companion.Action.GO_BACKWARD    -> move(agent, agent.angle - Math.PI / 2)
            BattleAgent.Companion.Action.GO_LEFT        -> move(agent, agent.angle + Math.PI)
            BattleAgent.Companion.Action.GO_RIGHT       -> move(agent, agent.angle)
            BattleAgent.Companion.Action.SKIP           -> { /* just skip */ }
            BattleAgent.Companion.Action.SHOOT          -> shoot(agent, bullets)
            else -> throw UnsupportedOperationException("Action %s isn't implemented!".format(actionName))
        }
    }

    /**
     * Adds new bullet to the game
     * @param agent agent which shoots
     * @param bullets list of all bullets in the state
     */
    private fun shoot(agent: BattleAgent, bullets: MutableList<BattleBullet>) {
        if (agent.cooldown > 0) {
            return
        }

        agent.cooldown = physicsParameters.agent.cooldown

        val speedX = Math.cos(agent.angle + Math.PI / 2.0) * physicsParameters.bullet.speed
        val speedY = Math.sin(agent.angle + Math.PI / 2.0) * physicsParameters.bullet.speed
        val accelerationX = Math.cos(agent.angle + Math.PI / 2.0) * physicsParameters.bullet.acceleration
        val accelerationY = Math.sin(agent.angle + Math.PI / 2.0) * physicsParameters.bullet.acceleration
        val enemy = agent.className() == BattleAgent.CLASS_ENEMY

        val bullet = BattleBullet(agent.x, agent.y, speedX, speedY, accelerationX, accelerationY, physicsParameters.bullet.damage, enemy, "bullet")
        addBullet(bullets, bullet)
    }

    private fun move(agent: BattleAgent, angle: Double, coefficient: Double = 1.0) {
        val x = agent.x + Math.cos(angle) * physicsParameters.agent.speed * coefficient
        val y = agent.y + Math.sin(angle) * physicsParameters.agent.speed * coefficient

        if (!intersectsWall(Line2D.Double(agent.x, agent.y, x, y))) {
            agent.x = Math.max(0.0, Math.min(x, physicsParameters.width))
            agent.y = Math.max(0.0, Math.min(y, physicsParameters.height))
        }
    }

    private fun rotate(agent: BattleAgent, angle: Double) {
        agent.angle += angle

        // TODO: fix module
        if (agent.angle < 0) {
            agent.angle += 2 * Math.PI
        } else if (agent.angle >= 2 * Math.PI) {
            agent.angle -= 2 * Math.PI
        }
    }

    private fun addBullet(bullets: MutableList<BattleBullet>, bullet: BattleBullet) {
        bullets.removeIf { it.isEmpty() }

        // TODO: implement replacement strategy
        // TODO: fix order of bullets to minimize count of the states
        if (bullets.size < BattleState.BULLETS_COUNT) {
            bullets.add(bullet)
        }

        while (bullets.size < BattleState.BULLETS_COUNT) {
            bullets.add(BattleBullet())
        }
    }

    /**
     * Updates bullets parameters such as speed, acceleration
     * Also affects agent HP if bullet hits in the agent
     * @param bullets list of all bullets in the state
     * @param agent agent
     * @param enemy enemy agent
     */
    private fun processBullets(bullets: MutableList<BattleBullet>, agent: BattleAgent, enemy: BattleAgent) {
        bullets.removeIf {
            if (it.isEmpty()) {
                return@removeIf true
            }

            it.speedX += it.accelerationX
            it.speedY += it.accelerationY

            val length = Math.sqrt(Math.pow(it.speedX, 2.0) + Math.pow(it.speedY, 2.0))
            val coef = Math.min(physicsParameters.bullet.maxSpeed, length) / length
            it.speedX *= coef
            it.speedY *= coef

            val trajectory = Line2D.Double(it.x, it.y, it.x + it.speedX, it.y + it.speedY)

            if (!it.enemy && hitsTarget(trajectory, Point2D.Double(enemy.x, enemy.y))) {
                enemy.hp = Math.max(0, enemy.hp - it.damage)
                return@removeIf true
            }

            if (it.enemy && hitsTarget(trajectory, Point2D.Double(agent.x, agent.y))) {
                agent.hp = Math.max(0, agent.hp - it.damage)
                return@removeIf true
            }

            if (isOutOfTheWorld(Point2D.Double(it.x, it.y)) || intersectsWall(trajectory)) {
                return@removeIf true
            }

            it.x = trajectory.x2
            it.y = trajectory.y2

            return@removeIf false
        }

        while (bullets.size < BattleState.BULLETS_COUNT) {
            bullets.add(BattleBullet())
        }
    }

    private fun isOutOfTheWorld(point: Point2D): Boolean {
        return !Rectangle2D.Double(0.0, 0.0, physicsParameters.width, physicsParameters.height).contains(point)
    }

    private fun intersectsWall(vector: Line2D): Boolean {
        return physicsParameters.walls.filter { it.intersects(vector) }.isNotEmpty()
    }

    private fun hitsTarget(vector: Line2D, target: Point2D.Double): Boolean  {
        return vector.ptSegDist(target) <= physicsParameters.bullet.range
    }
}