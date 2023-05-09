package com.mingyu2.happyhackingmain;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.mingyu2.database.DBConnection;
import com.mingyu2.happyhackingmain.noticeboard.NoticeBoardDAO;
import com.mingyu2.happyhackingmain.problems.problemauthentication.Problem1;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.LoginAuthMethods;
import com.mingyu2.login.SessionName.UserName;
import com.mingyu2.login.authentication.LoginAuthentication;
import com.mingyu2.login.authentication.database.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MainPage extends HttpServlet{
    private String sessionUserName = UserName.getUsername();
    @Override
    public void init() throws ServletException {
        var context = getServletContext();

        DBConnection.addDBINFO(context, "main", "/WEB-INF/db-info/main-db-info");
        DBConnection.addDBINFO(context, "prob", "/WEB-INF/db-info/problems-db-info");
        DBConnection.addDBINFO(context, "noticeBoard", "/WEB-INF/db-info/main-notice-board-info");
        // test
        // context.setAttribute("shareValue", "안녕 값 공유중이양!");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();
        if(uri.equals("/")){
            response.sendRedirect("/main_page");
            return;
        }
        var pattern = "/main_page[/_a-zA-Z0-9]*";
        if(!uri.matches(pattern)){ // 404 페이지 보여줌
            super.doGet(request, response);
            return;
        }
        // 세션 id 가 존재 하는지 확인
        var user = getSessionUser(request);
        // 세션 id 가 존재 안하면 로그인 화면으로 이동한다.
        if(user == null){
            response.sendRedirect("/sign_in_page"); // 로그인 페이지로 이동
            return;
        }

        var userInfo = LoginAuthentication.getUser(getServletContext(), user);
        if(userInfo == null){
            response.sendRedirect("/sign_in_page"); // 로그인 페이지로 이동
            deleteSession(request);
            return;
        }

        // 세션 id 존재! user이름에 해당하는 정보를 가져와 넣어준다.
        request.setAttribute("user", userInfo);

        if(uriSearch(request, response)){ // 일치하는 uri 찾음!! 성공!
           return; 
        }
        
        // 패턴에 일치 안 하면 무조건 404 페이지 보여준다.
        super.doGet(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();
        if(uri.equals("/")){
            response.sendRedirect("/main_page");
            return;
        }
        var pattern = "/main_page[/_a-zA-Z0-9]*";
        if(!uri.matches(pattern)){ // 404 페이지 보여줌
            super.doGet(request, response);
            return;
        }
        // 세션 id 가 존재 하는지 확인
        var user = getSessionUser(request);
        if(user == null){ // 로그인이 필요하다.
            response.sendRedirect("/sign_in_page"); // 로그인 페이지로 이동
            return;
        }

        var userInfo = LoginAuthentication.getUser(getServletContext(), user);
        if(userInfo == null){
            response.sendRedirect("/sign_in_page"); // 로그인 페이지로 이동
            return;
        }

        // 세션 id 존재! user이름에 해당하는 정보를 가져와 넣어준다.
        request.setAttribute("user", userInfo);

        // 일치하는 uri 찾음!! 성공!
        if(uriSearch(request, response)){
           return; 
        }

        // 패턴에 일치 안 하면 무조건 404 페이지 보여준다.
        super.doPost(request, response);
    }

    private boolean uriSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        var uri = request.getRequestURI();
        var context = getServletContext();
        var probURI = "/main_page/authentication_prob/problem1";
        var sqlProbURI = "/main_page/sql_injection_prob/login_methods";
        
        var noticeBoard = "/main_page/notice_board";
        var noticeBoardWrite = "/main_page/notice_board/notice_write";
        var noticeBoardSave = "/main_page/notice_board/notice_save";
        var noticeBoardDetail = "/main_page/notice_board/notice_detail";
        var noticeBoardModify = "/main_page/notice_board/notice_modify";
        
        if(uri.matches("/main_page[/]?")){
            // 메인페이지를 보여준다.
            request.setAttribute("authProblem1", probURI);
            request.setAttribute("loginMethods", sqlProbURI);

            // 메뉴
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("",true)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>(noticeBoard, false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));

            request.setAttribute("menus", menus);

            return mainPage(request, response, 1);
        }

        // 게시판
        if(uri.equals(noticeBoard)){
            // 메뉴들
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("/main_page",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>("",true)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            request.setAttribute("menus", menus);

            var noticeBoardDAO = new NoticeBoardDAO(getServletContext());
            var array = noticeBoardDAO.getNoticeList();
            request.setAttribute("noticeBoard", array);

            request.setAttribute("noticeBoardWrite", noticeBoardWrite);
            request.setAttribute("noticeBoardDetail", noticeBoardDetail);

            return mainPage(request, response, 2);
        }
        // 글 새로 쓰기
        if(uri.equals(noticeBoardWrite)){ // mod 1 글쓰기
            request.setAttribute("noticeBoardURL", noticeBoardSave);
            request.setAttribute("writeMod",1);
            gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_write.jsp");
            return true;
        }
        // 글 저장
        if(request.getMethod().equals("POST") && uri.equals(noticeBoardSave)){
            var title = request.getParameter("title");
            var mainText = request.getParameter("main-text");

            var result = "";
            if(title.equals("") || mainText.equals("")){
                result = "<script>alert('fail');location.href='"+noticeBoardWrite+"'</script>";
                simpleAlert(response, result);
                return true;
            }
            
            // 저장
            var user = (User)request.getAttribute("user");
            var notice = new NoticeBoardDAO(getServletContext());
            if(!notice.saveNotice(user, title, mainText)){
                result = "<script>alert('fail');location.href='"+noticeBoardWrite+"'</script>";
                simpleAlert(response, result);
                return true;
            }

            result = "<script>alert('success');location.href='"+noticeBoard+"'</script>";
            simpleAlert(response, result);
            return true;
        }
        // 글 자세히 보기
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardDetail)){
            try {
                // 게시글 sid
                var sid = request.getParameter("detailid");
                            
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(Long.parseLong(sid));

                var user = (User)request.getAttribute("user");

                if(notice != null){
                    request.setAttribute("notice", notice);

                    if(user.getSid() == notice.getUserSID()){
                        request.setAttribute("modifyButton", true);
                        request.setAttribute("modifyHref", noticeBoardModify);
                    }
                    
                    gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_detail.jsp");
                    return true;
                }
            }catch(Exception e){
                return false;
            }
        }

        // mod 2 글 수정 하기
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardModify)){
            try{
                var sid = request.getParameter("pageid");
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(Long.parseLong(sid));
                
                var user = (User)request.getAttribute("user");

                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함
                    return false;
                }

                request.setAttribute("noticeBoardURL", ""); // 수정 저장 URL
                
                request.setAttribute("title", notice.getTitle());
                request.setAttribute("mainText", notice.getMainText());

                System.out.println(notice.getUserSID() + "  "+ user.getSid());

                gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_write.jsp");
            }catch(Exception e){
                System.out.println(e.getMessage());
                return false;
            }

        }


        if(uri.matches(probURI+"[/_a-zA-Z0-9]*")){
            return (new Problem1(probURI, context, request, response)).connectPage();
        }
        if(uri.matches(sqlProbURI+"[/_a-zA-Z0-9]*")){
            return (new LoginAuthMethods(sqlProbURI, context, request, response)).connectPage();
        }
        return false;
    }
    private void simpleAlert(ServletResponse response, String result) throws IOException{
        var sb = new StringBuilder();
        sb.append(result);
        var out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
        out.write(sb.toString());
        out.flush();
        out.close();
    }
    private boolean mainPage(HttpServletRequest request, HttpServletResponse response, int menuNumber) throws ServletException, IOException{
        request.setAttribute("menuNumber", menuNumber);
        gotoForwardPage(request, response, "/WEB-INF/main_page/main_page.jsp"); 
        return true;
    }
    private void gotoForwardPage(HttpServletRequest request, HttpServletResponse response, String path) throws ServletException, IOException  {
        var dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response); 
    }
    // session 삭제
    private void deleteSession(HttpServletRequest request){
        var session = request.getSession(false);
        if(session == null){
            return;
        }
        session.invalidate(); // 세션 삭제
    }
    // session id 에 대응하는 값을 가지고 온다.
    private String getSessionUser(HttpServletRequest request){
        var session = request.getSession(false); // 세션 생성 또는 불러오기
        // 60 초 안에 새로고침 하면 다시 60초가 주어진다.
        if(session == null){
            return null;
        }
        return (String)session.getAttribute(sessionUserName);
    }
}
