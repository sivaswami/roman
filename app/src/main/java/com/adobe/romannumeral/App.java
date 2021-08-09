package com.adobe.romannumeral;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class App extends AbstractVerticle {
    private static final Logger log = Logger.getLogger(App.class.getName());
    final JsonObject loadedConfig = new JsonObject();

    /**
     * Read Configuration Parameters from the multiple locations like
     * Kubernetes configmaps, file and system environment variables
     */
    private Future<JsonObject> readConfig(Promise<Void> start) {
        JsonObject configObject = new JsonObject()
                .put("http.port", 8080)
                .put("roman.min", 1)
                .put("roman.max", 255);
        ConfigStoreOptions defaultConfig = new ConfigStoreOptions().setType("json").setConfig(configObject).setOptional(true);
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(defaultConfig);
        if (Files.exists(Paths.get("config.json"))) {
            System.out.println("Loading configuration from config.json file");
            ConfigStoreOptions fileStore = new ConfigStoreOptions()
                    .setType("file").setFormat("json")
                    .setConfig(new JsonObject().put("path", "config.json"));
            options.addStore(fileStore);
            System.out.println("config loaded");
        }

        ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, options);
        return Future.future(cfgRetriever::getConfig);
    }

    private Future<Void> storeConfig(JsonObject configData) {
        System.out.println(configData);
        loadedConfig.mergeIn(configData);
        return Future.succeededFuture();
    }

    @Override
    public void start(final Promise<Void> startedResult) {
        readConfig(startedResult)
                .compose(this::storeConfig)
                .compose(this::deployOtherVerticles);

    }

    private Future<Void> deployOtherVerticles(Void unused) {
        DeploymentOptions dop = new DeploymentOptions().setConfig(loadedConfig);
        DeploymentOptions dopWorker = new DeploymentOptions().setConfig(loadedConfig).setInstances(50);
        Future<String> webv = Future.future(promise -> vertx.deployVerticle(WebVerticle.class, dop, promise));
        Future<String> romanv = Future.future(promise -> vertx.deployVerticle(RomanVerticle.class, dopWorker, promise));
        return CompositeFuture.all(webv, romanv).mapEmpty();
    }


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new App());
    }

}
