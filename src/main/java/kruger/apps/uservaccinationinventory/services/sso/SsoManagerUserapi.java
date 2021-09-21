package kruger.apps.uservaccinationinventory.services.sso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserRegistrationRequest;
import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserResponse;

@Service
public class SsoManagerUserapi {

	@Value("${sso.url.base}${sso.url.user.registration}")
	private String ssoURIUser;

	@Autowired
	@Qualifier("masterSsoTokenService")
	private SsoTokenService masterSsoTokenService;

	public String createSsoUser(String username, String email, String password){
		HttpHeaders headers = masterSsoTokenService.createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		SsoUserRegistrationRequest request = new SsoUserRegistrationRequest(email, username, true);
		request.addSsoCredentials("password", password, false);

		HttpEntity<SsoUserRegistrationRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<?> responseEntity = new RestTemplate().postForEntity(ssoURIUser, entity, String.class);

		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar al SSO para crear nuevo usuario, statusCode: " + responseEntity.getStatusCodeValue());
		}
		return "dfgsdfg";
	}

	public SsoUserResponse getSsoUserData(String ssoUserId){

		HttpHeaders headers = masterSsoTokenService.createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
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

		HttpHeaders headers = masterSsoTokenService.createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		String uriBuilder = UriComponentsBuilder.fromHttpUrl(ssoURIUser).queryParam("username", username).toUriString();
		HttpEntity<?> entity = new HttpEntity<>(null, headers);

		ResponseEntity<List<SsoUserResponse>> responseEntity = new RestTemplate().exchange(uriBuilder, HttpMethod.GET, entity, new ParameterizedTypeReference<List<SsoUserResponse>>(){
		});

		if(responseEntity.getBody().size() > 0){
			return responseEntity.getBody().get(0);
		}

		return null;
	}

}
