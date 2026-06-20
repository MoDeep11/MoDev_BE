package modeep.modev.domain.catalog.registry

import modeep.modev.domain.catalog.registry.util.RegistryVersionSelector
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegistryVersionSelectorTest {
    @Test
    fun `selects first stable version`() {
        val latest =
            RegistryVersionSelector.latestStable(
                listOf("2.0.0-rc1", "1.5.0", "1.4.0"),
            )

        assertEquals("1.5.0", latest)
    }

    @Test
    fun `falls back to first version when only unstable versions exist`() {
        val latest =
            RegistryVersionSelector.latestStable(
                listOf("2.0.0-beta", "2.0.0-alpha"),
            )

        assertEquals("2.0.0-beta", latest)
    }
}
