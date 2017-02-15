package ru.spbau.mit.domain

import burlap.mdp.auxiliary.DomainGenerator
import burlap.mdp.core.Domain
import burlap.mdp.core.action.UniversalActionType
import burlap.mdp.singleagent.model.FactoredModel
import burlap.mdp.singleagent.oo.OOSADomain
import ru.spbau.mit.bot.BattleGreedyBot

class BattleDomain : DomainGenerator {
    val physicsParameters: BattlePhysicsParameters = BattlePhysicsParameters()

    override fun generateDomain(): Domain? {
        val domain = OOSADomain()

        domain.addStateClass(BattleAgent.CLASS, BattleAgent::class.java)
                .addStateClass(BattleAgent.CLASS_ENEMY, BattleEnemy::class.java)
                .addStateClass(BattleBullet.CLASS, BattleBullet::class.java)

        domain.addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_FORWARD))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_BACKWARD))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_LEFT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_RIGHT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.TURN_LEFT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.TURN_RIGHT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.SKIP))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.SHOOT))

        val bot = BattleGreedyBot()

        /*
        val bot = BattleCyclicBot()
                .addAction(BattleAgent.Companion.Action.SHOOT)
                .addAction(BattleAgent.Companion.Action.GO_RIGHT, 25)
                .addAction(BattleAgent.Companion.Action.TURN_RIGHT, 4)
                .addAction(BattleAgent.Companion.Action.SHOOT)
                .addAction(BattleAgent.Companion.Action.TURN_LEFT, 4)
                .addAction(BattleAgent.Companion.Action.GO_LEFT, 25)*/


        val battleStateModel = BattleModel(physicsParameters, bot)

        val rewardFunction = BattleRewardFunction(physicsParameters)
        val terminalFunction = BattleTerminalFunction()

        domain.model = FactoredModel(battleStateModel, rewardFunction, terminalFunction)

        return domain
    }
}
