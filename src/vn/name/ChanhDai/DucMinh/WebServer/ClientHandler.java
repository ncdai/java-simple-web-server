package vn.name.ChanhDai.DucMinh.WebServer;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

// Minh
// GET Method
// - getFileData()
// - getContentType()
// - fileNotFound()

// Dai
// POST Method
// - bodyParser()
// UI Design
// - HTML
// - CSS

public class ClientHandler extends Thread {
    Socket connect;

    public ClientHandler(Socket connect) {
        this.connect = connect;
    }

    @Override
    public void run() {
        BufferedReader headerReader = null;
        PrintWriter headerWriter = null;
        BufferedOutputStream dataWriter = null;
        String fileRequested = null;

        try {
            // Read HTTP Request Message
            // "Characters Input Stream"
            headerReader = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            // Send HTTP Response Message
            // "Character Output Stream" (for Headers)
            headerWriter = new PrintWriter(connect.getOutputStream());
            // "Binary Output Stream" (for Requested Data)
            dataWriter = new BufferedOutputStream(connect.getOutputStream());

            // Đọc thông tin Request Line
            String requestLine = headerReader.readLine();
            if (requestLine == null) {
                System.out.println("[" + connect.getPort() + "] Anonymous Access!");
                return;
            }

            System.out.println("[" + connect.getPort() + "] " + requestLine);

            // Tách dữ liệu từ chuỗi Request Line
            StringTokenizer parse = new StringTokenizer(requestLine);

            // HTTP Method
            String method = parse.nextToken().toUpperCase();

            // File Requested
            fileRequested = parse.nextToken().toLowerCase();

            // Xử lí GET Method
            if (method.equals("GET")) {
                // Nếu File Requested là / thì trả về file mặc định /index.html
                if (fileRequested.endsWith("/")) {
                    fileRequested += "index.html";
                }

                // Đọc nội dung File Requested
                byte[] fileData = getFileData(fileRequested);

                // Xác định độ dài nội dung
                int contentLength = fileData.length;

                // Xác định loại nội dung (text/plain, text/css, text/png, text/svg+xml,...)
                String contentType = getContentType(fileRequested);

                // Gửi HTTP Response Message
                headerWriter.println("HTTP/1.1 200 OK");
                headerWriter.println("Server: Java HTTP Server from Team 18120113-18120138");
                headerWriter.println("Date: " + new Date());
                headerWriter.println("Content-Type: " + contentType);
                headerWriter.println("Content-Length: " + contentLength);

                // In 1 dòng rỗng để phân biệt phần Headers và Data
                headerWriter.println();
                // Xoá bộ nhớ đệm và ghi hết byte stream ra ngoài
                headerWriter.flush();

                // Gửi HTTP Data (requested HTML File)
                dataWriter.write(fileData, 0, contentLength);
                // Xoá bộ nhớ đệm và ghi hết byte stream ra ngoài
                dataWriter.flush();

                System.out.println("[" + connect.getPort() + "] File Requested : " + fileRequested + " (" + contentType + ")");
            }

            // Xử lí Login (POST Method)
            if (method.equals("POST") && fileRequested.equals("/")) {
                // Đọc dữ liệu Header Lines
                String headerLine;
                while ((headerLine = headerReader.readLine()).length() != 0) {
                    System.out.println("[" + connect.getPort() + "] " + headerLine);
                }

                // Đọc dữ liệu gửi lên từ Login Form
                StringBuilder payload = new StringBuilder();
                while (headerReader.ready()) {
                    payload.append((char) headerReader.read());
                }

                String body = payload.toString();
                System.out.println("[" + connect.getPort() + "] Data : " + body);

                if (body.equals("")) {
                    headerWriter.println("HTTP/1.1 301 Moved Permanently");
                    headerWriter.println("Location: /404.html");
                    headerWriter.flush();
                    return;
                }

                // Lấy thông tin username/password từ chuỗi dữ liệu Client gửi lên
                Map<String, String> bodyParser = bodyParser(body);
                String username = bodyParser.get("username").toLowerCase();
                String password = bodyParser.get("password").toLowerCase();

                if (username.equals("admin") && password.equals("admin")) {
                    // Dùng HTTP Redirections để chuyển đến trang info.html nếu đúng username/password
                    headerWriter.println("HTTP/1.1 301 Moved Permanently");
                    headerWriter.println("Location: /info.html");
                    headerWriter.flush();
                } else {
                    // Dùng HTTP Redirections để chuyển đến trang 404.html nếu sai username/password
                    headerWriter.println("HTTP/1.1 301 Moved Permanently");
                    headerWriter.println("Location: /404.html");
                    headerWriter.flush();
                }
            }
        } catch (FileNotFoundException fileNotFoundException) {
            if (headerReader != null && dataWriter != null) {
                fileNotFound(fileRequested, headerWriter, dataWriter);
            }
        } catch (IOException ioException) {
            System.err.println("[" + connect.getPort() + "] Server Error!");
            ioException.printStackTrace();
        } finally {
            try {
                if (headerReader != null) headerReader.close();
                if (headerWriter != null) headerWriter.close();
                if (dataWriter != null) dataWriter.close();

                // Đóng kết nối Socket
                connect.close();

            } catch (IOException e) {
                System.err.println("[" + connect.getPort() + "] Error Closing Stream! " + e.getMessage());
            }

            System.out.println("[" + connect.getPort() + "] Connection Closed!\n");
        }
    }

