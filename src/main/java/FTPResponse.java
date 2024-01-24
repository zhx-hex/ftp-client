
// FTP的响应状态码和响应主体
public class FTPResponse {
    private final String code;
    private final String body;

    public FTPResponse(String code, String body) {
        this.code = code;
        this.body = body;
    }

    public String getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }
}
