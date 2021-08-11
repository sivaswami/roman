package com.adobe.romannumeral;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest {
    Vertx vertx;
    int port = 8080;
    TestContext context;

    @Before
    public void setUp() throws IOException {
        this.context = context;
        vertx = Vertx.vertx();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", port);
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port)
                );
        //       Vertx vertx = Vertx.vertx();
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new MicrometerMetricsOptions()
                        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                        .setEnabled(true)));
        vertx.deployVerticle(new App(), options);
        //vertx.deployVerticle(WebVerticle.class.getName(), options);

    }

    @After
    public void tearDown() {
        RestAssured.reset();
        vertx.close(); //context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/")
                .then()
                .extract()
                .response();
        assertTrue(response.getBody().prettyPrint().contains("ROMAN"));
    }

    @Test
    public void testMetrics() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/metrics")
                .then()
                .extract()
                .response();
        assertTrue(response.getBody().prettyPrint().contains("vert"));
    }

    @Test
    public void checkRomanServiceQuery() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/romannumeral?query=25")
                .then()
                .extract()
                .response();
        System.out.println(response.getBody().prettyPrint());
        assertEquals(200, response.statusCode());
        assertEquals(response.jsonPath().getString("output"), "XXV");
    }


    @Test
    public void checkRomanServiceMinMax() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .when()
                .get("/romannumeral?min=10&max=12")
                .then()
                .extract()
                .response();
        assertEquals(200, response.statusCode());
        assertEquals(response.jsonPath().getString("conversations.output"), "[X, XI, XII]");
    }

}