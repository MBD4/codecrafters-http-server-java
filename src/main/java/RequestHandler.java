import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            String requestLine = reader.readLine();
            String requestType = requestLine.split(" ")[0];
            String slashParameter = requestLine.split(" ")[1];
            List<String> requestHeaders = new ArrayList<>();
            while (true) {
                String currentLine = reader.readLine();
                if (currentLine.isEmpty()) {
                    break;
                }
                requestHeaders.add(currentLine);
            }
            String requestBody = null;
            if (getRequestHeaderValue(requestHeaders,"Content-Length") != null) {
                int contentLength = Integer.parseInt(getRequestHeaderValue(requestHeaders,"Content-Length"));
                char[] buffer = new char[contentLength];
                int totalCharsRead = 0;
                while (true) {
                    int charsRead = reader.read(buffer, totalCharsRead, contentLength - totalCharsRead);
                    if (charsRead == -1) {
                        out.write("HTTP/1.1 500 Server error\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                    totalCharsRead += charsRead;
                    if (totalCharsRead == contentLength) {
                        break;
                    }
                }
                requestBody = new String(buffer);
            }
            if (requestType.equals("GET")) {
                out.write(getRequest(slashParameter, requestHeaders, requestBody).getBytes(StandardCharsets.UTF_8));
            }
            else if (requestType.equals("POST")) {
                out.write(postRequest(slashParameter, requestHeaders, requestBody).getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e) {
            System.err.println("Something went wrong in requestHandler:" + e.getMessage());
        }
    }

    private String postRequest(String slashParameter, List<String> requestHeaders, String requestBody) {
        if (slashParameter.startsWith("/files/")) {
            if (rootDirectory == null) {
                return "HTTP/1.1 400 Bad Request (add --directory root)\r\n\r\n";
            }
            try {
                String tmpRoot = slashParameter.substring(slashParameter.indexOf("/") + 1, slashParameter.indexOf("/", 1));
                writeFile(slashParameter.replaceFirst(tmpRoot, rootDirectory), requestBody);
                return "HTTP/1.1 201 Created\r\n\r\n";
            } catch (IOException e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
                return "HTTP/1.1 404 Not Found\r\n\r\n";
            }
        }
        else {
            return "HTTP/1.1 404 Not Found\r\n\r\n";
        }
    }

    private String getRequest(String slashParameter, List<String> requestHeaders, String requestBody) throws IOException {
        if (slashParameter.startsWith("/files/")) {
            if (rootDirectory == null) {
                return "HTTP/1.1 400 Bad Request (add --directory root)\r\n\r\n";
            }
            try {
                String tmpRoot = slashParameter.substring(slashParameter.indexOf("/") + 1, slashParameter.indexOf("/", 1));
                String fileContent = readFile(slashParameter.replaceFirst(tmpRoot, rootDirectory));
                return "HTTP/1.1 200 OK\r\n" + "Content-Type: application/octet-stream\r\n" + "Content-Length: "
                        + fileContent.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + fileContent;

            } catch (IOException e) {
                return "HTTP/1.1 404 Not Found\r\n\r\n";
            }
        }
        else if (slashParameter.equals("/")) {
            return "HTTP/1.1 200 OK\r\n\r\n";
        }
        else if (slashParameter.startsWith("/echo/")) {
            String stringToEcho = slashParameter.substring("/echo/".length());
            return "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: "
                    + stringToEcho.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + stringToEcho;
        }
        else if (slashParameter.startsWith("/user-agent")) {
            String userAgentValue = getRequestHeaderValue(requestHeaders, "User-Agent");
            if (userAgentValue == null) {
                return "HTTP/1.1 400 Bad Request\r\n\r\n";
            }
            return "HTTP/1.1 200 OK\r\n" + "Content-Type: text/plain\r\n" + "Content-Length: "
                    + userAgentValue.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + userAgentValue;
        }
        else {
            return "HTTP/1.1 404 Not Found\r\n\r\n";
        }
    }

    private String getRequestHeaderValue(List<String> headers, String headerWanted) throws IOException {
        String headerToReturn = null;
        for (String header: headers) {
            String headerKey = header.split(":", 2)[0];
            String headerValue = header.split(":", 2)[1].trim();
            if (headerKey.equals(headerWanted)) {
                headerToReturn = headerValue;
                break;
            }
        }
        return headerToReturn;
    }

    private String readFile(String file) throws IOException {
        Path path = Path.of(file);
        return Files.readString(path);
    }

    private void writeFile(String file, String content) throws IOException {
        Files.writeString(Path.of(file), content);
    }
}
