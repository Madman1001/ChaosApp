package com.lhr.chaos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestTCPServer {
    private static final int PORT_SERVER = 10086;
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(PORT_SERVER);
        System.out.println("TCP SERVER IS READY " + InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort());
        while (true){
            Socket socket = serverSocket.accept();
            System.out.println("connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        handleSocket(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private static void handleSocket(Socket socket) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        while(true){
            String str = reader.readLine();
            if (str == null){
                if (socket.isClosed() || !socket.isConnected()){
                    break;
                }
            }
            System.out.println(socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " -- " + str);
            writer.write("Server is receive:" + str);
            writer.flush();
            if ("exit".equals(str)) {
                break;
            }
        }
        reader.close();
        writer.close();
        socket.close();
    }
}
