
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class HTTPServer {

    public static final int PORT = 80;
    public static final String IP = "127.0.0.1";
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;
    public static String currentDirectory = System.getProperty("user.dir") + "\\server_folder";

    public static void main(String[] args){
        System.out.println(currentDirectory);
        System.out.println("server is listening to port 80");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("get connection from IP: " + socket.getRemoteSocketAddress());

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                String fileRequest = ""; /* holds file name */
                String[] files; /* holds names of all files in a folder */
                FileInputStream readFile; /* for copying files */
                File currentFile; /* holds file to*/
                byte[] data; /* holds data to be sent*/
                int numBytes; /* number of bytes in file to be sent*/
                boolean hasFile = false; /* whether file requested exists or not*/

                // takes client HTML request and parses file name or file path
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(dataInputStream));
                String line = bufferedReader.readLine();
                while(line != null && !line.isEmpty()){
                    System.out.println(line);
                    if (line.substring(0,3).equals("GET")) {    // catches file name
                        String[] parts = line.split(" "); // splits into three parts
                        fileRequest = parts[1];                 // command, fileRequested, and the protocol and <CRLF>
                    }
                    line  = bufferedReader.readLine();
                }
				bufferedReader.close();

                // finds file if it exists
                if (fileRequest.equals("/")) { // ------------------------ no specified file
                    // get file contents
                    readFile = new FileInputStream(currentDirectory + "\\index.html");
                    data = readFile.readAllBytes();
                    numBytes = data.length;

                    hasFile = true;
                    readFile.close();
                } else { // ----------------------------------------------- user specified file name/path
                    if (fileRequest.charAt(0) == '/') { // file path is specified
                        fileRequest = fileRequest.replace("/","\\"); // switches / to \ for valid file path
                    } else { // file name is specified
                        fileRequest = "\\" + fileRequest; // adding \ for valid file path
                    }

                    // get file contents
                    try {
                        readFile = new FileInputStream(currentDirectory  + fileRequest);
                        data = readFile.readAllBytes();
                        numBytes = data.length;

                        hasFile = true;
                        readFile.close();
                    } catch (Exception e) {
                        System.out.println("File not found");
                    }
                }

                // create response message
//                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
//                printWriter.print("GET / HTTP/1.1" + CRLF);
//                printWriter.print("Host: " + SERVER_ADDR + CRLF);
//                printWriter.print("Connection: close" + CRLF);
//                printWriter.print("Accept: */*" + EOH);
//                printWriter.flush();
            }

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }




    }





}
