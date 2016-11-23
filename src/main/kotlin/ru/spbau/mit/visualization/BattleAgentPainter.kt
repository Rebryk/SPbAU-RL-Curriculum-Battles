package ru.spbau.mit.visualization

import burlap.mdp.core.oo.state.OOState
import burlap.mdp.core.oo.state.ObjectInstance
import burlap.visualizer.ObjectPainter
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattlePhysicsParameters
import sun.management.Agent
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Path2D

/**
 * Object painter for agents of a battle domain, rendered as circles with arrows
 */
open class BattleAgentPainter(private val physicsParameters: BattlePhysicsParameters,
                         private val color: Color = Color.BLUE,
                         private val radius: Int = 30) : ObjectPainter {

    override fun paintObject(g2: Graphics2D?, state: OOState?, obj: ObjectInstance?, cWidth: Float, cHeight: Float) {
        if (g2 == null) {
            throw RuntimeException("Graphics2D is null!")
        }

        if (state == null) {
            throw RuntimeException("State is null!")
        }

        if (obj == null) {
            throw RuntimeException("Object is null!")
        }

        val agent = obj as BattleAgent

        val scaleX = cWidth / physicsParameters.width
        val scaleY = cHeight / physicsParameters.height

        g2.color = color
        val x = (agent.x * scaleX).toInt()
        val y = (cHeight - scaleY * agent.y).toInt()
        val angle = agent.angle + Math.PI / 2

        g2.fillOval(x - radius, y - radius, 2 * radius, 2 * radius)

        drawPointer(g2, x, y, angle)
    }

    /**
     * Paints arrow which shows agent direction
     */
    private fun drawPointer(g2: Graphics2D, x: Int, y: Int, angle: Double) {
        val x1 = x + radius * Math.cos(angle + Math.PI / 9)
        val y1 = y - radius * Math.sin(angle + Math.PI / 9)
        val x2 = x + 1.3 * radius * Math.cos(angle)
        val y2 = y - 1.3 * radius * Math.sin(angle)
        val x3 = x + radius * Math.cos(angle - Math.PI / 9)
        val y3 = y - radius * Math.sin(angle - Math.PI / 9)

        val path = Path2D.Double()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.lineTo(x1, y1)
        path.closePath()

        g2.fill(path)
    }
}
