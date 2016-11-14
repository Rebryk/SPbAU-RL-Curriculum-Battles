package ru.spbau.mit.domain

/**
 * A data class for holding the physics parameters
 */
data class BattlePhysicsParameters(var width: Double = 1000.0,
                                   var height: Double = 1000.0,
                                   var unitSpeed: Double = 20.0,
                                   var unitRotationAngle: Double = Math.PI / 18.0,
                                   var bulletAcceleration: Double = 100.0)