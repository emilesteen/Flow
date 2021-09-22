import org.junit.Test

internal class WorkflowTest {
    @Test
    fun testWorkflow() {
        val user = WorkflowUserCreate("Emile", 23, "ZA").execute()
    }
}

class WorkflowUserCreate(val name: String, val age: Number, val country: String) : Workflow() {
    @Start
    @Transition("isSpecialPerson", "")
    fun assertUserUnique(name: String) {
        if (name == "Not Unique") {
            throw Exception();
        } else {
            // The user is unique.
        }
    }

    @Transition("createTag", "")
    fun isSpecialPerson(name: String, age: Number): Boolean {
        return name == "Emile" && age == 23;
    }

    @Transition("createUser", "")
    @Result("tag")
    fun createTag(): String {
        return "special"
    }

    @Result("user")
    fun createUser(name: String, age: Number, country: String, tag: String?): User {
        return User(name, age, country, tag);
    }
}

class User(name: String, age: Number, country: String, tag: String?)