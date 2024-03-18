package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    
    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket clientSocket = new Socket(hostname, port);
        
        OutputStream out = clientSocket.getOutputStream();

        out.write(toServerBytes);
        
        InputStream in = clientSocket.getInputStream();

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] small = new byte[1024];
        int bytesRead = in.read(small);
            while (bytesRead != -1) {
                response.write(small, 0, bytesRead);
                bytesRead = in.read(small);
            }

        
        out.close();
        in.close();
        clientSocket.close();
        
        return response.toByteArray();
    }
    public byte[] askServer(String hostname, int port) throws IOException{
        Socket clientSocket = new Socket();
        InputStream in = clientSocket.getInputStream();

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] small = new byte[1024];
        int bytesRead = in.read(small);
            while (bytesRead != -1) {
                response.write(small, 0, bytesRead);
                bytesRead = in.read(small);
            }

        in.close();
        clientSocket.close();
        
        return response.toByteArray();
    }
}
