package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.state.MutableState
import burlap.mdp.core.state.StateUtilities
import burlap.mdp.core.state.UnknownKeyException
import burlap.mdp.core.state.annotations.DeepCopyState

@DeepCopyState
class BattleBullet(var x: Double,
                   var y: Double,
                   var speedX: Double,
                   var speedY: Double,
                   var accelerationX: Double,
                   var accelerationY: Double,
                   var damage: Double,
                   var name: String) : ObjectInstance, MutableState {

    companion object {
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

    override fun get(variableKey: Any?): Any {
        return when(variableKey.toString()) {
            Var.X -> x
            Var.Y -> y
            Var.SPEED_X -> speedX
            Var.SPEED_Y -> speedY
            Var.ACCELERATION_X -> accelerationX
            Var.ACCELERATION_Y -> accelerationY
            Var.DAMAGE -> damage
            else -> throw UnknownKeyException(variableKey)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        val new_value = StateUtilities.stringOrNumber(value).toDouble()

        when (variableKey.toString()) {
            Var.X -> x = new_value
            Var.Y -> y = new_value
            Var.SPEED_X -> speedX = new_value
            Var.SPEED_Y -> speedY = new_value
            Var.ACCELERATION_X -> accelerationX = new_value
            Var.ACCELERATION_Y -> accelerationY = new_value
            Var.DAMAGE -> damage = new_value
            else -> throw UnknownKeyException(variableKey)
        }

        return this
    }

    override fun variableKeys(): MutableList<Any> = keys

    override fun className(): String = CLASS

    override fun name(): String = name

    override fun copy(): BattleBullet = BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, name)

    override fun copyWithName(objectName: String?): ObjectInstance {
        return BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, objectName!!)
    }

    override fun toString(): String = StateUtilities.stateToString(this)
}
