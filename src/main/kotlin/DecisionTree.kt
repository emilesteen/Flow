package main.kotlin

import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class DecisionTree(
    val function: KFunction<*>,
    val resultName: String?,
    val parameters: List<KParameter>,
    val decisionTreeTransitions: List<DecisionTreeTransition>
)