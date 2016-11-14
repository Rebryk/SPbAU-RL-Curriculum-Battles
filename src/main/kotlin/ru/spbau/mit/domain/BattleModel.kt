package ru.spbau.mit.domain

import burlap.mdp.core.StateTransitionProb
import burlap.mdp.core.action.Action
import burlap.mdp.core.state.State
import burlap.mdp.singleagent.model.statemodel.FullStateModel
import ru.spbau.mit.bot.BattleBot

class BattleModel(val physicsParameters: BattlePhysicsParameters, val bot: BattleBot) : FullStateModel {
    override fun stateTransitions(state: State?, action: Action?): MutableList<StateTransitionProb> {
        return FullStateModel.Helper.deterministicTransition(this, state, action)
    }

    override fun sample(state: State?, action: Action?): State {
        if (state == null || action == null) {
            throw RuntimeException("State and action have to be not null!")
        }

        val newState = state.copy() as BattleState

        performAction(newState.touchAgent(), action.actionName())
        performAction(newState.touchEnemy(), bot.nextAction(state, physicsParameters))

        return newState
    }

    /**
     * Applies agent action to the current state
     * @param agent agent which performs action
     * @param actionName name of the action
     */
    private fun performAction(agent: BattleAgent, actionName: String) {
        when (actionName) {
            BattleAgent.Companion.Action.TURN_LEFT -> rotate(agent, physicsParameters.unitRotationAngle)
            BattleAgent.Companion.Action.TURN_RIGHT -> rotate(agent, -physicsParameters.unitRotationAngle)
            BattleAgent.Companion.Action.GO_FORWARD -> move(agent, agent.angle + Math.PI / 2)
            BattleAgent.Companion.Action.GO_BACKWARD -> move(agent, agent.angle - Math.PI / 2)
            BattleAgent.Companion.Action.GO_LEFT -> move(agent, agent.angle + Math.PI)
            BattleAgent.Companion.Action.GO_RIGHT -> move(agent, agent.angle)
            BattleAgent.Companion.Action.SKIP -> { /* just skip */ }
            else -> throw UnsupportedOperationException("Action %s isn't implemented!".format(actionName))
        }
    }

    private fun move(agent: BattleAgent, angle: Double) {
        agent.x += Math.cos(angle) * physicsParameters.unitSpeed
        agent.y += Math.sin(angle) * physicsParameters.unitSpeed

        agent.x = Math.max(0.0, Math.min(agent.x, physicsParameters.width))
        agent.y = Math.max(0.0, Math.min(agent.y, physicsParameters.height))
    }

    private fun rotate(agent: BattleAgent, angle: Double) {
        agent.angle += angle

        // TODO: fix module
        if (agent.angle < 0) {
            agent.angle += 2 * Math.PI
        } else if (agent.angle >= 2 * Math.PI) {
            agent.angle -= 2 * Math.PI
        }
    }
}