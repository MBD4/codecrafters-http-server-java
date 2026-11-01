import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
      // You can use print statements as follows for debugging, they'll be visible when running tests.
      // System.out.println("Logs from your program will appear here!");
      // TODO: Uncomment the code below to pass the first stage

     try {
         ServerSocket serverSocket = new ServerSocket(4221);
         // Since the tester restarts your program quite often, setting SO_REUSEADDR
         // ensures that we don't run into 'Address already in use' errors
         serverSocket.setReuseAddress(true);

         // Wait for connection from client.
         Socket socket = serverSocket.accept();
//         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//         out.writeUTF("HTTP/1.1 200 OK\\r\\n\\r\\n");
         OutputStream out = socket.getOutputStream();
         out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
         System.out.println("accepted new connection");
     } catch (IOException e) {
         System.out.println("IOException: " + e.getMessage());
     }
  }
}
