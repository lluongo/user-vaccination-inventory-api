package kruger.apps.uservaccinationinventory.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "EMPLOYEE", uniqueConstraints = {@UniqueConstraint(name = "UK_EMPLOYEE", columnNames = {"cedula"
		})
})
public class Employee {

	@Id
	private Long cedula;
	private String name;
	private String lastName;
	private String email;
	private String address;
	private String addressNumber;
	private String cellPhoneNumber;

	@ManyToOne
	@JoinColumn(name = "id")
	private VaccinationStatus vaccinationStatus;

	public Employee(Long cedula, String name, String lastName, String email){
		this.cedula = cedula;
		this.name = name;
		this.lastName = lastName;
		this.email = email;
	}

	@CreationTimestamp
	private Date birthDate;

	@CreationTimestamp
	private Date insertionDate;

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

	public String getAddress(){
		return address;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public String getAddressNumber(){
		return addressNumber;
	}

	public void setAddressNumber(String addressNumber){
		this.addressNumber = addressNumber;
	}

	public String getCellPhoneNumber(){
		return cellPhoneNumber;
	}

	public void setCellPhoneNumber(String cellPhoneNumber){
		this.cellPhoneNumber = cellPhoneNumber;
	}

	public VaccinationStatus getVaccinationStatus(){
		return vaccinationStatus;
	}

	public void setVaccinationStatus(VaccinationStatus vaccinationStatus){
		this.vaccinationStatus = vaccinationStatus;
	}

	public Date getBirthDate(){
		return birthDate;
	}

	public void setBirthDate(Date birthDate){
		this.birthDate = birthDate;
	}

	public Date getInsertionDate(){
		return insertionDate;
	}

	public void setInsertionDate(Date insertionDate){
		this.insertionDate = insertionDate;
	}

}
