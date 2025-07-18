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
                .requestMatchers("/registro", "/login", "/css/**", "/js/**", "/imagenes/**", "/uploads/**",
                                 "/pelicula/{id}", "/accion", "/aventura", "/ciencia", "/comedia", "/drama").permitAll()

                .requestMatchers("/api/movies/search").permitAll()

                .requestMatchers("/registro-personal").hasRole("ADMIN")
                .requestMatchers("/peliculas/nueva").hasAnyRole("TRABAJADOR", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/peliculas").hasAnyRole("TRABAJADOR", "ADMIN")

                .requestMatchers("/mis-favoritos").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/peliculas/{peliculaId}/toggle-favorito").authenticated()

                .requestMatchers("/favoritos/export-excel").hasAnyRole("TRABAJADOR", "ADMIN")

                .requestMatchers("/admin/usuarios").hasRole("ADMIN")
                .requestMatchers("/admin/usuarios/export-excel").hasRole("ADMIN")

                .requestMatchers("/api/peliculas/{peliculaId}/progreso").authenticated()
                .requestMatchers("/api/movies/recommendations").authenticated() 

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .permitAll() 
                .defaultSuccessUrl("/", true) 
                .failureUrl("/login?error") 
            )
            .logout(logout -> logout
                .logoutUrl("/logout") 
                .logoutSuccessUrl("/login?logout") 
                .invalidateHttpSession(true) 
                .clearAuthentication(true) 
                .permitAll() 
            )
            .csrf(csrf -> csrf.disable()); 

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(usuarioService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        http.authenticationProvider(authenticationProvider);

        return http.build();
    }
}
