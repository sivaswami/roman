package com.adobe.romannumeral;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class AppTest {

    @Test
    public void testJson() {
        JsonObject configObject = new JsonObject()
                .put("http", new JsonObject()
                        .put("hostname", "localhost")
                        .put("port", 8080))
                .put("roman", new JsonObject().put("range", 255));
        System.out.println(configObject.encodePrettily());
        System.out.println(configObject.getJsonObject("http").getInteger("port"));

    }

    @Test
    public void testRoman() {
        // System.out.println(App.toRoman(254));
    }
   /* @Test
    public void testRomanZero() {
        System.out.println(App.toRoman(0));
    }
    @Test
    public void testRomanNegative() {
        System.out.println(App.toRoman(-20));
    }
*/
}