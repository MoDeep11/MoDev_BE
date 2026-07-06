package modeep.modev.global.security.config

import modeep.modev.global.filter.MdcFilter
import modeep.modev.global.ratelimit.RateLimitFilter
import modeep.modev.global.security.handler.CustomAccessDeniedHandler
import modeep.modev.global.security.jwt.JwtAuthenticationEntryPoint
import modeep.modev.global.security.jwt.JwtAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter,
    private val mdcFilter: MdcFilter,
    private val rateLimitFilter: RateLimitFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val accessDeniedHandler: CustomAccessDeniedHandler,
    private val corsConfigurationSource: CorsConfigurationSource,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {
                it.configurationSource(corsConfigurationSource)
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/auth/signup",
                        "/auth/login",
                        "/auth/token/refresh",
                        "/auth/email/send",
                        "/auth/email/verify",
                        "/auth/logout",
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST, "/projects").permitAll()
                    .requestMatchers(HttpMethod.GET, "/catalog/fields").permitAll()
                    .requestMatchers(HttpMethod.GET, "/catalog/stacks").permitAll()
                    .requestMatchers(HttpMethod.GET, "/catalog/dependencies").permitAll()
                    .requestMatchers(
                        "/projects/structures",
                        "/projects/structures/**",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(mdcFilter, JwtAuthenticationFilter::class.java)
            .addFilterAfter(rateLimitFilter, MdcFilter::class.java)

        return http.build()
    }

    @Bean
    fun mdcFilterRegistration(mdcFilter: MdcFilter): FilterRegistrationBean<MdcFilter> =
        FilterRegistrationBean(mdcFilter).apply {
            isEnabled = false
        }

    @Bean
    fun jwtFilterRegistration(jwtFilter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> =
        FilterRegistrationBean(jwtFilter).apply {
            isEnabled = false
        }
}
