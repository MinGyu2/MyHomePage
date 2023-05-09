package com.mingyu2.login.authentication;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.mingyu2.login.authentication.database.User;
import com.mingyu2.login.authentication.database.UsersDAO;
import jakarta.servlet.ServletContext;

public class LoginAuthentication {
    public static String userAuthenticatoin(ServletContext servletContext, String username, String password){
        var userDAO = new UsersDAO(servletContext);
        // 1. 존재하는 유저인지 확인하기.
        if(!userDAO.isExistUserName(username)){
            return null;
        }
        // 2. 아이디에 해당하는 salt, pwd 가져오기
        var user = userDAO.getUser(username);
        // 3. 암호 알고리즘 가져오기, salt 가져오기
        var algorithm = user.getAlgorithm();
        var salt = user.getSalt();
        try {
            var md = MessageDigest.getInstance(algorithm);
            md.update(salt.getBytes());
            md.update(password.getBytes());
            var authPwd = String.format("%064x", new BigInteger(1,md.digest()));

            if(!authPwd.equals(user.getPassword())){ 
                return null;
            }
            // 비밀번호 일치하면 유저 정보 전달
            
            // 유저 정보 가져오기
            return user.getUsername();
        }catch(Exception e){
            // result = false;
        }
        return null;
    }
    public static User getUser(ServletContext servletContext, String user){
        var userDAO = new UsersDAO(servletContext);
        return userDAO.getUser(user);
    }
    public static boolean isExistUserName(ServletContext servletContext, String user){
        var userDAO = new UsersDAO(servletContext);
        return userDAO.isExistUserName(user);
    }
}
