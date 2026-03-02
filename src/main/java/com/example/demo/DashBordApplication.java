package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DashBordApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashBordApplication.class, args);

		// Запусти один раз в main() или в тесте
//		System.out.println(new BCryptPasswordEncoder().encode("admin123"));
	}
}
