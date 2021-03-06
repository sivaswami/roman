package com.adobe.romannumeral;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is responsible for conversion of provided integer or min/max value to its Roman String
 */
public class RomanVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(RomanVerticle.class);
    private static final Map<Integer, String> romanCache = new LinkedHashMap<Integer, String>() {{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
    }};


    @Override
    public void start() {
        vertx.eventBus().consumer("com.adobe.romanverticle", msg -> {
            JsonObject inputMsg = (JsonObject) msg.body();
            Future<JsonObject> resultObj;
            Integer input = inputMsg.getInteger("input");
            Integer min = inputMsg.getInteger("min");
            Integer max = inputMsg.getInteger("max");
            logger.info(inputMsg);
            logger.info("INPUT = " + input);
            logger.info("MIN = " + min);
            logger.info("MAX = " + max);
            if (input != null) {
                resultObj = toRoman(input);
                if (resultObj.succeeded()) {
                    msg.reply(resultObj.result());
                } else {
                    msg.fail(400, resultObj.cause().getMessage());
                }
            } else if (min != null && max != null) {
                resultObj = toRoman(min, max);
                if (resultObj.succeeded()) {
                    msg.reply(resultObj.result());
                } else {
                    msg.fail(400, resultObj.cause().getMessage());
                }
            } else {
                msg.fail(400, "Invalid arguments");
            }
        });
    }

    public Future<JsonObject> toRoman(Integer input, Integer minRange, Integer maxRange) throws IllegalArgumentException {
        logger.info("toRoman:  input : " + input + " minRange : " + minRange + " maxRange: " + maxRange);
        StringBuilder romanString = new StringBuilder();
        JsonObject outputJson = new JsonObject();
        outputJson.put("input", input);

        if (input < minRange || input > maxRange) {
            return Future.failedFuture(String.format("Integer is out of range %d - %d", minRange, maxRange));
        }
        String isAvailableInCache = romanCache.get(input);
        if (null != isAvailableInCache) {
            outputJson.put("output", isAvailableInCache);
        } else { // compute the Roman
            for (Map.Entry<Integer, String> mapEntry : romanCache.entrySet()) {
                Integer keyVal = mapEntry.getKey();
                int temp = input / keyVal;
                input %= keyVal;
                romanString.append(String.join("", Collections.nCopies(temp, mapEntry.getValue())));
            }
            outputJson.put("output", romanString.toString());
        }
        return Future.succeededFuture(outputJson);

    }

    public Future<JsonObject> toRoman(Integer input) throws IllegalArgumentException {
        Integer maxRange = config().getInteger(App.ROMAN_MAX);
        Integer minRange = config().getInteger(App.ROMAN_MIN);
        if (minRange > maxRange) {
            return Future.failedFuture(String.format("Invalid configuration min (%d) > max (%d) range ", minRange, maxRange));
        }
        if (minRange < 1 || maxRange < 1) {
            return Future.failedFuture(String.format("Invalid configuration  Negative min (%d) > max (%d) range ", minRange, maxRange));
        }
        if (minRange > 3999 || maxRange > 3999) {
            return Future.failedFuture("Invalid configuration  Max range supported is 3999 ");
        }
        return toRoman(input, minRange, maxRange);
    }


    public Future<JsonObject> toRoman(Integer inputMinRange, Integer inputMaxRange) throws IllegalArgumentException {
        Integer maxRange = config().getInteger(App.ROMAN_MAX);
        Integer minRange = config().getInteger(App.ROMAN_MIN);
        logger.info("Input Min Range = " + inputMinRange + " Input Max Range = " + inputMaxRange + " max range = " + maxRange + " min range = " + minRange);
        if (minRange > maxRange) {
            return Future.failedFuture(String.format("Invalid configuration min (%d) > max (%d) range ", minRange, maxRange));
        }
        if (minRange < 1 || maxRange < 1) {
            return Future.failedFuture(String.format("Invalid configuration  Negative min (%d) > max (%d) range ", minRange, maxRange));
        }
        if (minRange > 3999 || maxRange > 3999) {
            return Future.failedFuture("Invalid configuration  Max range supported is 3999 ");
        }
        if (inputMinRange < minRange || inputMinRange > maxRange) {
            return Future.failedFuture("Invalid input range. should faill between min and max range supported.");
        }

        if (inputMaxRange < minRange || inputMaxRange > maxRange) {
            return Future.failedFuture("Invalid input max range. should faill between min and max range supported.");
        }
        if (inputMinRange > inputMaxRange) {
            return Future.failedFuture("Invalid input range. minimum range should be less than max range");
        }
        if (inputMinRange < minRange && inputMaxRange > maxRange) {
            return Future.failedFuture("Invalid input range. Input values should be between the min/max values");
        }

        JsonArray conversations = new JsonArray(
                IntStream.range(inputMinRange, inputMaxRange + 1)
                        .peek(inputData -> System.out.println(inputData + " " + minRange + ' ' + maxRange))
                        .mapToObj(inputData -> toRoman(inputData, minRange, maxRange))
                        .filter(Future::succeeded)
                        .map(Future::result)
                        .collect(Collectors.toList()));

        JsonObject outputObj = new JsonObject();
        outputObj.put("conversations", conversations);
        logger.info(conversations);
        return Future.succeededFuture(outputObj);
    }
}
