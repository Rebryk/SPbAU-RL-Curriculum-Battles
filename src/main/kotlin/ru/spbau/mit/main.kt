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
import ru.spbau.mit.domain.*
import ru.spbau.mit.visualization.BattleVisualizer

fun main(args: Array<String>) {
    val generator = BattleDomain()
    val domain = generator.generateDomain() as OOSADomain

    val initState = BattleState(BattleAgent(20.0, 20.0, 0.0, 100, 0, "agent"),
            BattleEnemy(220.0, 180.0, -Math.PI / 2.0, 100, 0, "enemy"))

    val inputFeatures = ConcatenatedObjectFeatures()
            .addObjectVectorizion(BattleAgent.CLASS, NumericVariableFeatures())
            .addObjectVectorizion(BattleBullet.CLASS, NumericVariableFeatures())

    val nTilings = 4
    val resolution = 20.0

    val xWidth = generator.physicsParameters.width / resolution
    val yWidth = generator.physicsParameters.height / resolution
    val angleWidth = 2 * Math.PI / resolution
    val hpWidth = 100.0 / resolution
    val cooldownWidth = generator.physicsParameters.agent.cooldown / resolution
    val speedWidth = generator.physicsParameters.bullet.maxSpeed / resolution

    val widths = mutableListOf(xWidth, yWidth, angleWidth, hpWidth, cooldownWidth)
    for (i in 1..(2 * BattleState.BULLETS_COUNT)) {
        widths.addAll(listOf(xWidth, yWidth, speedWidth, speedWidth))
    }

    val tilecoding = TileCodingFeatures(inputFeatures)
    tilecoding.addTilingsForAllDimensionsWithWidths(widths.toDoubleArray(), nTilings, TilingArrangement.RANDOM_JITTER)

    val defaultQ = 0.5
    val vfa = tilecoding.generateVFA(defaultQ / nTilings)
    val agent = GradientDescentSarsaLam(domain, 0.99, vfa, 0.02, 0.5)

    val visualizer = BattleVisualizer.getVisualizer(generator.physicsParameters)

    val stateGenerator = CyclicStateGenerator().addState(initState)
    val environment = SimulatedEnvironment(domain, stateGenerator)

    // uncomment to use keyboard control
    // setupExplorer(domain, environment, visualizer)

    val observer = VisualActionObserver(visualizer)
    observer.initGUI()


    for (i in 0..5010) {
        val episode = agent.runLearningEpisode(environment)
        println("%d: steps count = %d, reward = %f".format(i, episode.maxTimeStep(), episode.rewardSequence.sum()))

        // call to save episode
        // saveEpisode(episode, i)

        environment.resetEnvironment()
    }

    EpisodeSequenceVisualizer(visualizer, domain, "episodes/")
}

fun saveEpisode(episode: Episode, index: Int) {
    episode.write("episodes/battle_$index")
}

fun setupExplorer(domain: OOSADomain, environment: SimulatedEnvironment, visualizer: Visualizer) {
    val explorer = VisualExplorer(domain, environment, visualizer)
    explorer.addKeyAction("w", BattleAgent.Companion.Action.GO_FORWARD, "")
    explorer.addKeyAction("s", BattleAgent.Companion.Action.GO_BACKWARD, "")
    explorer.addKeyAction("a", BattleAgent.Companion.Action.GO_LEFT, "")
    explorer.addKeyAction("d", BattleAgent.Companion.Action.GO_RIGHT, "")
    explorer.addKeyAction("q", BattleAgent.Companion.Action.TURN_LEFT, "")
    explorer.addKeyAction("e", BattleAgent.Companion.Action.TURN_RIGHT, "")
    explorer.addKeyAction("x", BattleAgent.Companion.Action.SHOOT, "")
    explorer.initGUI()
}