package com.mingyu2.happyhackingmain.problems.problemsqlinjection;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.mingyu2.happyhackingmain.Pair;
import com.mingyu2.happyhackingmain.problems.Problem;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.BlindSQLi;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthDiv;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthDivCheckIdTwice;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthDivErrorBasedSQLi;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthDivPWDHash;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthSame;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth.LoginAuthSamePWDHash;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginAuthMethods implements Problem{
    private String baseURI;
    private ServletContext context;
    private HttpServletRequest request;
    private HttpServletResponse response;
    public LoginAuthMethods(String uri, ServletContext context, HttpServletRequest request, HttpServletResponse response){
        this.baseURI = uri;
        this.context = context;
        this.request = request;
        this.response = response;
    }
    @Override
    public boolean connectPage() throws ServletException, IOException {
        var uri = request.getRequestURI();
        var distingAndAuthSame = baseURI+"/at_the_same1"; // 식별과 인증 동시
        var distingAndAuthDiv = baseURI+"/division"; // 식별과 인증 따로 하기
        var distingAndAuthSamePWDHASH = baseURI+"/at_the_same_pwdhash"; // 식별과 인증 동시 + 비밀번호 해시 암호화하여 저장
        var distingAndAuthDivPWDHASH = baseURI+"/division_pwdhash"; // 식별과 인증 동시 + 비밀번호 해시 암호화하여 저장
        var distingAndAuthDivTowCheckIdTwice = baseURI+"/division_check_id_twice"; // 식별과 인증 동시 + 비밀번호 해시 암호화하여 저장
        var distingAndAuthDivErrorBasedInjection = baseURI+"/division_error_based_injection"; // 식별과 인증 따로 + 에러기반 injection
        var distingAndAuthDivBlindSQLInjection = baseURI+"/division_blind_sql_injection"; // blind sql injection 시도해보기.
        if(uri.matches(baseURI+"[/]?")){
            var pairList = new ArrayList<Pair<String,String>>();
            pairList.add(new Pair<>(distingAndAuthSame, "식별 & 인증을 동시에 할 경우 (1)"));
            pairList.add(new Pair<>(distingAndAuthDiv, "식별 & 인증을 분리하여 하는 경우"));
            pairList.add(new Pair<>(distingAndAuthSamePWDHASH, "식별과 인증 동시 + 비밀번호 해시 암호화하여 저장"));
            pairList.add(new Pair<>(distingAndAuthDivPWDHASH, "식별과 인증 분리 + 비밀번호 해시 암호화하여 저장"));
            pairList.add(new Pair<>(distingAndAuthDivTowCheckIdTwice,"식별과 인증 분리 + 아이디 두번 확인하기"));
            pairList.add(new Pair<>(distingAndAuthDivErrorBasedInjection, "Error Based Injection"));
            pairList.add(new Pair<>(distingAndAuthDivBlindSQLInjection, "Blind SQL Injection"));

            // uri 를 저장한 list를 jsp 로 보낸다.
            request.setAttribute("pairList", pairList);
            gotoForwardPage(request, response, "/WEB-INF/main_page/problems/sql_injection/login_methods.jsp");
            return true;
        }
        if(gotoProbPage(uri,distingAndAuthSame,new LoginAuthSame(context),
                "식별과 인증 동시(1)",
                "너의 아이디는 user2 이고 비번은 2222 다. root 로 로그인 해보자.")){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthDiv, new LoginAuthDiv(context), "식별과 인증 분리", "너의 아이디는 user2 이고 비번은 2222 다. root 로 로그인 해보자.")){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthSamePWDHASH, new LoginAuthSamePWDHash(context), "식별과 인증 동시에 하기 + 비밀번호 해시 암호화하여 저장", "너의 아이디는 user2 이고 비번은 2222 다. root 로 로그인 해보자.")){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthDivPWDHASH, new LoginAuthDivPWDHash(context), "식별과 인증 분리 + 비밀번호 해시 암호화하여 저장", "너의 아이디는 user2 이고 비번은 2222 다. root 로 로그인 해보자.")){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthDivTowCheckIdTwice, new LoginAuthDivCheckIdTwice(context), "식별과 인증 분리 + 아이디 두번 확인하기", "너의 아이디는 user2 이고 비번은 2222 다. root 로 로그인 해보자.")){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthDivErrorBasedInjection, new LoginAuthDivErrorBasedSQLi(context), "Error Based SQL Injection", null)){
            return true;
        }
        if(gotoProbPage(uri, distingAndAuthDivBlindSQLInjection, new BlindSQLi(context), "Blind SQL Injection", "너의 아이디는 user2 이고 비번은 2222 다. DB 속 정보를 추출해 보자.")){
            return true;
        }
        return false;
    }
    private boolean gotoProbPage(String uri, String probURI, LoginAuth auth, String title,String signInMSG) throws ServletException, IOException{
        if(uri.matches(probURI+"[/]?")){
            request.setAttribute("title", title);
            request.setAttribute("formAction", probURI+"/authentication");
            if(signInMSG == null) {
                var msg = request.getParameter("msg");
                request.setAttribute("signInMSG", msg);
            }else{
                request.setAttribute("signInMSG", signInMSG);
            }
            gotoForwardPage(request, response, "/WEB-INF/sign_in_page/sign_in_page.jsp");
            return true;
        }
        if(uri.equals(probURI+"/authentication")){ // 인증 성공
            if(!request.getMethod().equals("POST")){
                return false;
            }
            // 식별 및 인증 동시에 하기
            // 사용자 입력
            var id = (String)request.getParameter("user_name");
            var pwd = (String)request.getParameter("user_password");

            // 인증
            var member = auth.authAndMember(id, pwd);
            var query = auth.getSqlQuery(); // 인증할 때 사용한 sql 질의문 확인
            var msgC = auth.getMSG();
            if(member == null) {
                // 인증 실패
                // distingAndAuthSame 으로 리다이렉션 한다.
                var msg = "";
                if(msgC != null){
                    msg = "?msg="+URLEncoder.encode(msgC, "UTF-8");
                }
                response.sendRedirect(probURI+msg);
                return true;
            }
            // 인증 성공
            request.setAttribute("title", title);
            request.setAttribute("username", member.getId());
            request.setAttribute("inputSqlSTM", query);
            request.setAttribute("logoutURI", probURI);
            request.setAttribute("member", member);
            gotoForwardPage(request, response, "/WEB-INF/main_page/problems/success_page.jsp");
            return true;
        }
        return false;
    }
    private void gotoForwardPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException  {
        var dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response); 
    }
}
