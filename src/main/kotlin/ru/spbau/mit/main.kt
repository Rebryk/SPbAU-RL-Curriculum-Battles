package ru.spbau.mit

import burlap.mdp.singleagent.environment.SimulatedEnvironment
import burlap.mdp.singleagent.oo.OOSADomain
import burlap.shell.visual.VisualExplorer
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattleDomain
import ru.spbau.mit.domain.BattleEnemy
import ru.spbau.mit.domain.BattleState
import ru.spbau.mit.visualization.BattleVisualizer

fun main(args: Array<String>) {
    val generator = BattleDomain()
    val domain = generator.generateDomain() as OOSADomain
    val initState = BattleState(BattleAgent(100.0, 100.0, 0.0, 100.0, "agent"),
            BattleEnemy(900.0, 900.0, 0.0, 100.0, "enemy"),
            arrayListOf())

    val environment = SimulatedEnvironment(domain, initState)

    val visualizer = BattleVisualizer.getVisualizer(generator.physicsParameters)

    val explorer = VisualExplorer(domain, environment, visualizer)
    explorer.addKeyAction("w", BattleAgent.Companion.Action.GO_FORWARD, "")
    explorer.addKeyAction("s", BattleAgent.Companion.Action.GO_BACKWARD, "")
    explorer.addKeyAction("a", BattleAgent.Companion.Action.GO_LEFT, "")
    explorer.addKeyAction("d", BattleAgent.Companion.Action.GO_RIGHT, "")
    explorer.addKeyAction("q", BattleAgent.Companion.Action.TURN_LEFT, "")
    explorer.addKeyAction("e", BattleAgent.Companion.Action.TURN_RIGHT, "")
    explorer.initGUI()
}