import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class FlowTree(
    val function: KFunction<*>,
    val resultName: String?,
    val parameters: List<KParameter>,
    val flowTransitions: List<FlowTransition>
)