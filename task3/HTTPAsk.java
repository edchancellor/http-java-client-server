import java.net.*;
import java.util.regex.PatternSyntaxException;
import java.io.*;
import tcpclient.TCPClient;
import java.nio.charset.StandardCharsets;

public class HTTPAsk {
    private static int BUFFERSIZE = 4048;
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

        // ** SETTING UP THE CONNECTION SOCKET **//

        while(true){

            Socket connection = null;

            try{

                connection = welcome.accept();
                connection.setSoTimeout(1000);
                InputStream input = connection.getInputStream();
                OutputStream output = connection.getOutputStream();

                int count;
                StringBuilder inFromClient = new StringBuilder();
                StringBuilder builder = new StringBuilder();

                // ** READ IN THE GET REQUEST FROM THE CONNECTION SOCKET ** //

                try{

                    while ((count = input.read(recbuffer)) > -1){

                        String clientResponse = new String(recbuffer, 0, count, StandardCharsets.UTF_8);
                        if (clientResponse.contains("\r\n")){

                            inFromClient.append(clientResponse);
                            break;
                        }

                        inFromClient.append(clientResponse);
                    }

                    clientString = inFromClient.toString();
                }

                catch(SocketTimeoutException e){

                    clientString = inFromClient.toString();
                }

                // ** ANALYSE THE GET REQUEST ** //

                String host = null;
                int portnumber = -1;
                String text = null;
                boolean errors = false;

                try{

                    // ** CHECK THAT THE GET REQUEST CONTAINS ASK, HOSTNAME, AND PORT ** //
                    String [] GETArray = clientString.split("\r\n");
                    String GETRequest = GETArray[0];

                    // ASK
                    String [] askArray = GETRequest.split("/ask?");
                    String check = askArray[0];
                    if(check != "GET " && askArray.length != 2){
                        throw new Exception("Problem with /ask?");
                    }

                    // HOSTNAME
                    String [] firstArray = GETRequest.split("hostname=");
                    if(firstArray.length != 2){
                        throw new Exception("Problem with hostname");
                    }
                    String [] secondArray = firstArray[1].split(" ");
                    try{
                        String [] thirdArray = secondArray[0].split("&");
                        host = thirdArray[0];
                    }
                    catch(Exception ex1){
                        host = firstArray[0];
                    }

                    // PORT
                    String [] fourthArray = GETRequest.split("port=");
                    if(fourthArray.length != 2){
                        throw new Exception("Problem with port");
                    }
                    String [] fifthArray = fourthArray[1].split(" ");
                    try{
                        String [] sixthArray = fifthArray[0].split("&");
                        String portnumberString = sixthArray[0];
                        portnumber = Integer.parseInt(portnumberString);
                    }
                    catch(Exception ex2){
                        String portnumberString = fifthArray[0];
                        portnumber = Integer.parseInt(portnumberString);
                    }
                }
                // IF THE MESSAGE DOES NOT SPECIFY A PORT NUMBER, HOSTNAME OR ASK, IT IS A BAD REQUEST
                catch(Exception ex3){
                    errors = true;
                }

                // ** TRY TO GET A STRING FROM THE GET REQUEST, IF NOT KEEP STRING AS NULL ** //

                try{
                    String [] GETArray = clientString.split("\r\n"); // could change back to Host:
                    String GETRequest = GETArray[0]; 
                    String [] seventhArray = GETRequest.split("string=");
                    if(seventhArray.length > 2){
                        errors = true;
                    }

                    String [] eighthArray = seventhArray[1].split(" ");
                    try{
                        String [] ninthArray = eighthArray[0].split("&");
                        text = ninthArray[0];
                    }
                    catch(Exception ex4){
                        text = eighthArray[0];
                    }
                }
                catch(Exception ex5){
                    text = null;
                }

                // ** TRY TO COMMUNICATE WITH THE SERVER **//

                if (errors == false){

                    try{
                        String response = TCPClient.askServer(host,portnumber,text);
                        builder.append("HTTP/1.1 200 OK");
                        builder.append('\r');
                        builder.append('\n');
                        builder.append("Connection: close");
                        builder.append('\r');
                        builder.append('\n');
                        builder.append('\r');
                        builder.append('\n');
                        builder.append(response);
                    }
                    catch(Exception ex8){
                        builder.append("HTTP/1.1 404 NOT_FOUND");
                        builder.append('\r');
                        builder.append('\n');
                        builder.append("Connection: close");
                        builder.append('\r');
                        builder.append('\n');
                        builder.append('\r');
                        builder.append('\n');
                        builder.append("File not found. Your URL syntax was correct, but nothing could be retreived from the server.");
                    }
                    
                    String finalResponse = builder.toString();
                    output.write(finalResponse.getBytes());
                    connection.close();
                }
                else{
                    builder.append("HTTP/1.1 400 BAD_REQUEST");
                    builder.append('\r');
                    builder.append('\n');
                    builder.append("Connection: close");
                    builder.append('\r');
                    builder.append('\n');
                    builder.append('\r');
                    builder.append('\n');
                    builder.append("You made a bad request. For example, your URL was corrupted, you failed to provide a port or hostname, or your used the wrong syntax in your URL.");
                    String finalResponse = builder.toString();
                    output.write(finalResponse.getBytes());
                    connection.close();
                }
                
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

