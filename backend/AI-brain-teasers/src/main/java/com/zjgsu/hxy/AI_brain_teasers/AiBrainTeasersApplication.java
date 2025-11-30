package com.zjgsu.hxy.AI_brain_teasers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.zjgsu.hxy.AI_brain_teasers", "com.tfzhang.ainaojin"})
public class AiBrainTeasersApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBrainTeasersApplication.class, args);
    }

}
