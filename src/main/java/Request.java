import java.util.List;
import java.util.Map;

public class Request {

    private String slashParameter;
    private List<String> requestHeaders;
    private String requestBody;
    private String responseLine;
    private Map<String,String> responseHeaders;
    private byte[] responseBody;

    public Request(String slashParameter, List<String> requestHeaders, String requestBody, String responseLine, Map<String,String> responseHeaders, byte[] responseBody) {
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

    public Map<String,String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String,String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public void addResponseHeader(String key, String value) {
        responseHeaders.put(key, value);
    }

    public void changeResponseHeader(String headerKey, String newHeaderValue) {
        responseHeaders.replace(headerKey, newHeaderValue);
    }

    public String responseHeadersToString() {
        StringBuilder sb = new StringBuilder();
        responseHeaders.forEach((key, value) -> sb.append(key).append(": ").append(value).append("\r\n"));
        return sb.toString();
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }
}
