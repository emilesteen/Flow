package decisionTree

class DecisionTreeTransition(
    val condition: String?,
    val shouldNegateCondition: Boolean?,
    val decisionTree: DecisionTree?
)