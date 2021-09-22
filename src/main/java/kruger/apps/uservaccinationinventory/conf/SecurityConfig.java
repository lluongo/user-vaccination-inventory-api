package kruger.apps.uservaccinationinventory.conf;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

	@Bean
	public KeycloakConfigResolver KeycloakConfigResolver(){
		return new KeycloakSpringBootConfigResolver();
	}

	@Autowired
	public KeycloakClientRequestFactory keycloakClientRequestFactory;

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KeycloakRestTemplate keycloakRestTemplate(){
		return new KeycloakRestTemplate(keycloakClientRequestFactory);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception{
		auth.authenticationProvider(keycloakAuthenticationProvider());
	}

	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy(){
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.csrf().disable().authorizeRequests()
				.antMatchers("/v2/api-docs", 
						"/configuration/ui", 
						"/swagger-resources", 
						"/configuration/security",
						"/swagger-ui.html", 
						"/webjars/**", 
						"/swagger-resources/configuration/ui",
						"/swagger-resources/configuration/security", 
						"/*.wsdl", 
						"/actuator/health")
				.permitAll()
				.antMatchers(HttpMethod.POST,"/v1/employee").hasRole("ADMINISTRATOR")
				.antMatchers(HttpMethod.GET,"/v1/employee/vaccinated/*").hasRole("ADMINISTRATOR")
				.antMatchers(HttpMethod.GET,"/v1/employee/vaccine/*").hasRole("ADMINISTRATOR")
				.antMatchers(HttpMethod.POST,"/employee/byDates").hasRole("ADMINISTRATOR")
				.antMatchers(HttpMethod.GET,"/v1/employee").hasRole("EMPLOYEE")
				.antMatchers(HttpMethod.PUT,"/v1/employee").hasRole("EMPLOYEE")
				
//				.antMatchers(HttpMethod.PUT,"/v1/cashIn/confirmation/*").hasRole("CASH_IN_GENERAL")
//				.antMatchers(HttpMethod.GET,"/v1/cashIn/findByClientTrxId/*").hasRole("GET_CI_CLIENTTRXID")
//				.antMatchers(HttpMethod.DELETE,"/v1/cashIn/pendings").hasRole("DELETE_CI_PENDINGS")
				.anyRequest().denyAll();
	}

}
