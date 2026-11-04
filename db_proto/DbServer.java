import java.io.*;
import java.net.*;

public class DbServer {

    // the "database": two arrays that belong together
    // indexes[0] belongs to records[0], indexes[1] to records[1], ...
    static int[] indexes = new int[100];
    static String[] records = new String[100];
    static int count = 0; // how many records are stored

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5001);
        System.out.println("Database server started on port 5001");

        while (true) {
            // wait for a client
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // read requests until the client closes the connection
            RPC_Request request = RPC_Request.parseDelimitedFrom(in);
            while (request != null) {

                RPC_Response.Builder response = RPC_Response.newBuilder();
                response.setOperation(request.getOperation());

                if (request.getOperation() == RPC_Request.Operation.GET_RECORD) {
                    int index = request.getGetRecordArg().getIndex();
                    GetRecordResponse.Builder result = GetRecordResponse.newBuilder();

                    // look for the index in the array
                    boolean found = false;
                    for (int i = 0; i < count; i++) {
                        if (indexes[i] == index) {
                            result.setFound(true);
                            result.setRecord(records[i]);
                            found = true;
                        }
                    }
                    if (found == false) {
                        result.setFound(false); // index does not exist
                    }

                    response.setGetRecordResult(result);
                    System.out.println("getRecord(" + index + ")");

                } else if (request.getOperation() == RPC_Request.Operation.ADD_RECORD) {
                    int index = request.getAddRecordArg().getIndex();
                    String record = request.getAddRecordArg().getRecord();

                    // if the index already exists, replace the record
                    boolean exists = false;
                    for (int i = 0; i < count; i++) {
                        if (indexes[i] == index) {
                            records[i] = record;
                            exists = true;
                        }
                    }
                    // otherwise add it at the end
                    if (exists == false) {
                        indexes[count] = index;
                        records[count] = record;
                        count++;
                    }

                    response.setAddRecordResult(AddRecordResponse.newBuilder().setSuccess(true));
                    System.out.println("addRecord(" + index + ", " + record + ")");

                } else if (request.getOperation() == RPC_Request.Operation.GET_SIZE) {
                    int size = count;
                    response.setGetSizeResult(GetSizeResponse.newBuilder().setSize(size));
                    System.out.println("getSize() = " + size);

                } else {
                    response.setError("Unknown operation");
                }

                // send the answer back to the client
                response.build().writeDelimitedTo(out);
                out.flush();

                request = RPC_Request.parseDelimitedFrom(in);
            }

            socket.close();
            System.out.println("Client disconnected");
        }
    }
}