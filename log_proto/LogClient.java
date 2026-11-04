import java.io.*;
import java.net.*;

public class LogClient {

    static OutputStream out;

    // builds one log message and sends it to the server
    static void sendLog(LogMessage.Severity severity, String text) throws IOException {
        LogMessage msg = LogMessage.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setSource("LogClient on my laptop")
                .setSeverity(severity)
                .setText(text)
                .build();

        msg.writeDelimitedTo(out); // writes the length first, then the message
        System.out.println("Sent: " + severity + " " + text);
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5000);
        out = socket.getOutputStream();

        sendLog(LogMessage.Severity.DEBUG, "Client started");
        sendLog(LogMessage.Severity.INFO, "Connected to the log server");
        sendLog(LogMessage.Severity.WARN, "Disk space is getting low");
        sendLog(LogMessage.Severity.ERROR, "Could not open config file");
        sendLog(LogMessage.Severity.FATAL, "Program must stop");

        // one message without the optional source field
        LogMessage noSource = LogMessage.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setSeverity(LogMessage.Severity.INFO)
                .setText("This message has no source")
                .build();
        noSource.writeDelimitedTo(out);

        out.flush();
        socket.close();
        System.out.println("Done, all messages sent");
    }
}
