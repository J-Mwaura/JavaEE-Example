/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;
import java.util.Random;

import com.google.gson.Gson;

/**
 *
 * @author atjkm
 */
public class Utils {
	  private static final char[] symbols;
      private static final Gson gson = new Gson();
	 
      static {
	    StringBuilder tmp = new StringBuilder();
	    for (char ch = '0'; ch <= '9'; ++ch)
	      tmp.append(ch);
	    for (char ch = 'a'; ch <= 'z'; ++ch)
	      tmp.append(ch);
	    symbols = tmp.toString().toCharArray();
	  }   

	  private static final Random random = new Random();

	  public static String prepareRandomString(int len) {
		char[] buf = new char[len];
	    for (int idx = 0; idx < buf.length; ++idx) 
	      buf[idx] = symbols[random.nextInt(symbols.length)];
	    return new String(buf);
	  }
	  
	  public static String toJson(Object obj) {
		  return gson.toJson(obj);
	  }
	  
	  public static void main(String args[]) {
		  System.out.println(BCrypt.gensalt());
	  }
    
}
