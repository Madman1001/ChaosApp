package com.lhr.chaos;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class TestUDPClient {
    public static void main(String[] args) throws Exception{
        DatagramSocket client = new DatagramSocket();
        byte[] data = "Hello, java UDP.".getBytes();
        DatagramPacket dp = new DatagramPacket(data,data.length);
        dp.setSocketAddress(new InetSocketAddress("192.168.2.2",10086));
        client.send(dp);

        // byte[] receiveData = new byte[1024];
        // DatagramPacket receiveDp = new DatagramPacket(receiveData,receiveData.length);
        // client.receive(receiveDp);
        // String str = new String(receiveData,0,receiveDp.getLength());
        // System.out.println(str);
        client.close();
    }
}