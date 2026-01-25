package org.example.titanworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class TitanWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TitanWorkerApplication.class, args);
    }

}
