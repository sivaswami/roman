package com.adobe.romannumeral;

import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.logging.Logger;


public class App extends AbstractVerticle {
    private static final Logger log = Logger.getLogger(App.class.getName());

    private static void doConfig(Promise<Void> start) {
        ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");
        //   ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore).addStore(sysPropsStore);
        //   ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

    }


    @Override
    public void start(final Promise<Void> startedResult) {
        Router router = Router.router(vertx);
        // Add REST API mappings as routers
        router.route().handler(LoggerHandler.create());
        router.route().handler(StaticHandler.create("web").setIndexPage("index.html"));
        router.get("/").respond(ctx ->
                ctx.response().end("<h1>WELCOME TO ROMAN SERVICE</h1>")
        );
        router.get("/romannumeral").handler(this::romanServiceHandler);

        int httpPort;
        try {
            httpPort = Integer.parseInt(System.getProperty("http.port", "9090"));
        } catch (NumberFormatException nfe) {
            httpPort = 9090;
        }
        vertx.createHttpServer().requestHandler(router).listen(httpPort);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new App());
        DeploymentOptions dop = new DeploymentOptions()
                .setInstances(50);
        vertx.deployVerticle(RomanVerticle.class,dop);
    }


    public void romanServiceHandler(RoutingContext ctx) {
        MultiMap parameterMap = ctx.request().params();
        System.out.println(parameterMap);
        List<String> queryInput = ctx.queryParam("query");
        int input = Integer.parseInt(queryInput.get(0));
        ctx.response().setChunked(true);
        ctx.response().putHeader("Content-Type", "application/json");
        vertx.eventBus().request("com.adobe.romanverticle", input, aresponse -> {
            if (aresponse.succeeded()) {
                JsonObject outputjson = new JsonObject();
                outputjson.put("input", input);
                outputjson.put("output", aresponse.result().body());
                System.out.println(outputjson);
                ctx.response().setStatusCode(200).end(Json.encodePrettily(outputjson));
            } else {
                ctx.response().setStatusCode(400).end("ERROR: " + aresponse.cause());
            }
        });
    }
}
