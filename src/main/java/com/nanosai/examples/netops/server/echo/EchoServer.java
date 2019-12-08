package com.nanosai.examples.netops.server.echo;

import com.nanosai.examples.repeatedtasks.IRepeatedTask;
import com.nanosai.examples.repeatedtasks.RepeatedTaskExecutor;
import com.nanosai.netops.tcp.BytesBatch;
import com.nanosai.netops.tcp.TcpMessagePort;
import com.nanosai.netops.tcp.TcpServer;
import com.nanosai.threadops.threadloops.ThreadLoop;

import java.io.IOException;

public class EchoServer {

    public static void main(String[] args) throws IOException {

        NetOpsServerBuilder serverBuilder = new NetOpsServerBuilder();

        startTcpServer(serverBuilder);

        TcpMessagePort tcpMessagePort = serverBuilder.createTcpMessagePort();

        BytesBatch incomingMessageBatch = new BytesBatch(16, 64);

        EchoServerThreadLoopCycle echoServerThreadLoopCycle =
                new EchoServerThreadLoopCycle(tcpMessagePort, incomingMessageBatch);

        IRepeatedTask proactor = () -> {
            echoServerThreadLoopCycle.exec();
            return 0;
        };

        RepeatedTaskExecutor repeatedTaskExecutor = new RepeatedTaskExecutor(proactor);

        // echo server message processing loop:

        while(true) {
            repeatedTaskExecutor.exec();
        }



    }

    private static ThreadLoop startTcpServer(NetOpsServerBuilder serverBuilder) {

        TcpServer tcpServer = serverBuilder.createTcpServer();
        // create server
        try {
            tcpServer.init();
        } catch (IOException e) {
            System.out.println("Error initializing TcpServer: " + e.getMessage());
            throw new RuntimeException("Error initializing TcpServer: " + e.getMessage(), e);
        }

        // start threads to run it.
        return new ThreadLoop(() -> {
            try {
                tcpServer.checkForNewInboundConnections();
            } catch (IOException e) {
                System.out.println("Error checking for new connections. ");
                e.printStackTrace();
            }
        })
        .start();
    }



}
