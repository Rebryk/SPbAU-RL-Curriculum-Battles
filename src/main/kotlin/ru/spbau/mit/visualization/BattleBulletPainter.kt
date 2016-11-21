package ru.spbau.mit.visualization

import burlap.mdp.core.oo.state.OOState
import burlap.mdp.core.oo.state.ObjectInstance
import burlap.visualizer.ObjectPainter
import ru.spbau.mit.domain.BattleBullet
import ru.spbau.mit.domain.BattlePhysicsParameters
import java.awt.Color
import java.awt.Graphics2D

class BattleBulletPainter(private val physicsParameters: BattlePhysicsParameters,
                          private val color: Color = Color.GREEN,
                          private val radius: Int = 5) : ObjectPainter {

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

        val bulletX = obj.get(BattleBullet.Companion.Var.X) as Double
        val bulletY = obj.get(BattleBullet.Companion.Var.Y) as Double

        val scaleX = cWidth / physicsParameters.width
        val scaleY = cHeight / physicsParameters.height

        val x = (bulletX * scaleX).toInt()
        val y = (cHeight - scaleY * bulletY).toInt()

        g2.color = color
        g2.fillOval(x - radius, y - radius, 2 * radius, 2 * radius)
    }
}