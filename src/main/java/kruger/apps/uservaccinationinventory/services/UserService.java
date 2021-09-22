package kruger.apps.uservaccinationinventory.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kruger.apps.uservaccinationinventory.model.Employee;
import kruger.apps.uservaccinationinventory.repository.EmployeeRepository;
import kruger.apps.uservaccinationinventory.ws.requests.RequestNewEmployee;
import kruger.apps.uservaccinationinventory.wsdao.sso.SsoDao;

@Service
public class UserService {

	@Autowired
	private SsoDao ssoDao;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Transactional
	public boolean createUser(RequestNewEmployee requestNewEmployee){
		saveSso(requestNewEmployee);
		save(new Employee(requestNewEmployee.getCedula(), requestNewEmployee.getName(), requestNewEmployee.getLastName(), requestNewEmployee.getEmail()));
		return true;
	}

	public Employee save(Employee user){
		return employeeRepository.save(user);
	}

	public String saveSso(RequestNewEmployee requestNewEmployee){
		return ssoDao.createSsoUser(requestNewEmployee);
	}

	public Employee findEmployee(Long cedula){
		return employeeRepository.getOne(cedula);
	}
	
	public List<Employee> findVaccinatedByStatus(boolean isVaccinated){
		return employeeRepository.findEmployeeByIsVaccinated(isVaccinated);
	}

	public List<Employee> findEmployeeByVaccine(String vaccine){
		List<Employee> employees = employeeRepository.findAll();
		return employees.stream().filter(employee -> employee.getVaccinationStatus().getTypeVaccine().toString() == vaccine).collect(Collectors.toList());
	}

	public List<Employee> findEmployeeByVaccinationDateFromAndVaccinationDateTo(Date dateFrom, Date dateTo){
		return employeeRepository.findEmployeesByDates(dateFrom, dateTo);
	}

}
