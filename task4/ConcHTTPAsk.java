import java.net.*;
import java.util.regex.PatternSyntaxException;
import java.io.*;
import dealwithclient.DealWithClient;
import dealwithclient.TCPClient;
import java.nio.charset.StandardCharsets;

public class ConcHTTPAsk {

    public static void main( String[] args) throws IOException{

        // ** SETTING UP THE WELCOME SOCKET ** //

        int port;
        try {

            port = Integer.parseInt(args[0]);
        }

        catch(Exception e){

            System.err.println("Usage: HTTPEcho port" + '\n' + "(Port must be integer value.)");
            return;
        }
        
        ServerSocket welcome = null;
    
        try{

            welcome = new ServerSocket(port);
        }

        catch(Exception e){

            System.err.println("Error setting up welcome socket.");
            return;
        }

        // ** SETTING UP THE CONNECTION SOCKET **//

        while(true){

            Socket connection = null;

            try{

                connection = welcome.accept();
                Thread t = new Thread(new DealWithClient(connection));
                t.start();
            }

            catch(Exception ex6){

                if (connection != null){

                    connection.close();
                }

                System.err.println("Error with connection socket.");
            } 
        }
    }
}

