package com.lhr.chaos;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class TestUDPServer {
    private static final int PORT_SERVER = 10086;
    public static void main(String[] args) throws Exception{
        DatagramSocket receive = new DatagramSocket(PORT_SERVER);
        byte[] data = new byte[1024];
        DatagramPacket dp = new DatagramPacket(data,data.length);
        System.out.println("UDP SERVER IS READY " + InetAddress.getLocalHost().getHostAddress() + ":" + receive.getLocalPort());

        while(true){
        	receive.receive(dp);
        	String str = new String(data,0,dp.getLength(), StandardCharsets.UTF_8);
        	if (!str.equals("exit")) {
        		System.out.println(dp.getAddress().getHostAddress() + ":" + dp.getPort() + " -- " + str);
                byte[] receiveData = "Server is receive".getBytes();
                DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);
                receivePacket.setSocketAddress(dp.getSocketAddress());
                receive.send(receivePacket);
        		continue;
        	}

        	break;
        }
        
        System.out.println("socket is over!");
        receive.close();
    }
}