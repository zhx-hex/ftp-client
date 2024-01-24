import java.io.BufferedReader;
import java.io.IOException;

// FTP服务器响应解析器
public class FTPResponseParser {
    private final BufferedReader reader;

    public FTPResponseParser(BufferedReader reader) {
        this.reader = reader;
    }

    // 解析FTP的响应报文，返回一个FTPResponse的对象
    public FTPResponse parseResponse() throws IOException {
        String responseCode = null;
        StringBuilder responseBody = new StringBuilder();
        String line;

        // 分别取出响应的状态码（前3个字符）和响应主体
        while ((line = reader.readLine()) != null) {
            if (responseCode == null) {
                // 从当前行中提取前三个字符，即状态码
                responseCode = line.substring(0, 3);
                // 从当前行中提取除了前三个字符之外的部分，即响应正文
                responseBody.append(line.substring(4));
            } else {
                responseBody.append("\n").append(line);
            }

            // 如果当前行数据长度大于等于4，并且第四个字符不是连字符（-）则说明当前行是FTP服务器响应数据的最后一行
            if (line.length() >= 4 && line.charAt(3) != '-') {
                break;
            }
        }

        return new FTPResponse(responseCode, responseBody.toString());
    }
}
