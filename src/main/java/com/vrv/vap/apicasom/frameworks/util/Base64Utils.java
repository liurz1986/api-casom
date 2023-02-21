package com.vrv.vap.apicasom.frameworks.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author: 梁国露
 * @since: 2023/2/17 14:49
 * @description:
 */
public class Base64Utils {

    public static String encodeBase64(String message){
        return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeBase64(String encode){
        String message = new String(Base64.getDecoder().decode(encode), StandardCharsets.UTF_8);
        return message;
    }

    public static void main(String[] args) {
        System.out.println(encodeBase64("aaa:bbb"));
    }
}
