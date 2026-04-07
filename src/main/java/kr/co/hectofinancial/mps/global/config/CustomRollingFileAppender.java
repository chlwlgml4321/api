package kr.co.hectofinancial.mps.global.config;

import ch.qos.logback.core.rolling.RollingFileAppender;
import java.io.File;
import java.io.IOException;

public class CustomRollingFileAppender<E> extends RollingFileAppender<E> {

    @Override
    public void openFile(String fileName) throws IOException {
        super.openFile(fileName);

        // 파일 권한을 664으로 설정
        File logFile = new File(fileName);
        String command = "chmod 664 " + logFile.getAbsolutePath();
        Runtime.getRuntime().exec(command);
    }
}
