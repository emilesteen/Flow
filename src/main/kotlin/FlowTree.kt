import annotations.Result
import decisionTree.DecisionTree
import decisionTree.DecisionTreeBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

abstract class FlowTree<R>(vararg arguments: Any?) {
    companion object {
        val decisionTreeMap: MutableMap<KClass<*>, DecisionTree> = mutableMapOf()
        val resultNameMap: MutableMap<KClass<*>, String> = mutableMapOf()
    }

    val state = mutableMapOf<String, Any?>()

    init {
        val parameters = this.javaClass.kotlin.constructors.first().parameters

        parameters.forEachIndexed { index, parameter ->
            this.state[parameter.name!!] =
                arguments[index] // TODO: Check that argument matches parameter type
        }
    }

    inline fun <reified T : R> execute(): T {
        var decisionTree: DecisionTree? = determineDecisionTree()

        do {
            decisionTree = executeStep(decisionTree!!);
        } while (decisionTree != null)

        return getResult()
    }

    fun determineDecisionTree(): DecisionTree {
        return decisionTreeMap.getOrPut(this::class) {
            DecisionTreeBuilder.buildDecisionTree(this)
        }
    }

    fun executeStep(DecisionTree: DecisionTree): DecisionTree? {
        val arguments = generateArguments(DecisionTree.function, DecisionTree.parameters)
        callFunction(DecisionTree, arguments)

        return determineNextDecisionTreeBranch(DecisionTree)
    }

    private fun generateArguments(
        function: KFunction<*>,
        parameters: List<KParameter>,
    ): MutableMap<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        arguments[function.parameters.first()] = this

        for (parameter in parameters) {
            arguments[parameter] = state[parameter.name.toString()]
        }

        return arguments
    }

    private fun callFunction(decisionTree: DecisionTree, arguments: Map<KParameter, Any?>) {
        if (decisionTree.resultName == null) {
            decisionTree.function.callBy(arguments)
        } else {
            state[decisionTree.resultName] = decisionTree.function.callBy(arguments)
        }
    }

    private fun determineNextDecisionTreeBranch(decisionTree: DecisionTree): DecisionTree? {
        for (decisionTreeTransition in decisionTree.decisionTreeTransitions) {
            if (
                isConditionTrueOrUndefined(
                    decisionTreeTransition.condition,
                    decisionTreeTransition.shouldNegateCondition
                )
            ) {
                return decisionTreeTransition.decisionTree
            }
        }

        throw Exception("No valid transition")
    }

    private fun isConditionTrueOrUndefined(condition: String?, shouldNegate: Boolean?): Boolean {
        return if (condition == null) {
            return true
        } else {
            isConditionTrue(condition, shouldNegate)
        }
    }

    private fun isConditionTrue(condition: String, shouldNegate: Boolean?): Boolean {
        val conditional = state[condition]

        return if (shouldNegate!!) {
            conditional == false
        } else {
            conditional == true
        }
    }

    inline fun <reified T> getResult(): T {
        val resultName = determineResultName()

        when (val result = state[resultName]) {
            null -> {
                throw Exception("The workflow has no result")
            }
            is T -> {
                return result
            }
            else -> {
                val resultType = result::class.simpleName
                val expectedType = T::class.simpleName

                throw Exception("Result is of type $resultType, but was expecting type $expectedType")
            }
        }
    }

    fun determineResultName(): String {
        return resultNameMap.getOrPut(this::class) {
            val result = this::class.annotations.filterIsInstance<Result>().firstOrNull()

            if (result == null) {
                val workflowName = this::class.simpleName

                throw Exception("The workflow $workflowName has no result annotation")
            } else {
                return result.name
            }
        }
    }
}