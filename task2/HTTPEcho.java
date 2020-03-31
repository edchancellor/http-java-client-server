import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HTTPEcho {
    private static int BUFFERSIZE = 4048;
    public static void main( String[] args) throws IOException{

        int port;
        try {

            port = Integer.parseInt(args[0]);
        }

        catch(Exception e){

            System.err.println("Usage: HTTPEcho port" + '\n' + "(Port must be integer value.)");
            return;
        }
        
        byte [] recbuffer = new byte[BUFFERSIZE];
        String clientString = "";
        ServerSocket welcome = null;
    
        try{

            welcome = new ServerSocket(port);
        }

        catch(Exception e){

            System.err.println("Error setting up welcome socket.");
            return;
        }

        while(true){

            Socket connection = null;

            try{

                connection = welcome.accept();
                connection.setSoTimeout(1000);
                InputStream input = connection.getInputStream();
                OutputStream output = connection.getOutputStream();

                int count;
                StringBuilder builder = new StringBuilder();
                builder.append("HTTP/1.1 200 OK");
                builder.append('\r');
                builder.append('\n');
                builder.append("Connection: close");
                builder.append('\r');
                builder.append('\n');
                builder.append('\r');
                builder.append('\n');

                try{

                    while ((count = input.read(recbuffer)) > -1){

                        String clientResponse = new String(recbuffer, 0, count, StandardCharsets.UTF_8);
                        if (clientResponse.contains("\r\n\r\n")){

                            builder.append(clientResponse);
                            break;
                        }

                        builder.append(clientResponse);
                    }

                    clientString = builder.toString();
                }

                catch(SocketTimeoutException e){

                    clientString = builder.toString();
                }
            
                output.write(clientString.getBytes());
                connection.close();
            }

            catch(Exception e2){

                if (connection != null){

                    connection.close();
                }

                System.err.println("Error with connection socket.");
            } 
        }
    }
}

