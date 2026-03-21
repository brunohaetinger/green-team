package com.greenteam.webserverdatacollector.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/collector")
public class HelloController {

	@GetMapping("/hello")
	public ResponseEntity<String> sayHello() {

		return ResponseEntity.ok("Hello");
	}

}
