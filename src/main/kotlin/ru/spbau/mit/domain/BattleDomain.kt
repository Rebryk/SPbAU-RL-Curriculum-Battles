package ru.spbau.mit.domain

import burlap.mdp.auxiliary.DomainGenerator
import burlap.mdp.auxiliary.common.SinglePFTF
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

        OODomain.Helper.addPfsToDomain(domain, generatePropositionalFunctions())

        val battleStateModel = BattleModel(physicsParameters, BattleBotEmpty())

        // TODO: replace reward function
        //val rewardFunction = SingleGoalPFRF(domain.propFunction(SAME_POINT), 1000.0, -1.0)
        val rewardFunction = BattleRewardFunction(domain)

        // TODO: replace terminal function
        val terminalFunction = SinglePFTF(domain.propFunction(ENEMY_IS_DEAD))

        domain.model = FactoredModel(battleStateModel, rewardFunction, terminalFunction)

        return domain
    }

    /**
     * Describes propositional functions names
     */
    companion object {
        val UNIT_IS_DEAD = "UNIT_IS_DEAD"
        val ENEMY_IS_DEAD = "ENEMY_IS_DEAD"
        val SAME_POINT = "SAME_POINT"
    }

    /** Generates all propositional functions
     * @return propositional functions
     */
    fun generatePropositionalFunctions(): List<PropositionalFunction> {
        return listOf(UnitIsDead(UNIT_IS_DEAD), EnemyIsDead(ENEMY_IS_DEAD), AtTheSamePoint(SAME_POINT))
    }

    open class AgentIsDead(name: String, className: String) : PropositionalFunction(name, arrayOf(className)) {
        /**
         * Propositional function to check if agent is dead
         * @return true - if agent is dead, otherwise - false
         */
        override fun isTrue(state: OOState?, vararg params: String?): Boolean {
            if (state == null) {
                throw RuntimeException("Propositional function \'AgentIsDead\' has received null state!")
            }

            if (params[0] == BattleAgent.CLASS) {
                return (state as BattleState).agent.hp == 0
            }

            return (state as BattleState).enemy.hp == 0
        }
    }

    class UnitIsDead(name: String) : AgentIsDead(name, BattleAgent.CLASS)

    class EnemyIsDead(name: String) : AgentIsDead(name, BattleAgent.CLASS_ENEMY)

    class AtTheSamePoint(name: String) : PropositionalFunction(name, arrayOf()) {
        override fun isTrue(state: OOState?, vararg params: String?): Boolean {
            if (state == null) {
                throw RuntimeException("Propositional function \'AtTheSamePoint\' has received null state!")
            }

            val battleState = state as BattleState

            val x1 = battleState.agent.x
            val y1 = battleState.agent.y

            val x2 = battleState.enemy.x
            val y2 = battleState.enemy.y

            return Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0) < 100.0
        }
    }
}
