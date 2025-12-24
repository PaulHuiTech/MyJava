package org.paulhui.pub;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static void print(String content) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        System.out.println("["+formattedDateTime+" Thread-"+Thread.currentThread().getName()+"]"+content);
    }
}
