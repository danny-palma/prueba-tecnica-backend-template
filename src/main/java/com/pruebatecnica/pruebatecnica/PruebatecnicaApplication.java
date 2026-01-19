package com.pruebatecnica.pruebatecnica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal que inicia la aplicación Spring Boot.
 * Configura automáticamente el contexto de Spring y arranca el servidor
 * embebido.
 */
@SpringBootApplication
public class PruebatecnicaApplication {

	/**
	 * Punto de entrada principal de la aplicación.
	 * 
	 * @param args Argumentos de la línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(PruebatecnicaApplication.class, args);
	}

}
