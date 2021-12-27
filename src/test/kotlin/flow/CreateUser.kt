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