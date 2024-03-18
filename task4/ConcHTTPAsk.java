import java.io.*;
import java.net.*;
import tcpclient.*;

public class ConcHTTPAsk {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java HTTPAsk <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("HTTPAsk server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from client");

                // Create an instance of MyRunnable and pass clientSocket to it
                MyRunnable myRunnable = new MyRunnable(clientSocket);
                Thread thread = new Thread(myRunnable);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
