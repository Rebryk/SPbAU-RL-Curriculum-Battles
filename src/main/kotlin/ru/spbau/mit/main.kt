package ru.spbau.mit

import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement
import burlap.behavior.singleagent.Episode
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam
import burlap.mdp.singleagent.common.VisualActionObserver
import burlap.mdp.singleagent.environment.SimulatedEnvironment
import burlap.mdp.singleagent.oo.OOSADomain
import burlap.shell.visual.VisualExplorer
import burlap.visualizer.Visualizer
import ru.spbau.mit.domain.BattleAgent
import ru.spbau.mit.domain.BattleDomain
import ru.spbau.mit.domain.BattleEnemy
import ru.spbau.mit.domain.BattleState
import ru.spbau.mit.visualization.BattleVisualizer
import java.util.*

fun main(args: Array<String>) {
    val generator = BattleDomain()
    val domain = generator.generateDomain() as OOSADomain
    val initState = BattleState(BattleAgent(20.0, 20.0, 0.0, 100.0, "agent"),
            BattleEnemy(220.0, 180.0, 0.0, 100.0, "enemy"),
            arrayListOf())

    val inputFeatures = ConcatenatedObjectFeatures()
            .addObjectVectorizion(BattleAgent.CLASS, NumericVariableFeatures())

    val nTilings = 4
    val resolution = 20.0

    val xWidth = generator.physicsParameters.width / resolution
    val yWidth = generator.physicsParameters.height / resolution
    val angleWidth = 2 * Math.PI / resolution
    val hpWidth = 100.0 / resolution

    val tilecoding = TileCodingFeatures(inputFeatures)
    tilecoding.addTilingsForAllDimensionsWithWidths(doubleArrayOf(xWidth, yWidth, angleWidth, hpWidth),
            nTilings,
            TilingArrangement.RANDOM_JITTER)

    val defaultQ = 0.5
    val vfa = tilecoding.generateVFA(defaultQ / nTilings)
    val agent = GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5)

    val visualizer = BattleVisualizer.getVisualizer(generator.physicsParameters)
    val environment = SimulatedEnvironment(domain, initState)

    //setupExplorer(domain, environment, visualizer)


    val observer = VisualActionObserver(visualizer)
    observer.initGUI()

    val episodes = ArrayList<Episode>()
    for (i in 0..10000) {
        val episode = agent.runLearningEpisode(environment)
        println(i.toString() + ": " + episode.maxTimeStep())
        environment.resetEnvironment()

        if (i == 9000) {
            environment.addObservers(observer)
        }
    }

    EpisodeSequenceVisualizer(visualizer, domain, episodes)
}

fun setupExplorer(domain: OOSADomain, environment: SimulatedEnvironment, visualizer: Visualizer) {
    val explorer = VisualExplorer(domain, environment, visualizer)
    explorer.addKeyAction("w", BattleAgent.Companion.Action.GO_FORWARD, "")
    explorer.addKeyAction("s", BattleAgent.Companion.Action.GO_BACKWARD, "")
    explorer.addKeyAction("a", BattleAgent.Companion.Action.GO_LEFT, "")
    explorer.addKeyAction("d", BattleAgent.Companion.Action.GO_RIGHT, "")
    explorer.addKeyAction("q", BattleAgent.Companion.Action.TURN_LEFT, "")
    explorer.addKeyAction("e", BattleAgent.Companion.Action.TURN_RIGHT, "")
    explorer.initGUI()
}