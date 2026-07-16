package co.edu.escuelaing.techcup.logistics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import co.edu.escuelaing.techcup.logistics.support.AbstractMongoIntegrationTest;

class ServiceLogisticsApplicationTests extends AbstractMongoIntegrationTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
	}

}
