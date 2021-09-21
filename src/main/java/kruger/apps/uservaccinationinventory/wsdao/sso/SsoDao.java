package kruger.apps.uservaccinationinventory.wsdao.sso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import kruger.apps.uservaccinationinventory.dtos.sso.SsoTokenResponse;
import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserRegistrationRequest;
import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserResponse;
import kruger.apps.uservaccinationinventory.services.sso.SsoTokenService;
import kruger.apps.uservaccinationinventory.ws.requests.RequestNewEmployee;

@Service
public class SsoDao {

	@Autowired
	private SsoTokenService masterSsoTokenService;

	@Value("${sso.url.base}${sso.url.user.registration}")
	private String ssoURIUser;

	public SsoTokenResponse getSsoToken(String uri, String username, String password, String clientId, String grantType){
		HttpEntity<MultiValueMap<String, String>> entity = createSsoHttpEntityMultivalueMap(username, password, clientId, grantType);
		ResponseEntity<SsoTokenResponse> responseEntity = new RestTemplate().exchange(uri, HttpMethod.POST, entity, SsoTokenResponse.class);
		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar a getSsoToken, statusCode: " + responseEntity.getStatusCodeValue());
		}
		return responseEntity.getBody();
	}

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

	public String createSsoUser(RequestNewEmployee requestNewEmployee){
		HttpHeaders headers = createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		SsoUserRegistrationRequest request = new SsoUserRegistrationRequest(requestNewEmployee.getEmail(), requestNewEmployee.getCedula(), true);
		request.addSsoCredentials("password", requestNewEmployee.getPassword(), false);
		HttpEntity<SsoUserRegistrationRequest> entity = new HttpEntity<>(request, headers);
		ResponseEntity<?> responseEntity = new RestTemplate().postForEntity(ssoURIUser, entity, String.class);
		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar al SSO para crear nuevo usuario, statusCode: " + responseEntity.getStatusCodeValue());
		}
		// return UserUtil.getUserIdFromUrl(responseEntity.getHeaders().getLocation().toString());
		return "dsfgsd";
	}

	public SsoUserResponse getSsoUserData(String ssoUserId){

		HttpHeaders headers = createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		String uri = UriComponentsBuilder.fromHttpUrl(ssoURIUser).pathSegment(ssoUserId).toUriString();
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		try{
			ResponseEntity<SsoUserResponse> responseEntity = new RestTemplate().exchange(uri, HttpMethod.GET, entity, SsoUserResponse.class);
			return responseEntity.getBody();
		} catch(Exception e){
		}
		return null;
	}

	public SsoUserResponse findSsoUserByUsername(String username){

		HttpHeaders headers = createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		String uriBuilder = UriComponentsBuilder.fromHttpUrl(ssoURIUser).queryParam("username", username).toUriString();
		HttpEntity<?> entity = new HttpEntity<>(null, headers);
		ResponseEntity<List<SsoUserResponse>> responseEntity = new RestTemplate().exchange(uriBuilder, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SsoUserResponse>>(){
		});

		if(responseEntity.getBody().size() > 0){
			return responseEntity.getBody().get(0);
		}
		return null;
	}

	public HttpHeaders createHeaderJsonWithSsoToken(String token){
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

}
