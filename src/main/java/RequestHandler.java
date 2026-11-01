import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler {

    public static void handleRequest(Socket socket) throws IOException {
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
        else if(slashParameter.startsWith("/user-agent")) {
            boolean containsRequestBody = false;
            int contentLength = 0;
            String userAgentValue = null;
            while (true) {
                String currentLine = reader.readLine();
                if(currentLine.isEmpty()) {
                    break;
                }
                String headerKey = currentLine.split(":", 2)[0];
                String headerValue = currentLine.split(":", 2)[1].trim();
                if (headerKey.equals("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(headerValue);
                        containsRequestBody = true;
                    }
                    catch (NumberFormatException e) {
                        out.write("HTTP/1.1 400 Bad Request 1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                }
                else if (headerKey.equals("User-Agent")) {
                    userAgentValue = headerValue;
                }
            }
            if (containsRequestBody) {
                // do nothing for now, but I think this will be useful later.
            }
            if (userAgentValue == null) {
                out.write("HTTP/1.1 400 Bad Request 2\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }
            out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: "
                    + userAgentValue.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + userAgentValue).getBytes(StandardCharsets.UTF_8));
        }
        else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        }
        return;
    }
}
