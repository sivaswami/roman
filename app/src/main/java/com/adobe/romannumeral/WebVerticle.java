package com.adobe.romannumeral;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.Optional;

public class WebVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> start) {
                configureRouter()
                .compose(this::startHttpServer);
    }
    /**
     * Configure different REST API mappings
     *
     * @return
     */
    private Future<Router> configureRouter() {
        System.out.println("Router");
        Router router = Router.router(vertx);
        router.route().handler(LoggerHandler.create());
        router.route().handler(CorsHandler.create("localhost"));
        router.get("/romannumeral").handler(this::romanServiceHandler);
        router.route().handler(StaticHandler.create("web").setIndexPage("index.html"));
        return Future.succeededFuture(router);
    }

    /**
     * Start HTTP Server in Non-blocking mode from loaded config
     *
     * @param router
     * @return
     */
    private Future<HttpServer> startHttpServer(Router router) {
        System.out.println("Server");
        final int httpPort = config().getInteger("http.port");
        System.out.println("Starting server in port : " + httpPort);
        HttpServer server = vertx.createHttpServer().requestHandler(router);
        return Future.future(promise -> server.listen(httpPort, promise));
    }

    public void romanServiceHandler(RoutingContext ctx) {
        List<String> queryInput = ctx.queryParam("query");
        List<String> queryMinInput = ctx.queryParam("min");
        List<String> queryMaxInput = ctx.queryParam("max");
        Optional<Integer> input = Optional.of(Integer.parseInt(queryInput.get(0)));
        Optional<Integer> minData = Optional.of(Integer.parseInt(queryMinInput.get(0)));
        Optional<Integer> maxData = Optional.of(Integer.parseInt(queryMaxInput.get(0)));
        JsonObject message = new JsonObject();
        input.ifPresent(integer -> message.put("input", integer));
        minData.ifPresent(integer -> message.put("min", minData));
        maxData.ifPresent(integer -> message.put("max", maxData));
        ctx.response().setChunked(true);
        ctx.response().putHeader("Content-Type", "application/json");
        vertx.eventBus().request("com.adobe.romanverticle", message, aresponse -> {
            if (aresponse.succeeded()) {
                System.out.println(aresponse.result().body());
                ctx.response().setStatusCode(200).end(Json.encodePrettily(aresponse.result().body()));
            } else {
                ctx.response().setStatusCode(400).end("ERROR: " + aresponse.cause());
            }
        });
    }

}
