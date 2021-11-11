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
            val transitionAnnotation = currentFunction.annotations.filterIsInstance<Flow.TransitionTemporary>().first()

            for (transition in transitionAnnotation.transitions) {
                flowTransitions.add(generateFlowTransition(transition, flowFunctions))
            }

            return flowTransitions
        }

        private fun generateFlowTransition(transition: String, flowFunctions: List<KFunction<*>>): FlowTransition {
            val transitionSplit = transition.split("->")
            val condition = transitionSplit[0]
            val functionName = transitionSplit[1]

            val shouldNegateCondition = determineShouldNegateConditionOrNull(condition)

            return FlowTransition(
                determineConditionOrNull(condition, shouldNegateCondition),
                shouldNegateCondition,
                determineFlowBranchOrNull(flowFunctions, functionName)
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

        private fun determineFlowBranchOrNull(flowFunctions: List<KFunction<*>>, functionName: String): FlowTree? {
            return if (functionName == "END") {
                null
            } else {
                val transitionFunction = flowFunctions.find { flowFunction -> flowFunction.name == functionName }

                if (transitionFunction == null) {
                    throw Exception("The function $functionName does not exist")
                } else {
                    buildFlowTreeBranch(transitionFunction, flowFunctions)
                }
            }
        }
    }
}