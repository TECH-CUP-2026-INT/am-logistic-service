package co.edu.escuelaing.techcup.logistics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import co.edu.escuelaing.techcup.logistics.security.OrganizadorInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final OrganizadorInterceptor organizadorInterceptor;

    public WebMvcConfig(OrganizadorInterceptor organizadorInterceptor) {
        this.organizadorInterceptor = organizadorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(organizadorInterceptor);
    }
}
