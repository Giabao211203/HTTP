import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class HttpClient {
    private JFrame frame;
    private JTextField urlField;
    private JTextArea contentArea;
    private JTextArea postDataArea;

    public HttpClient() {
        frame = new JFrame("HTTP Client");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        urlField = new JTextField("http://localhost:8080/index.txt"); // Default URL
        JButton getButton = new JButton("GET");
        getButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchContent();
            }
        });

        JButton postButton = new JButton("POST");
        postButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendPostRequest();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(urlField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(getButton);
        buttonPanel.add(postButton);
        panel.add(buttonPanel, BorderLayout.EAST);

        contentArea = new JTextArea();
        contentArea.setEditable(false);

        postDataArea = new JTextArea(5, 20);
        postDataArea.setBorder(BorderFactory.createTitledBorder("POST Data"));

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        frame.add(new JScrollPane(postDataArea), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void fetchContent() {
        String urlString = urlField.getText();
        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();
                contentArea.setText(content.toString());
            } else if (responseCode == 404) {
                contentArea.setText("404 Not Found");
            } else {
                contentArea.setText("Error: " + responseCode);
            }
        } catch (URISyntaxException | MalformedURLException e) {
            contentArea.setText("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            contentArea.setText("IO Error: " + e.getMessage());
        }
    }

    private void sendPostRequest() {
        String urlString = urlField.getText();
        String postData = postDataArea.getText();
        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();
                contentArea.setText(content.toString());
            } else {
                contentArea.setText("Error: " + responseCode);
            }
        } catch (URISyntaxException | MalformedURLException e) {
            contentArea.setText("Invalid URL: " + e.getMessage());
        } catch (IOException e) {
            contentArea.setText("IO Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HttpClient::new);
    }
}
