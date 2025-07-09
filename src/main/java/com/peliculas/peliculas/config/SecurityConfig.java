// src/main/java/com/peliculas/peliculas/config/SecurityConfig.java
package com.peliculas.peliculas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.peliculas.peliculas.service.UsuarioService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioService usuarioService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SecurityConfig(UsuarioService usuarioService, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Rutas que son accesibles para TODOS (incluyendo no autenticados)
                .requestMatchers("/registro", "/login", "/css/**", "/js/**", "/imagenes/**", "/uploads/**",
                                 "/pelicula/{id}", "/accion", "/aventura", "/ciencia", "/comedia", "/drama").permitAll()

                // La ruta "/" (página principal) ahora requiere autenticación
                // Esto forzará a los usuarios no autenticados a ir a /login
                // Las APIs de búsqueda también son públicas
                .requestMatchers("/api/movies/search").permitAll()

                // Rutas que requieren roles específicos o autenticación
                .requestMatchers("/registro-personal").hasRole("ADMIN")
                .requestMatchers("/peliculas/nueva").hasAnyRole("TRABAJADOR", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/peliculas").hasAnyRole("TRABAJADOR", "ADMIN")

                .requestMatchers("/mis-favoritos").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/peliculas/{peliculaId}/toggle-favorito").authenticated()

                .requestMatchers("/favoritos/export-excel").hasAnyRole("TRABAJADOR", "ADMIN")

                .requestMatchers("/admin/usuarios").hasRole("ADMIN")
                .requestMatchers("/admin/usuarios/export-excel").hasRole("ADMIN")

                .requestMatchers("/api/peliculas/{peliculaId}/progreso").authenticated()
                .requestMatchers("/api/movies/recommendations").authenticated() // Las recomendaciones requieren autenticación

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // La página de login personalizada
                .permitAll() // Permitir acceso a la página de login
                .defaultSuccessUrl("/", true) // Redirigir a la página principal después de un login exitoso
                .failureUrl("/login?error") // Redirigir a login con error si falla
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout") // Redirigir a login después de cerrar sesión
                .invalidateHttpSession(true) // Invalidar la sesión HTTP
                .clearAuthentication(true) // Limpiar la autenticación
                .permitAll() // Permitir acceso a la URL de logout
            )
            .csrf(csrf -> csrf.disable()); // Deshabilitar CSRF para simplificar (considerar habilitar en producción)

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(usuarioService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        http.authenticationProvider(authenticationProvider);

        return http.build();
    }
}
