package co.edu.escuelaing.techcup.logistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Servicio de Logistica - TechCup Futbol")
                        .description("Refrigerios y dotacion para equipos y jugadores del torneo. "
                                + "El JWT ya se valida en el API Gateway: este servicio espera los headers "
                                + "X-User-Id y X-User-Role reenviados por el Gateway. Los endpoints que "
                                + "requieren rol organizador documentan X-User-Role en su propia operacion; "
                                + "X-User-Id se pide como parametro estandar donde aplica.")
                        .version("v1")
                        .contact(new Contact().name("Equipo Logistica - TechCup Futbol")));
    }
}
