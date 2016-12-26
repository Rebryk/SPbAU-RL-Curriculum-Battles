package ru.spbau.mit.domain

import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.RewardFunction
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.*

class BattleRewardFunction(val physicsParameters: BattlePhysicsParameters) : RewardFunction {
    companion object {
        private val SHOT_MAX_DELTA_ANGLE: Double = Math.PI / 12.0
        private val MIN_DISTANCE_TO_OBSTACLE: Double = 15.0

        /**
         * Reward for different actions
         */
        private val DEFAULT_REWARD: Double = -2.0
        private val ANGLE_DELTA_REWARD: Double = 100.0 / Math.PI
        private val DISTANCE_DELTA_REWARD: Double = 0.6
        private val ENEMY_IS_DEAD_REWARD: Double = 1000.0
        private val ENEMY_INJURED_REWARD: Double = 200.0
        private val AGENT_INJURED_REWARD: Double = -100.0
        private val INACCURATE_SHOT_REWARD: Double = -50.0
        private val USELESS_SHOT_REWARD: Double = -50.0
        private val TOUCHING_OBSTACLE_REWARD: Double = -50.0
        private val MOVING_TO_WALL_REWARD: Double = -50.0

        private val CELL_SIZE: Double = 5.0
    }

    val maps: Array<Array<Map>>

    init {
        maps = findDistances()
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
            throw RuntimeException("newState isn\'t BattleState!")
        }

        if (enemyIsDead(newState)) {
            return ENEMY_IS_DEAD_REWARD
        }

        var reward = DEFAULT_REWARD

        if (BattleAgent.Companion.Action.isMoving(action.actionName()) && touchingObstacle(state, newState)) {
            reward += TOUCHING_OBSTACLE_REWARD
        }

        reward += Math.min(0.0, (1 - getDistanceToObstacle(newState) / MIN_DISTANCE_TO_OBSTACLE) * MOVING_TO_WALL_REWARD)

        if (BattleAgent.Companion.Action.isShooting(action.actionName()) && inaccurateShot(newState)) {
            reward += INACCURATE_SHOT_REWARD
        }


        if (BattleAgent.Companion.Action.isShooting(action.actionName()) && isUselessShot(newState)) {
            reward += USELESS_SHOT_REWARD
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

    private fun enemyIsDead(state: BattleState): Boolean = state.enemy.hp == 0

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
        return getDistanceToEnemy(newState) - getDistanceToEnemy(state)
    }

    private fun getAngleDelta(state: BattleState, newState: BattleState): Double {
        return getAngle(newState.agent, newState.enemy) - getAngle(state.agent, state.enemy)
    }

    /**
     * Intersects shooting trajectory with walls
     * @param state current battle state
     * @return true if shooting trajectory intersects some wall, false - otherwise
     */
    private fun isUselessShot(state: BattleState): Boolean {
        val start = Point2D.Double(state.agent.x, state.agent.y)
        val distance = start.distance(state.enemy.x, state.enemy.y)
        val end = Point2D.Double(start.x + distance * Math.cos(state.agent.angle), start.y + distance * Math.sin(state.agent.angle))
        val trajectory = Line2D.Double(start, end)

        val upBorder = Line2D.Double(0.0, physicsParameters.height, physicsParameters.width, physicsParameters.height)
        val bottomBorder = Line2D.Double(0.0, 0.0, physicsParameters.width, 0.0)
        val leftBorder = Line2D.Double(0.0, 0.0, 0.0, physicsParameters.height)
        val rightBorder = Line2D.Double(physicsParameters.width, 0.0, physicsParameters.width, physicsParameters.height)

        var intersectsBorder = trajectory.intersectsLine(upBorder)
        intersectsBorder = intersectsBorder or trajectory.intersectsLine(bottomBorder)
        intersectsBorder = intersectsBorder or trajectory.intersectsLine(leftBorder)
        intersectsBorder = intersectsBorder or trajectory.intersectsLine(rightBorder)

        return physicsParameters.walls.filter { trajectory.intersects(it) }.isNotEmpty() || intersectsBorder
    }

