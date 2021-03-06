package ru.spbau.mit.domain

import burlap.mdp.core.StateTransitionProb
import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.statemodel.FullStateModel
import ru.spbau.mit.bot.BattleBot
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.*

class BattleModel(val physicsParameters: BattlePhysicsParameters,
                  val bot: BattleBot) : FullStateModel {

    private val RANDOM_ACTION_PROBABILITY: Double = 0.02
    private val RANDOM_ACTIONS = listOf(
            BattleAgent.Companion.Action.GO_FORWARD,
            BattleAgent.Companion.Action.GO_BACKWARD,
            BattleAgent.Companion.Action.GO_LEFT,
            BattleAgent.Companion.Action.GO_RIGHT)

    private var step: Int = 0
    private var skipActionProbability: ((Int) -> Double) = HARD_VERSION

    private fun getBotActionProbability(): Double = 1.0 - RANDOM_ACTIONS.size * RANDOM_ACTION_PROBABILITY - skipActionProbability(step)

    fun setSkipActionProbabilityAlgorithm(algorithm: (Int) -> Double) {
        skipActionProbability = algorithm
    }

    fun getSkipActionProbabilityAlgorithm(): ((Int) -> Double) {
        return skipActionProbability
    }

    fun nextStep() {
        ++step
    }

    fun resetStepsCount() {
        step = 0
    }

    // Deterministic variant
    /*
    override fun stateTransitions(state: State?, action: Action?): MutableList<StateTransitionProb> {
        return FullStateModel.Helper.deterministicTransition(this, state, action)
    }
    */

    override fun stateTransitions(state: State?, action: Action?): MutableList<StateTransitionProb> {
        if (state == null || action == null) {
            throw RuntimeException("State and action have to be not null!")
        }

        if (state !is BattleState) {
            throw RuntimeException("State is not BattleState!")
        }

        val states = mutableListOf(
                StateTransitionProb(state, skipActionProbability(step)),
                StateTransitionProb(sample(state, action, bot.nextAction(state, physicsParameters)), getBotActionProbability() )
        )

        RANDOM_ACTIONS.forEach {
            states.add(StateTransitionProb(sample(state, action, it), RANDOM_ACTION_PROBABILITY))
        }

        return states
    }

    fun sample(state: State, action: Action, botActionName: String): State {
        val newState = state.copy() as BattleState
        val agent = newState.touchAgent()
        val enemy = newState.touchEnemy()
        val agentBullets = newState.touchAgentBullets()
        val enemyBullets = newState.touchEnemyBullets()

        performAction(agent, action.actionName(), newState)
        performAction(enemy, botActionName, newState)

        agent.cooldown = Math.max(0, agent.cooldown - 1)
        enemy.cooldown = Math.max(0, enemy.cooldown - 1)

        processBullets(agentBullets, enemy)
        processBullets(enemyBullets, agent)

        return newState
    }

    override fun sample(state: State?, action: Action?): State {
        if (state == null || action == null) {
            throw RuntimeException("State and action have to be not null!")
        }

        if (state !is BattleState) {
            throw RuntimeException("State is not BattleState!")
        }

        val probability = Math.random()

        val randomActionProbability = RANDOM_ACTIONS.size * RANDOM_ACTION_PROBABILITY
        if (probability < randomActionProbability) {
            val randomActionName = RANDOM_ACTIONS[Random().nextInt(RANDOM_ACTIONS.size)]
            return sample(state, action, randomActionName) as BattleState
        }

        if (probability < randomActionProbability + skipActionProbability(step)) {
            return sample(state, action, BattleAgent.Companion.Action.SKIP)
        }

        return sample(state, action, bot.nextAction(state, physicsParameters))
    }

    /**
     * Applies agent action to the current state
     * @param agent agent which performs action
     * @param actionName name of the action
     */
    private fun performAction(agent: BattleAgent, actionName: String, state: BattleState) {
        when (actionName) {
            BattleAgent.Companion.Action.TURN_LEFT -> rotate(agent, physicsParameters.agent.rotationAngle)
            BattleAgent.Companion.Action.TURN_RIGHT -> rotate(agent, -physicsParameters.agent.rotationAngle)
            BattleAgent.Companion.Action.GO_FORWARD -> move(agent, agent.angle, physicsParameters, 1.2)
            BattleAgent.Companion.Action.GO_BACKWARD -> move(agent, agent.angle + Math.PI, physicsParameters)
            BattleAgent.Companion.Action.GO_LEFT -> move(agent, agent.angle + Math.PI / 2.0, physicsParameters)
            BattleAgent.Companion.Action.GO_RIGHT -> move(agent, agent.angle - Math.PI / 2.0, physicsParameters)
            BattleAgent.Companion.Action.SKIP -> { /* just skip */ }
            BattleAgent.Companion.Action.SHOOT -> shoot(agent, state.getBulletsFor(agent))
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

        val speedX = Math.cos(agent.angle) * physicsParameters.bullet.speed
        val speedY = Math.sin(agent.angle) * physicsParameters.bullet.speed
        val accelerationX = Math.cos(agent.angle) * physicsParameters.bullet.acceleration
        val accelerationY = Math.sin(agent.angle) * physicsParameters.bullet.acceleration

        val bullet = BattleBullet(agent.x, agent.y, speedX, speedY, accelerationX, accelerationY, physicsParameters.bullet.damage, "bullet")
        addBullet(bullets, bullet)
    }

    companion object {
        val HARD_VERSION: ((Int) -> Double) = { x -> 0.0 }

        fun move(agent: BattleAgent, angle: Double, physicsParameters: BattlePhysicsParameters, coefficient: Double = 1.0) {
            val x = agent.x + Math.cos(angle) * physicsParameters.agent.speed * coefficient
            val y = agent.y + Math.sin(angle) * physicsParameters.agent.speed * coefficient

            if (!intersectsWall(Line2D.Double(agent.x, agent.y, x, y), physicsParameters)) {
                agent.x = Math.max(0.0, Math.min(x, physicsParameters.width))
                agent.y = Math.max(0.0, Math.min(y, physicsParameters.height))
            }
        }

        fun intersectsWall(vector: Line2D, physicsParameters: BattlePhysicsParameters): Boolean {
            return physicsParameters.walls.filter { vector.intersects(it) }.isNotEmpty()
        }

        fun hitsTarget(vector: Line2D, target: Point2D.Double, physicsParameters: BattlePhysicsParameters): Boolean {
            return vector.ptSegDist(target) <= physicsParameters.bullet.range
        }
    }

    private fun rotate(agent: BattleAgent, angle: Double) {
        agent.angle += angle

        if (agent.angle > Math.PI) {
            agent.angle -= 2 * Math.PI
        }

        if (agent.angle <= -Math.PI) {
            agent.angle += 2 * Math.PI
        }
    }

    private fun addBullet(bullets: MutableList<BattleBullet>, bullet: BattleBullet) {
        bullets.removeAll(bullets.filter { it.isEmpty() })

        // TODO: implement replacement strategy
        if (bullets.size == BattleState.BULLETS_COUNT) {
            throw RuntimeException("Too many bullets!")
        }

        bullets.add(bullet)
        bullets.sort()

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
    private fun processBullets(bullets: MutableList<BattleBullet>, agent: BattleAgent) {
        bullets.removeAll(bullets.filter {
            if (it.isEmpty()) {
                return@filter true
            }

            it.speedX += it.accelerationX
            it.speedY += it.accelerationY

            val length = Math.sqrt(Math.pow(it.speedX, 2.0) + Math.pow(it.speedY, 2.0))
            val coef = Math.min(physicsParameters.bullet.maxSpeed, length) / length
            it.speedX *= coef
            it.speedY *= coef

            val trajectory = Line2D.Double(it.x, it.y, it.x + it.speedX, it.y + it.speedY)

            if (hitsTarget(trajectory, Point2D.Double(agent.x, agent.y), physicsParameters)) {
                agent.hp = Math.max(0, agent.hp - it.damage)
                return@filter true
            }

            if (isOutOfTheWorld(Point2D.Double(it.x, it.y)) || intersectsWall(trajectory, physicsParameters)) {
                return@filter true
            }

            it.x = trajectory.x2
            it.y = trajectory.y2

            return@filter false
        })

        while (bullets.size < BattleState.BULLETS_COUNT) {
            bullets.add(BattleBullet())
        }
    }

    private fun isOutOfTheWorld(point: Point2D): Boolean {
        return !Rectangle2D.Double(0.0, 0.0, physicsParameters.width, physicsParameters.height).contains(point)
    }
}