package ru.spbau.mit

import burlap.behavior.functionapproximation.dense.ConcatenatedObjectFeatures
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures
import burlap.behavior.functionapproximation.sparse.tilecoding.TileCodingFeatures
import burlap.behavior.functionapproximation.sparse.tilecoding.TilingArrangement
import burlap.behavior.singleagent.Episode
import burlap.mdp.singleagent.environment.SimulatedEnvironment
import burlap.mdp.singleagent.oo.OOSADomain
import burlap.shell.visual.VisualExplorer
import burlap.visualizer.Visualizer
import ru.spbau.mit.domain.*
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric
import burlap.behavior.singleagent.auxiliary.performance.TrialMode
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter
import burlap.behavior.singleagent.learning.LearningAgentFactory
import burlap.mdp.singleagent.environment.Environment
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    val generator = BattleDomain()
    val domain = generator.generateDomain() as OOSADomain

    val initState = BattleState(BattleAgent(5.0, 5.0, 0.0, 100, 0, "agent"),
            BattleEnemy(37.0, 27.0, -Math.PI / 2.0, 100, 0, "enemy"))

    val inputFeatures = ConcatenatedObjectFeatures()
            .addObjectVectorizion(BattleAgent.CLASS, NumericVariableFeatures())
            .addObjectVectorizion(BattleBullet.CLASS, NumericVariableFeatures())

    val nTilings = args[0].toInt()
    val resolution = args[1].toDouble()

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

    val stateGenerator = CyclicStateGenerator().addState(initState)
    val environment = SimulatedEnvironment(domain, stateGenerator)
    val factory = BattleAgentFactory(domain, vfa)

    factory.getModel().setSkipActionProbabilityAlgorithm(BattleModel.HARD_VERSION)
    runExperiment(environment, factory, "data/hard")
}

fun calculateStandardError(values: List<Double>): Double {
    val average = values.average()
    val delta = values.map { Math.pow(it - average, 2.0) }.sum()

    return Math.sqrt(delta) / values.size
}

fun saveStatistics(values: List<List<Double>>, fileName: String) {
    if (!File(fileName).exists()) {
        Files.createFile(Paths.get(fileName))
    }

    val writer = FileWriter(File(fileName))
    writer.write("Run, Min, Average, Max\n")

    values
            .map {
                it.mapIndexed { index, value -> Pair(index, value) }
            }
            .flatten()
            .groupBy { it.first }
            .forEach {
                val data = it.value.map { it.second }
                val average = data.average()
                val error = calculateStandardError(data)
                writer.write("${it.key + 1}, ${average - error}, $average, ${average + error}\n")
            }

    writer.flush()
}

fun runExperiment(environment: Environment, factory: BattleAgentFactory, folder: String) {
    val RUNS_COUNT = 20

    val rewards = mutableListOf<List<Double>>()
    val steps = mutableListOf<List<Double>>()

    (1..RUNS_COUNT).forEach {
        println("\nExperiment: $it trial")
        val result = runLearning(environment, factory)
        rewards.add(result.first)
        steps.add(result.second)
    }

    saveStatistics(rewards, "$folder/rewards.csv")
    saveStatistics(steps, "$folder/step.csv")
}

fun runLearning(environment: Environment, factory: BattleAgentFactory): Pair<List<Double>, List<Double>> {
    val RUNS_COUNT = 1500
    val RANGE = 100
    val MAX_STEPS = 1000

    val agent = factory.generateAgent()
    val rewards = mutableListOf<Double>()
    val steps = mutableListOf<Double>()

    for (i in 1..RUNS_COUNT) {
        val episode = agent.runLearningEpisode(environment, MAX_STEPS)
        println("Episode $i: steps = ${episode.numActions()}, reward = ${episode.rewardSequence.sum()}")

        environment.resetEnvironment()

        factory.getModel().nextStep()

        if (i % RANGE == 0) {
            println("Starting performance evaluating...")

            val skipActionProbabilityAlgorithm = factory.getModel().getSkipActionProbabilityAlgorithm()
            factory.getModel().setSkipActionProbabilityAlgorithm(BattleModel.HARD_VERSION)

            val performance = agent.evaluatePerformance(environment)
            rewards.add(performance.reward)
            steps.add(performance.steps)

            factory.getModel().setSkipActionProbabilityAlgorithm(skipActionProbabilityAlgorithm)

            println("Performance evaluating has finished")
        }
    }

    return Pair(rewards, steps)
}

fun visualExperiment(environment: Environment, factory: LearningAgentFactory, runsCount: Int, trialLength: Int) {
    val exp = LearningAlgorithmExperimenter(environment, runsCount, trialLength, factory)

    exp.setUpPlottingConfiguration(400, 200, 2, 1000,
            TrialMode.MOST_RECENT_AND_AVERAGE,
            PerformanceMetric.AVERAGE_EPISODE_REWARD,
            PerformanceMetric.STEPS_PER_EPISODE,
            PerformanceMetric.CUMULATIVE_REWARD_PER_EPISODE)

    exp.startExperiment()
}


fun experiment(environment: Environment, factory: LearningAgentFactory, runsCount: Int, trialLength: Int, folder: String) {
    for (run in 1..runsCount) {
        println("Run $run")

        val exp = LearningAlgorithmExperimenter(environment, 1, trialLength, factory)
        exp.toggleVisualPlots(false)
        exp.startExperiment()
        exp.writeEpisodeDataToCSV("$folder/$run")
    }
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