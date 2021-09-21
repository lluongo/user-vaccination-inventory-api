package kruger.apps.uservaccinationinventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kruger.apps.uservaccinationinventory.model.Employee;

@Repository
public interface UserRepository extends JpaRepository<Employee, Long> {

}
