package kruger.apps.uservaccinationinventory.conf;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).select()
			.apis(RequestHandlerSelectors.withClassAnnotation(RestController.class)).build()
			.apiInfo(getMetadata());

	}

	private ApiInfo getMetadata() {
		ApiInfo apiInfo = new ApiInfo("Api de registracion de vacunacion", "WebService que registra el estado de vacunacion de empleados", "1", "",
			new Contact("Kruger", "https://www.krugercorp.com", "kruger@ec.krugercorp.com"), "Standard License", "",
			new ArrayList<>());
		return apiInfo;
	}

	@Bean
	public UiConfiguration tryItOutConfig() {
		final String[] methodsWithTryItOutButton = { "get" };
		return UiConfigurationBuilder.builder().supportedSubmitMethods(methodsWithTryItOutButton).build();
	}
}