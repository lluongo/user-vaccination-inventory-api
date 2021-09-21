package kruger.apps.uservaccinationinventory.services.sso;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;

import kruger.apps.uservaccinationinventory.ws.responses.SsoResponse;

@Service
public class SsoTokenService {

	@Value("${keycloak.auth-server-url}${sso.url.realms}${sso.url.realm}${sso.url.token}")
	private String ssoTokenUrl;
	@Value("${sso.username}")
	private String ssoUsername;
	@Value("${sso.password}")
	private String ssoPassword;
	@Value("${sso.grant_type}")
	private String grantType;
	@Value("${sso.client_id}")
	private String clientId;

	private String token;

	public synchronized String getToken(){

		if(token == null || JWT.decode(token).getExpiresAt().before(new Date())){
			token = getSsoToken(createSsoHttpEntityMultivalueMap(ssoUsername, ssoPassword, clientId, grantType), ssoTokenUrl);
		}
		return token;
	}

	public HttpEntity<MultiValueMap<String, String>> createSsoHttpEntityMultivalueMap(String ssoUsername, String ssoPassword, String clientId, String grantType){

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", ssoUsername);
		map.add("password", ssoPassword);
		map.add("client_id", clientId);
		map.add("grant_type", grantType);
		HttpEntity<MultiValueMap<String, String>> result = new HttpEntity<>(map, headers);
		return result;
	}

	public String getSsoToken(HttpEntity<MultiValueMap<String, String>> entity, String ssoTokenUrl){
		ResponseEntity<SsoResponse> responseEntity = new RestTemplate().exchange(ssoTokenUrl, HttpMethod.POST, entity, SsoResponse.class);
		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar a getSsoToken, statusCode: " + responseEntity.getStatusCodeValue());
		}
		return responseEntity.getBody().getAccessToken();
	}

}
