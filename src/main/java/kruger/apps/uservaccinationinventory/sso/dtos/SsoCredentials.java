package kruger.apps.uservaccinationinventory.sso.dtos;

public class SsoCredentials {
	
	private String type;
	private String value;
	private boolean temporary;
	
	public String getType() {
		return type;
	}
	public SsoCredentials(String type, String value, boolean temporary) {
		super();
		this.type = type;
		this.value = value;
		this.temporary = temporary;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isTemporary() {
		return temporary;
	}
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
	
	

}
