package ru.spbau.mit.domain

import burlap.behavior.functionapproximation.sparse.LinearVFA
import burlap.behavior.singleagent.learning.LearningAgent
import burlap.behavior.singleagent.learning.LearningAgentFactory
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam
import burlap.mdp.singleagent.SADomain

class BattleAgentFactory(private val domain: SADomain,
                         private val vfa: LinearVFA) : LearningAgentFactory {
    override fun getAgentName() = "GD_SARSA"

    override fun generateAgent(): LearningAgent = GradientDescentSarsaLam(domain, 0.99, vfa.copy(), 0.02, 0.5)
}