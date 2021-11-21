import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

abstract class Flow {
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Start

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Result(val resultName: String)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class Transition(val condition: String, val next: String)

    companion object {
        val flowTreeMap: MutableMap<KClass<*>, FlowTree> = mutableMapOf()
    }

    abstract val resultKey: String

    val environment = mutableMapOf<String, Any?>()

    inline fun <reified T: Any>execute(): T {
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
        val result = environment[resultKey]

        if (result == null) {
            throw Exception("No result")
        } else {
            return result as T
        }
    }
}