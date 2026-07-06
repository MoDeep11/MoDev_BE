package modeep.modev.global.util

import org.springframework.security.core.Authentication

fun Authentication?.isAuthenticatedUser(): Boolean =
    this != null &&
        isAuthenticated &&
        name != "anonymousUser"
