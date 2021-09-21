package kruger.apps.uservaccinationinventory.dtos.sso;

import java.util.ArrayList;
import java.util.List;

public class SsoUserRegistrationRequest {

	private String email;
	private Long cedula;
	private boolean enabled;
	private List<SsoCredentials> credentials = new ArrayList<>();

	public SsoUserRegistrationRequest(String email, Long cedula, boolean enabled) {
		super();
		this.email = email;
		this.cedula = cedula;
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<SsoCredentials> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<SsoCredentials> credentials) {
		this.credentials = credentials;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public Long getCedula(){
		return cedula;
	}

	public void setCedula(Long cedula){
		this.cedula = cedula;
	}

	public void addSsoCredentials(String type, String value, boolean temporary) {
		SsoCredentials ssoCredentials = new SsoCredentials(type, value, temporary);
		credentials.add(ssoCredentials);

	}

}
