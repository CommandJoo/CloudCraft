package de.johannes.rest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.johannes.commons.util.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloudCalls {

    public static List<ServerInfo> servers() {
        try {
            String response = get("http://localhost:2200/servers");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                } else if (!obj.has("servers")) {
                    throw new IllegalStateException("No servers were found.");
                } else {
                    ServerInfo[] infos = convert(obj.get("servers").getAsJsonArray(), ServerInfo[].class);
                    return Arrays.asList(infos);
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static ServerInfo server(String id) {
        try {
            String response = get("http://localhost:2200/servers/" + URLEncoder.encode(id, StandardCharsets.UTF_8));
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                } else if (!obj.has("server")) {
                    throw new IllegalStateException("Server not found.");
                } else {
                    return convert(obj.get("server").getAsJsonObject(), ServerInfo.class);
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String startServer(String template, int maxPlayers) {
        try {
            String response = post("http://localhost:2200/servers/start", "{\"template\": \"" + template + "\", \"max_players\": " + maxPlayers + "}");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean() || !obj.has("server_id")) {
                    throw new RuntimeException("Something went wrong.");
                } else {
                    return obj.get("server_id").getAsString();
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static boolean sendCommand(String id, String command) {
        try {
            String response = post("http://localhost:2200/servers/command/"+URLEncoder.encode(id, StandardCharsets.UTF_8), "{\"command\": \"" + command + "\"}");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                } else {
                    return true;
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean stopServer(String id) {
        try {
            String response = post("http://localhost:2200/servers/stop/"+URLEncoder.encode(id, StandardCharsets.UTF_8), "");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                } else {
                    return true;
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static List<String> templates() {
        try {
            String response = get("http://localhost:2200/servers/templates");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                } else if(!obj.has("templates")){
                    throw new RuntimeException("No templates found.");
                }else {
                    String[] s = convert(obj.get("templates").getAsJsonArray(), String[].class);
                    return Arrays.asList(s);
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean heartbeat(ServerHeartbeat heartbeat) {
        try {
            String response = post("http://localhost:2200/servers/heartbeat/"+URLEncoder.encode(heartbeat.id, StandardCharsets.UTF_8), "{\"online_players\": "+ Json.get(heartbeat.online_players)+"}");
            JsonObject obj = json(response);
            if (obj.has("success")) {
                if (!obj.get("success").getAsBoolean()) {
                    throw new RuntimeException("Something went wrong.");
                }else if(!obj.has("should_stop")){
                    throw new RuntimeException("Something went wrong.");
                }else {
                    return obj.get("should_stop").getAsBoolean();
                }
            } else {
                throw new IllegalStateException("Invalid Response...");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }








    public record ServerInfo(String id, int max_players, List<String> online_players, int port, ServerStatus status) {
    }
    public record ServerHeartbeat(String id, List<String> online_players) {
    }

    public enum ServerStatus {
        STARTING, ONLINE, STOPPING, ERROR;

        public ServerStatus valueOfOr(String name, ServerStatus fallback) {
            try {
                return valueOf(name);
            } catch (Exception ex) {
                return fallback;
            }
        }
    }

    private static JsonObject json(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static <T> T convert(JsonElement obj, Class<T> cls) {
        return new GsonBuilder().create().fromJson(obj, cls);
    }

    private static String post(String location, String content) throws IOException, URISyntaxException {
        URL url = new URI(location).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true); // allows writing to body
        conn.setRequestProperty("Content-Type", "application/json"); // or whatever you send

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();

        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String get(String location) throws IOException, URISyntaxException {
        URL url = new URI(location).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();

        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

}
