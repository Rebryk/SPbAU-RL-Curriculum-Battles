package ru.spbau.mit.domain

import java.awt.geom.Line2D
import java.awt.geom.Point2D


/**
 * A data class for holding the physics parameters
 */
data class BattlePhysicsParameters(var width: Double = 240.0,
                                   var height: Double = 200.0,
                                   val agent: Agent = Agent(),
                                   val bullet: Bullet = Bullet()) {

    data class Agent(var minHp: Int = 0,
                     var maxHP: Int = 100,
                     var cooldown: Int = 20,
                     var speed: Double = 2.0,
                     var rotationAngle: Double = Math.PI / 12.0)

    data class Bullet(var speed: Double = 3.0,
                      var maxSpeed: Double = 20.0,
                      var acceleration: Double = 1.5,
                      var range: Double = 10.0,
                      var damage: Int = 20)

    /**
     * A data class which describes obstacles.
     * @param x the X coordinate of the lower-left corner
     * @param y the Y coordinate of the lower-left corner
     * @param width the width of the wall
     * @param height the height of the wall
     */

    class Wall(x: kotlin.Double,
               y: kotlin.Double,
               width: kotlin.Double,
               height: kotlin.Double) : java.awt.geom.Rectangle2D.Double(x, y, width, height) {

        fun getDistanceTo(point: Point2D): kotlin.Double {
            val points = arrayListOf(Point2D.Double(x, y),
                    Point2D.Double(x + width, y),
                    Point2D.Double(x + width, y + height),
                    Point2D.Double(x, y + height))

            return (0..3).map { Line2D.Double(points[it], points[(it + 1) % 4]).ptSegDist(point) }.min() ?: kotlin.Double.MAX_VALUE
        }
    }

    val walls: List<Wall> = mutableListOf(
            Wall(40.0, 95.0, 160.0, 10.0),
            Wall(30.0, 170.0, 30.0, 30.0)
    )
}