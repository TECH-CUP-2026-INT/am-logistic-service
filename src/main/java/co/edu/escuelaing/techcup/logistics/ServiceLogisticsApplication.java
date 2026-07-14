package co.edu.escuelaing.techcup.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServiceLogisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceLogisticsApplication.class, args);
	}

}
