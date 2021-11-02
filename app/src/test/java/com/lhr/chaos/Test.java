package com.lhr.chaos;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws InterruptedException, IOException {
        Runtime runtime = Runtime.getRuntime();
        int i = 0;
        while (++i <= 1000) {
            Thread.sleep(10000);
            runtime.exec("adb shell input swipe 700 500 500 100");
            System.out.println(i);
        }
    }
}
