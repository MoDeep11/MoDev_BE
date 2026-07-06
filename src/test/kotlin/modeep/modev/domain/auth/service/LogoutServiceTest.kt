package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.repository.RefreshTokenStore
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class LogoutServiceTest {
    private val refreshTokenStore = mock(RefreshTokenStore::class.java)
    private val logoutService = LogoutService(refreshTokenStore)

    @Test
    fun `deletes refresh token`() {
        logoutService.execute("refresh-token")

        verify(refreshTokenStore).delete("refresh-token")
    }

    @Test
    fun `does not delete when refresh token cookie is missing`() {
        logoutService.execute("")

        verify(refreshTokenStore, never()).delete(org.mockito.ArgumentMatchers.anyString())
    }
}
