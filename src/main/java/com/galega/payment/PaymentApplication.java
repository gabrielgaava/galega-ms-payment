package com.galega.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.galega.payment.domain",
		"com.galega.payment.adapters",
		"com.galega.payment.entrypoints",
		"com.galega.payment.infrastructure"
})
public class PaymentApplication {

	public static void main(String[] args) {

		SpringApplication.run(PaymentApplication.class, args);
	}

}
