package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class 	DashBordApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashBordApplication.class, args);
	}
}


//(c) Корпорация Майкрософт (Microsoft Corporation). Все права защищены.
//
//		C:\Users\Acer>netstat -ano | findstr :8080
//TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       20964
//TCP    [::]:8080              [::]:0                 LISTENING       20964

//C:\Users\Acer>taskkill /F /PID 20964
//Успешно: Процесс, с идентификатором 20964, успешно завершен.








