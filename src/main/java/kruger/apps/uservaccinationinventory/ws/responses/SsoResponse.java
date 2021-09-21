package kruger.apps.uservaccinationinventory.ws.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SsoResponse {

	private String accessToken;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonProperty("access_token")
	public String getAccessToken(){
		return accessToken;
	}

	public void setAccessToken(String accessToken){
		this.accessToken = accessToken;
	}

}
