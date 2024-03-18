package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    private boolean shutdown;
    private Integer timeout;
    private Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        Socket clientSocket = new Socket(hostname, port);

        if(timeout != null){
            clientSocket.setSoTimeout(timeout);
        }

        OutputStream out = clientSocket.getOutputStream();
        out.write(toServerBytes);
        if(shutdown){
            clientSocket.shutdownOutput();
        }
        InputStream in = clientSocket.getInputStream();
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        try{
        byte[] small = new byte[1024];
        int bytesRead = in.read(small);
            while (bytesRead != -1) {
                response.write(small, 0, bytesRead);
                if (limit != null && response.size() >= limit) {
                    break;
                }

                bytesRead = in.read(small);
            }
        }catch(SocketTimeoutException e){

        }finally{
            out.close();
            in.close();
            clientSocket.close();
    
        
        }
        return response.toByteArray();
    }
    public byte[] askServer(String hostname, int port) throws IOException{
        Socket clientSocket = new Socket();
        InputStream in = clientSocket.getInputStream();

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] small = new byte[1024];
        int bytesRead = in.read(small);
        long startTime = System.currentTimeMillis();
            while (bytesRead != -1) {
                response.write(small, 0, bytesRead);
                if (limit != null && response.size() >= limit) {
                    break;
                }
                if(timeout != null && System.currentTimeMillis() - startTime > timeout){
                    break;
                }
                bytesRead = in.read(small);
            }

        in.close();
        clientSocket.close();
        
        return response.toByteArray();
    }
}
