import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    public static boolean handleRequest(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream out = socket.getOutputStream();
        String slashParameter = reader.readLine().split(" ")[1];
        if (slashParameter.equals("/")) {
            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        }
        else if (slashParameter.startsWith("/echo/")) {
            String stringToEcho = slashParameter.substring("/echo/".length());
            out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: "
                        + stringToEcho.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + stringToEcho).getBytes(StandardCharsets.UTF_8));
        }
        else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        }
        return true;
    }
}
