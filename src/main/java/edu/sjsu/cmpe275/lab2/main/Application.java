package edu.sjsu.cmpe275.lab2.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by parth on 4/16/2017.
 */
@ComponentScan(value = "edu.sjsu.cmpe275.lab2.controller")
@EntityScan("edu.sjsu.cmpe275.lab2.model")
@EnableJpaRepositories("edu.sjsu.cmpe275.lab2.dao")
@SpringBootApplication
public class Application
{
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

