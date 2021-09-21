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
	protected void configure(HttpSecurity http) throws Exception{
		super.configure(http);
		http.csrf().disable().authorizeRequests().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui/**", "/webjars/**",
			"/swagger-resources/configuration/ui", "/swagger-resources/configuration/security", "/*.wsdl", "/actuator/health")
//				.antMatchers("/v2/api-docs", "/v3/api-docs", "/swagger-ui/**", "/swagger-resources/**", "/actuator/**","/.~~spring-boot!~/restart"),
			.permitAll().antMatchers("/v1/pay/sendMoney").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/getBalance").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/getTransactions")
			.hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/getTransactionsByUserId").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/getLatestRecipients").hasRole("PAY_API_GENERAL")
			.antMatchers("/v1/pay/generateDynamicMerchantQRCode").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/generateDynamicQRCodePNG").hasRole("PAY_API_GENERAL")
			.antMatchers("/v1/pay/validateScannedQRCode").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/QrPay").hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/createWallet")
			.hasRole("PAY_API_GENERAL").antMatchers("/v1/pay/reverseTransaction").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v1/pay/subeChargePay").hasRole("SUBE_CHARGE_OPERATION")
			.antMatchers("/v1/pay/getTransactionReversed/*").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v1/pay/getTransaction/*").hasRole("SUBE_CHARGE_OPERATION")
			.antMatchers("/v1/pay/getTransactionByCharge/*").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v1/pay/generateMoney").hasRole("PAY_API_GENERATE_MONEY").antMatchers("/v1/pay/wireTransfer")
			.hasRole("PAY_API_WIRE_TRANSFER").antMatchers("/v1/pay/getTransactionToTransfer").hasRole("PAY_API_WIRE_TRANSFER").antMatchers("/v1/pay/chargeWallet").hasRole("PAY_API_BULK_CHARGE")

			.antMatchers("/v2/pay/getBalance/*").hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/getTransactions").hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/getTransactionsByUserId")
			.hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/getLatestRecipients").hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/generateDynamicMerchantQRCode").hasRole("PAY_API_GENERAL")
			.antMatchers("/v2/pay/generateDynamicQRCodePNG").hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/reverseTransaction").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v2/pay/subeChargePay")
			.hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v2/pay/getTransactionReversed/*").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v2/pay/getTransaction/*").hasRole("SUBE_CHARGE_OPERATION")
			.antMatchers("/v2/pay/getTransactionByCharge/*").hasRole("SUBE_CHARGE_OPERATION").antMatchers("/v2/pay/generateMoney").hasRole("PAY_API_GENERATE_MONEY").antMatchers("/v2/pay/wireTransfer")
			.hasRole("PAY_API_WIRE_TRANSFER").antMatchers("/v2/pay/validateOperation").hasRole("VALIDATE_OPERATION").antMatchers("/v2/pay/createWallet").hasRole("PAY_API_GENERAL")
			.antMatchers("/v2/pay/exchange").hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/exchangeApi").hasRole("PAY_API_EXCHANGE_API").antMatchers("/v2/pay/getWallets/*")
			.hasRole("PAY_API_GENERAL").antMatchers("/v2/pay/getTransactionToTransfer").hasRole("PAY_API_WIRE_TRANSFER").antMatchers("/v2/pay/validateScannedQRCode").hasRole("PAY_API_GENERAL")
			.antMatchers("/v2/pay/chargeWallet").hasRole("PAY_API_BULK_CHARGE").antMatchers("/v2/pay/validateLimits").hasRole("PAY_API_VALIDATE_LIMITS").anyRequest().denyAll();
	}

}
