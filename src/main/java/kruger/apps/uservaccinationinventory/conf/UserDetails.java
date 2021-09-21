package kruger.apps.uservaccinationinventory.conf;

import java.util.Set;

import io.swagger.annotations.ApiModelProperty;

/**
 * The Class UserDetails.
 */
public class UserDetails {

	/** The id. */
	private String id;

	/** The id. */
	private Long cedula;

	/** The email. */
	private String email;

	private String userName;

	/** The first name. */
	private String firstName;

	/** The last name. */
	private String lastName;

	/** The roles. */
	private Set<String> roles;

	/**
	 * Instantiates a new user details.
	 *
	 * @param id
	 *            the id
	 * @param firstName
	 *            the first name
	 * @param lastName
	 *            the last name
	 * @param email
	 *            the email
	 * @param roles
	 *            the roles
	 * @param deviceSupplierTypes
	 *            the "redes"
	 */

	public UserDetails(){
		super();
	}

	public UserDetails(String id, Long cedula, String userName, String firstName, String lastName, String email, Set<String> roles){
		super();
		this.id = id;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.roles = roles;
		this.cedula = cedula;
	}

	/**
	 * Gets the email.
	 *
	 * @return the email
	 */
	public String getEmail(){
		return email;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName(){
		return userName;
	}

	/**
	 * Gets the first name.
	 *
	 * @return the first name
	 */
	public String getFirstName(){
		return firstName;
	}

	/**
	 * Gets the last name.
	 *
	 * @return the last name
	 */
	public String getLastName(){
		return lastName;
	}

	/**
	 * Gets the roles.
	 *
	 * @return the roles
	 */
	public Set<String> getRoles(){
		return roles;
	}

	/**
	 * Gets the full name.
	 *
	 * @return the full name
	 */
	@ApiModelProperty(hidden = true)
	public String getFullName(){
		return getFirstName() + " " + getLastName();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId(){
		return id;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Long getCedula(){
		return cedula;
	}

}
