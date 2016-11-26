package ru.spbau.mit.domain

import burlap.mdp.auxiliary.DomainGenerator
import burlap.mdp.core.Domain
import burlap.mdp.core.action.UniversalActionType
import burlap.mdp.core.oo.OODomain
import burlap.mdp.core.oo.propositional.PropositionalFunction
import burlap.mdp.core.oo.state.OOState
import burlap.mdp.singleagent.model.FactoredModel
import burlap.mdp.singleagent.oo.OOSADomain
import ru.spbau.mit.bot.BattleBotEmpty

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

        val battleStateModel = BattleModel(physicsParameters, BattleBotEmpty())

        val rewardFunction = BattleRewardFunction()
        val terminalFunction = BattleTerminalFunction()

        domain.model = FactoredModel(battleStateModel, rewardFunction, terminalFunction)

        return domain
    }
}
