package ru.spbau.mit.domain

import java.awt.geom.Line2D
import java.awt.geom.Point2D

/**
 * A data class for holding the physics parameters
 */
data class BattlePhysicsParameters(var width: Double = 240.0,
                                   var height: Double = 200.0,
                                   var unitSpeed: Double = 4.0,
                                   var unitRotationAngle: Double = Math.PI / 18.0,
                                   var bulletAcceleration: Double = 100.0) {

    val walls: List<Wall> = mutableListOf(
            Wall(40.0, 95.0, 160.0, 10.0),
            Wall(30.0, 170.0, 30.0, 30.0)
    )

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

    fun intersectsWall(vector: Line2D.Double) = walls.filter { it.intersects(vector) }.isNotEmpty()
}