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