package ru.spbau.mit.domain

import burlap.mdp.auxiliary.DomainGenerator
import burlap.mdp.auxiliary.common.SinglePFTF
import burlap.mdp.core.Domain
import burlap.mdp.core.action.UniversalActionType
import burlap.mdp.core.oo.OODomain
import burlap.mdp.core.oo.propositional.PropositionalFunction
import burlap.mdp.core.oo.state.OOState
import burlap.mdp.singleagent.common.SingleGoalPFRF
import burlap.mdp.singleagent.model.FactoredModel
import burlap.mdp.singleagent.oo.OOSADomain
import ru.spbau.mit.bot.BattleBotEmpty

class BattleDomain : DomainGenerator {
    val physicsParameters: BattlePhysicsParameters = BattlePhysicsParameters()

    override fun generateDomain(): Domain? {
        val domain = OOSADomain()

        domain.addStateClass(BattleAgent.CLASS, BattleAgent::class.java)
                .addStateClass(BattleAgent.CLASS_ENEMY, BattleEnemy::class.java)

        domain.addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_FORWARD))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_BACKWARD))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_LEFT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.GO_RIGHT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.TURN_LEFT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.TURN_RIGHT))
                .addActionType(UniversalActionType(BattleAgent.Companion.Action.SKIP))

        OODomain.Helper.addPfsToDomain(domain, generatePropositionalFunctions())

        val battleStateModel = BattleModel(physicsParameters, BattleBotEmpty())

        // TODO: replace reward function
        val rewardFunction = SingleGoalPFRF(domain.propFunction(SAME_POINT), 1000.0, -1.0)

        // TODO: replace terminal function
        val terminalFunction = SinglePFTF(domain.propFunction(SAME_POINT))

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
            val agent = state?.`object`(params[0])
            agent?.let {
                return (it.get(BattleAgent.Companion.Var.HP) as Double) == 0.0
            }

            return false
        }
    }

    class UnitIsDead(name: String) : AgentIsDead(name, BattleAgent.CLASS)

    class EnemyIsDead(name: String) : AgentIsDead(name, BattleAgent.CLASS_ENEMY)

    class AtTheSamePoint(name: String) : PropositionalFunction(name, arrayOf(BattleAgent.CLASS, BattleAgent.CLASS_ENEMY)) {
        override fun isTrue(state: OOState?, vararg params: String?): Boolean {
            val agent = state?.`object`(params[0])
            val enemy = state?.`object`(params[1])

            agent?.let {
                val x1 = it.get(BattleAgent.Companion.Var.X) as Double
                val y1 = it.get(BattleAgent.Companion.Var.Y) as Double

                enemy?.let {
                    val x2 = it.get(BattleAgent.Companion.Var.X) as Double
                    val y2 = it.get(BattleAgent.Companion.Var.Y) as Double

                    return Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0) < 100.0
                }
            }

            return false
        }
    }
}
