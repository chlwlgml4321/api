package kr.co.hectofinancial.mps.global.config.cicd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Release {
    private static final String BUILD_ID;
    private static final String BUILD_TIMESTAMP;

    private static final Logger log = LoggerFactory.getLogger(Release.class);

    private Release() {}

    static {
        String id = "MPS_API-build#0";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {
            Properties props = new Properties();
            ClassPathResource resource = new ClassPathResource("build-info.properties");
            props.load(resource.getInputStream());

            id = props.getProperty("build.id", id);
            timestamp = props.getProperty("build.timestamp", timestamp);
        }
        catch(IOException e) {}

        BUILD_ID = id;
        BUILD_TIMESTAMP = timestamp;
    }

    private static String repeatChar() {
        StringBuilder buff = new StringBuilder(50);
        for(int i = 0; i < 50; i++) {
            buff.append('#');
        }

        return buff.toString();
    }

    public static void print() {
        String border = repeatChar();

        StringBuilder buff = new StringBuilder();
        buff.append("\n").append(border).append("\n");
        buff.append(" APPLICATION BUILD INFO\n\n");
        buff.append(String.format("%-16s", " Build-ID")).append(" : ").append(BUILD_ID).append("\n");
        buff.append(String.format("%-16s", " Build-Timestamp")).append(" : ").append(BUILD_TIMESTAMP).append("\n");
        buff.append(border);

        log.info(buff.toString());
    }

}
