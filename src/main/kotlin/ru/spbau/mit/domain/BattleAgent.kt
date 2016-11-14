package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.state.MutableState
import burlap.mdp.core.state.StateUtilities
import burlap.mdp.core.state.UnknownKeyException
import burlap.mdp.core.state.annotations.DeepCopyState

@DeepCopyState
open class BattleAgent(var x: Double,
                       var y: Double,
                       var angle: Double,
                       var hp: Double,
                       var name: String): ObjectInstance, MutableState {

    companion object {
        val CLASS: String = "CLASS_AGENT"
        val CLASS_ENEMY: String = "CLASS_ENEMY"

        /**
         * Agent parameters:
         * X, Y - coordinates
         * ANGLE - [0, 2 * Pi)
         * HP - health points
         */
        object Var {
            val X: String = "X"
            val Y: String = "Y"
            val ANGLE: String = "ANGLE"
            val HP: String = "HP"
        }

        /**
         * Agent possible actions
         */
        object Action {
            val GO_LEFT: String = "GO_LEFT"
            val GO_RIGHT: String = "GO_RIGHT"
            val GO_FORWARD: String = "GO_FORWARD"
            val GO_BACKWARD: String = "GO_BACKWARD"

            val TURN_LEFT: String = "TURN_LEFT"
            val TURN_RIGHT: String = "TURN_RIGHT"

            val SKIP: String = "SKIP"

            val SHOOT: String = "SHOOT"
        }

        val keys = mutableListOf<Any>(Var.X, Var.Y, Var.ANGLE, Var.HP)
    }

    override fun get(variableKey: Any?): Any {
        return when (variableKey.toString()) {
            Var.X -> x
            Var.Y -> y
            Var.ANGLE -> angle
            Var.HP -> hp
            else -> throw UnknownKeyException(variableKey)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        val new_value = StateUtilities.stringOrNumber(value).toDouble()

        when (variableKey.toString()) {
            Var.X -> x = new_value
            Var.Y -> y = new_value
            Var.ANGLE -> angle = new_value
            Var.HP -> hp = new_value
            else -> throw UnknownKeyException(variableKey)
        }

        return this
    }

    override fun variableKeys(): MutableList<Any> = keys

    override fun className(): String = CLASS

    override fun name(): String = name

    override fun copy(): BattleAgent = BattleAgent(x, y, angle, hp, name)

    override fun copyWithName(objectName: String?): ObjectInstance = BattleAgent(x, y, angle, hp, objectName!!)

    override fun toString(): String = StateUtilities.stateToString(this)
}