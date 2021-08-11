package com.adobe.romannumeral;


import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

public class RomanLauncher extends Launcher {

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        options.setMetricsOptions(new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true)
                        .setStartEmbeddedServer(true)
                        .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                        .setEmbeddedServerEndpoint("/metrics"))
                .setEnabled(true));
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        registry.config().meterFilter(
                new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                        return DistributionStatisticConfig.builder()
                                .percentilesHistogram(true)
                                .build()
                                .merge(config);
                    }
                });
    }
}