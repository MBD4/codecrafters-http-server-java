import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        String rootDirectory = null;
        if (args.length > 1) {
            if (args[0].equals("--directory")) {
                rootDirectory = args[1];
            }
        }
        try {
            ServerSocket serverSocket = new ServerSocket(4221);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("accepted new connection");
                RequestHandler requestHandler = new RequestHandler(socket, rootDirectory);
                Thread thread = new Thread(requestHandler);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
