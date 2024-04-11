package net.pheocnetafr.africapheocnet.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Autowired
    private MfaService mfaService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/css/**", "/js/**", "/assets/**", "/images/**", "/fonts/**", "/libs/**", "/lang/**", "/resetpassword", "/application-form", "/success-page","/auth-signout","/message").permitAll()
                    .requestMatchers("/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/trainer/**").hasAuthority("TRAINER")
                    .requestMatchers("/user/**").hasAuthority("USER")
                    .requestMatchers("/application-form").permitAll()
                    .requestMatchers("/applications/add").permitAll()
                    .requestMatchers("/users/reset-password").permitAll() 
                    .requestMatchers("/users/password-reset").permitAll()
                    .requestMatchers("/verify-code").permitAll()
                    .requestMatchers("/home").permitAll()
                    .requestMatchers("/about", "/membership", "/events", "/partnership", "/advocacy",
                            "/country-materials", "/publications", "/scientific-papers", "/standard-training",
                            "/contact", "/faq")
                    .permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form ->
                form
                    .loginPage("/login")
                    .successHandler(authenticationSuccessHandler())
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/auth-signout")
                    .permitAll()
            )
            .exceptionHandling(exceptions ->
                exceptions
                    .accessDeniedPage("/403")
            )
            .sessionManagement(sessionManagement ->
                sessionManagement
                    .maximumSessions(1)
                    .expiredUrl("/login?expired=true")
                    .sessionRegistry(sessionRegistry())
                    .and()
                    .invalidSessionUrl("/login?invalid=true")
            )
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                    .ignoringRequestMatchers("/csrf-token-endpoint")
            )
            .headers(headers ->
                headers
                    .frameOptions(frameOptions -> frameOptions.deny()) 
                    .cacheControl(cacheControl -> cacheControl.disable()) 
            )
            .headers(headers ->
                headers
                    .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff")) // Set X-Content-Type-Options header
                    .referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)) 
            );

        return http.build();
    }

    @Bean
    public CustomAuthenticationSuccessHandler authenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(customUserDetailsService, mfaService);
    }

    
    public PasswordEncoder passwordEncoder() {
        // Ensure your EncoderConfig returns an appropriate PasswordEncoder instance
        EncoderConfig encoderConfig = new EncoderConfig();
        return encoderConfig.passwordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