    /**
     * Finds distance from the agent to the nearest obstacle
     * @param state current battle state
     * @return min distance
     */
    private fun getDistanceToObstacle(state: BattleState): Double {
        val point = Point2D.Double(state.agent.x, state.agent.y)
        return physicsParameters.walls.map { it.getDistanceTo(point) }.min() ?: Double.MAX_VALUE
    }

    /**
     * Keeps distance from one cell to all other
     */
    class Map(val width: Int, val height: Int) {
        val distance = (0..width).map { (0..height).map { Int.MAX_VALUE }.toMutableList() }.toMutableList()
        val previous = (0..width).map { (0..height).map { Pair(-1, -1) }.toMutableList() }.toMutableList()
    }

    private fun bfs(from: Pair<Int, Int>, map: Map, wall: List<BooleanArray>) {
        val queue = LinkedList<Pair<Int, Int>>()

        map.distance[from.first][from.second] = 0
        queue.add(from)

        val next = arrayOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1))

        while (queue.isNotEmpty()) {
            val cell = queue.pop()

            next.forEach {
                val nextCell = Pair(cell.first + it.first, cell.second + it.second)

                if (nextCell.first < 0 || nextCell.first > map.width || nextCell.second < 0 || nextCell.second > map.height) {
                    return@forEach
                }

                if (wall[nextCell.first][nextCell.second]) {
                    return@forEach
                }

                if (map.distance[nextCell.first][nextCell.second] == Int.MAX_VALUE) {
                    map.distance[nextCell.first][nextCell.second] = map.distance[cell.first][cell.second] + 1
                    map.previous[nextCell.first][nextCell.second] = cell
                    queue.add(nextCell)
                }
            }
        }
    }

    /**
     * Calculates all distances
     */
    private fun findDistances(): Array<Array<Map>> {
        /**
         * @param x x-coordinate of cell
         * @param y y-coordinate of cell
         * @return rectangle which represents the given cell
         */
        fun getCell(x: Int, y: Int): Rectangle2D.Double = Rectangle2D.Double(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE)

        /**
         * @param x x-coordinate of cell
         * @param y y-coordinate of cell
         * @return true if the given cell intersects some wall, false otherwise
         */
        fun isWall(x: Int, y: Int): Boolean = physicsParameters.walls.filter { it.intersects(getCell(x, y)) }.isNotEmpty()

        val width = Math.ceil(physicsParameters.width / CELL_SIZE).toInt() + 1
        val height = Math.ceil(physicsParameters.height / CELL_SIZE).toInt() + 1

        val wall = (0..width).map { x -> (0..height).map { y -> isWall(x, y) }.toBooleanArray() }

        return Array(width, { x ->
            Array(height, { y ->
                val map = Map(width, height)
                bfs(Pair(x, y), map, wall)
                map
            })
        })
    }

    private fun getDistanceToEnemy(state: BattleState): Double {
        /**
         * @param x x-coordinate of cell
         * @param y y-coordinate of cell
         * @return rectangle which represents the given cell
         */
        fun getCell(x: Int, y: Int): Rectangle2D.Double = Rectangle2D.Double(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE)


        val agentCell = Pair((state.agent.x / CELL_SIZE).toInt(), (state.agent.y / CELL_SIZE).toInt())
        val enemyCell = Pair((state.enemy.x / CELL_SIZE).toInt(), (state.enemy.y / CELL_SIZE).toInt())
        val distance = maps[agentCell.first][agentCell.second].distance
        val previous = maps[agentCell.first][agentCell.second].previous

        /**
         * Returns Double.MAX_VALUE if there is no way to get the enemy
         */
        if (distance[enemyCell.first][enemyCell.second] == Int.MAX_VALUE) {
            return Double.MAX_VALUE
        }

        /**
         * Finds the second cell in the path from the agent cell to the enemy cell
         */
        var end = enemyCell

        if (end != agentCell) {
            while (previous[end.first][end.second] != agentCell) {
                end = previous[end.first][end.second]
            }
        }

        val endCell = getCell(end.first, end.second)
        val extraDistance = Point2D.Double(state.agent.x, state.agent.y).distance(endCell.centerX, endCell.centerY)

        return (distance[enemyCell.first][enemyCell.second] - 1) * CELL_SIZE + extraDistance
    }
}
