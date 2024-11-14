package com.galega.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.galega.payment.domain",
		"com.galega.payment.application",
		"com.galega.payment.domain",
		"com.galega.payment.infrastructure",
		"com.galega.payment.infrastructure.modules.aws",
		"com.galega.payment.infrastructure.modules.spring"
})
public class PaymentApplication {

	public static void main(String[] args) {

		SpringApplication.run(PaymentApplication.class, args);
	}

}
