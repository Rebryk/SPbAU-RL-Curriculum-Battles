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
                   var damage: Int,
                   var enemy: Boolean,
                   var name: String) : ObjectInstance, MutableState {

    constructor(): this(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, false, "empty")

    companion object {
        val CLASS: String = "CLASS_BULLET"

        /**
         * Bullet parameters:
         * X, Y - coordinates
         * SPEED_X, SPEED_Y - vector of speed
         * ENEMY - is it enemy's bullet?
         */
        object Var {
            val X: String = "X"
            val Y: String = "Y"
            val SPEED_X: String = "SPEED_X"
            val SPEED_Y: String = "SPEED_Y"
            val ENEMY: String = "ENEMY"
        }

        val keys = mutableListOf<Any>(Var.X, Var.Y, Var.SPEED_X, Var.SPEED_Y, Var.ENEMY)
    }

    fun isEmpty(): Boolean = speedX == 0.0 && speedY == 0.0

    override fun get(variableKey: Any?): Any {
        return when(variableKey.toString()) {
            Var.X               -> x
            Var.Y               -> y
            Var.SPEED_X         -> speedX
            Var.SPEED_Y         -> speedY
            Var.ENEMY           -> if (enemy) 1.0 else 0.0
            else                -> throw UnknownKeyException(variableKey)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        val new_value = StateUtilities.stringOrNumber(value).toDouble()

        when (variableKey.toString()) {
            Var.X               -> x = new_value
            Var.Y               -> y = new_value
            Var.SPEED_X         -> speedX = new_value
            Var.SPEED_Y         -> speedY = new_value
            Var.ENEMY           -> throw IllegalArgumentException("You can't change bullet owner!")
            else                -> throw UnknownKeyException(variableKey)
        }

        return this
    }

    override fun variableKeys(): MutableList<Any> = keys

    override fun className(): String = CLASS

    override fun name(): String = name

    override fun copy(): BattleBullet = BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, enemy, name)

    override fun copyWithName(objectName: String?): ObjectInstance {
        return BattleBullet(x, y, speedX, speedY, accelerationX, accelerationY, damage, enemy, objectName!!)
    }

    override fun toString(): String = StateUtilities.stateToString(this)
}
