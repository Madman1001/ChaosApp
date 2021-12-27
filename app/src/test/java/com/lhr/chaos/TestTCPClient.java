package com.lhr.chaos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TestTCPClient {
    public static void main(String[] args) throws Exception{
        //auto connect
        Socket client = new Socket("192.168.2.249",10086);
        Thread inHand = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleInput(client.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            handleOutput(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        inHand.interrupt();
        client.close();
    }

    private static void handleInput(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        while (!Thread.interrupted()) {
            String resp = reader.readLine();
            System.out.println("<<<" + resp);
        }
        reader.close();
    }

    private static void handleOutput(OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(System.in);
        while (!Thread.interrupted()) {
            System.out.print(">>>");
            String ss = scanner.nextLine();
            writer.write(ss);
            writer.newLine();
            writer.flush();
            if (ss.equals("exit")){
                break;
            }
        }
        writer.close();
    }
}