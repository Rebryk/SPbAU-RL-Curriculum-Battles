package ru.spbau.mit.domain

class BattleEnemy(x: Double,
                  y: Double,
                  angle: Double,
                  hp: Int,
                  cooldown: Int,
                  name: String) : BattleAgent(x, y, angle, hp, cooldown, name) {

    constructor(): this(0.0, 0.0, 0.0, 0, 0, "")

    override fun className(): String = CLASS_ENEMY

    override fun copy(): BattleEnemy = BattleEnemy(x, y, angle, hp, cooldown, name)
}