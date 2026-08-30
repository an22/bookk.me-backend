package library.permissions

import com.bookk.core.domain.entity.Error
import com.bookk.core.test.given
import com.bookk.core.test.runUnitTest
import com.bookk.core.test.then
import com.bookk.core.test.whenn
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

internal class ObjectPermissionTest {

    @Test
    fun `should allow when granted permission meets the minimum regardless of assignee`() = runUnitTest {
        given()
        val actorId = Uuid.random()
        val assigneeId = Uuid.random()

        whenn()
        val result = runCatching {
            ObjectPermission.EDIT.int.assertOrOwner(ObjectPermission.EDIT, actorId = actorId, assigneeId = assigneeId)
        }

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should allow when granted permission exceeds the minimum regardless of assignee`() = runUnitTest {
        given()
        val actorId = Uuid.random()
        val assigneeId = Uuid.random()

        whenn()
        val result = runCatching {
            ObjectPermission.OWNER.int.assertOrOwner(ObjectPermission.EDIT, actorId = actorId, assigneeId = assigneeId)
        }

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should allow a read holder acting on their own resource`() = runUnitTest {
        given()
        val actorId = Uuid.random()

        whenn()
        val result = runCatching {
            ObjectPermission.READ.int.assertOrOwner(ObjectPermission.EDIT, actorId = actorId, assigneeId = actorId)
        }

        then()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should reject a read holder acting on someone elses resource`() = runUnitTest {
        given()
        val actorId = Uuid.random()
        val assigneeId = Uuid.random()

        whenn()
        val result = runCatching {
            ObjectPermission.READ.int.assertOrOwner(ObjectPermission.EDIT, actorId = actorId, assigneeId = assigneeId)
        }

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }

    @Test
    fun `should reject a missing grant even when acting on their own resource`() = runUnitTest {
        given()
        val actorId = Uuid.random()

        whenn()
        val result = runCatching {
            null.assertOrOwner(ObjectPermission.EDIT, actorId = actorId, assigneeId = actorId)
        }

        then()
        assertTrue(result.exceptionOrNull() is Error.OperationNotAllowed)
    }
}
