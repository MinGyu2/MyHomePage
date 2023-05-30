package com.mingyu2.login;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import com.mingyu2.login.SessionName.UserName;
import com.mingyu2.login.authentication.LoginAuthentication;
import com.mingyu2.login.authentication.SignUp;
import com.mingyu2.login.authentication.searchaddress.AddressDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Login extends HttpServlet{
    private String sessionUserName = UserName.getUsername();
    // /sign_in_page 요청이오면 이쪽으로 온다.
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();
        
        // sign_in_page 아닌 다른 페이지로 가면 오류 페이지를 보여준다.
        if(!uri.matches("/sign_in_page[/_a-zA-Z0-9]*")){ 
            super.doGet(request, response);
            return;
        }
        
        String user = getSessionUser(request);
        if(user != null){ 
            // 이전에 로그인 한 세션 키가 살아 있으며 바로 메인 페이지로 간다.
            // 로그인 성공
            // 메인 페이지로 이동하자
            response.sendRedirect("/main_page");
            return;
        }


        // 회원가입사이트 시작 =====
        if(uri.equals("/sign_in_page/sign_up_page")){
            // 회원가입 페이지로 이동한다.
            // request.setAttribute("statusMSG", "오류");
            request.setAttribute("usernameDupleCheck", "/sign_in_page/sign_up_page/username_duple_confirm");
            request.setAttribute("signUpSubmitURI", "/sign_in_page/sign_up_page/sign_up");
            request.setAttribute("signUpFindAddressNum", "/sign_in_page/sign_up_page/find_address_number");
            gotoForwardPage(request, response, "/WEB-INF/sign_up_page/sign_up_page.jsp");
            return;
        }
        //아이디 중복 확인 get 으로 id를 받는다.
        if(uri.matches("/sign_in_page/sign_up_page/username_duple_confirm[/]?")){
            var username = request.getParameter("username");
            var userResult = LoginAuthentication.isExistUserName(getServletContext(), username);
            var result = "fail";
            if(!userResult){
                result = "success";
            }

            var sb = new StringBuilder();
            sb.append(result);
            var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            out.write(sb.toString());
            out.flush();
            out.close();
            return;
        }

        // 우편번호 찾기
        if(uri.equals("/sign_in_page/sign_up_page/find_address_number")){
            var address = request.getParameter("address");
            
            // 주소를 이용하여 우편 번호 찾기 시작!
            var addressDAO = new AddressDAO(getServletContext());
            var result = addressDAO.getAddressNumber(address);
            
            // 끝

            var sb = new StringBuilder();
            sb.append(result);
            var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            out.write(sb.toString());
            out.flush();
            out.close();
            return;
        }
        //회원가입을 위한 주소 끝 =====

        // sign_in_page 아닌 다른 페이지로 가면 오류 페이지를 보여준다.
        if(!uri.equals("/sign_in_page")){ 
            super.doGet(request, response);
            return;
        }
        var msg = request.getParameter("msg");
        // msg html 태그를 html entity로 치환하기.
        if(msg == null){
            msg = "";
        }
        msg = filter(msg);
        request.setAttribute("signInMSG", msg);

        String title = "Happy Hacking";
        request.setAttribute("title", title);
        // form action 주소 

        String fromAction = "/sign_in_page/authentication";
        request.setAttribute("formAction", fromAction);
        request.setAttribute("signUpLink", "/sign_in_page/sign_up_page");
        // session id 가 만료되었거나 없어서 새롭게 로그인 해 줘야한다.
        // 로그인 페이지를 보여준다.
        gotoForwardPage(request, response, "/WEB-INF/sign_in_page/sign_in_page.jsp");
    }
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();

        // 회원 가입 id pwd email 을 post 로 받음! start =====
        if(uri.equals("/sign_in_page/sign_up_page/sign_up")){

            var username = request.getParameter("username");
            var email = request.getParameter("email");
            var pwd1 = request.getParameter("pwd1");
            var pwd2 = request.getParameter("pwd2");
            var addrNumber = request.getParameter("address_number");
            var address = request.getParameter("address1");
            var addressPlus = request.getParameter("address2");
        
            if(!SignUp.createUser(getServletContext(), username, email, pwd1, pwd2, addrNumber, address, addressPlus)){
                // 계정 생성 실패! 다시 회원가입 화면으로 넘어온다.
                var result = "<script>alert('fail');location.href='/sign_in_page/sign_up_page'</script>";

                var sb = new StringBuilder();
                sb.append(result);
                var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
                out.write(sb.toString());
                out.flush();
                out.close();
                return;
            }
            
            // 회원가입 성공 ! 로그인 페이지로 이동한다.
            var result = "<script>alert('success!');location.href='/sign_in_page'</script>";

            var sb = new StringBuilder();
            sb.append(result);
            var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            out.write(sb.toString());
            out.flush();
            out.close();
            
            return;
        }
        // 회원가입 end ===== 
        
        if(!uri.equals("/sign_in_page/authentication")){
            super.doPost(request, response);
            return;
        }
        
        // 인증 페이지에 왔다는 것은 session 에 저장이 안되어 있다는 것이다. 하지만 혹시 모르니 다시 검사하자.
        // ----- 로그인 세션 키가 일치하면 main page 로 이동한다.--------
        var user = getSessionUser(request);
        if(user != null){
            response.sendRedirect("/main_page");
            return;
        }
        // ------------------------------------------------------------
        
        var userName = request.getParameter("user_name");
        var userPwd = request.getParameter("user_password");
        // 아이디 및 비밀번호 검사
        user = LoginAuthentication.userAuthenticatoin(getServletContext(),userName, userPwd); // 로그인 성공하면 user 정보 받음
   
        if(user == null) { // 로그인 실패
            // 실패시 다시 로그인 페이지ㅣ
            var fail = URLEncoder.encode("로그인 실패", "UTF-8");
            response.sendRedirect("/sign_in_page?msg="+fail);
            return;
        }
        // 로그인 성공
        // 세션키 발급 및 쿠키에 session id 넣기 // 확인해 보니 쿠키에 자동으로 들어간다.
        setSessionUser(request, response, user);
        
        // 메인페이지로 이동한다.
        response.sendRedirect("/main_page");
        // var re = "";
        // var sb = new StringBuilder();
        // re = String.format("%s %s (auth: %s)",user.getUsername(), user.getEmail(),user.getAuthority());
        // sb.append("<head><meta charset=\"utf-8\"></head>");
        // sb.append("post요청 받은 로그인 페이지: "+uri+"<br>");
        // sb.append(String.format("user name : %s<br>",userName));
        // sb.append(String.format("user password : %s<br>",userPwd));
        // sb.append(re);
        // tempMainPage(response, sb.toString());
        return;
    }
    // private void tempMainPage(HttpServletResponse response, String sout) throws IOException{
    //     var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
    //     out.write(sout);
    //     out.flush();
    //     out.close();
    // }
    // session id 에 대응하는 값을 가지고 온다.
    private String getSessionUser(HttpServletRequest request){
        var session = request.getSession(false); // 세션 생성 또는 불러오기
        // 60 초 안에 새로고침 하면 다시 60초가 주어진다.
        if(session == null){
            return null;
        }
        return (String)session.getAttribute(sessionUserName);
    }
    private void setSessionUser(HttpServletRequest request, HttpServletResponse response,String user){
        var session = request.getSession(true); // 세션 생성 또는 불러오기
        session.setMaxInactiveInterval(3600); // 1시간 동안만 유지된다.
        session.setAttribute(sessionUserName, user);
    }
    // private String getCookieValue(HttpServletRequest request,String name){
    //     var cookies = request.getCookies();
    //     if(cookies == null){
    //         return "";
    //     }
    //     for(var cookie:cookies){
    //         if(!cookie.getName().equals(name)){
    //             continue;
    //         }
    //         // 같은 이름의 쿠키 찾음
    //         return cookie.getValue(); // 쿠키에 저장된 값을 전달한다.
    //     }
    //     return ""; // 일치하는 쿠키가 없음
    // }
    private void gotoForwardPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException  {
        var dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response); 
    }

    // xss 방지
    private String filter(String in){
        // 순서데로 해야한다.
        // & &amp;  이게 무조건 첫번째로 와야한다.
        // < &lt;
        // > &gt;
        // ' &#x27;
        // " &quot;
        // ( &#40;
        // ) &#41;
        // / &#x2F;
        var re = in.replaceAll("[&]", "&amp;")
                .replaceAll("[<]", "&lt;")
                .replaceAll("[>]", "&gt;")
                .replaceAll("[']", "&#x27;")
                .replaceAll("[\"]", "&quot;")
                .replaceAll("[(]", "&#40;")
                .replaceAll("[)]", "&#41;")
                .replaceAll("[/]", "&#x2F;");
        return re;
    }
}
