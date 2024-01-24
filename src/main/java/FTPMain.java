/**
 * FTP客户端的设计与实现
 * 2023-12-21
 * 作者：smileleooo
 */

import java.io.IOException;
import java.util.Scanner;

public class FTPMain {
    public static void main(String[] args) throws IOException {

        // 打印banner信息
        printFTPBanner();

        Scanner scanner = new Scanner(System.in);
        System.out.print("FTP服务器主机名(ip地址)：");
        String serverAddress = scanner.nextLine().trim();
        int serverPort = 21;

        // 实例化ftpClient对象
        FTPClient ftpClient = new FTPClient(serverAddress, serverPort);
        // 登录
        ftpClient.login(FTPClient.writer, FTPClient.reader, scanner);
        System.out.println("[STATUS] 登录成功，输入'help'获取更多信息！");

        // 循环执行命令直到退出
        while (true) {
            System.out.print("\nFTP> ");
            String command = scanner.nextLine().trim();

            if (command.equals("ls") || command.equals("dir")) {
                ftpClient.listFiles();

            } else if (command.matches("^put\\s+\\S+$")) {
                String localFile = command.substring(4);
                ftpClient.uploadFile(localFile);

            } else if (command.matches("^get\\s+\\S+$")) {
                String remoteFile = command.substring(4);
                ftpClient.downloadFile(remoteFile);

            } else if (command.matches("^cd\\s+\\S+$")) {
                String path = command.substring(3);
                ftpClient.changeDirectory(path);

            } else if (command.matches("^del\\s+\\S+$")) {
                String fileName = command.substring(4);
                ftpClient.removeFileOrDirectory(fileName);

            } else if (command.matches("^mkd\\s+\\S+$")) {
                String directoryName = command.substring(4);
                ftpClient.makeDirectory(directoryName);

            } else if (command.matches("^rem\\s+\\S+\\s+\\S+$")) {
                String[] parts = command.split(" ");
                String oldFile = parts[1];
                String newFile = parts[2];
                ftpClient.renameOrMoveFile(oldFile, newFile);

            } else if (command.equals("pwd")) {
                ftpClient.printWorkingDirectory();

            } else if (command.equals("help") || command.equals("info")) {
                ftpClient.helpInfo();

            } else if (command.equals("quit") || command.equals("exit")) {
                ftpClient.quit();
                break;
            } else {
                System.out.println("[STATUS] 错误的命令！");
            }
        }
    }

    // 初始banner
    public static void printFTPBanner() {
        String banner =
                "   _____   _____   ___               ______     \n"+
                "  |  ___| |_   _| |  _ \\    ____    / _____|    \n" +
                "  | |_      | |   | |_) |  |____|   | |        \n" +
                "  |  _|     | |   |  __/            | |____      \n" +
                "  |_|       |_|   |_|       v1.0     \\_____|    \n";

        System.out.println(banner);
    }
}
