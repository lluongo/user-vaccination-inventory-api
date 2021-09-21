package kruger.apps.uservaccinationinventory.dtos.sso;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class SsoUserResponse {
	@JsonIgnoreProperties(ignoreUnknown = true)	
	private String id;
	private boolean enabled;
	private boolean emailVerified;
	private List<String> requiredActions;
	private Map<String, List<String>> attributes;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEmailVerified() {
		return emailVerified;
	}
	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}
	public List<String> getRequiredActions() {
		return requiredActions;
	}
	public void setRequiredActions(List<String> requiredActions) {
		this.requiredActions = requiredActions;
	}
	public Map<String, List<String>> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, List<String>> attributes) {
		this.attributes = attributes;
	}
	@Override
	public String toString() {
		return "SsoUserResponse [id=" + id + ", enabled=" + enabled + ", emailVerified=" + emailVerified
				+ ", requiredActions=" + requiredActions + ", attributes=" + attributes + "]";
	}
}
