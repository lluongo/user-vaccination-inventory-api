package kruger.apps.uservaccinationinventory.ws;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserRegistrationRequest;
import kruger.apps.uservaccinationinventory.dtos.sso.SsoUserResponse;
import kruger.apps.uservaccinationinventory.services.sso.SsoTokenService;

@RestController
@RequestMapping("/v1/UserVaccinationInventory")
public class UserVaccinationInventoryWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserVaccinationInventoryWs.class);

	@Value("${sso.url.base}${sso.url.user.registration}")
	private String ssoURIUser;

	@Autowired
	private SsoTokenService masterSsoTokenService;

	@RequestMapping(value = "/user", method = RequestMethod.PUT, produces = "application/json")
	@ApiOperation(value = "Crear nueva billetera asociadas a un usuario")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = String.class)
	})
	public @ResponseBody ResponseEntity<?> createUser(){

		try{

		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("Se creo la cuenta Sube correctamente", HttpStatus.OK);
	}

	public String createSsoUser(String username, String email, String password){
		HttpHeaders headers = masterSsoTokenService.createHeaderJsonWithSsoToken(masterSsoTokenService.getToken());
		SsoUserRegistrationRequest request = new SsoUserRegistrationRequest(email, username, true);
		request.addSsoCredentials("password", password, false);

		HttpEntity<SsoUserRegistrationRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<?> responseEntity = new RestTemplate().postForEntity(ssoURIUser, entity, String.class);

		if(responseEntity.getStatusCode().isError()){
			throw new RuntimeException("Error al invocar al SSO para crear nuevo usuario, statusCode: " + responseEntity.getStatusCodeValue());
		}
		// return UserUtil.getUserIdFromUrl(responseEntity.getHeaders().getLocation().toString());
		return "dsfgsd";
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
