package kruger.apps.uservaccinationinventory.ws;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import kruger.apps.uservaccinationinventory.conf.CurrentUser;
import kruger.apps.uservaccinationinventory.conf.UserDetails;
import kruger.apps.uservaccinationinventory.dtos.ApiError;
import kruger.apps.uservaccinationinventory.services.UserService;
import kruger.apps.uservaccinationinventory.ws.requests.RequestNewEmployee;

@RestController
@RequestMapping("/v1/UserVaccinationInventory")
public class UserVaccinationInventoryWs {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserVaccinationInventoryWs.class);

	@Autowired
	private UserService userService;

	ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	Validator validator = factory.getValidator();

	@RequestMapping(value = "/employee", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "Crear un nuevo empleado")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class), @ApiResponse(code = 500, message = "Internal Server Error", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = String.class)
	})
	public @ResponseBody ResponseEntity<?> createUser(@RequestBody RequestNewEmployee requestNewEmployee, @CurrentUser UserDetails userDetail){

		ApiError apiErrorResponse = new ApiError();

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

}
