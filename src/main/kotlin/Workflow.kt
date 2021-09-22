import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

//interface WorkflowInterface
//fun <T : Workflow> T.execute(): T {
//    val kClass = this.javaClass.kotlin
//    val workflowEnvironmentMap = mutableMapOf<String, Any?>()
//
//    val properties = kClass.members.filterIsInstance<KProperty<*>>()
//    val functions = kClass.members.filterIsInstance<KFunction<*>>()
//
//    for (property in properties) {
//        workflowEnvironmentMap[property.name.toString()] = property.getter.call(this)
//    }
//
//    for (function in functions) {
//        callFunction(function, workflowEnvironmentMap)
//    }
//
//    return this;
//}

abstract class Workflow {
    fun execute() {
        val kClass = this.javaClass.kotlin
        val workflowEnvironmentMap = mutableMapOf<String, Any?>()

        val properties = kClass.members.filterIsInstance<KProperty<*>>()
        val functions = kClass.members.filterIsInstance<KFunction<*>>()

        for (property in properties) {
            workflowEnvironmentMap[property.name.toString()] = property.getter.call(this)
        }

        for (function in functions) {
            this.callFunction(function, workflowEnvironmentMap)
        }
    }

    private fun callFunction(function: KFunction<*>, workflowEnvironmentMap: Map<String, Any?>) {
        val args = mutableMapOf<KParameter, Any?>()

        var parameters = function.parameters
        args[parameters.first()] = this
        parameters = parameters.drop(1)

        for (parameter in parameters) {
            args[parameter] = workflowEnvironmentMap[parameter.name.toString()]
        }

        function.callBy(args)
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
annotation class Transition(val transitionString: String, val condition: String)
