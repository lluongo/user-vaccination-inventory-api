package kruger.apps.uservaccinationinventory.sso.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SsoConfig {
	
	@Value("${sso.grant_type}")
	private String grantType;
	@Value("${sso.username}")
	private String ssoUsername;
	@Value("${sso.password}")
	private String ssoPassword;
	
	@Value("${sso.url.base}${sso.master.uri_token}")
	private String ssoMasterUriToken;
	@Value("${sso.master.client_id}")
	private String ssoMasterClientId;
	
	@Value("${sso.url.base}${sso.renaper.uri_token}")
	private String ssoRenaperUriToken;
	@Value("${sso.renaper.client_id}")
	private String ssoRenaperClientId;
	
	@Value("${sso.url.base}${sso.afip.uri_token}")
	private String ssoAfipUriToken;
	@Value("${sso.afip.client_id}")
	private String ssoAfipClientId;
	
	@Value("${sso.url.base}${sso.messageapi.uri_token}")
	private String ssoMessageApiUriToken;
	@Value("${sso.messageapi.client_id}")
	private String ssoMessageApiClientId;

	@Bean
	@Qualifier("masterSsoTokenService")
	public SsoTokenService masterSsoTokenService() {
		return new SsoTokenService(ssoMasterUriToken, ssoUsername, ssoPassword, ssoMasterClientId, grantType);
	}

	@Bean
	@Qualifier("renaperSsoTokenService")
	public SsoTokenService renaperSsoTokenService() {
		return new SsoTokenService(ssoRenaperUriToken, ssoUsername, ssoPassword, ssoRenaperClientId, grantType);
	}

	@Bean
	@Qualifier("afipSsoTokenService")
	public SsoTokenService afipSsoTokenService() {
		return new SsoTokenService(ssoAfipUriToken, ssoUsername, ssoPassword, ssoAfipClientId, grantType);
	}
	
	@Bean
	@Qualifier("messageApiSsoTokenService")
	public SsoTokenService messageApiSsoTokenService() {
		return new SsoTokenService(ssoMessageApiUriToken, ssoUsername, ssoPassword, ssoMessageApiClientId, grantType);
	}
}
