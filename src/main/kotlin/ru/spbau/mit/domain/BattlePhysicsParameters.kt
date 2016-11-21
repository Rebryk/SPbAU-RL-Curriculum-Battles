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
                     var cooldown: Int = 5,
                     var speed: Double = 2.0,
                     var rotationAngle: Double = Math.PI / 18.0)

    data class Bullet(var speed: Double = 3.0,
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

    data class Wall(val x: Double, val y: Double, val width: Double, val height: Double) {
        fun contains(point: Point2D.Double) = point.x >= x && point.x < x + width && point.y >= y && point.y < y + height

        fun intersects(vector: Line2D.Double) = vector.intersects(x, y, width, height)
    }

    val walls: List<Wall> = mutableListOf(
            Wall(40.0, 95.0, 160.0, 10.0),
            Wall(30.0, 170.0, 30.0, 30.0)
    )
}