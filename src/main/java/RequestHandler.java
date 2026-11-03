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

            // get request headers
            List<String> requestHeaders = new ArrayList<>();
            while (true) {
                String currentLine = reader.readLine();
                if (currentLine.isEmpty()) {
                    break;
                }
                requestHeaders.add(currentLine);
            }

            // get the request body
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

            String responseLine = "";
            String responseHeaders = "";
            String responseBody = "";

            // get compression type. if = null, no compression type is used
            String compressionType = getRequestHeaderValue(requestHeaders, "Accept-Encoding");
            if(!(compressionType == null) && compressionType.equals("gzip")) {
                responseHeaders += "Content-Encoding: gzip\r\n";
            }

            Request request = new Request(slashParameter, requestHeaders, requestBody, responseLine, responseHeaders, responseBody);
            if (requestType.equals("GET")) {
                boolean succes = getRequest(request);
                out.write((request.getResponseLine() + "\r\n" + request.getResponseHeaders() + "\r\n" + request.getResponseBody()).getBytes(StandardCharsets.UTF_8));
            }
            else if (requestType.equals("POST")) {
                boolean succes = postRequest(request);
                out.write((request.getResponseLine() + "\r\n" + request.getResponseHeaders() + "\r\n" + request.getResponseBody()).getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (Exception e) {
            System.err.println("Something went wrong in requestHandler:" + e.getMessage());
        }
    }

    private boolean postRequest(Request request) {
        if (request.getSlashParameter().startsWith("/files/")) {
            if (rootDirectory == null) {
                request.setResponseLine("HTTP/1.1 400 Bad Request (add --directory root)");
                return false;
            }
            try {
                String tmpRoot = request.getSlashParameter().substring(request.getSlashParameter().indexOf("/") + 1, request.getSlashParameter().indexOf("/", 1));
                writeFile(request.getSlashParameter().replaceFirst(tmpRoot, rootDirectory), request.getRequestBody());
                request.setResponseLine("HTTP/1.1 201 Created");
                return true;
            } catch (IOException e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
                request.setResponseLine("HTTP/1.1 404 Not Found");
                return false;
            }
        }
        else {
            request.setResponseLine("HTTP/1.1 404 Not Found");
            return false;
        }
    }

    private boolean getRequest(Request request) throws IOException {
        if (request.getSlashParameter().startsWith("/files/")) {
            if (rootDirectory == null) {
                request.setResponseLine("HTTP/1.1 400 Bad Request (add --directory root)");
                return false;
            }
            try {
                String tmpRoot = request.getSlashParameter().substring(request.getSlashParameter().indexOf("/") + 1, request.getSlashParameter().indexOf("/", 1));
                String fileContent = readFile(request.getSlashParameter().replaceFirst(tmpRoot, rootDirectory));
                request.setResponseLine("HTTP/1.1 200 OK");
                request.addResponseHeader("Content-Type: application/octet-stream");
                request.addResponseHeader("Content-Length: " + fileContent.getBytes(StandardCharsets.UTF_8).length);
                request.setResponseBody(fileContent);
                return true;

            } catch (IOException e) {
                request.setResponseLine("HTTP/1.1 404 Not Found");
                return false;
            }
        }
        else if (request.getSlashParameter().equals("/")) {
            request.setResponseLine("HTTP/1.1 200 OK");
            return true;
        }
        else if (request.getSlashParameter().startsWith("/echo/")) {
            String stringToEcho = request.getSlashParameter().substring("/echo/".length());
            request.setResponseLine("HTTP/1.1 200 OK");
            request.addResponseHeader("Content-Type: text/plain");
            request.addResponseHeader("Content-Length: " + stringToEcho.getBytes(StandardCharsets.UTF_8).length);
            request.setResponseBody(stringToEcho);
            return true;
        }
        else if (request.getSlashParameter().startsWith("/user-agent")) {
            String userAgentValue = getRequestHeaderValue(request.getRequestHeaders(), "User-Agent");
            if (userAgentValue == null) {
                request.setResponseLine("HTTP/1.1 400 Bad Request");
                return false;
            }
            request.setResponseLine("HTTP/1.1 200 OK");
            request.addResponseHeader("Content-Type: text/plain");
            request.addResponseHeader("Content-Length: " + userAgentValue.getBytes(StandardCharsets.UTF_8).length);
            request.setResponseBody(userAgentValue);
            return true;
        }
        else {
            request.setResponseLine("HTTP/1.1 404 Not Found");
            return false;
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
