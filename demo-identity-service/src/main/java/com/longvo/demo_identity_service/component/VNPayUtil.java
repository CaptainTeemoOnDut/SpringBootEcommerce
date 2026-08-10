package com.longvo.demo_identity_service.component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            mac.init(secretKey);

            byte[] hmacData = mac.doFinal(data.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hmacData) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC");
        }
    }

    public static String getRandomNumber(int length) {
        String chars = "0123456789";
        StringBuilder result = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(rand.nextInt(chars.length())));
        }

        return result.toString();
    }
}
