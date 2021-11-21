import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class FlowTreeBuilder {
    companion object {
        fun buildFlowTree(flow: Flow): FlowTree {
            val flowFunctions: List<KFunction<*>> = flow::class.members.filterIsInstance<KFunction<*>>()

            val start = flowFunctions.find {
                function -> function.annotations.filterIsInstance<Flow.Start>().isNotEmpty()
            }

            if (start == null) {
                throw Exception("No Start found.")
            } else {
                return buildFlowTreeBranch(start, flowFunctions)
            }
        }

        private fun buildFlowTreeBranch(currentFunction: KFunction<*>, flowFunctions: List<KFunction<*>>): FlowTree {
            return FlowTree(
                currentFunction,
                determineResultNameOrNull(currentFunction),
                generateParameters(currentFunction),
                generateFlowTransitions(currentFunction, flowFunctions)
            )
        }

        private fun determineResultNameOrNull(currentFunction: KFunction<*>): String? {
            return currentFunction.annotations.filterIsInstance<Flow.Result>().firstOrNull()?.resultName
        }

        private fun generateParameters(currentFunction: KFunction<*>): List<KParameter> {
            return currentFunction.parameters.drop(1)
        }

        private fun generateFlowTransitions(
            currentFunction: KFunction<*>,
            flowFunctions: List<KFunction<*>>
        ): List<FlowTransition> {
            val flowTransitions = mutableListOf<FlowTransition>()
            val transitions = currentFunction.annotations.filterIsInstance<Flow.Transition>()

            for (transition in transitions) {
                flowTransitions.add(generateFlowTransition(transition, flowFunctions))
            }

            return flowTransitions
        }

        private fun generateFlowTransition(transition: Flow.Transition, flowFunctions: List<KFunction<*>>): FlowTransition {
            val shouldNegateCondition = determineShouldNegateConditionOrNull(transition.condition)

            return FlowTransition(
                determineConditionOrNull(transition.condition, shouldNegateCondition),
                shouldNegateCondition,
                determineNextFlowTreeBranchOrNull(flowFunctions, transition.next)
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

        private fun determineNextFlowTreeBranchOrNull(
            flowFunctions: List<KFunction<*>>,
            functionName: String
        ): FlowTree? {
            return if (functionName == "END") {
                null
            } else {
                determineNextFlowTreeBranch(flowFunctions, functionName)
            }
        }

        private fun determineNextFlowTreeBranch(flowFunctions: List<KFunction<*>>, functionName: String): FlowTree {
            val transitionFunction = flowFunctions.find { flowFunction -> flowFunction.name == functionName }

            if (transitionFunction == null) {
                throw Exception("The function $functionName does not exist")
            } else {
                return buildFlowTreeBranch(transitionFunction, flowFunctions)
            }
        }
    }
}