package nemo.seon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/upload-asta").hasRole("USER")
                        .requestMatchers("/", "/publications", "/ontology/**", "/login", "/static/**", "/css/**", "/js/**", "/images/**", "/generated-images/**", "/astah-images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                        // .defaultSuccessUrl("/", true)
                        .defaultSuccessUrl("/upload", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        UserDetails adminDetails = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(userDetails, adminDetails);
    }
}