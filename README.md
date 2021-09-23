# Flow

## What is Flow

Flow is framework to abstract programs into a decision tree flow chart. To illustrate some core concepts of Flow I will use a simple example:

```Kotlin
package flow

import Result
import Start
import TransitionTemporary
import model.User
import Flow

class CreateUser(val name: String, val age: Number, val country: String) : Flow<User>() {
    override val resultKey = "user"

    @Start
    @TransitionTemporary([
        "->isSpecialPerson"
    ])
    fun assertUserUnique(name: String) {
        if (name == "Not Unique") {
            throw Exception();
        }
    }

    @Result("isSpecialPerson")
    @TransitionTemporary([
        "isSpecialPerson->createTag",
        "!isSpecialPerson->createUser"
    ])
    fun isSpecialPerson(name: String, age: Number): Boolean {
        return name == "Emile" && age == 23;
    }

    @Result("tag")
    @TransitionTemporary([
        "->createUser",
    ])
    fun createTag(): String {
        return "special"
    }

    @Result("user")
    @TransitionTemporary([
        "->END"
    ])
    fun createUser(name: String, age: Number, country: String, tag: String?): User {
        return User(name, age, country, tag);
    }
}
```

To execute this flow you should run:

```Kotlin
CreateUser("Emile", 23, "ZA").execute()
```

This is a simple flow that is creating a `User` object. In this flow we have 4 steps:

1. We assert that the user is not unique
2. We determine if the user is a special user
3. If the user is a special user, we create a "special" tag for the user
4. Finally, we create the user

Now some things to notice:

- By utilising Flow we are able to keep functions small and simple
- Most of the nested conditional logic is extracted and handled in the Transition

## How does it work

The main building blocks of Flow is the use of annotations.

Annotation|Parameters|Explanation
----------|----------|-----------
`@Start`| |The `@Start` annotation is used to signify the entry point of the flow.
`@Result`|`resultString: String`| The `@Result` annotation is used to specify the name of the property in which the result of the function will be stored
`@TransitionTemporary`|`transitions: Array<String>`| The `@TransitionTemporary` annotation is used to determine which function should be called next. Each possible transition should be added in the form `"condition->next"`, where condition is a boolean variable and based on it's value the respective `next` function will be called. Conditions can also be negated by adding a `!` prefix to the condition like: `"!condition->next"`. Note: `TransitionTemporary(transitions: Array<String>)` will be replaced by `@Transition(condition: String, transitionString: String)` once Kotlin releases support for repeatable runtime annotations.

Apart from annotations your Flow should just extend the `Flow` class, the generic value should be set for the return value of the flow and the parameter name should override `resultKey` that stores the result of the Flow. In the example's case I am creating a `User` so my flow will return the created `User` that is stored in the `"user"` result