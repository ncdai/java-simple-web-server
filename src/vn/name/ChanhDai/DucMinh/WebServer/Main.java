package vn.name.ChanhDai.DucMinh.WebServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        try {
            // Cấu hình PORT
            int PORT = 80;

            // Tạo Socket để lắng nghe kết nối và đăng ký Port cho Socket
            ServerSocket serverConnect = new ServerSocket(PORT);
            System.out.println("Server is running on PORT " + PORT);

            while (true) {
                // Khi có Client yêu cầu kết nối :
                // 1. Chấp nhận kết nối từ Client
                // 2. Khởi tạo Thread để quản lý kết nối từ Client
                // Tạo Thread để cho phép nhiều Client kết nối đến Web Server cùng lúc

                Socket connect = serverConnect.accept();
                new ClientHandler(connect).start();

                System.out.println("[" + connect.getPort() + "] Connection Opened At " + new Date());
            }
        } catch (IOException e) {
            System.err.println("Server Connection Error : " + e.getMessage());
        }
    }
}
