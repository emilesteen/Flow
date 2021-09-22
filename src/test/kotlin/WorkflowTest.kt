import org.junit.Test

internal class WorkflowTest {
    @Test
    fun testWorkflow() {
        val user = CreateUser("Emile", 22, "ZA").execute().getResult()

        println(user.toString())
    }
}

class CreateUser(val name: String, val age: Number, val country: String) : Workflow() {
    @Start
    @Result("isUserUnique")
    @TransitionTemporary([
            "->isSpecialPerson"
    ])
    fun assertUserUnique(name: String): Boolean {
        if (name == "Not Unique") {
            throw Exception();
        } else {
            return true
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

    override fun getResult(): User {
        val result = this.environment["user"]

        if (result is User) {
            return result
        } else {
            throw Exception("Unexpected result")
        }
    }
}

class User(val name: String, val age: Number, val country: String, val tag: String?)