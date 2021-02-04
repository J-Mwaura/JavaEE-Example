/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author atjkm
 */
public class MD5Util {

    public static String generateMD5(String value) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(value.getBytes());

            BigInteger number = new BigInteger(1, messageDigest);
            return number.toString(16);

        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }
    
}
