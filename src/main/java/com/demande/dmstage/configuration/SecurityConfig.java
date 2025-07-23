package com.demande.dmstage.configuration;

import com.demande.dmstage.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@SuppressWarnings({"deprecation", "removal"})
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        System.out.println("🔐 SECURITY CONFIG PRODUCTION - SÉCURITÉ RÉTABLIE");
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("🔐 CONFIGURATION SECURITY PRODUCTION");
        System.out.println("✅ Endpoints publics configurés");
        System.out.println("🔒 Endpoints protégés configurés");
        
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Endpoints publics (SANS AUTHENTIFICATION)
                .antMatchers("/api/auth/register").permitAll()
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers("/api/auth/create-admin").permitAll()
                .antMatchers("/api/auth/create-admin-bypass").permitAll()  // Garder pour backup
                .antMatchers("/api/auth/system-status").permitAll()
                .antMatchers("/api/demandes/suivi").permitAll()  // Suivi public par email
                
                // Endpoints admin uniquement
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints utilisateur authentifié (USER ou ADMIN)
                .antMatchers("/api/auth/profile").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/demandes/mes-demandes").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/demandes/export/excel").hasAnyRole("USER", "ADMIN")
                .antMatchers("/api/demandes/**").hasAnyRole("USER", "ADMIN")
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            .and()
            .httpBasic();
            
        System.out.println("🔐 SÉCURITÉ PRODUCTION CONFIGURÉE AVEC SUCCÈS");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("🔐 PasswordEncoder Bean créé (BCrypt)");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}