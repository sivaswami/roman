package com.adobe.romannumeral;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.logging.Logger;

public class WebVerticle extends AbstractVerticle {
    private static final Logger log = Logger.getLogger(WebVerticle.class.getName());

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
        log.info("Configuring routes");
        Router router = Router.router(vertx);
        final Handler<RoutingContext> loggingHandler = routingContext -> {
            final HttpServerRequest request = routingContext.request();
            routingContext.next();
        };
        router.route().handler(loggingHandler);
        //  router.route().handler(LoggerHandler.create());
//        router.route().handler(CorsHandler.create("localhost"));
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
        final int httpPort = config().getInteger("http.port");
        log.info("Starting server in port : " + httpPort);
        HttpServer server = vertx.createHttpServer().requestHandler(router);
        return Future.future(promise -> server.listen(httpPort, promise));
    }

    /**
     * This is the handler which processes any /romannumeral call and its parameters
     * @param ctx
     */

    public void romanServiceHandler(RoutingContext ctx) {
        log.info("Handling request from " + ctx.request().host());
        List<String> queryInput = ctx.queryParam("query");
        List<String> queryMinInput = ctx.queryParam("min");
        List<String> queryMaxInput = ctx.queryParam("max");
        int input, minData, maxData;
        JsonObject message = new JsonObject();
        /*
          A request can have any number of query parameter say /min=10&min=20
          We take the first parameter to consideration.
         */
        if (!queryInput.isEmpty()) {
            input = Integer.parseInt(queryInput.get(0));
            message.put("input", input);
        } else if (!queryMinInput.isEmpty() && !queryMaxInput.isEmpty()) {
            minData = Integer.parseInt(queryMinInput.get(0));
            maxData = Integer.parseInt(queryMaxInput.get(0));
            if (minData < 1 || maxData < 1 || minData > 3999 || maxData > 3999) {
                ctx.response().setStatusCode(400).end("Invalid value  min " + minData + " or max Data " + maxData);
                return;
            }
            message.put("min", minData);
            message.put("max", maxData);
        } else {
            ctx.response().setStatusCode(400).end("ERROR: Invalid arguments. Ensure min and max or query parameter is present");
            return;
        }
        ctx.response().setChunked(true);
        ctx.response().putHeader("Content-Type", "application/json");
        // Send the input arguments for next step of processing to convert to Roman String
        vertx.eventBus().request("com.adobe.romanverticle", message, aresponse ->
        {
            if (aresponse.succeeded()) {
                ctx.response().setStatusCode(200).end(Json.encodePrettily(aresponse.result().body()));
            } else {
                ctx.response().setStatusCode(400).end("ERROR: " + aresponse.cause());
            }
        });
    }

}
