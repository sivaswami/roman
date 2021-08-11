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
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * This is the main application or verticle code. This registers all asynchronous processing
 * code like HttpServer and Roman Converter. This class is also responsible for parsing configuration
 * inputs like Port numbers, min max ranges
 */
public class App extends AbstractVerticle {
    private static final Logger log = Logger.getLogger(App.class.getName());
    public static final String ROMAN_MIN = "roman.min";
    public static final String ROMAN_MAX = "roman.max";
    public static final String HTTP_PORT = "http.port";
    public static final String POD_NAME = "pod.name";
    final JsonObject loadedConfig = new JsonObject();
    public static final int ENV_HTTP_PORT = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "0"));
    public static final String ENV_POD_NAME = System.getenv().getOrDefault("POD_NAME", "unknown");

    /**
     * Read Configuration Parameters from the multiple locations like
     * Kubernetes configmaps, file and system environment variables
     */
    private Future<JsonObject> readConfig(Promise<Void> start) {
        log.info("Reading config");

        JsonObject configObject = new JsonObject()
                .put(HTTP_PORT, 8080)
                .put(ROMAN_MIN, 1)
                .put(POD_NAME, "unknown")
                .put(ROMAN_MAX, 255);
        ConfigStoreOptions defaultConfig = new ConfigStoreOptions().setType("json").setConfig(configObject).setOptional(true);
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(defaultConfig);
        if (Files.exists(Paths.get("config.json"))) {
            log.info("Loading configuration from config.json file");
            ConfigStoreOptions fileStore = new ConfigStoreOptions()
                    .setType("file").setFormat("json")
                    .setConfig(new JsonObject().put("path", "config.json"));
            options.addStore(fileStore);
        }

        ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, options);
        return Future.future(cfgRetriever::getConfig);
    }

    private Future<Void> storeConfig(JsonObject configData) {
        log.info("Storing config");
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
        log.info("Deploying verticles");
        DeploymentOptions dop = new DeploymentOptions().setConfig(loadedConfig);
        DeploymentOptions dopWorker = new DeploymentOptions().setConfig(loadedConfig).setInstances(50);
        Future<String> webv = Future.future(promise -> vertx.deployVerticle(WebVerticle.class, dop, promise));
        Future<String> romanv = Future.future(promise -> vertx.deployVerticle(RomanVerticle.class, dopWorker, promise));
        return CompositeFuture.all(webv, romanv).mapEmpty();
    }


    public static void main(String[] args) {
        //       Vertx vertx = Vertx.vertx();
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new MicrometerMetricsOptions()
                        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                        .setEnabled(true)));
        vertx.deployVerticle(new App());
    }

}
