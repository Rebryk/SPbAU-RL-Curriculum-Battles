package ru.spbau.mit.visualization

import burlap.mdp.core.state.State
import burlap.visualizer.StatePainter
import ru.spbau.mit.domain.BattlePhysicsParameters
import ru.spbau.mit.domain.BattleState
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D
import java.awt.geom.Point2D

/**
 * State painter for a battle domain
 */
class BattleStatePainter(private val physicsParameters: BattlePhysicsParameters) : StatePainter {
    companion object {
        const val DRAW_SHOOTING_TRAJECTORY: Boolean = true
    }

    override fun paint(g2: Graphics2D?, state: State?, cWidth: Float, cHeight: Float) {
        if (g2 == null) {
            throw NullPointerException("Graphics2D is null!")
        }

        if (state == null) {
            throw NullPointerException("State is null!")
        }

        g2.color = Color.WHITE
        g2.fillRect(0, 0, cWidth.toInt(), cHeight.toInt())

        g2.color = Color.BLACK
        val scaleX = cWidth / physicsParameters.width
        val scaleY = cHeight / physicsParameters.height
        physicsParameters.walls.forEach {
            g2.fillRect((it.x * scaleX).toInt(),
                    (cHeight - (it.y + it.height) * scaleY).toInt(),
                    (it.width * scaleX).toInt(),
                    (it.height * scaleY).toInt())
        }

        if (DRAW_SHOOTING_TRAJECTORY) {
            val trajectory = getShootingTrajectory(state as BattleState)
            g2.color = Color.MAGENTA
            g2.drawLine((trajectory.x1 * scaleX).toInt(),
                    (cHeight - trajectory.y1 * scaleY).toInt(),
                    (trajectory.x2 * scaleX).toInt(),
                    (cHeight - trajectory.y2 * scaleY).toInt())
        }
    }

    private fun getShootingTrajectory(state: BattleState): Line2D.Double {
        val start = Point2D.Double(state.agent.x, state.agent.y)
        val distance = start.distance(state.enemy.x, state.enemy.y)
        val end = Point2D.Double(start.x + distance * Math.cos(state.agent.angle), start.y + distance * Math.sin(state.agent.angle))
        return Line2D.Double(start, end)
    }
}
