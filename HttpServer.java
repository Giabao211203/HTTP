import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class HttpServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is listening on port 8080");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                handleClient(socket);
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }

        private void handleClient(Socket socket) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] tokens = requestLine.split(" ");
                String method = tokens[0];
                String path = tokens[1];

                if (method.equals("GET")) {
                    handleGetRequest(path, out, socket.getOutputStream());
                } else if (method.equals("POST")) {
                    handlePostRequest(path, in, out);
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("Unsupported request");
                    out.flush();
                }
            }

            socket.close();
        }

        private void handleGetRequest(String path, PrintWriter out, OutputStream outputStream) throws IOException {
            if (path.startsWith("/")) {
                path = path.substring(1); // Remove leading slash
            }

            if (path.contains(",")) {
                // Handle multiple file download
                String[] files = path.split(",");
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                for (String file : files) {
                    File fileObj = new File("www", file);
                    if (fileObj.exists() && !fileObj.isDirectory()) {
                        byte[] fileBytes = Files.readAllBytes(fileObj.toPath());
                        byteStream.write(fileBytes);
                        byteStream.write("\n".getBytes());
                    } else {
                        byteStream.write(("404 Not Found: " + file + "\n").getBytes());
                    }
                }
                byte[] responseBytes = byteStream.toByteArray();
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Content-Length: " + responseBytes.length);
                out.println();
                out.flush();
                outputStream.write(responseBytes);
                outputStream.flush();
            } else {
                // Handle single file download
                File file = new File("www", path);
                if (file.exists() && !file.isDirectory()) {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println("Content-Length: " + fileBytes.length);
                    out.println();
                    out.flush();
                    outputStream.write(fileBytes);
                    outputStream.flush();
                } else {
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("404 Not Found");
                    out.flush();
                }
            }
        }

        private void handlePostRequest(String path, BufferedReader in, PrintWriter out) throws IOException {
            // Skip headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                // Read until the empty line which indicates the end of the headers
            }
        
            // Read the actual data sent in the POST request
            StringBuilder payload = new StringBuilder();
            while (in.ready()) {
                payload.append((char) in.read());
            }
        
            String body = payload.toString();
            if (path.startsWith("/")) {
                path = path.substring(1); // Remove leading slash
            }
        
            File file = new File("www", path);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(body);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("File saved successfully");
            } catch (IOException e) {
                out.println("HTTP/1.1 500 Internal Server Error");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Error saving file");
            }
        
            out.flush();
        }
        
    }
}
