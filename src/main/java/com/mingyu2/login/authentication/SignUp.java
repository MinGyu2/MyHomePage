package com.mingyu2.login.authentication;

import com.mingyu2.login.authentication.database.UsersDAO;

import jakarta.servlet.ServletContext;

public class SignUp {
    public static boolean createUser(ServletContext servletContext,String username, String email, String pwd1, String pwd2, String addrNumber, String address, String addressPlus){
        var usersDao = new UsersDAO(servletContext);
        return usersDao.createUser(username, email, pwd1, pwd2, addrNumber, address, addressPlus);
    }
}
