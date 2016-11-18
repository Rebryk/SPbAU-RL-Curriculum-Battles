package ru.spbau.mit.visualization

import burlap.mdp.core.state.State
import burlap.visualizer.StatePainter
import ru.spbau.mit.domain.BattlePhysicsParameters
import java.awt.Color
import java.awt.Graphics2D

/**
 * State painter for a battle domain
 */
class BattleStatePainter(private val physicsParameters: BattlePhysicsParameters) : StatePainter {
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
    }
}
