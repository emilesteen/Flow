import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

abstract class Flow {
    companion object {
        val flowTreeMap: MutableMap<KClass<*>, FlowTree> = mutableMapOf()
    }

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Start

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Result(val resultName: String)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class Transition(val condition: String, val functionName: String)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TransitionTemporary(val transitions: Array<String>)

    abstract val resultKey: String

    val environment = mutableMapOf<String, Any?>()

    inline fun <reified T: Any>execute(): T {
        this.generateEnvironment()
        var flowTree: FlowTree? = this.determineFlowTree()

        while (true) {
            if (flowTree == null) {
                return this.getResult()
            } else {
                flowTree = executeStep(flowTree)
            }
        }
    }

    fun determineFlowTree(): FlowTree {
        return if (flowTreeMap.containsKey(this::class)) {
            flowTreeMap[this::class]!!
        } else {
            val flowTree = FlowTreeBuilder.buildFlowTree(this)
            flowTreeMap[this::class] = flowTree

            flowTree
        }
    }

    fun executeStep(flowTree: FlowTree): FlowTree? {
        val arguments = this.generateArguments(flowTree.function, flowTree.parameters)

        if (flowTree.resultName == null) {
            flowTree.function.callBy(arguments)
        } else {
            this.environment[flowTree.resultName] = flowTree.function.callBy(arguments)
        }

        for (flowTransition in flowTree.flowTransitions) {
            if (isConditionTrueOrUndefined(flowTransition.condition, flowTransition.shouldNegateCondition)) {
                return flowTransition.flowTree
            }
        }

        throw Exception("No valid transition")
    }

    fun generateEnvironment() {
        val properties = this.javaClass.kotlin.members.filterIsInstance<KProperty<*>>()

        for (property in properties) {
            this.environment[property.name] = property.getter.call(this)
        }
    }

    private fun isConditionTrueOrUndefined(condition: String?, shouldNegate: Boolean?): Boolean {
        return if (condition == null) {
            return true
        } else {
            val conditional = this.environment[condition]

            if (shouldNegate!!) {
                conditional == false
            } else {
                conditional == true
            }
        }
    }

    private fun generateArguments(
        function: KFunction<*>,
        parameters: List<KParameter>
    ): MutableMap<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        arguments[function.parameters.first()] = this

        for (parameter in parameters) {
            arguments[parameter] = this.environment[parameter.name.toString()]
        }

        return arguments
    }

    inline fun <reified T>getResult(): T
    {
        val result = this.environment[this.resultKey]

        if (result == null) {
            throw Exception("No result")
        } else {
            return result as T
        }
    }
}