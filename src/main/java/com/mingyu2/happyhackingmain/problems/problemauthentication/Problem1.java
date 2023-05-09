package com.mingyu2.happyhackingmain.problems.problemauthentication;

import java.io.IOException;

import com.mingyu2.happyhackingmain.problems.Problem;
import com.mingyu2.happyhackingmain.problems.authentication.ProbLoginAuthenticaton;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// session 탈취 취약점이 존재한다. session id
// 쿠키 변조
public class Problem1 implements Problem{
    private String mainURI;
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    public Problem1(String mainURI,ServletContext servletContext, HttpServletRequest request, HttpServletResponse response){
        this.mainURI = mainURI;
        this.servletContext = servletContext;
        this.request = request;
        this.response = response;
    }
    @Override
    public boolean connectPage() throws ServletException, IOException {
        var uri = request.getRequestURI();
        var authenticationURI = mainURI+"/authentication";
        var logoutURI = mainURI+"/logout";

        var memberSessionId ="679fc13e74347c4860537ff09";
        var adminSessionId = "f9a81477552594c79f2abc3fc";
        if(uri.matches(mainURI+"[/]?")){
            request.setAttribute("title", "문제 1");
            request.setAttribute("formAction", authenticationURI);
            // 쿠키에 session id 있는지 확인.
            var sessionId = getCookieValue(request, "PROBSESSIONID");
            if(sessionId.equals("")) {
                // 없다. session id 새로 발급한다.
                // response.addCookie(new Cookie("PROBSESSIONID",memberSessionId));
                addCookie(new Cookie("PROBSESSIONID",memberSessionId),response);
            }else if(sessionId.equals(adminSessionId)){
                // admin 계정으로 로그인함
                adminPageLoginSuccess(request, response, logoutURI);
                return true;
            }

            // 로그인 페이지를 보여준다.
            String signInMSG = "탈취한 session id 는 f9a81477552594c79f2abc3fc 이다";
            request.setAttribute("signInMSG", signInMSG);
            gotoForwardPage(request, response, "/WEB-INF/sign_in_page/sign_in_page.jsp");
            return true;
        }
        if(uri.equals(authenticationURI)){
            if(!request.getMethod().equals("POST")){
                return false;
            }

            // 쿠키에 들어있는 session id 확인
            var sessionId = getCookieValue(request, "PROBSESSIONID");
            if(sessionId.equals("")) {
                // 없다. session id 새로 발급한다.
                // response.addCookie(new Cookie("PROBSESSIONID", memberSessionId));
                addCookie(new Cookie("PROBSESSIONID", memberSessionId), response);
            }else if(sessionId.equals(adminSessionId)){
                // admin 계정으로 로그인함
                adminPageLoginSuccess(request, response, logoutURI);
                return true;
            }

            // 인증 시작
            String id = request.getParameter("user_name");
            String pwd = request.getParameter("user_password");
            var member = ProbLoginAuthenticaton.userAuthenticatoin(servletContext, id, pwd);
            var text = "fail";
            if(member == null){ // 로그인 실패 다시 로그인 창으로 돌아간다.
                response.sendRedirect(mainURI+"");
                return true;
            }
            
            // 인증 성공!
            text = member.getId();
            request.setAttribute("test", text); 
            request.setAttribute("logout",logoutURI);
            gotoForwardPage(request, response, "/WEB-INF/main_page/problems/authenticaton_prob/p1.jsp");
            return true;
        }

        if(uri.equals(logoutURI)){
            // 쿠키 만료 시키기
            var cookie = new Cookie("PROBSESSIONID", null);
            cookie.setMaxAge(0);
            addCookie(cookie, response);
            response.sendRedirect(mainURI+"");
            return true;
        }

        return false;
    }
    private void addCookie(Cookie cookie,  HttpServletResponse response){
        cookie.setPath(mainURI);
        response.addCookie(cookie);
    }
    private void adminPageLoginSuccess(HttpServletRequest request, HttpServletResponse response, String logoutURI) throws ServletException, IOException {
        // admin 계정으로 로그인함
        request.setAttribute("test", "난 admin 이다<br>"); 
        request.setAttribute("logout",logoutURI);
        gotoForwardPage(request, response, "/WEB-INF/main_page/problems/authenticaton_prob/p1.jsp");
    }
    private String getCookieValue(HttpServletRequest request,String name){
        var cookies = request.getCookies();
        if(cookies == null){
            return "";
        }
        for(var cookie:cookies){
            if(!cookie.getName().equals(name)){
                continue;
            }
            if(cookie.getMaxAge() == 0){ // 쿠키 만료됨
                break;
            }
            // 같은 이름의 쿠키 찾음
            return cookie.getValue(); // 쿠키에 저장된 값을 전달한다.
        }
        return ""; // 일치하는 쿠키가 없음
    }
    private void gotoForwardPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException  {
        var dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response); 
    }
}
