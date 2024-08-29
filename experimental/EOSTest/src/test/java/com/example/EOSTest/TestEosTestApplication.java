package com.example.EOSTest;

import org.springframework.boot.SpringApplication;

public class TestEosTestApplication {

	public static void main(String[] args) {
		SpringApplication.from(EosTestApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
