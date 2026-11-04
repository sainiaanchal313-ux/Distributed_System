import java.io.*;
import java.net.*;
import java.util.Date;

public class LogServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Log server started on port 5000");

        while (true) {
            // wait for a client
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            InputStream in = socket.getInputStream();
            FileWriter logFile = new FileWriter("server.log", true); // true = append

            // read log messages until the client closes the connection
            LogMessage msg = LogMessage.parseDelimitedFrom(in);
            while (msg != null) {
                String source = "unknown";
                if (msg.hasSource()) {
                    source = msg.getSource();
                }

                // one log entry = one line in the file
                String line = new Date(msg.getTimestamp()) + " [" + msg.getSeverity() + "] "
                        + source + ": " + msg.getText();

                logFile.write(line + "\n");
                logFile.flush();
                System.out.println(line);

                msg = LogMessage.parseDelimitedFrom(in);
            }

            logFile.close();
            socket.close();
            System.out.println("Client disconnected");
        }
    }
}
