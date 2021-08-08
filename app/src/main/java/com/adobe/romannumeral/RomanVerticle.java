package com.adobe.romannumeral;

import io.vertx.core.AbstractVerticle;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RomanVerticle extends AbstractVerticle {

    // TODO: CACHE THIS. map containing roman characters till 255.
    public static final Map<Integer, String> romanMapCache = new LinkedHashMap<Integer, String>() {{
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
            System.out.println("Message received: " + msg.body());
            msg.reply(toRoman((Integer) msg.body()));

        });
    }

    public static String toRoman(Integer input) throws IllegalArgumentException {
        StringBuilder romanString = new StringBuilder();
        if (input < 1 || input > 255) {
            return "ERROR: Integer is less than 1 or greater than 255";
        }
        String isAvailableInCache = romanMapCache.get(input);
        if (null != isAvailableInCache) {
            return isAvailableInCache;
        } else { // compute the Roman and store it in cache for future queries
            for (Map.Entry<Integer, String> mapEntry : romanMapCache.entrySet()) {
                Integer keyVal = mapEntry.getKey();
                int temp = input / keyVal;
                input %= keyVal;
                romanString.append(String.join("", Collections.nCopies(temp, mapEntry.getValue())));
            }
            //romanMapCache.putIfAbsent(input, romanString);
            return romanString.toString();
        }
    }

}
