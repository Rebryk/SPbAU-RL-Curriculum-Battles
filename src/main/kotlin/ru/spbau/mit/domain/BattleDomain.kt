package ru.spbau.mit.domain

import burlap.mdp.auxiliary.DomainGenerator
import burlap.mdp.core.Domain
import burlap.mdp.core.action.UniversalActionType
import burlap.mdp.core.oo.OODomain
import burlap.mdp.core.oo.propositional.PropositionalFunction
import burlap.mdp.core.oo.state.OOState
import burlap.mdp.singleagent.oo.OOSADomain

class BattleDomain : DomainGenerator {
    override fun generateDomain(): Domain? {
        val domain = OOSADomain()

        domain.addStateClass(BattleAgent.Static.CLASS, BattleAgent::class.java)
                .addStateClass(BattleAgent.Static.CLASS_ENEMY, BattleAgent.Enemy::class.java)

        domain.addActionType(UniversalActionType(BattleAgent.Static.Action.GO_FORWARD))
                .addActionType(UniversalActionType(BattleAgent.Static.Action.GO_BACKWARD))
                .addActionType(UniversalActionType(BattleAgent.Static.Action.GO_LEFT))
                .addActionType(UniversalActionType(BattleAgent.Static.Action.GO_RIGHT))

        OODomain.Helper.addPfsToDomain(domain, generatePropositionalFunctions())

        // TODO: add full state model
        // TODO: add reward function
        // TODO: add terminal function

        return domain
    }

    /**
     * Describes propositional functions names
     */
    object Static {
        val UNIT_IS_DEAD = "UNIT_IS_DEAD"
        val ENEMY_IS_DEAD = "ENEMY_IS_DEAD"
    }

    /** Generate all propositional functions
     * @return propositional functions
     */
    fun generatePropositionalFunctions(): List<PropositionalFunction> {
        return listOf(UnitIsDead(Static.UNIT_IS_DEAD), EnemyIsDead(Static.ENEMY_IS_DEAD))
    }

    open class AgentIsDead : PropositionalFunction {
        constructor(name: String, className: String) : super(name, arrayOf(className))

        /**
         * Propositional function to check if agent is dead
         * @return true - if agent is dead, otherwise - false
         */
        override fun isTrue(state: OOState?, vararg params: String?): Boolean {
            val agent = state?.`object`(params[0])
            agent?.let {
                return (it.get(BattleAgent.Static.Var.HP) as Double) == 0.0
            }

            return false
        }
    }

    class UnitIsDead : AgentIsDead {
        constructor(name: String) : super(name, BattleAgent.Static.CLASS)
    }

    class EnemyIsDead : AgentIsDead {
        constructor(name: String) : super(name, BattleAgent.Static.CLASS_ENEMY)
    }
}
