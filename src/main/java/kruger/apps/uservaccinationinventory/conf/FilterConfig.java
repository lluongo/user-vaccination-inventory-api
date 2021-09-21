package kruger.apps.uservaccinationinventory.conf;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.access.servlet.TeeFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<TeeFilter> requestResponseFilter() {
		final FilterRegistrationBean<TeeFilter> filterRegBean = new FilterRegistrationBean<>();
		TeeFilter filter = new TeeFilter();
		filterRegBean.setFilter(filter);
		filterRegBean.setUrlPatterns(Arrays.asList("/v1/*"));
		filterRegBean.setName("requestFilter");
		filterRegBean.setAsyncSupported(Boolean.TRUE);
		return filterRegBean;
	}
}
