package kruger.apps.uservaccinationinventory.ws.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;

public class RequestNewEmployee {

	@ApiModelProperty(value = "cedula = la cedula del empleado (se utiliza como ID unico)", example = "3645486215", required = false)
	@Min(1)
	@Max(9999999999L)
	@NotEmpty
	private Long cedula;

	@ApiModelProperty(value = "nombre = nombre del empleado", example = "Juan Manuel", required = false)
	@NotBlank
	@NotEmpty
	@Pattern(regexp = "[a-zA-Z]")
	private String name;

	@ApiModelProperty(value = "apellido = apellido del empleado", example = "Rodriguez Cuarta", required = false)
	@NotBlank
	@NotEmpty
	@Pattern(regexp = "[a-zA-Z]")
	private String lastName;

	@ApiModelProperty(value = "email = email del empleado", example = "Rodriguez.Cuarta@gmail.com", required = false)
	@NotBlank
	@NotEmpty
	@Email
	private String email;

	
	@ApiModelProperty(value = "password = password del empleado", example = "password.!", required = false)
	@NotBlank
	@NotEmpty
	private String password;
	
	public Long getCedula(){
		return cedula;
	}

	public void setCedula(Long cedula){
		this.cedula = cedula;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getLastName(){
		return lastName;
	}

	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}
	
}
