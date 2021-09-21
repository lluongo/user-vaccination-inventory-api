package kruger.apps.uservaccinationinventory.dtos;

import java.util.HashMap;
import java.util.Map;

public class ApiError {
	private String type;
	private String title;

	private Map<String, String> detail = new HashMap<>();

	public ApiError(){
	}

	public ApiError(String type, String title, Map<String, String> detail){
		super();
		this.type = type;
		this.title = title;
		this.detail = detail;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public Map<String, String> getDetail(){
		return detail;
	}

	public void setDetail(Map<String, String> detail){
		this.detail = detail;
	}

}
