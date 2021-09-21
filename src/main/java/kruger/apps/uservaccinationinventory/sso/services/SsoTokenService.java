package kruger.apps.uservaccinationinventory.sso.services;

import java.util.Date;

import org.springframework.http.HttpHeaders;

import com.auth0.jwt.JWT;

public class SsoTokenService {

	SsoManager ssoManager;

	private String token = null;

	private String uri;
	private String username;
	private String password;
	private String clientId;
	private String grantType;

	public String getUri(){
		return uri;
	}

	public String getUsername(){
		return username;
	}

	public String getPassword(){
		return password;
	}

	public String getClientId(){
		return clientId;
	}

	public String getGrantType(){
		return grantType;
	}

	public void setUri(String uri){
		this.uri = uri;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public void setClientId(String clientId){
		this.clientId = clientId;
	}

	public void setGrantType(String grantType){
		this.grantType = grantType;
	}

	public SsoTokenService(String uri, String username, String password, String clientId, String grantType){
		super();
		ssoManager = new SsoManager();
		this.uri = uri;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.grantType = grantType;
	}

	public String getToken(){

		if(token == null || JWT.decode(token).getExpiresAt().before(new Date())){
			token = ssoManager.getSsoToken(uri, username, password, clientId, grantType).getAccessToken();
		}
		return token;
	}

	public HttpHeaders createHeaderJsonWithSsoToken(String token){
		return ssoManager.createHeaderJsonWithSsoToken(token);
	}
}
