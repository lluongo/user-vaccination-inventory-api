package kruger.apps.uservaccinationinventory.sso.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SsoTokenResponse {
	@JsonIgnoreProperties(ignoreUnknown = true)	
	@JsonProperty("access_token")
	private String accessToken;
	

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	

}
