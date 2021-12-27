# FlowTree

## What is FlowTree

FlowTree is framework to abstract programs into a decision tree flow chart. To illustrate some core concepts of FlowTree I will use a simple example:

```Kotlin
package test.kotlin.flow

import test.kotlin.model.User
import main.kotlin.annotations.Result
import main.kotlin.annotations.Start
import main.kotlin.annotations.Transition
import main.kotlin.FlowTree

@Result("user")
internal class CreateUser(val name: String, val age: Number, val country: String) : FlowTree<User>() {
    @Start
    @Transition("", "isSpecialPerson")
    fun assertUserUnique(name: String) {
        if (name == "Not Unique") {
            throw Exception();
        }
    }

    @Result("isSpecialPerson")
    @Transition("isSpecialPerson", "createTag")
    @Transition("!isSpecialPerson", "createUser")
    fun isSpecialPerson(name: String, age: Number): Boolean {
        return name == "Emile" && age == 23;
    }

    @Result("tag")
    @Transition("", "createUser")
    fun createTag(): String {
        return "special"
    }

    @Result("user")
    @Transition("", "END")
    fun createUser(name: String, age: Number, country: String, tag: String?): User {
        return User(name, age, country, tag);
    }
}
```

To execute this flow you should run:

```Kotlin
val user = CreateUser("Emile", 23, "ZA").execute<User>()
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

| Annotation    | Parameters                          | Explanation                                                                                                                                              |
|---------------|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@Start`      |                                     | The `@Start` annotation is used to signify the entry point of the flow.                                                                                  |
| `@Result`     | `resultString: String`              | The `@Result` annotation is used to specify the name of the property in which the result of the function will be stored                                  |
| `@Transition` | `condition: String`, `next: String` | The `@Transition` annotation is used to determine which function should be called next. If the condition is true, then the next function will be called. |

Apart from annotations your FlowTree should just extend the `FlowTree` class, the generic value should be set for the return value of the flow and the parameter name should override `resultKey` that stores the result of the Flow. In the example's case I am creating a `User` so my flow will return the created `User` that is stored in the `"user"` result