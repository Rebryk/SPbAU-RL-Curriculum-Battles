package ru.spbau.mit.domain

import burlap.behavior.functionapproximation.DifferentiableStateActionValue
import burlap.behavior.functionapproximation.sparse.LinearVFA
import burlap.behavior.learningrate.LearningRate
import burlap.behavior.policy.GreedyQPolicy
import burlap.behavior.policy.Policy
import burlap.behavior.singleagent.learning.LearningAgent
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam
import burlap.mdp.singleagent.SADomain
import burlap.mdp.singleagent.environment.Environment
import burlap.mdp.singleagent.model.FactoredModel

class BattleSarsa : GradientDescentSarsaLam {
    private val RUNS_COUNT = 50

    private var lastVfa: LinearVFA
    // private lateinit var lastLearningRate: LearningRate
    private lateinit var lastLearningPolicy: Policy
    private var lastLambda: Double = 0.0
    private var lastTotalNumberOfSteps: Int = 0

    constructor(domain: SADomain, vfa: LinearVFA) : super(domain, 0.99, vfa, 0.02, 0.5) {
        lastVfa = vfa
    }

    data class Performance(val reward: Double, val steps: Double)

    fun evaluatePerformance(environment: Environment): Performance {
        dump()

        var totalReward = 0.0
        var totalSteps = 0.0

        (0..RUNS_COUNT).forEach {
            val episode = runLearningEpisode(environment)
            environment.resetEnvironment()

            totalReward += episode.rewardSequence.sum()
            totalSteps += episode.numActions().toLong()
        }

        restore()

        return Performance(totalReward / RUNS_COUNT, totalSteps / RUNS_COUNT)
    }

    private fun dump() {
        vfa = lastVfa.copy()

        // lastLearningRate = learningRate

        // swap learning policy
        lastLearningPolicy = learningPolicy
        learningPolicy = GreedyQPolicy(this)

        lastLambda = lambda
        lastTotalNumberOfSteps = totalNumberOfSteps
    }

    private fun restore() {
        vfa = lastVfa

        // learningRate = lastLearningRate

        // swap learning policy
        learningPolicy = lastLearningPolicy

        lambda = lastLambda
        totalNumberOfSteps = lastTotalNumberOfSteps
    }
}