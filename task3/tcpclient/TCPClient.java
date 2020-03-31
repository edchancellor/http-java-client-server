package tcpclient;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class TCPClient {

    // NOTE: This client is intended for servers which close their connection. If the server does not close their connection, the 
    // client will wait one second, and then return any information which has been sent so far.

    private static int BUFFERSIZE = 4048;
    
    public static String askServer(String hostname, int port, String ToServer) throws  IOException, SocketTimeoutException{
        
        if (ToServer == null){
            return askServer(hostname, port);
        }

        else{

            Socket clientSocket = null;
            String serverOutput = "";
            try{

                // Create a socket to connect to the provided hostname and port.
                // Set a timeout for if the server is unresponsive.
                clientSocket = new Socket(hostname, port);
                clientSocket.setSoTimeout(3000);

                // Create input and output streams on this socket.
                OutputStream output = clientSocket.getOutputStream();
                InputStream input = clientSocket.getInputStream();

                // Send the String to the server.
                byte[] senbuffer = ToServer.getBytes(StandardCharsets.UTF_8);
                output.write(senbuffer,0,senbuffer.length);
                output.write('\n');
        
                // Create a buffer for the response, and load in the response. If there is no response from the server within 
                // 1000 miliseconds, an exception is thrown.
                int count;
                StringBuilder builder = new StringBuilder();
                byte[] recbuffer = new byte[BUFFERSIZE];

                try{

                    while ((count = input.read(recbuffer)) > -1){
                        String serverResponse = new String(recbuffer, 0, count, StandardCharsets.UTF_8);
                        builder.append(serverResponse);
                    }

                    serverOutput = builder.toString();
                }

                // If the server stops sending data after one second waiting, close the connection and return the data sent so far.
                catch(SocketTimeoutException e){
            
                    clientSocket.close();
                    throw new SocketTimeoutException("Server timed out.");
                }
            }

            // Check if there have been any IO exceptions during the whole conversation.
            catch(Exception e2){

                throw new IOException("There has been an error in the IO Connection.");
                
            }

            // finally, close the connection, regardless of whether things have gone well or not.
            finally{

                try{

                    if(clientSocket != null){

                        clientSocket.close();
                    }
                }

                catch(IOException e3){

                    throw new IOException("There has been an error closing the connection.");
                }
            }

            return serverOutput;
        }
    }





    public static String askServer(String hostname, int port) throws  IOException, SocketTimeoutException
    {   
        // Nearly identical to the code above, although since there is no optional string provided, 
        // we only need to transmit the newline character to the server. Receiving works in the same way as before.

        Socket clientSocket = null;
        String serverOutput = "";
        try{

            // Create a socket to connect to the provided hostname and port.
            clientSocket = new Socket(hostname, port);
            clientSocket.setSoTimeout(3000);

            // Create input and output streams on this socket.
            OutputStream output = clientSocket.getOutputStream();
            InputStream input = clientSocket.getInputStream();
            output.write('\n');
        
            // Create a buffer for the response, and a Stringbuilder for building the string from it.
            int count;
            StringBuilder builder = new StringBuilder();
            byte[] recbuffer = new byte[BUFFERSIZE];
            try{
                
                while ((count = input.read(recbuffer)) > -1){

                    String serverResponse = new String(recbuffer, 0, count, StandardCharsets.UTF_8);
                    builder.append(serverResponse);
                }

                serverOutput = builder.toString();

            }

            // If the server stops sending data after a timeout, close the socket and return any data we have received so far.
            catch(SocketTimeoutException e){
                
                clientSocket.close();
                throw new SocketTimeoutException("Server timed out.");
            }
        }

        catch(Exception e2){

            throw new IOException("There has been an error in the IO connection.");
        }
        
        finally{
            
            try{
                
                if(clientSocket != null){

                    clientSocket.close();
                }
            }

            catch(IOException e3){

                throw new IOException("There has been an error closing the connection.");
            }
        }

        return serverOutput;
    }
}

