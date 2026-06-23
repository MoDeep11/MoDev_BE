package modeep.modev.domain.catalog.registry

import modeep.modev.domain.catalog.registry.util.RegistryVersionSelector
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegistryVersionSelectorTest {
    @Test
    fun `selects latest stable version after sorting descending`() {
        val latest =
            RegistryVersionSelector.latestStable(
                listOf("1.5.0", "10.0.0-beta", "2.0.0", "1.4.0"),
            )

        assertEquals("2.0.0", latest)
    }

    @Test
    fun `falls back to latest version when only unstable versions exist`() {
        val latest =
            RegistryVersionSelector.latestStable(
                listOf("2.0.0-alpha", "10.0.0-beta", "2.0.0-beta"),
            )

        assertEquals("10.0.0-beta", latest)
    }
}
