package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.state.MutableState
import burlap.mdp.core.state.StateUtilities
import burlap.mdp.core.state.UnknownKeyException
import burlap.mdp.core.state.annotations.DeepCopyState

@DeepCopyState
class BattleBullet : ObjectInstance, MutableState {
    var name: String

    var x: Double
    var y: Double
    var speedX: Double
    var speedY: Double
    var accelerationX: Double
    var accelerationY: Double
    var damage: Double

    object Static {
        val CLASS: String = "CLASS_BULLET"

        /**
         * Bullet parameters:
         * X, Y - coordinates
         * SPEED_X, SPEED_Y - vector of speed
         * ACC_X, ACC_Y - vector of acceleration
         * DAMAGE - bullet damage
         */
        object Var {
            val X: String = "X"
            val Y: String = "Y"
            val SPEED_X: String = "SPEED_X"
            val SPEED_Y: String = "SPEED_Y"
            val ACCELERATION_X: String = "ACCELERATION_X"
            val ACCELERATION_Y: String = "ACCELERATION_Y"
            val DAMAGE: String = "DAMAGE"
        }

        val keys = mutableListOf<Any>(Var.X, Var.Y, Var.SPEED_X, Var.SPEED_Y, Var.ACCELERATION_X, Var.ACCELERATION_Y, Var.DAMAGE)
    }

    constructor(x: Double,
                y: Double,
                speedX: Double,
                speedY: Double,
                accelerationX: Double,
                accelerationY: Double,
                damage: Double,
                name: String = "bullet") {
        this.x = x
        this.y = y
        this.speedX = speedX
        this.speedY = speedY
        this.accelerationX = accelerationX
        this.accelerationY = accelerationY
        this.damage = damage
        this.name = name
    }

    override fun get(variableKey: Any?): Any {
        return when(variableKey.toString()) {
            Static.Var.X -> x
            Static.Var.Y -> y
            Static.Var.SPEED_X -> speedX
            Static.Var.SPEED_Y -> speedY
            Static.Var.ACCELERATION_X -> accelerationX
            Static.Var.ACCELERATION_Y -> accelerationY
            Static.Var.DAMAGE -> damage
            else -> throw UnknownKeyException(variableKey)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        val new_value = StateUtilities.stringOrNumber(value).toDouble()

        when (variableKey.toString()) {
            Static.Var.X -> x = new_value
            Static.Var.Y -> y = new_value
            Static.Var.SPEED_X -> speedX = new_value
            Static.Var.SPEED_Y -> speedY = new_value
            Static.Var.ACCELERATION_X -> accelerationX = new_value
            Static.Var.ACCELERATION_Y -> accelerationY = new_value
            Static.Var.DAMAGE -> damage = new_value
            else -> throw UnknownKeyException(variableKey)
        }

        return this
    }

    override fun variableKeys(): MutableList<Any> = Static.keys

    override fun className(): String = Static.CLASS

    override fun name(): String = name

    override fun copy(): BattleBullet = BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, name)

    override fun copyWithName(objectName: String?): ObjectInstance {
        return BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, objectName.toString())
    }

    override fun toString(): String = StateUtilities.stateToString(this)
}
