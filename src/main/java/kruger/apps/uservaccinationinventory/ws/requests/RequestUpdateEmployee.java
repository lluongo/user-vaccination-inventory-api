package kruger.apps.uservaccinationinventory.ws.requests;

import java.util.Date;

public class RequestUpdateEmployee {

	private Long cedula;
	private String address;
	private String cellPhoneNumber;
	private boolean isVaccinated;
	private Date birthDate;
	private Date vaccinationDate;
	private String vaccine;
	private int doseNumber;

	public RequestUpdateEmployee(Long cedula, Date birthDate, String address, String cellPhoneNumber, boolean isVaccinated, String vaccine, Date vaccinationDate, int doseNumber){
		this.cedula = cedula;
		this.birthDate = birthDate;
		this.address = address;
		this.cellPhoneNumber = cellPhoneNumber;
		this.isVaccinated = isVaccinated;
		this.vaccine = vaccine;
		this.vaccinationDate = vaccinationDate;
		this.doseNumber = doseNumber;
	}

	public Long getCedula(){
		return cedula;
	}

	public void setCedula(Long cedula){
		this.cedula = cedula;
	}

	public String getAddress(){
		return address;
	}

	public void setAddress(String address){
		this.address = address;
	}

	public String getCellPhoneNumber(){
		return cellPhoneNumber;
	}

	public void setCellPhoneNumber(String cellPhoneNumber){
		this.cellPhoneNumber = cellPhoneNumber;
	}

	public boolean isVaccinated(){
		return isVaccinated;
	}

	public void setVaccinated(boolean isVaccinated){
		this.isVaccinated = isVaccinated;
	}

	public Date getBirthDate(){
		return birthDate;
	}

	public void setBirthDate(Date birthDate){
		this.birthDate = birthDate;
	}

	public String getVaccine(){
		return vaccine;
	}

	public void setVaccine(String vaccine){
		this.vaccine = vaccine;
	}

	public Date getVaccinationDate(){
		return vaccinationDate;
	}

	public void setVaccinationDate(Date vaccinationDate){
		this.vaccinationDate = vaccinationDate;
	}

	public int getDoseNumber(){
		return doseNumber;
	}

	public void setDoseNumber(int doseNumber){
		this.doseNumber = doseNumber;
	}

}
