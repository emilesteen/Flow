import org.junit.Test
import flow.CreateUser
import kotlin.test.assertEquals

internal class FlowTest {
    @Test
    fun testFlow() {
        val notSpecialUser = CreateUser("Emile", 22, "ZA").execute()
        assertEquals(notSpecialUser.tag, null)

        val specialUser = CreateUser("Emile", 23, "ZA").execute()
        assertEquals(specialUser.tag, "special")
    }
}