package kruger.apps.uservaccinationinventory.ws;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import kruger.apps.uservaccinationinventory.conf.CurrentUser;
import kruger.apps.uservaccinationinventory.conf.UserDetails;
import kruger.apps.uservaccinationinventory.dtos.ApiError;
import kruger.apps.uservaccinationinventory.enums.TypeVaccine;
import kruger.apps.uservaccinationinventory.model.Employee;
import kruger.apps.uservaccinationinventory.services.UserService;
import kruger.apps.uservaccinationinventory.ws.requests.RequestNewEmployee;
import kruger.apps.uservaccinationinventory.ws.requests.RequestUpdateEmployee;

@RestController
@RequestMapping("/v1/UserVaccinationInventory")
public class EmployeeWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeWs.class);

	@Autowired
	private UserService userService;

	ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	Validator validator = factory.getValidator();

	@RequestMapping(value = "/employee", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Visualiza datos de empleado --> ROL Empleado  ")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Employee.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
	})
	public @ResponseBody ResponseEntity<?> getEmployeeData(@CurrentUser UserDetails userDetail){

		try{
			return new ResponseEntity<>(userService.findEmployee(userDetail.getCedula()), HttpStatus.OK);
		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/employee", method = RequestMethod.PUT, produces = "application/json")
	@ApiOperation(value = "Actualiza datos de empleado --> ROL Empleado  ")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Employee.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
	})
	public @ResponseBody ResponseEntity<?> updateEmployee(@RequestBody RequestUpdateEmployee requestUpdateEmployee, @CurrentUser UserDetails userDetail){

		ApiError apiErrorResponse = new ApiError();

		try{

			Employee employee = userService.findEmployee(userDetail.getCedula());

			if(employee == null){
				apiErrorResponse.setTitle("Empleado no existe");
				apiErrorResponse.setType("Error de validacion");
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			boolean isVaccinated = !requestUpdateEmployee.getVaccine().isBlank() ? true : false;

			if(isVaccinated && requestUpdateEmployee.getVaccine().isBlank()){
				apiErrorResponse.setTitle("Empleado vacunado, debe informar la vacuna");
				apiErrorResponse.setType("Error de validacion");
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			employee.setAddress(requestUpdateEmployee.getAddress());
			employee.setBirthDate(requestUpdateEmployee.getBirthDate());
			employee.setCellPhoneNumber(requestUpdateEmployee.getCellPhoneNumber());
			employee.setVaccinated(true);
			employee.getVaccinationStatus().setTypeVaccine(TypeVaccine.valueOf(requestUpdateEmployee.getVaccine()));
			employee.getVaccinationStatus().setDateVaccination(requestUpdateEmployee.getVaccinationDate());
			employee.getVaccinationStatus().setNumberDose(requestUpdateEmployee.getDoseNumber());

			return new ResponseEntity<>(userService.save(employee), HttpStatus.OK);
		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/employee", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "Crear un nuevo empleado --> ROL Administrador")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = String.class)
	})
	public @ResponseBody ResponseEntity<?> createUser(@RequestBody RequestNewEmployee requestNewEmployee){

		ApiError apiErrorResponse = new ApiError();

		System.out.println("ENTRA EN EL ENDPOINT");

		try{

			Set<ConstraintViolation<RequestNewEmployee>> violations = validator.validate(requestNewEmployee);
			if(!violations.isEmpty()){
				apiErrorResponse.setTitle("Ocurrio un error en la validacion de los datos de la peticion");
				apiErrorResponse.setType("Error de validacion");
				for(ConstraintViolation<RequestNewEmployee> violation : violations){
					apiErrorResponse.getDetail().put(violation.getPropertyPath().toString(), violation.getMessage());
				}
				return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
			}

			userService.createUser(requestNewEmployee);

			return new ResponseEntity<>("Se creo el empleado correctamente", HttpStatus.OK);

		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/employee/vaccinated/{isVaccinated}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Consulta de empleados por estado de vacunacion --> ROL Administrador")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Employee.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
	})
	public @ResponseBody ResponseEntity<?> getEmployeeByVaccinatedState(@PathVariable("isVaccinated") boolean isVaccinated){

		try{
			return new ResponseEntity<>(userService.findVaccinatedByStatus(isVaccinated), HttpStatus.OK);
		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/employee/vaccine/{vaccineName}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Consulta de empleados por vacuna aplicada --> ROL Administrador")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Employee.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
	})
	public @ResponseBody ResponseEntity<?> getEmployeeByVaccine(@PathVariable("vaccineName") String vaccineName){

		try{
			return new ResponseEntity<>(userService.findEmployeeByVaccine(vaccineName), HttpStatus.OK);
		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/employee/byDates", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "Consulta de empleados por fechas de vacunacion desde y hasta --> ROL Administrador", response = Employee.class, responseContainer = "List")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Employee.class, responseContainer = "List"),
			@ApiResponse(code = 500, message = "Internal Server Error", response = String.class), @ApiResponse(code = 400, message = "Bad Request", response = ApiError.class)
	})
	public @ResponseBody ResponseEntity<?> getEmployeeByDates(@RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
		@RequestParam("dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo){

		try{
			List<Employee> employeeList = userService.findEmployeeByVaccinationDateFromAndVaccinationDateTo(dateFrom, dateTo);
			return new ResponseEntity<>(employeeList, HttpStatus.OK);
		} catch(Exception e){
			LOGGER.info(e.getMessage());
			return new ResponseEntity<>("Error Interno", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
