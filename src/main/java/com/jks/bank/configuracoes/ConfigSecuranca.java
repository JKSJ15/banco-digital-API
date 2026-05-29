package com.jks.bank.configuracoes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class ConfigSecuranca {
	private final FiltroSeguranca filtro;

	public ConfigSecuranca(FiltroSeguranca filtro) {
		super();
		this.filtro = filtro;
	}

	@Bean
	SecurityFilterChain correnteFiltro(HttpSecurity http) {
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth.
						requestMatchers(HttpMethod.POST, "/auth/login", "/auth/registro", "/auth/refresh").permitAll()
								.anyRequest().authenticated())
				.addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	AuthenticationManager gerenciadorAutenticacao(AuthenticationConfiguration config) {
		return config.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
