package com.ticketmanagement.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.ticketmanagement.workflow",
        "org.kie.kogito.app",
        "org.kie.kogito.spring.auth"
})
public class WorkflowSlaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowSlaServiceApplication.class, args);
    }
}
