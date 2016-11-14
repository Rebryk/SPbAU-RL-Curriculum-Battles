package ru.spbau.mit.domain

class BattleEnemy(x: Double, y: Double, angle: Double, hp: Double, name: String) : BattleAgent(x, y, angle, hp, name) {
    override fun className(): String = CLASS_ENEMY

    override fun copy(): BattleEnemy = BattleEnemy(x, y, angle, hp, name)
}