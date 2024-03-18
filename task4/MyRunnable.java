import java.io.*;
import java.net.*;
import tcpclient.*;

public class MyRunnable implements Runnable {
    private Socket clientSocket;

    public MyRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
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
                handleGetRequest(queryString, http); // Pass queryString
            } else if(!path.equals("/ask")){
                sendNotFoundResponse();
            }else{
                sendBadRequestResponse();
            }
            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(String queryString, String http) throws IOException {
        String[] params = queryString.split("&");
        String hostname = null;
        int port = 0;
        String stringToSend = "";
        boolean shutdown = false;
        Integer limit = null;
        Integer timeout = null;

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length != 2) {
                sendBadRequestResponse();
                return;
            }
            String key = keyValue[0];
            String value = keyValue[1];
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
        if (hostname == null || port == -1 || !http.equals("HTTP/1.1")) {
            sendBadRequestResponse();
            return;
        }


        try {
            TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
            byte[] responseBytes = tcpClient.askServer(hostname, port, stringToSend.getBytes());

            // Send HTTP response
            OutputStream out = clientSocket.getOutputStream();
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: text/plain\r\n".getBytes());
            out.write("\r\n".getBytes()); // End of headers
            out.write(responseBytes);
            out.flush();

        } catch (Exception e) {
            sendNotFoundResponse();
        }
    }

    private void sendBadRequestResponse() throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
        out.flush();
        clientSocket.close();
    }

    private void sendNotFoundResponse() throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        out.flush();
        clientSocket.close();
    }
}
