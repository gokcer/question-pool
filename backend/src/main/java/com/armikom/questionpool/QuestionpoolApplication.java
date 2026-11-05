package com.armikom.questionpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuestionpoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuestionpoolApplication.class, args);
	}

	/** Background work (sending mail, AI calls) so that requests never wait for them. */
	@Bean(destroyMethod = "shutdown")
	ExecutorService backgroundExecutor() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}
}
