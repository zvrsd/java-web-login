package com.nope.webapp.util;

import javax.servlet.http.Cookie;

/**
 * 
 * @author zvr
 */
public class CookieUtil {
    
    private static CookieUtil instance;
    
    private CookieUtil(){
        
    }
    
    public static CookieUtil getInstance(){
        if(instance == null){
            instance = new CookieUtil();
        }
        return instance;
    }
    
    public Cookie getCookieByName(Cookie[] cookies, String name){
        
        if(cookies == null){
            return null;
        }
        
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(name)){
                return cookie;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param cookies 
     */
    public void deleteAllCookies(Cookie[] cookies){
        
        if(cookies == null){
            return;
        }
        
        for(Cookie cookie : cookies){
            cookie.setMaxAge(0);
            return;
        }
    }
}
