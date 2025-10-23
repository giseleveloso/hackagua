package dev.tanz.esterbot.modules.ia.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.tanz.esterbot.EsterBOT;
import dev.tanz.esterbot.modules.ia.utils.GeminiBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static dev.tanz.esterbot.EsterBOT.log;

/**
 * Nova versão: em vez de enviar arquivos para a API,
 * lemos diretamente todos os .md da pasta "wiki" e injetamos o texto no JSON.
 */
public class ChatAPI {

    private final String apiKey = EsterBOT.dotenv.get("GEMINI_API_KEY");

    /** Conteúdos (strings) carregados dos .md da pasta wiki. */
    private final List<String> wikiContents = new ArrayList<>();

    public ChatAPI() {
        // Ao instanciar, carregamos os arquivos .md
        loadWikiFiles();
    }

    /**
     * Lê todos os arquivos .md de "wiki" (e subpastas, se quiser) e
     * guarda o texto de cada um em "wikiContents".
     */
    private void loadWikiFiles() {
        File wikiFolder = new File("wiki");
        if (!wikiFolder.exists() || !wikiFolder.isDirectory()) {
            log("[IA] Pasta wiki não existe ou não é um diretório. Nenhum arquivo será lido.");
            return;
        }

        List<File> mdFiles = new ArrayList<>();
        findMdFiles(wikiFolder, mdFiles);

        for (File mdFile : mdFiles) {
            String content = readFileContent(mdFile);
            if (content != null && !content.isEmpty()) {
                // Opcional: você pode adicionar "nome do arquivo" como título
                // ou simplesmente concatenar o texto
                String fullContent = "**Arquivo: " + mdFile.getName() + "**\n\n" + content;
                wikiContents.add(fullContent);
            }
        }
        log("[IA] Carregados " + wikiContents.size() + " arquivos .md da pasta wiki.");
    }

    /** Busca recursivamente arquivos .md dentro da pasta dada. */
    private void findMdFiles(File folder, List<File> mdFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findMdFiles(file, mdFiles);
            } else if (file.isFile() && file.getName().endsWith(".md")) {
                mdFiles.add(file);
            }
        }
    }

    /** Lê o conteúdo completo de um arquivo de texto (UTF-8). */
    private String readFileContent(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            log("[IA] Erro ao ler arquivo .md: " + file.getName() + " - " + e.getMessage());
        }
        return sb.toString().trim();
    }

    /**
     * Monta e envia a requisição final para gemini-2.0-flash:generateContent.
     * Agora, ao invés de "fileUri", passamos todo o texto dos .md direto no JSON.
     */
    public String sendPostRequest(String userInput) {
        String requestBody = GeminiBody.buildRequestBody(userInput, wikiContents);
        // ↑ Aqui você constrói o JSON com os .md embutidos em "wikiTexts" (ou como você tiver feito)
        String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        BufferedReader reader = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setDoOutput(true);

            // Envia o body
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = con.getResponseCode();
            if (status == 429) {
                return "429";
            } else if (status != 200) {
                // Lê resposta de erro
                reader = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                log("[IA] Erro HTTP " + status + ": " + errorResponse);
                return null;
            }

            // Resposta de sucesso
            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String jsonResponse = sb.toString().trim();

            // Faz o parse do JSON para pegar apenas o texto
            return extractTextFromResponse(jsonResponse);

        } catch (IOException e) {
            log("[IA] Error while post request: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException ignored) {}
            }
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * Extrai o texto principal da IA em:
     *   "candidates"[0].content.parts[0].text
     */
    private String extractTextFromResponse(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (jsonObject.has("candidates")) {
                var candidates = jsonObject.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            var parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject partObj = parts.get(0).getAsJsonObject();
                                if (partObj.has("text")) {
                                    return partObj.get("text").getAsString();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Caso não encontre nada
        return null;
    }


}
