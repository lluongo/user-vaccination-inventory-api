package kruger.apps.uservaccinationinventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kruger.apps.uservaccinationinventory.model.VaccinationStatus;

@Repository
public interface VaccinationStatusRepository extends JpaRepository<VaccinationStatus, Long> {

}
