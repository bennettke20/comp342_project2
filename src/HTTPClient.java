
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HTTPClient {

    public static final int PORT = 80;
    public static final String CRLF = "\r\n";
    public static final String EOH = CRLF + CRLF;

    public static final int CHUNK_SIZE = 512;				// size of fragment to process
    public static String currentDirectory;

    public static void main(String[] args)  {

        // Check for client_folder
        File currentFile;
        String[] files;
        PrintWriter writeFile;

        currentDirectory =  System.getProperty("user.dir");
        currentFile = new File(currentDirectory);
        files = currentFile.list();
        boolean foundDir = false;
        for (int i=0; i<files.length; i++) {
            if (files[i].equals("client_folder")) foundDir = true;
        }
        if (foundDir) {
            System.out.println("client_folder detected successfully");
            currentDirectory = currentDirectory + "\\client_folder";
        } else {
            System.out.println("ERROR: client_folder must be present in working directory");
            System.exit(0);
        }

        // Get user input and check validity
        Scanner scan = new Scanner(System.in);
        System.out.println("Welcome HTTP Client, input your request as <IP/hostname> <optional, file to reqeust>:");
        String request = scan.nextLine();
        String[] cmd = request.split(" "); // split command into individual fields

        if (cmd.length < 1 || cmd.length > 2) {
            System.out.println("ERROR: incorrect input format, exiting");
            System.exit(0);
        }

        // Generate HTTP request and receive response
        boolean isFileRequest = false;
        String fileName = "index.html";

        String serverAddr = cmd[0];
        if (cmd.length == 2) {
            isFileRequest = true;
            String[] pathSplit = cmd[1].split("/");
            // get last element of path to extract expected file name
            fileName = pathSplit[pathSplit.length-1];
            System.out.println("Sending request for specific file...");
        } else {
            System.out.println("Sending request...");
        }
        try {
            Socket socket = new Socket(serverAddr, PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(dataInputStream));

            // generate a HTTP request a print writer, handy to handle output stream
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(dataOutputStream, StandardCharsets.ISO_8859_1));
            if (isFileRequest) {
                printWriter.print("GET /" + cmd[1] + " HTTP/1.1" + CRLF);
            } else {
                printWriter.print("GET / HTTP/1.1" + CRLF);
            }
            printWriter.print("Host: " + serverAddr + CRLF);
            printWriter.print("Connection: close" + CRLF);
            printWriter.print("Accept: */*" + EOH);
            printWriter.flush();

            StringBuilder resp = new StringBuilder();
            StringBuilder body = new StringBuilder();
            boolean isError = false;
            boolean isMessageBody = false;

            String line = bufferedReader.readLine();
            String[] codes = line.split(" ");

            // check for error codes
            int code = Integer.parseInt(codes[1]);
            // check for error
            if (code >= 500) {
                isError = true;
                System.out.print("Received Server Error HTML response code (5xx)\nError: ");
            } else if (code >= 400) {
                isError = true;
                System.out.print("Received Client Error HTML response code (4xx)\nError: ");
            }
            // check for successful file transfer (if applicable)
            if (isFileRequest && (code < 200 || code >= 300)) { // then request was a success
                fileName = "index.html";
                // if response is anything other than success, returned text will be saved as index.html
            }

            while(line != null) {// && !line.isEmpty()){
                //System.out.println(line);
                if (isMessageBody) {
                    body.append(line + "\n");
                } else if (line.isEmpty()) {
                    // should see an empty line when the message body begins (double CRlF)
                    isMessageBody = true;
                    resp.append(line);
                } else {
                    resp.append(line + "\n");
                }
                line  = bufferedReader.readLine();
            }
            System.out.println(resp);

            // Write to file
            writeFile = new PrintWriter(currentDirectory + "\\" + fileName);
            writeFile.print(body);
            //System.out.println("AVAILABLE " + dataInputStream.available());

            bufferedReader.close();
            writeFile.close();
            printWriter.close();

        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

}
