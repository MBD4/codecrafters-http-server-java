import java.util.List;

public class Request {

    private String slashParameter;
    private List<String> requestHeaders;
    private String requestBody;
    private String responseLine;
    private String responseHeaders;
    private String responseBody;

    public Request(String slashParameter, List<String> requestHeaders, String requestBody, String responseLine, String responseHeaders, String responseBody) {
        this.slashParameter = slashParameter;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.responseLine = responseLine;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public String getSlashParameter() {
        return slashParameter;
    }

    public void setSlashParameter(String slashParameter) {
        this.slashParameter = slashParameter;
    }

    public List<String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(List<String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseLine() {
        return responseLine;
    }

    public void setResponseLine(String responseLine) {
        this.responseLine = responseLine;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public void addResponseHeader(String header) {
        responseHeaders += header + "\r\n";
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
