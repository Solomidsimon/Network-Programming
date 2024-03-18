import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import tcpclient.*;

public class HTTPAsk {
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

                // Read HTTP request from client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String request = in.readLine();
                String[] requestParts = request.split(" ");
                String method = requestParts[0];
                String uri = requestParts[1];
                String http = requestParts[2];

                // Parse URI for parameters
                String[] uriParts = uri.split("\\?");
                String path = uriParts[0];
                String queryString = uriParts.length > 1 ? uriParts[1] : "";

                // Process GET request
                if (method.equals("GET") && path.equals("/ask")) {
                    handleGetRequest(clientSocket, queryString, http); // Pass queryString
                } else if(!path.equals("/ask")){
                    sendNotFoundResponse(clientSocket);
                }else{
                    sendBadRequestResponse(clientSocket);
                }

                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleGetRequest(Socket clientSocket, String queryString, String http) {
        String[] params = queryString.split("&");
        String hostname = "";
        int port = 0;
        String stringToSend = "";
        boolean shutdown = false;
        int limit = -1;
        Integer timeout = null; 

        for (String param : params) {
            String[] keyValue = param.split("=");
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            switch (key) {
                case "hostname":
                    hostname = value;
                    break;
                case "port":
                    port = Integer.parseInt(value);
                    break;
                case "string":
                    stringToSend = value;
                    break;
                case "shutdown":
                    shutdown = Boolean.parseBoolean(value);
                    break;
                case "limit":
                    limit = Integer.parseInt(value);
                    break;
                case "timeout":
                    timeout = Integer.parseInt(value);
                    break;
                default:
                    // Ignore unrecognized parameters
                    break;
            }
        }

        // Validate parameters
        if (hostname.isEmpty() || port == -1 || !http.equals("HTTP/1.1")) {
            try {
                sendBadRequestResponse(clientSocket); // Send 400 Bad Request response
            } catch (IOException e) {
                e.printStackTrace();
            }
            return; // Terminate the connection
        }

    

        try {
            TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
            byte[] responseBytes;
            if (timeout != null) {
                responseBytes = tcpClient.askServer(hostname, port, stringToSend.getBytes());
            } else {
                responseBytes = tcpClient.askServer(hostname, port, stringToSend.getBytes());
            }

            // Send HTTP response
            OutputStream out = clientSocket.getOutputStream();
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: text/plain\r\n".getBytes());
            out.write("\r\n".getBytes()); // End of headers
            out.write(responseBytes);
            out.flush();

        } catch (Exception e) {
            try {
                sendNotFoundResponse(clientSocket); // Send 404 Not Found response
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                clientSocket.close(); // Ensure the connection is closed after processing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendBadRequestResponse(Socket clientSocket) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
        out.flush();
        clientSocket.close();
    }

    private static void sendNotFoundResponse(Socket clientSocket) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        out.flush();
        clientSocket.close();
    }
}
