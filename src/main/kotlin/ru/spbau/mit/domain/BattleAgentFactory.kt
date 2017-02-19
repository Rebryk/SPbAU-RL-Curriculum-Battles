package ru.spbau.mit.domain

import burlap.behavior.functionapproximation.sparse.LinearVFA
import burlap.behavior.singleagent.learning.LearningAgent
import burlap.behavior.singleagent.learning.LearningAgentFactory
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam
import burlap.mdp.singleagent.SADomain
import burlap.mdp.singleagent.model.FactoredModel

class BattleAgentFactory(private val domain: SADomain,
                         private val vfa: LinearVFA) : LearningAgentFactory {
    override fun getAgentName() = "GD_SARSA"

    override fun generateAgent(): LearningAgent {
        ((domain.model as FactoredModel).stateModel as BattleModel).nextEpisode()
        return GradientDescentSarsaLam(domain, 0.99, vfa.copy(), 0.02, 0.5)
    }
}