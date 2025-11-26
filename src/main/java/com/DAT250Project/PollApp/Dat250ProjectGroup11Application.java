package com.DAT250Project.PollApp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "PollApp Documentation",
                version = "1.0",
                description = "Poll Application API from DAT250-Group11"
        )
)
public class Dat250ProjectGroup11Application {

	public static void main(String[] args) {
		SpringApplication.run(Dat250ProjectGroup11Application.class, args);
	}

}
