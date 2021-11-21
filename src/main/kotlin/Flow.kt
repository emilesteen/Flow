import kotlin.reflect.*

abstract class Flow<R> {
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Start

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Result(val name: String)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class Transition(val condition: String, val next: String)

    companion object {
        val flowTreeMap: MutableMap<KClass<*>, FlowTree> = mutableMapOf()
        val resultNameMap: MutableMap<KClass<*>, String> = mutableMapOf()
    }

    val environment = mutableMapOf<String, Any?>()

    inline fun <reified T: R>execute(): T {
        generateEnvironment()
        var flowTree: FlowTree? = determineFlowTree()

        while (true) {
            if (flowTree == null) {
                return getResult()
            } else {
                flowTree = executeStep(flowTree)
            }
        }
    }

    fun generateEnvironment() {
        val properties = this.javaClass.kotlin.members.filterIsInstance<KProperty<*>>()

        for (property in properties) {
            environment[property.name] = property.getter.call(this)
        }
    }

    fun determineFlowTree(): FlowTree {
        return flowTreeMap.getOrPut(this::class) { FlowTreeBuilder.buildFlowTree(this) }
    }

    fun executeStep(flowTree: FlowTree): FlowTree? {
        val arguments = generateArguments(flowTree.function, flowTree.parameters)
        callFunction(flowTree, arguments)

        return determineNextFlowTreeBranch(flowTree)
    }

    private fun generateArguments(
        function: KFunction<*>,
        parameters: List<KParameter>
    ): MutableMap<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        arguments[function.parameters.first()] = this

        for (parameter in parameters) {
            arguments[parameter] = environment[parameter.name.toString()]
        }

        return arguments
    }

    private fun callFunction(flowTree: FlowTree, arguments: Map<KParameter, Any?>) {
        if (flowTree.resultName == null) {
            flowTree.function.callBy(arguments)
        } else {
            environment[flowTree.resultName] = flowTree.function.callBy(arguments)
        }
    }

    private fun determineNextFlowTreeBranch(flowTree: FlowTree): FlowTree? {
        for (flowTransition in flowTree.flowTransitions) {
            if (isConditionTrueOrUndefined(flowTransition.condition, flowTransition.shouldNegateCondition)) {
                return flowTransition.flowTree
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
        val conditional = environment[condition]

        return if (shouldNegate!!) {
            conditional == false
        } else {
            conditional == true
        }
    }

    inline fun <reified T>getResult(): T
    {
        val resultName = determineResultName()

        when (val result = environment[resultName]) {
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