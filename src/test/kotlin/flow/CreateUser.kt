package flow

import model.User
import Flow

class CreateUser(val name: String, val age: Number, val country: String) : Flow() {
    override val resultKey = "user"

    @Flow.Start
    @Flow.TransitionTemporary([
        "->isSpecialPerson"
    ])
    fun assertUserUnique(name: String) {
        if (name == "Not Unique") {
            throw Exception();
        }
    }

    @Flow.Result("isSpecialPerson")
    @Flow.TransitionTemporary([
        "isSpecialPerson->createTag",
        "!isSpecialPerson->createUser"
    ])
    fun isSpecialPerson(name: String, age: Number): Boolean {
        return name == "Emile" && age == 23;
    }

    @Flow.Result("tag")
    @Flow.TransitionTemporary([
        "->createUser",
    ])
    fun createTag(): String {
        return "special"
    }

    @Flow.Result("user")
    @Flow.TransitionTemporary([
        "->END"
    ])
    fun createUser(name: String, age: Number, country: String, tag: String?): User {
        return User(name, age, country, tag);
    }
}