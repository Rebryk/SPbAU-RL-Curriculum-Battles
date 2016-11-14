package ru.spbau.mit.visualization

import burlap.mdp.core.state.State
import burlap.visualizer.StatePainter
import java.awt.Color
import java.awt.Graphics2D

/**
 * State painter for a battle domain
 */
class BattleStatePainter : StatePainter {
    override fun paint(g2: Graphics2D?, state: State?, cWidth: Float, cHeight: Float) {
        if (g2 == null) {
            throw RuntimeException("Graphics2D is null!")
        }

        if (state == null) {
            throw RuntimeException("State is null!")
        }

        g2.color = Color.WHITE
        g2.fillRect(0, 0, cWidth.toInt(), cHeight.toInt())
    }
}
