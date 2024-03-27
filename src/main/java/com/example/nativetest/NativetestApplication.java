package com.example.nativetest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class NativetestApplication {
	Logger logger = LoggerFactory.getLogger(NativetestApplication.class);

	@Autowired
	SampleBean sampleBean;

	public static void main(String[] args) {
		SpringApplication.run(NativetestApplication.class, args);
	}

	@RequestMapping("/")
	String home() {
		logger.info("Home method executed");
		return sampleBean.getMessage();
	}

}
