package ru.spbau.mit.domain

import burlap.behavior.functionapproximation.sparse.LinearVFA
import burlap.behavior.learningrate.LearningRate
import burlap.behavior.policy.GreedyQPolicy
import burlap.behavior.singleagent.learning.LearningAgent
import burlap.behavior.singleagent.learning.LearningAgentFactory
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam
import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.SADomain
import burlap.mdp.singleagent.model.FactoredModel

class BattleAgentFactory(private val domain: SADomain,
                         private val vfa: LinearVFA) {
    fun generateAgent(): BattleSarsa {
        getModel().resetStepsCount()
        return BattleSarsa(domain, vfa.copy())
    }

    fun getModel(): BattleModel {
        return (domain.model as FactoredModel).stateModel as BattleModel
    }
}