    byte[] getFileData(String fileName) throws FileNotFoundException {
        byte[] fileData;

        try (InputStream inputStream = ClientHandler.class.getResourceAsStream("public" + fileName)) {
            if (inputStream == null) throw new FileNotFoundException();
            fileData = inputStream.readAllBytes();
        } catch (IOException ioException) {
            throw new FileNotFoundException();
        }

        return fileData;
    }

    String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return "text/html";
        }

        if (fileRequested.endsWith(".css")) {
            return "text/css";
        }

        if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (fileRequested.endsWith(".png")) {
            return "image/png";
        }

        if (fileRequested.endsWith(".svg")) {
            return "image/svg+xml";
        }

        return "text/plain";
    }

    void fileNotFound(String fileRequested, PrintWriter headerWriter, OutputStream dataWriter) {
        try {
            // Xác định độ dài nội dung file 404.html
            byte[] fileData = getFileData("/404.html");

            // Xác định độ dài nội dung file 404.html
            int contentLength = fileData.length;

            // Xác định loại nội dung file 404.html (text/html)
            String contentType = getContentType("/404.html");

            // Gửi HTTP Response Message
            headerWriter.println("HTTP/1.1 404 File Not Found");
            headerWriter.println("Server: Java HTTP Server from Team 18120113-18120138");
            headerWriter.println("Date: " + new Date());
            headerWriter.println("Content-Length: " + contentLength);
            headerWriter.println("Content-Type: " + contentType);

            // In 1 dòng rỗng để phân biệt phần Headers và Data
            headerWriter.println();
            headerWriter.flush();

            // Gửi HTTP Data (requested HTML File)
            dataWriter.write(fileData, 0, contentLength);
            dataWriter.flush();

            System.out.println("[" + connect.getPort() + "] File " + fileRequested + " Not Found!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<String, String> bodyParser(String body) {
        Map<String, String> params = new HashMap<>();

        StringTokenizer bodyTokenizer = new StringTokenizer(body, "&");

        System.out.println("bodyParser");
        while (bodyTokenizer.hasMoreTokens()) {
            StringTokenizer paramTokenizer = new StringTokenizer(bodyTokenizer.nextToken(), "=");

            String key = paramTokenizer.hasMoreTokens() ? paramTokenizer.nextToken() : "";
            String value = paramTokenizer.hasMoreTokens() ? paramTokenizer.nextToken() : "";

            System.out.println("--- " + key + " " + value);

            params.put(key, value);
        }

        return params;
    }
}