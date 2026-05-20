package moe.takochan.webnei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebneiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebneiApplication.class, args);
	}

}
