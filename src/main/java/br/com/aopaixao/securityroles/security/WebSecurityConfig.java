package br.com.aopaixao.securityroles.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.aopaixao.securityroles.security.jwt.AuthEntryPointJwt;
import br.com.aopaixao.securityroles.security.jwt.AuthTokenFilter;
import br.com.aopaixao.securityroles.security.service.UserDetailsServiceImpl;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
	@Autowired
	UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.cors() //habilita o cors
			.and()
			.csrf().disable() //desabilita o csrf
			.exceptionHandling().authenticationEntryPoint(unauthorizedHandler) //configura a classe para tratamento da excecao de autenticacao
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //define a politica de sessao
			.and().authorizeRequests()
			.antMatchers("/auth/**", "/h2-console/**", "/roles/**").permitAll() //define as rotas publicas/abertas
			.antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").hasRole("ADMIN") // autoriza o acesso a rotas por perfil
			.antMatchers("/test/user/**").hasAnyRole("USER", "ADMIN") //autoriza o acesso a rotas por perfis
			.anyRequest().authenticated() //demais rotas, nao configuradas acima, so poderao ser acessadas mediante autenticacao
		;

		http.authenticationProvider(authenticationProvider()); //define o provedor de autenticacao

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class); //define o filtro a ser aplicado no ciclo de vida da requisicao

		return http.build();
	}

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
