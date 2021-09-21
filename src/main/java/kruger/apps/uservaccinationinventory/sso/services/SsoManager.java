package kruger.apps.uservaccinationinventory.sso.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import kruger.apps.uservaccinationinventory.sso.dtos.SsoTokenResponse;

public class SsoManager {

	public HttpEntity<MultiValueMap<String, String>> createSsoHttpEntityMultivalueMap(String username, String password, String clientId, String grantType){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("username", username);
		map.add("password", password);
		map.add("client_id", clientId);
		map.add("grant_type", grantType);
		HttpEntity<MultiValueMap<String, String>> result = new HttpEntity<>(map, headers);
		return result;
	}

	public HttpHeaders createHeaderJsonWithSsoToken(String token){
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	public SsoTokenResponse getSsoToken(String uri, String username, String password, String clientId, String grantType){
		HttpEntity<MultiValueMap<String, String>> entity = createSsoHttpEntityMultivalueMap(username, password, clientId, grantType);

		ResponseEntity<SsoTokenResponse> responseEntity = new RestTemplate().exchange(uri, HttpMethod.POST, entity, SsoTokenResponse.class);
		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar a getSsoToken, statusCode: " + responseEntity.getStatusCodeValue());
		}
		return responseEntity.getBody();
	}
}
