package br.senac.alocasenac;

import org.springframework.boot.SpringApplication;

public class TestAlocaSenacApplication {

	public static void main(String[] args) {
		SpringApplication.from(AlocaSenacApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
