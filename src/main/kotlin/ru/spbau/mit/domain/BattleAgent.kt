package ru.spbau.mit.domain

import burlap.mdp.core.oo.state.ObjectInstance
import burlap.mdp.core.state.MutableState
import burlap.mdp.core.state.StateUtilities
import burlap.mdp.core.state.UnknownKeyException
import burlap.mdp.core.state.annotations.DeepCopyState

@DeepCopyState
open class BattleAgent : ObjectInstance, MutableState {
    var name: String

    var x: Double
    var y: Double
    var angle: Double
    var hp: Double

    object Static {
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

            val SHOOT: String = "SHOOT"
        }

        val keys = mutableListOf<Any>(Var.X, Var.Y, Var.ANGLE, Var.HP)
    }

    constructor(x: Double, y: Double, angle: Double, hp: Double, name: String = "agent") {
        this.x = x
        this.y = y
        this.angle = angle
        this.hp = hp
        this.name = name
    }

    override fun get(variableKey: Any?): Any {
        return when (variableKey.toString()) {
            Static.Var.X -> x
            Static.Var.Y -> y
            Static.Var.ANGLE -> angle
            Static.Var.HP -> hp
            else -> throw UnknownKeyException(variableKey)
        }
    }

    override fun set(variableKey: Any?, value: Any?): MutableState {
        val new_value = StateUtilities.stringOrNumber(value).toDouble()

        when (variableKey.toString()) {
            Static.Var.X -> x = new_value
            Static.Var.Y -> y = new_value
            Static.Var.ANGLE -> angle = new_value
            Static.Var.HP -> hp = new_value
            else -> throw UnknownKeyException(variableKey)
        }

        return this
    }

    override fun variableKeys(): MutableList<Any> = Static.keys

    override fun className(): String = Static.CLASS

    override fun name(): String = name

    override fun copy(): BattleAgent = BattleAgent(x, y, angle, hp, name)

    override fun copyWithName(objectName: String?): ObjectInstance = BattleAgent(x, y, angle, hp, objectName.toString())

    override fun toString(): String = StateUtilities.stateToString(this)

    class Enemy : BattleAgent {
        constructor(x: Double, y: Double, angle: Double, hp: Double, name: String) : super(x, y, angle, hp, name) { }

        override fun className(): String = Static.CLASS_ENEMY

        override fun copy(): Enemy = Enemy(x, y, angle, hp, name)
    }
}