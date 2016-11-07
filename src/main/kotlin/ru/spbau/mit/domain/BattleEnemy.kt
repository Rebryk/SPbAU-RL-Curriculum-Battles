package ru.spbau.mit.domain

class BattleEnemy : BattleAgent {
    constructor(x: Double, y: Double, angle: Double, hp: Double, name: String) : super(x, y, angle, hp, name)

    override fun className(): String = Static.CLASS_ENEMY

    override fun copy(): BattleEnemy = BattleEnemy(x, y, angle, hp, name)
}