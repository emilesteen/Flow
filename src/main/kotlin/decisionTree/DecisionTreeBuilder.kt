package decisionTree

import annotations.Result
import annotations.Start
import annotations.Transition
import FlowTree
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class DecisionTreeBuilder {
    companion object {
        fun buildDecisionTree(flowTree: FlowTree<*>): DecisionTree {
            val flowTreeFunctions: List<KFunction<*>> = flowTree::class.members.filterIsInstance<KFunction<*>>()
            val start = findStart(flowTreeFunctions)

            if (start == null) {
                throw Exception("No Start found.")
            } else {
                return buildDecisionTreeBranch(start, flowTreeFunctions)
            }
        }

        private fun findStart(flowTreeFunctions: List<KFunction<*>>): KFunction<*>? {
            return flowTreeFunctions.find {
                function -> function.annotations.filterIsInstance<Start>().isNotEmpty()
            }
        }

        private fun buildDecisionTreeBranch(
            currentFunction: KFunction<*>,
            flowFunctions: List<KFunction<*>>
        ): DecisionTree {
            return DecisionTree(
                currentFunction,
                determineResultNameOrNull(currentFunction),
                generateParameters(currentFunction),
                generateDecisionTreeTransitions(currentFunction, flowFunctions)
            )
        }

        private fun determineResultNameOrNull(currentFunction: KFunction<*>): String? {
            return currentFunction.annotations.filterIsInstance<Result>().firstOrNull()?.name
        }

        private fun generateParameters(currentFunction: KFunction<*>): List<KParameter> {
            return currentFunction.parameters.drop(1)
        }

        private fun generateDecisionTreeTransitions(
            currentFunction: KFunction<*>,
            flowTreeFunctions: List<KFunction<*>>
        ): List<DecisionTreeTransition> {
            val decisionTreeTransitions = mutableListOf<DecisionTreeTransition>()
            val transitions = currentFunction.annotations.filterIsInstance<Transition>()

            for (transition in transitions) {
                decisionTreeTransitions.add(generateDecisionTreeTransition(transition, flowTreeFunctions))
            }

            return decisionTreeTransitions
        }

        private fun generateDecisionTreeTransition(
            transition: Transition,
            flowTreeFunctions: List<KFunction<*>>
        ): DecisionTreeTransition {
            val shouldNegateCondition = determineShouldNegateConditionOrNull(transition.condition)

            return DecisionTreeTransition(
                determineConditionOrNull(transition.condition, shouldNegateCondition),
                shouldNegateCondition,
                determineNextDecisionTreeBranchOrNull(flowTreeFunctions, transition.next)
            )
        }

        private fun determineShouldNegateConditionOrNull(condition: String): Boolean? {
            return if (condition == "") {
                null
            } else {
                condition.first() == '!'
            }
        }

        private fun determineConditionOrNull(condition: String, shouldNegateCondition: Boolean?): String? {
            return when {
                shouldNegateCondition == null -> {
                    null
                }
                shouldNegateCondition -> {
                    condition.drop(1)
                }
                else -> {
                    condition
                }
            }
        }

        private fun determineNextDecisionTreeBranchOrNull(
            flowFunctions: List<KFunction<*>>,
            functionName: String
        ): DecisionTree? {
            return if (functionName == "END") {
                null
            } else {
                determineNextDecisionTreeBranch(flowFunctions, functionName)
            }
        }

        private fun determineNextDecisionTreeBranch(
            flowFunctions: List<KFunction<*>>,
            functionName: String
        ): DecisionTree {
            val transitionFunction = flowFunctions.find { flowFunction -> flowFunction.name == functionName }

            if (transitionFunction == null) {
                throw Exception("The function $functionName does not exist")
            } else {
                return buildDecisionTreeBranch(transitionFunction, flowFunctions)
            }
        }
    }
}