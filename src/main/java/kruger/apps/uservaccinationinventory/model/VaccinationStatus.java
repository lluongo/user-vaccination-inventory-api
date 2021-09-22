package kruger.apps.uservaccinationinventory.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import kruger.apps.uservaccinationinventory.enums.TypeVaccine;

@SequenceGenerator(name = "SEQ_STATUS", initialValue = 1, allocationSize = 1, sequenceName = "SEQ_STATUS")
@Entity
@Table(name = "Vaccination_Status")
public class VaccinationStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STATUS")
	private Long statusVaccinationId;

	private TypeVaccine typeVaccine;

	@CreationTimestamp
	private Date dateVaccination;
	private int numberDose;

	@OneToMany(mappedBy  = "vaccinationStatus",fetch = FetchType.LAZY)
	private List<Employee> listUsers;

	public TypeVaccine getTypeVaccine(){
		return typeVaccine;
	}

	public void setTypeVaccine(TypeVaccine typeVaccine){
		this.typeVaccine = typeVaccine;
	}

	public Date getDateVaccination(){
		return dateVaccination;
	}

	public void setDateVaccination(Date dateVaccination){
		this.dateVaccination = dateVaccination;
	}

	public int getNumberDose(){
		return numberDose;
	}

	public void setNumberDose(int numberDose){
		this.numberDose = numberDose;
	}

}
