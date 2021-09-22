import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

abstract class Flow<T> {
    abstract val resultKey: String

    private val _kProperties: List<KProperty<*>> = this.javaClass.kotlin.members.filterIsInstance<KProperty<*>>()
    private val _kFunctions: List<KFunction<*>> = this.javaClass.kotlin.members.filterIsInstance<KFunction<*>>()
    private val _environment = mutableMapOf<String, Any?>()

    fun execute(): Flow<T> {
        this.generateEnvironment()
        val start = this._kFunctions.find { kFunction -> kFunction.annotations.filterIsInstance<Start>().isNotEmpty() }

        if (start == null) {
            throw Exception("No Start found.")
        } else {
            callFunction(start)
        }

        return this
    }

    fun getResult(): T
    {
        val result = this._environment[this.resultKey]

        if (result == null) {
            throw Exception("No result")
        } else {
            return result as T
        }
    }

    protected fun get(key: String): Any? {
        return _environment[key]
    }

    private fun generateEnvironment() {
        for (kProperty in this._kProperties) {
            this._environment[kProperty.name] = kProperty.getter.call(this)
        }
    }

    private fun callFunction(kFunction: KFunction<*>, ) {
        val arguments = this.generateArguments(kFunction)
        val resultAnnotation = kFunction.annotations.filterIsInstance<Result>().firstOrNull()

        if (resultAnnotation == null) {
            kFunction.callBy(arguments)
        } else {
            this._environment[resultAnnotation.resultString] = kFunction.callBy(arguments)
        }

        transitionTemporary(kFunction)
    }

    private fun transition(kFunction: KFunction<*>) {
        val transitionAnnotations = kFunction.annotations.filterIsInstance<Transition>()

        for (transitionAnnotation in transitionAnnotations) {
            if (transitionAnnotation.condition == "") {
                if (transitionAnnotation.transitionString == "End") {
                    return
                } else {
                    callTransitionFunction(transitionAnnotation.transitionString)
                }
            } else if (this.isConditionTrue(transitionAnnotation.condition)) {
                if (transitionAnnotation.transitionString == "End") {
                    return
                } else {
                    callTransitionFunction(transitionAnnotation.transitionString)
                }
            }
        }

        throw Exception("No valid transition")
    }

    private fun transitionTemporary(kFunction: KFunction<*>) {
        val transitionAnnotation = kFunction.annotations.filterIsInstance<TransitionTemporary>().first()

        for (transition in transitionAnnotation.transitions) {
            val transitionSplit = transition.split("->")
            val condition = transitionSplit[0]
            val transitionString = transitionSplit[1]

            if (condition == "") {
                if (transitionString == "END") {
                    return
                } else {
                    callTransitionFunction(transitionString)
                }
            } else if (this.isConditionTrue(condition)) {
                if (transitionString == "END") {
                    return
                } else {
                    callTransitionFunction(transitionString)
                }
            }
        }
    }

    private fun callTransitionFunction(transitionString: String) {
        val next = _kFunctions.first { kFunction -> kFunction.name == (transitionString) }

        callFunction(next)
    }

    private fun isConditionTrue(condition: String): Boolean {
        return if (this.shouldNegate(condition)) {
            val conditional = this._environment[condition.drop(1)]

            conditional == false
        } else {
            val conditional = this._environment[condition]

            conditional == true
        }
    }

    private fun shouldNegate(condition: String): Boolean
    {
        return condition.first() == '!'
    }

    private fun generateArguments(
        kFunction: KFunction<*>,
    ): MutableMap<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>()

        var parameters = kFunction.parameters
        arguments[parameters.first()] = this
        parameters = parameters.drop(1)

        for (parameter in parameters) {
            arguments[parameter] = this._environment[parameter.name.toString()]
        }

        return arguments
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Start

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Result(val resultString: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Transition(val condition: String, val transitionString: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TransitionTemporary(val transitions: Array<String>)