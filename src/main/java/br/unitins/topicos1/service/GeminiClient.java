package br.unitins.topicos1.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class GeminiClient {

    private static final Logger LOG = Logger.getLogger(GeminiClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String generateJson(String apiKey, String systemPrompt, String userJson) throws IOException {
        String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                + apiKey;
        String requestBody = buildBody(systemPrompt, userJson);

        int attempts = 0;
        IOException last = null;
        while (attempts < 2) {
            attempts++;
            HttpURLConnection con = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int status = con.getResponseCode();
                if (status == 429) {
                    try {
                        Thread.sleep(800L * attempts);
                    } catch (InterruptedException ignored) {
                    }
                    continue;
                }
                if (status != 200) {
                    reader = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
                    StringBuilder err = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        err.append(line);
                    LOG.errorf("Gemini HTTP %d: %s", status, err.toString());
                    throw new IOException("Gemini HTTP " + status);
                }

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line).append('\n');
                String jsonResponse = sb.toString().trim();
                return extractPrettyJsonFromCandidates(jsonResponse);
            } catch (IOException e) {
                last = e;
            } finally {
                if (con != null)
                    con.disconnect();
            }
        }
        throw last != null ? last : new IOException("Falha ao chamar Gemini");
    }

    private String buildBody(String systemPrompt, String userJson) {
        String escapedSystem = systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        String escapedUser = userJson.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "{" +
                "\"contents\":[" +
                "{\"role\":\"user\",\"parts\":[{\"text\":\"SYSTEM:\\n" + escapedSystem + "\"}]}," +
                "{\"role\":\"user\",\"parts\":[{\"text\":\"" + escapedUser + "\"}]}]," +
                "\"generationConfig\":{\"temperature\":0.2,\"response_mime_type\":\"application/json\"}}";
    }

    private String extractPrettyJsonFromCandidates(String jsonResponse) {
        try {
            JsonNode root = MAPPER.readTree(jsonResponse);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.isNull()) {
                return jsonResponse;
            }
            String inner = textNode.asText();

            // Algumas respostas vêm com markdown code fences, remover se existir
            inner = inner.strip();
            if (inner.startsWith("```)")) {
                int first = inner.indexOf('\n');
                int last = inner.lastIndexOf("```\n");
                if (first >= 0 && last > first)
                    inner = inner.substring(first + 1, last);
            }

            JsonNode innerJson = MAPPER.readTree(inner);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(innerJson);
        } catch (Exception e) {
            return jsonResponse;
        }
    }
}
