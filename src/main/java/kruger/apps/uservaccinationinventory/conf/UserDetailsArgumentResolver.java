package kruger.apps.uservaccinationinventory.conf;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
/**
 * The Class UserDetailsArgumentResolver.
 */
public class UserDetailsArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception{
		if(supportsParameter(methodParameter)){
			return createUserDetails(webRequest);
		}
		return WebArgumentResolver.UNRESOLVED;
	}

	/**
	 * Creates the user details.
	 *
	 * @param webRequest
	 *            the web request
	 * @return the object
	 */
	private Object createUserDetails(NativeWebRequest webRequest){
		KeycloakAuthenticationToken principal = (KeycloakAuthenticationToken) webRequest.getUserPrincipal();
		AccessToken token = principal.getAccount().getKeycloakSecurityContext().getToken();
		Set<String> roles = token.getRealmAccess().getRoles().stream().collect(Collectors.toSet());
		roles.addAll(principal.getAccount().getRoles().stream().collect(Collectors.toSet()));

		Map<String, Object> otherClaims = token.getOtherClaims();
		Long userId = Long.valueOf(otherClaims.get("userId").toString());

		return new UserDetails(token.getId(), userId, token.getPreferredUsername(), token.getGivenName(), token.getFamilyName(), token.getEmail(), roles);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver# supportsParameter(org.springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter methodParameter){
		return methodParameter.getParameterAnnotation(CurrentUser.class) != null && methodParameter.getParameterType().equals(UserDetails.class);
	}

}
