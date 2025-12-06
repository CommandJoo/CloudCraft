package de.johannes.rest;

import de.johannes.cloud.FileManager;
import de.johannes.cloud.ServerManager;
import de.johannes.cloud.server.Server;
import de.johannes.commons.util.Json;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.util.List;
import java.util.stream.Collectors;

public class RestAPI {

    private final ServerManager manager;
    private final Javalin app;

    public RestAPI(ServerManager manager) {
        this.manager = manager;
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.util.log.stderr.DEBUG", "false");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        this.app = Javalin.create(config -> {
            config.jetty.modifyServer(server -> {
                server.setRequestLog(null);
            });
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
            config.showJavalinBanner = false;
        });
        app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*"); // or your React host
            ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        this.templatesGet();
        this.serversGet();
        this.serversPost();
        this.serverHeartbeat();

        app.start(2200);
    }

    public void templatesGet() {
        this.app.get("/templates", ctx -> {
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            ctx.result("{\"success\": true, \"templates\": "+Json.get(FileManager.listTemplates())+"}");
        });
    }

    public void serversGet() {
        this.serverList();
        this.serverInfo();
        this.serverLog();
    }

    public void serverList() {
        app.get("/servers", ctx -> {
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            ctx.result("{\"success\": true, \"servers\": "+Json.get(manager.servers().values().stream().map(Server::info).collect(Collectors.toList()))+"}");
        });
    }

    public void serverInfo() {
        app.get("/servers/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Server server = manager.get(id);
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            if(server == null) {
                success(ctx, false, "Server not found.");
            }else {
                ctx.result("{\"success\": true, \"server\": "+Json.get(server.info())+"}");
            }
        });
    }

    public void serverLog() {
        app.get("/servers/log/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Server server = manager.get(id);
            if(server == null) {
                ctx.status(HttpStatus.OK);
                ctx.contentType("application/json");
                success(ctx, false, "Server not found.");
            }else {
                String log = server.process().log();
                ctx.res().setContentType("text/plain");
                ctx.result(log);
            }
        });
    }

    public void serversPost() {
        this.serverStart();
        this.serverCommand();
        this.serverStop();
    }

    public void serverStart() {
        app.post("/servers/start", ctx -> {
            ServerStartBody body = Json.read(ctx.body(), ServerStartBody.class);
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            if(body.max_players < 0) {
                success(ctx, false, "Max players may not be less than 0.");
            }else if(!FileManager.hasTemplate(body.template)) {
                success(ctx, false, "Template not found.");
            }else {
                String server = manager.startServer(body.template, body.max_players);
                ctx.result("{\"success\": true, \"message\": \"Started server.\", \"server_id\": \""+server+"\"}");
            }
        });
    }

    public void serverCommand() {
        app.post("/servers/command/{id}", ctx -> {
            Server server = manager.get(ctx.pathParam("id"));
            ServerCommandBody body = Json.read(ctx.body(), ServerCommandBody.class);
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            if(server == null) {
                success(ctx, false, "Server not found.");
            } else if(body.command.trim().isEmpty()) {
                success(ctx, false, "Command may not be empty.");
            }else {
                boolean b = server.process().message(body.command);
                if(!b) {
                    success(ctx, false, "Error sending command!");
                }else {
                    ctx.result("{\"success\": true, \"message\": \"Succesfully sent command.\"}");
                }
            }
        });
    }

    public void serverStop() {
        app.post("/servers/stop/{id}", ctx -> {
            String id = ctx.pathParam("id");
            Server server = manager.get(id);
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            if(server == null) {
                success(ctx, false, "Server not found.");
            }else {
                server.status(CloudCalls.ServerStatus.STOPPING);
                server.shouldStop(true);
                ctx.result("{\"success\": true, \"message\": \"Stopped server.\", \"server_id\": \""+server.id()+"\"}");
            }
        });
    }

    public void serverHeartbeat() {
        app.post("/servers/heartbeat/{id}", ctx -> {
            Server server = manager.get(ctx.pathParam("id"));
            ServerHeartbeatBody body = Json.read(ctx.body(), ServerHeartbeatBody.class);
            ctx.status(HttpStatus.OK);
            ctx.contentType("application/json");
            if(server == null) {
                ctx.result("{\"success\": false, \"message\": \"Server not found.\"}");
            }else if(body.online_players == null) {
                ctx.result("{\"success\": false, \"message\": \"Invalid player count.\"}");
            }else {
                if(!server.shouldStop()) {
                    server.onlinePlayers(body.online_players);
                    server.status(CloudCalls.ServerStatus.ONLINE);
                }
                ctx.result("{\"success\": true, \"message\": \"Successfully sent heartbeat.\", \"should_stop\": "+server.shouldStop()+"}");
            }
        });
    }

    public void success(Context ctx, boolean success, String message) {
        ctx.result("{\"success\": "+success+", \"message\": \""+message+"\"}");
    }







    public void kill() {
        this.app.stop();
    }

    static class ServerStartBody {
        public int max_players;
        public String template;
    }

    static class ServerCommandBody {
        public String command;
    }

    static class ServerHeartbeatBody {
        public List<String> online_players;
        public String status;
    }

}
