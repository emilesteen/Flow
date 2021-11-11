import org.junit.Test
import flow.CreateUser
import model.User
import kotlin.test.assertEquals

internal class FlowTest {
    @Test
    fun testFlow() {
        val notSpecialUser = CreateUser("Emile", 22, "ZA").execute<User>()
        assertEquals(User("Emile", 22, "ZA", null), notSpecialUser)

        val specialUser = CreateUser("Emile", 23, "ZA").execute<User>()
        assertEquals(User("Emile", 23, "ZA", "special"), specialUser)
    }
}