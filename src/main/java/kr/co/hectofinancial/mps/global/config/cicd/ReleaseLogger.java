package kr.co.hectofinancial.mps.global.config.cicd;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ReleaseLogger {

    @PostConstruct
    public void printBuildInfo() {
//        Release.print();
    }
}
