package kruger.apps.uservaccinationinventory.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kruger.apps.uservaccinationinventory.model.Employee;
import kruger.apps.uservaccinationinventory.repository.UserRepository;
import kruger.apps.uservaccinationinventory.ws.requests.RequestNewEmployee;
import kruger.apps.uservaccinationinventory.wsdao.sso.SsoDao;

@Service
public class UserService {

	@Autowired
	private SsoDao ssoDao;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public boolean createUser(RequestNewEmployee requestNewEmployee){
		saveSso(requestNewEmployee);
		save(new Employee(requestNewEmployee.getCedula(), requestNewEmployee.getName(), requestNewEmployee.getLastName(), requestNewEmployee.getEmail()));
		return true;
	}

	public Employee save(Employee user){
		return userRepository.save(user);
	}

	public String saveSso(RequestNewEmployee requestNewEmployee){
		return ssoDao.createSsoUser(requestNewEmployee);
	}

}
