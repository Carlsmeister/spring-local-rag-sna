package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
		"org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
		"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
		"org.springframework.boot.devtools.autoconfigure.DevToolsDataSourceAutoConfiguration",
		"org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration"
})
public class LocalAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalAiApplication.class, args);
	}

}
