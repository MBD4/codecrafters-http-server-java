import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class RequestHandler implements Runnable {

    private final Socket socket;
    private final String rootDirectory;

    public RequestHandler (Socket socket, String rootDirectory) {
        this.socket = socket;
        this.rootDirectory = rootDirectory;
    }

    @Override
    public void run() {
        try (socket) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();
            String slashParameter = reader.readLine().split(" ")[1];
            if (slashParameter.startsWith("/files/")) {
                if (rootDirectory == null) {
                    out.write("HTTP/1.1 400 Bad Request (add --directory root)\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    return;
                }
                try {
                    String tmpRoot = slashParameter.substring(slashParameter.indexOf("/") + 1, slashParameter.indexOf("/", 1));
                    String fileContent = readFile(slashParameter.replaceFirst(tmpRoot, rootDirectory));
                    out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: application/octet-stream\r\n" + "Content-Length: "
                            + fileContent.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + fileContent).getBytes(StandardCharsets.UTF_8));
                    return;

                } catch (IOException e) {
                    System.err.println(Arrays.toString(e.getStackTrace()));
                    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }
            else if (slashParameter.equals("/")) {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }
            else if (slashParameter.startsWith("/echo/")) {
                String stringToEcho = slashParameter.substring("/echo/".length());
                out.write(("HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: "
                        + stringToEcho.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + stringToEcho).getBytes(StandardCharsets.UTF_8));
                return;
            }
            else if (slashParameter.startsWith("/user-agent")) {
                boolean containsRequestBody = false;
                int contentLength = 0;
                String userAgentValue = null;
                while (true) {
                    String currentLine = reader.readLine();
                    if (currentLine.isEmpty()) {
                        break;
                    }
                    String headerKey = currentLine.split(":", 2)[0];
                    String headerValue = currentLine.split(":", 2)[1].trim();
                    if (headerKey.equals("Content-Length")) {
                        try {
                            contentLength = Integer.parseInt(headerValue);
                            containsRequestBody = true;
                        } catch (NumberFormatException e) {
                            out.write("HTTP/1.1 400 Bad Request 1\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                            return;
                        }
                    } else if (headerKey.equals("User-Agent")) {
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
                return;
            }
            else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }
        catch (Exception e) {
            System.err.println("Something went wrong in requestHandler:" + e.getMessage());
        }
    }

    private String readFile(String file) throws IOException {
        Path path = Path.of(file);
        return Files.readString(path);
    }
}
