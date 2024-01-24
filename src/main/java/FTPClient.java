import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class FTPClient {
    private static Socket socket;
    static BufferedReader reader;
    static BufferedWriter writer;
    private static FTPResponseParser responseParser;

    // 初始化FTPClient对象，建立socket连接
    public FTPClient(String serverAddress, int serverPort) {
        try {
            System.out.println("[STATUS] 建立连接... ");
            socket = new Socket(serverAddress, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (socket != null) {
                System.out.println("[STATUS] 建立连接成功：" + socket.getLocalSocketAddress() + " ---> " + socket.getRemoteSocketAddress());
                responseParser = new FTPResponseParser(reader);
                FTPResponse response = responseParser.parseResponse();
                System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            }

        } catch (IOException e) {
            System.out.println("[STATUS] 建立连接异常 => " + e.getMessage());
            exit(123);
        }
    }

    // 向FTP服务端发送命令
    public void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

     // FTP用户登录
    public void login(BufferedWriter writer, BufferedReader reader, Scanner scanner) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            System.out.print("\n用户名：");
            String username = scanner.nextLine().trim();
            sendCommand(writer, "USER " + username);
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            if (Objects.equals(response.getCode(), "331")) {
                System.out.print("密码：");
                String password = scanner.nextLine().trim();
                sendCommand(writer, "PASS " + password);
                response = responseParser.parseResponse();
                System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
                if (!(Objects.equals(response.getCode(), "230"))) {
                    System.out.println("[STATUS] 登录失败！");
                    exit(124);
                }
            } else {
                System.out.println("[STATUS] 用户名错误！");
                exit(125);
            }

        } catch (IOException e) {
            System.out.println("[STATUS] 异常 => " + e.getMessage());
            exit(131);
        }
    }

    // 列出当前目录的文件和子目录
    public void listFiles() {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            // 进入被动模式
            sendCommand(writer,"PASV");
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 解析被动模式下服务器返回的IP地址和端口
            String numbersString = response.getBody().replaceAll("[^\\d,]", "");
            String[] parts = numbersString.split(",");
            String serverIp = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
            int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            // System.out.println(serverIp + ":" + port);

            // 建立数据连接
            Socket dataSocket = new Socket(serverIp, port);
            BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

            // 获取目录列表
            sendCommand(writer,"LIST");
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            String line;
            while ((line = dataReader.readLine()) != null) {
                System.out.println(line);
            }
            dataReader.close();

            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 上传文件到服务器
    public void uploadFile(String localFile) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            // 校验本地文件
            File file = new File(localFile);
            if (!file.exists()) {
                System.out.println("[STATUS] 该文件不存在！");
                return;
            }
            BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));

            // 设置传输类型为二进制
            sendCommand(writer, "TYPE I");
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 进入被动模式
            sendCommand(writer, "PASV");
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 解析被动模式下服务器返回的IP地址和端口
            String numbersString = response.getBody().replaceAll("[^\\d,]", "");
            String[] parts = numbersString.split(",");
            String serverIp = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
            int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            // System.out.println(serverIp + ":" + port);

            // 建立数据连接
            Socket dataSocket = new Socket(serverIp, port);
            BufferedOutputStream outputStream = new BufferedOutputStream(dataSocket.getOutputStream());

            // 上传文件
            sendCommand(writer, "STOR " + file.getName());
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

             // 将缓冲区中的数据写入到输出流中
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            System.out.println("[STATUS] 文件上传成功！");

            } catch (IOException e) {
                System.out.println("[STATUS]  文件上传异常 => " + e.getMessage());
                exit(128);
        }
    }

    // 将服务器上的文件下载到本地
    public void downloadFile(String remoteFile) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            // 设置传输类型为二进制
            sendCommand(writer, "TYPE I");
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 进入被动模式
            sendCommand(writer, "PASV");
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 解析被动模式下服务器返回的IP地址和端口
            String numbersString = response.getBody().replaceAll("[^\\d,]", "");
            String[] parts = numbersString.split(",");
            String serverIp = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
            int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
            // System.out.println(serverIp + ":" + port);

            // 建立数据连接
            Socket dataSocket = new Socket(serverIp, port);
            BufferedInputStream inputStream = new BufferedInputStream(new DataInputStream(dataSocket.getInputStream()));

            //下载文件
            File file = new File(remoteFile);
            //System.out.println(file.getName());
            sendCommand(writer,"RETR " + remoteFile);
            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());

            // 校验远程文件
            if (Objects.equals(response.getCode(), "550")) {
                System.out.println("[STATUS] 文件下载失败！");
                return;
            }

            // 将缓冲区中的数据写入到输出流
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            response = responseParser.parseResponse();
            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            System.out.println("[STATUS] 文件下载成功！");

        } catch (IOException e) {
            System.out.println("[STATUS]  文件下载异常 => " + e.getMessage());
            exit(127);
        }
    }

    // 更改当前工作目录
    public void changeDirectory(String directory) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            sendCommand(writer,"CWD " + directory);
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            if (!Objects.equals(response.getCode(), "250")) {
                System.out.println("[STATUS] 更改目录失败！");
                return;
            }

            sendCommand(writer,"PWD ");
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            int startIndex = response.getBody().indexOf('"');
            int endIndex = response.getBody().lastIndexOf('"');
            String path = response.getBody().substring(startIndex + 1, endIndex);
            System.out.println("[STATUS] 更改目录成功，当前目录：" +  path);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     // 删除服务器上的文件或目录
    public void removeFileOrDirectory(String fileName) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            sendCommand(writer,"DELE " + fileName);
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            if (!Objects.equals(response.getCode(), "250")) {
                sendCommand(writer,"RMD " + fileName);
                response = responseParser.parseResponse();
                if (!Objects.equals(response.getCode(), "250")) {
                    System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
                    System.out.println("[STATUS] 删除失败！");
                    return;
                }
            }

            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            System.out.println("[STATUS] 删除成功！");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 重命名或移动文件
    public void renameOrMoveFile(String oldFileName, String newFileName) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            sendCommand(writer,"RNFR " + oldFileName);
            response = responseParser.parseResponse();
            if (!Objects.equals(response.getCode(), "350")) {
                System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
                System.out.println("[STATUS] 该文件不存在！");
                return;
            }

            sendCommand(writer,"RNTO " + newFileName);
            response = responseParser.parseResponse();
            if (!Objects.equals(response.getCode(), "250")) {

                System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
                System.out.println("[STATUS] 重命名失败！");
                return;
            }

            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            if (newFileName.contains("/")) {
                System.out.println("[STATUS] 移动文件成功！");
            } else {
                System.out.println("[STATUS] 重命名成功！");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建新的目录
    public void makeDirectory(String directoryName) {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            sendCommand(writer,"MKD " + directoryName);
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            if (!Objects.equals(response.getCode(), "257")) {
                System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
                System.out.println("[STATUS] 目录创建失败！");
                return;
            }

            System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            System.out.println("[STATUS] 目录创建成功！");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 打印当前工作目录的路径
    public void printWorkingDirectory() {
        try {
            responseParser = new FTPResponseParser(reader);
            FTPResponse response;

            sendCommand(writer,"PWD");
            response = responseParser.parseResponse();
            // System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(response.getBody());
            if (matcher.find()) {
                String extractedContent = matcher.group(1);
                System.out.println("[STATUS] 当前目录：" + extractedContent);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 帮助信息
    public void helpInfo() {
        String info =
            "               命令 [参数]   命令解释    \n"+
            " ------------------------------------------------------\n"+
            "                 help/info   获取命令帮助信息 \n"+
            "                    ls/dir   列出当前目录的文件和子目录 \n"+
            "                       pwd   打印当前工作目录的路径\n"+
            "            cd [directory]   更改当前工作目录 \n"+
            "          get [remotefile]   从服务器下载文件到本地 \n"+
            "           put [localfile]   将本地文件上传到服务器 \n"+
            "      del [file/directory]   删除服务器上的文件或目录 \n"+
            "           mkd [directory]   创建新的目录 \n"+
            "   rem [oldfile] [newfile]   重命名或移动文件 \n"+
            "                 quit/exit   关闭与FTP服务器的连接 \n"+
            " ------------------------------------------------------";
        System.out.println(info);
    }

    // 关闭与FTP服务器的连接
    public void quit() throws IOException {
        responseParser = new FTPResponseParser(reader);
        FTPResponse response;
        sendCommand(writer,"QUIT");
        response = responseParser.parseResponse();
        System.out.println("[INFO] " + response.getCode() + " " + response.getBody());
        try {
            socket.close();
            exit(123);
        } catch (IOException e) {
            System.out.println("[STATUS] 关闭连接异常 => " + e.getMessage());
        }
    }
}
