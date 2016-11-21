package ru.spbau.mit.visualization

import burlap.visualizer.OOStatePainter
import burlap.visualizer.StateRenderLayer
import burlap.visualizer.Visualizer
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattleBullet
import ru.spbau.mit.domain.BattlePhysicsParameters
import java.awt.Color

/**
 * Class for creating a 2D visualizer for a battle domain
 */
class BattleVisualizer {
    companion object {
        fun getVisualizer(physicsParameters: BattlePhysicsParameters): Visualizer {
            return Visualizer(getStateRenderLayer(physicsParameters))
        }

        fun getStateRenderLayer(physicsParameters: BattlePhysicsParameters): StateRenderLayer {
            val renderLayer = StateRenderLayer()
            renderLayer.addStatePainter(BattleStatePainter(physicsParameters))

            val statePainter = OOStatePainter()
            statePainter.addObjectClassPainter(BattleAgent.CLASS, BattleAgentPainter(physicsParameters))
            statePainter.addObjectClassPainter(BattleAgent.CLASS_ENEMY, BattleAgentPainter(physicsParameters, Color.RED))
            statePainter.addObjectClassPainter(BattleBullet.CLASS, BattleBulletPainter(physicsParameters))

            renderLayer.addStatePainter(statePainter)
            return renderLayer
        }
    }
}