package kr.co.hectofinancial.mps.global.mtms;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.lang.management.ManagementFactory;

public class ProcessIdConverter extends ClassicConverter {
    private static final String PROCESS_ID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0].trim();

    @Override
    public String convert(ILoggingEvent event) {
        return PROCESS_ID;
    }
}
