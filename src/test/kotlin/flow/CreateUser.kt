import annotations.Result
import annotations.Start
import annotations.Transition
import model.User

@Result("user")
internal class CreateUser(
    name: String,
    age: Number,
    country: String
) : FlowTree<User>(name, age, country) {
    @Start
    @Transition("isSpecialPerson")
    fun assertUserUnique(name: String) {
        if (name == "Not Unique") {
            throw Exception();
        }
    }

    @Result("isSpecialPerson")
    @Transition("createTag", "isSpecialPerson")
    @Transition("createUser", "!isSpecialPerson")
    fun isSpecialPerson(name: String, age: Number): Boolean {
        return name == "Emile" && age == 23;
    }

    @Result("tag")
    @Transition("createUser")
    fun createTag(): String {
        return "special"
    }

    @Result("user")
    @Transition("END")
    fun createUser(name: String, age: Number, country: String, tag: String?): User {
        return User(name, age, country, tag);
    }
}