package com.monkey01.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 15:09 17/11/27.
 * @modify by:
 */
@SpringBootApplication
@EnableScheduling
public class BatchApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(BatchApplication.class, args);
	}
}
