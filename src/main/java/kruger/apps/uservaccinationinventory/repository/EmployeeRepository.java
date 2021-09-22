package kruger.apps.uservaccinationinventory.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kruger.apps.uservaccinationinventory.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	@Query("select t from Employee t where t.vaccinationStatus.statusVaccinationId between :dateFrom and :dateTo")
    public List<Employee> findEmployeesByDates(@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo );

	public List<Employee> findEmployeeByIsVaccinated(@Param("status") boolean status);

}
