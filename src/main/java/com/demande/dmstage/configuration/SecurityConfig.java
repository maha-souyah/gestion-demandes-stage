package com.demande.dmstage.configuration;

import com.demande.dmstage.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
        System.out.println("🔐 SECURITY CONFIG - SESSIONS UNIQUEMENT (SANS HTTP BASIC)");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println("🔐 CONFIGURATION SECURITY - SESSIONS PURES");
        
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement()
                // ✅ SESSIONS ACTIVÉES
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(10)
                .maxSessionsPreventsLogin(false)
                .and() // ✅ CORRECTION: Ajout du .and() manquant
            .and()
            .authorizeRequests()
                // ✅ ENDPOINTS PUBLICS (SANS AUTHENTIFICATION)
                .antMatchers("/api/auth/register").permitAll()           // Inscription: PUBLIC (sinon impossible de créer le 1er USER)
                .antMatchers("/api/auth/login").permitAll()              // Connexion: USER + ADMIN
                .antMatchers("/api/auth/create-admin").permitAll()       // Création admin (setup initial)
                .antMatchers("/api/auth/system-status").permitAll()      // Statut système (public)
                .antMatchers("/api/demandes/suivi").permitAll()          // Suivi par email (public)
                .antMatchers("/api/demandes").hasRole("USER")            // Création demande: USER seulement  
                .antMatchers("/api/demandes/mes-demandes").hasRole("USER") // Mes demandes: USER seulement
                
                // ✅ ENDPOINTS ADMIN UNIQUEMENT
                .antMatchers("/api/admin/**").hasRole("ADMIN")           // Toutes fonctions admin
                .antMatchers("/api/demandes/export/excel").hasRole("ADMIN") // Export Excel: ADMIN seulement
                
                // ✅ ENDPOINTS MIXTES (USER + ADMIN)
                .antMatchers("/api/auth/profile").hasAnyRole("USER", "ADMIN") // Profil: USER + ADMIN
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            .and()
            // ✅ SUPPRESSION HTTP BASIC - Sessions uniquement
            .formLogin().disable() // Pas de formulaire de login
            .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");
            
        System.out.println("🔐 SESSIONS CONFIGURÉES (HTTP BASIC DÉSACTIVÉ)");
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
        System.out.println("🌐 Configuration CORS avec support cookies");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // ✅ ESSENTIEL pour les cookies
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}