package com.mingyu2.login;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Logout extends HttpServlet{
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();
        if(!uri.equals("/user-logout")){ // 로그아웃 요청이 아니면 실패
            super.doGet(request, response);
            return;
        }
        var session = request.getSession(false);
        if(session != null){
            session.invalidate();
        }
        response.sendRedirect("/sign_in_page");
    }
}
