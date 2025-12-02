package com.example.android_onenet;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Token {
    public static String assembleToken(String version, String resourceName, String expirationTime, String signatureMethod, String accessKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder sb = new StringBuilder();
        String res = URLEncoder.encode(resourceName, "UTF-8");
        String sig = URLEncoder.encode(generatorSignature(version, resourceName, expirationTime, accessKey, signatureMethod), "UTF-8");
        sb.append("version=")
                .append(version)
                .append("&res=")
                .append(res)
                .append("&et=")
                .append(expirationTime)
                .append("&method=")
                .append(signatureMethod)
                .append("&sign=")
                .append(sig);
        return sb.toString();
    }

    public static String generatorSignature(String version, String resourceName, String expirationTime, String accessKey, String signatureMethod)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String encryptText = expirationTime + "\n" + signatureMethod + "\n" + resourceName + "\n" + version;
        byte[] bytes = HmacEncrypt(encryptText, accessKey, signatureMethod);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] HmacEncrypt(String data, String key, String signatureMethod)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signinKey = new SecretKeySpec(Base64.decode(key, Base64.NO_WRAP), "Hmac" + signatureMethod.toUpperCase());
        Mac mac = Mac.getInstance("Hmac" + signatureMethod.toUpperCase());
        mac.init(signinKey);
        return mac.doFinal(data.getBytes());
    }

    public enum SignatureMethod {
        SHA1, MD5, SHA256;
    }

    public String key(String ProductID,String Device_Name, String Device_AccessKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String version = "2018-10-31";
        String resourceName = "products/"+ProductID+"/devices/"+Device_Name+"";
        String expirationTime = System.currentTimeMillis() / 1000 + 100 * 24 * 60 * 60 + "";  // 当前时间 + 1天
        String signatureMethod = SignatureMethod.SHA1.name().toLowerCase();
        String accessKey = Device_AccessKey;
        String token = assembleToken(version, resourceName, expirationTime, signatureMethod, accessKey);
        System.out.println("Authorization:" + token);
        return token;
    }
}