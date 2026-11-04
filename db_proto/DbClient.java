import java.io.*;
import java.net.*;

public class DbClient {

    static InputStream in;
    static OutputStream out;

    // sends a request and waits for the response
    static RPC_Response sendRequest(RPC_Request request) throws IOException {
        request.writeDelimitedTo(out);
        out.flush();
        return RPC_Response.parseDelimitedFrom(in);
    }

    static void addRecord(int index, String record) throws IOException {
        RPC_Request request = RPC_Request.newBuilder()
                .setOperation(RPC_Request.Operation.ADD_RECORD)
                .setAddRecordArg(AddRecordRequest.newBuilder().setIndex(index).setRecord(record))
                .build();

        RPC_Response response = sendRequest(request);
        System.out.println("addRecord(" + index + ", " + record + ") success = "
                + response.getAddRecordResult().getSuccess());
    }

    static void getRecord(int index) throws IOException {
        RPC_Request request = RPC_Request.newBuilder()
                .setOperation(RPC_Request.Operation.GET_RECORD)
                .setGetRecordArg(GetRecordRequest.newBuilder().setIndex(index))
                .build();

        RPC_Response response = sendRequest(request);
        if (response.getGetRecordResult().getFound()) {
            System.out.println("getRecord(" + index + ") = " + response.getGetRecordResult().getRecord());
        } else {
            System.out.println("getRecord(" + index + ") = not found");
        }
    }

    static void getSize() throws IOException {
        RPC_Request request = RPC_Request.newBuilder()
                .setOperation(RPC_Request.Operation.GET_SIZE)
                .setGetSizeArg(GetSizeRequest.newBuilder())
                .build();

        RPC_Response response = sendRequest(request);
        System.out.println("getSize() = " + response.getGetSizeResult().getSize());
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5001);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // write the records from the table
        addRecord(4101, "Appen");
        addRecord(4102, "Ahrensburg");
        addRecord(4103, "Wedel");
        addRecord(4104, "Aum\u00fchle"); // Aumuehle with u-umlaut
        addRecord(4105, "Seevetal");
        addRecord(4106, "Quickborn");

        // read two records (4107 does not exist)
        getRecord(4103);
        getRecord(4107);

        // check that 6 records are stored
        getSize();

        socket.close();
    }
}
