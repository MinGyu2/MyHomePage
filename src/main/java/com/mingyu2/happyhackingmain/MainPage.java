package com.mingyu2.happyhackingmain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig(
    maxFileSize = -1,
    maxRequestSize = -1,
    fileSizeThreshold = 1024
)
public class MainPage extends HttpServlet{
    private String sessionUserName = UserName.getUsername();
    private final int MAX_FILE_NUM = 2;
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
        var noticeBoardModifySave = "/main_page/notice_board/notice_modify_save";
        var noticeBoardDelete = "/main_page/notice_board/notice_delete";

        var noticeBoardFileDownload = "/main_page/notice_board/notice_file_download";
        var noticeBoardFileDelete = "/main_page/notice_board/notice_file_delete";
        
        if(uri.matches("/main_page[/]?")){
            // 메인페이지를 보여준다.
            request.setAttribute("authProblem1", probURI);
            request.setAttribute("loginMethods", sqlProbURI);

            // 메뉴
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("",true)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>(noticeBoard+"?page=1&q=", false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));

            request.setAttribute("menus", menus);

            return mainPage(request, response, 1);
        }

        // 게시판
        if(request.getMethod().equals("GET") && uri.equals(noticeBoard)){
            // 메뉴들
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("/main_page",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>("",true)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            request.setAttribute("menus", menus);

            var p = request.getParameter("page");

            // 현재 페이지
            var page = 1;
            try {
                page = Integer.parseInt(p);
                if(page < 1){
                    page = 1;
                }
            }catch(Exception e){
                e.printStackTrace();
                page=1;
            }
            System.out.println(page);

            var q = request.getParameter("q"); // null or "" 면 모든 데이터 다 보여주기
            System.out.println(q);

            var noticeBoardDAO = new NoticeBoardDAO(getServletContext());

            // 모든 데이터 다 가져오기.
            if(q == null){
                q="";
            }

            // 검색 결과 총 게시글 수 알아보기
            int cnt = noticeBoardDAO.getCount(q);
            System.out.println("검색 개시글 수 : "+cnt);


            // ****** 클라이언트가 수정 가능 *******
            // 한 페이지 보여줄 데이터 갯수 정하기
            int number = 5;
            // ****** 클라이언트가 수정 가능 *******

            // 마지막 페이지
            int lastPage = (cnt/number)+((cnt%number == 0)?0:1);
            // 최대 페이지 수를 초과하면 가장 마지막 페이지로 이동한다.
            if(page > lastPage){
                response.sendRedirect(noticeBoard+"?page="+lastPage+"&q="+q);
                return true;
            }
            // 데이터 시작 위치
            int start = (page-1)*number;
            

            // 검색 데이터 가져오기.
            var array = noticeBoardDAO.getNoticeList(q,start,number);


            //***** 페이징 정보 *****
            var pagination=new ArrayList<Integer>();


            // ****** 클라이언트가 수정 가능 *******
            var paginationBase = 5; // 기본이되는 페이징 홀수
            // ****** 클라이언트가 수정 가능 *******

            pagination.add(page);

            // 페이징을 배열에 저장한다.
            var leftEnd = false;
            var rightEnd = false;
            for(int i = 0, left=page, right=page;i<paginationBase-1;){
                // left
                if(left > 1){
                    left -=1;
                    pagination.add(0, left);
                    i++;
                }else {
                    leftEnd=true;
                }
                // right
                if(right < lastPage){
                    right+=1;
                    pagination.add(right);
                    i++;
                }else {
                    rightEnd=true;
                }

                if(leftEnd && rightEnd){
                    break;
                }
            }


            // 첫번째 페이지와 마지막 페이지를 포함해야 하는지 검사한다. 포함해야 하면 포함시킨다.
            // 0 은 생략을 의미한다.
            // prev 1 ... 3 4 5 6 next
            var f = pagination.get(0);
            if(f != 1){
                if(f != 2){
                    pagination.add(0,0);
                }
                pagination.add(0,1);
            }

            var l = pagination.get(pagination.size()-1);
            if(l != lastPage){
                if(l != lastPage-1){
                    pagination.add(0);
                }
                pagination.add(lastPage);
            }

            //***** 페이징 정보 end *****

            
            //***** mainpage 로 페이징 정보 넘겨주기 *****

            request.setAttribute("noticeBoard", array); // 검색 결과 jsp 로 전달함.
            request.setAttribute("noticeBoardWrite", noticeBoardWrite); // new write
            request.setAttribute("noticeBoardDetail", noticeBoardDetail); // show detail

            // 페이지 번호를 위한 것
            // 현재 페이지 정보 page
            request.setAttribute("currentPage", page);
            // 마지막 Page
            request.setAttribute("lastPage", lastPage);
            // 질문 
            request.setAttribute("question", q);
            // baseURL
            request.setAttribute("noticeBoardURL", noticeBoard);
            // 검색된 글 수
            request.setAttribute("countNotices", cnt);

            // 페이징 번호 리스트
            request.setAttribute("pagination", pagination);

            //***** mainpage 로 페이징 정보 넘겨주기 end*****

            System.out.println(pagination);

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
            var charSet = "utf-8";
            request.setCharacterEncoding(charSet);
            if(!request.getContentType().toLowerCase().startsWith("multipart/form-data")){
                //404 에러 발생 
                // application/x-www-form-urlencoded 타입이 아니라.
                // 오직 multipart/form-data 타입만 받는다.
                return false;
            }
            
            // 게시판 정보 저장
            var title = request.getParameter("title");
            var mainText = request.getParameter("main-text");

            var result = "";
            if(title.equals("") || mainText.equals("")){
                result = "<script>alert('fail');location.href='"+noticeBoardWrite+"'</script>";
                simplePage(response, result);
                return true;
            }
            
            // 저장
            var user = (User)request.getAttribute("user");
            var notice = new NoticeBoardDAO(getServletContext());
            var genTime = System.currentTimeMillis(); // 게시글 생성 시각
            var noticeSid = notice.saveNotice(user, title, mainText,genTime);
            
            if(noticeSid == 0){ // 저장 실패
                result = "<script>alert('fail');location.href='"+noticeBoardWrite+"'</script>";
                simplePage(response, result);
                return true;
            }

            // ***** 파일 업로드 시작 *****
            var noticeRe = "no file";
            try{
                // 폴더 생성 또는 존재 확인
                //  

                // 저장을 위한 폴더
                var folderName = genTime+"_"+ noticeSid;
                var uploadPath = getFilePath(request, folderName);
                // genTime + _ + noticeSID => 폴더명
                var parts = request.getParts();
                for(var part:parts){
                    if(!part.getHeader("Content-Disposition").contains("filename=")){
                        continue;
                    }
                    if(part.getSubmittedFileName().equals("")){
                        continue;
                    }
                    var file = new File(uploadPath);
                    if(!file.exists()){
                        // 폴더 생성
                        file.mkdir();
                        System.out.println(noticeSid+"게시글 폴더 생성 함");
                    }

                    var count = file.listFiles().length;
                    if(count > MAX_FILE_NUM){
                        System.out.println("최대 파일 갯수 초과");
                        break;
                    }

                    System.out.println("퐅더 존재? => "+file.isDirectory());

                    System.out.println("타입 : "+part.getHeader("Content-Disposition"));
                    System.out.println("크기 : "+part.getSize());
                    System.out.println("이름 : "+part.getSubmittedFileName());

                    part.write(uploadPath+File.separator+part.getSubmittedFileName());
                    part.delete(); // 임시파일 삭제

                    System.out.println("업로드 성공");
                    noticeRe = "file upload success";
                    System.out.println("경로 : "+uploadPath);
                }
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("파일 업로드 실패");
                noticeRe = "file upload fail";
            }
            // ***** 파일 업로드 end *****

            result = "<script>alert('success "+noticeRe+"');location.href='"+noticeBoard+"?page=1&q='</script>";
            simplePage(response, result);
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

                if(notice == null){
                    return false;
                }
                // 게시글 조회수 1 증가 시키기.
                var noticeSID = notice.getSid();
                noticeDAO.incressViews(noticeSID, notice.getViews());
                notice = noticeDAO.getNotice(noticeSID);

                if(notice == null){
                    return false;
                }
                // 게시글 조회수 증가 end

                // *** 파일들 읽어 오기 ***
                // href 링크 만들기
                var folderName = notice.getGenTime()+"_"+noticeSID;
                var uploadPath = getFilePath(request, folderName);
                // 파일 다운로드 get 파라미터 이름
                var fileNameList = new ArrayList<Pair<String,String>>();
                try{
                    var folder = new File(uploadPath); // 폴더
                    if(folder.exists()){
                        var files = folder.listFiles(); // 파일들
                        for(var file : files) {
                            var fileName = file.getName();
                            var hrefPrameter = fileName;
                            fileNameList.add(new Pair<String,String>(fileName,hrefPrameter));
                            System.out.println(fileNameList);
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                request.setAttribute("fileNameList", fileNameList);
                request.setAttribute("noticeBoardFileDownload", noticeBoardFileDownload);
                // *** 파일들 읽어 오기 end ***

                request.setAttribute("notice", notice);

                if(user.getSid() == notice.getUserSID()){
                    request.setAttribute("modifyButton", true);
                    request.setAttribute("modifyHref", noticeBoardModify);
                    request.setAttribute("deleteHref", noticeBoardDelete);
                }
                
                gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_detail.jsp");
                return true;
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }

        // mod 2 글 수정 하기
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardModify)){
            try{
                var sid = request.getParameter("pageid");
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(Long.parseLong(sid));
                if(notice == null){
                    System.out.println("존재 안하는 글");
                    return false;
                }
                
                var user = (User)request.getAttribute("user");

                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함 실패
                    return false;
                }

                var noticeSID = notice.getSid();
                var folderName = notice.getGenTime()+"_"+noticeSID;
                var uploadPath = getFilePath(request, folderName);
                // 파일 다운로드 get 파라미터 이름
                var fileNameList = new ArrayList<Pair<String,String>>();
                try{
                    var folder = new File(uploadPath); // 폴더
                    if(folder.exists()){
                        var files = folder.listFiles(); // 파일들
                        for(var file : files) {
                            var fileName = file.getName();
                            var hrefPrameter = folderName+"%2F"+fileName;
                            fileNameList.add(new Pair<String,String>(fileName,hrefPrameter));
                            System.out.println(fileNameList);
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                request.setAttribute("fileNameList", fileNameList);
                request.setAttribute("noticeBoardFileDownload", noticeBoardFileDownload);
                request.setAttribute("noticeBoardFileDelete", noticeBoardFileDelete);
                // *** 파일들 읽어 오기 end ***

                request.setAttribute("modifyMode",true);

                request.setAttribute("noticeBoardURL", noticeBoardModifySave); // 수정 저장 URL
                
                request.setAttribute("noticeID", sid); // 페이지 아이디
                request.setAttribute("title", notice.getTitle());
                request.setAttribute("mainText", notice.getMainText());

                System.out.println(notice.getUserSID() + "  "+ user.getSid() + "글 수정모드");

                gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_write.jsp");
                return true;
            }catch(Exception e){
                System.out.println(e.getMessage());
                return false;
            }
        }

        // 글 수정 저장하기
        if(request.getMethod().equals("POST") && uri.equals(noticeBoardModifySave)) {
            // delete and insert 삭제 -> 저장 아니면 alter 그냥 수정
            try {
                var result = "";
                var sid = Long.parseLong(request.getParameter("noticeID"));
                var title = request.getParameter("title");
                var mainText = request.getParameter("main-text");

                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(sid);
                if(notice == null){
                    System.out.println("존재 안하는 글");
                    return false;
                }

                var user = (User)request.getAttribute("user");
                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함 실패
                    return false;
                }

                // 전 저장위치
                var noticeSID = notice.getSid();
                var beforeFolderName = notice.getGenTime()+"_"+noticeSID;
                var beforeUploadPath = getFilePath(request, beforeFolderName);
                
                // 글 삭제
                if(!noticeDAO.deleteNotice(sid)){ // 삭제 실패
                    return false;
                }
                
                // 새로운 글 저장
                var genTime = System.currentTimeMillis();
                var newSid = noticeDAO.saveNotice(user, title, mainText, genTime);
                if(newSid==0){ // 실패
                    result = "<script>alert('fail');location.href='"+noticeBoard+"'</script>";
                    simplePage(response, result);
                    return true;
                }

                var noticeRe = "no new file";
                try {
                    var folder = new File(beforeUploadPath); // 폴더

                    var newFolderName = genTime+"_"+newSid;
                    var afterUploadPath = getFilePath(request, newFolderName);
                    var newFolder = new File(afterUploadPath);
                    if(folder.exists()){ 
                        // 폴더 이름만 변경.
                        folder.renameTo(newFolder);
                    }
                    // 
                    var parts = request.getParts();
                    for(var part:parts){
                        if(!part.getHeader("Content-Disposition").contains("filename=")){
                            continue;
                        }
                        if(part.getSubmittedFileName().equals("")){
                            continue;
                        }
                        if(!newFolder.exists()){
                            // 폴더 생성
                            newFolder.mkdir();
                            System.out.println(newSid+"게시글 폴더 생성 함");
                        }
                        
                        var count = newFolder.listFiles().length;
                        System.out.println("파일 갯수 : "+count);
                        if(count > MAX_FILE_NUM){
                            System.out.println("최대 파일 갯수 초과");
                            noticeRe = "out of file number ( maxcount 3 )";
                            break;
                        }

                        System.out.println("퐅더 존재? => "+newFolder.isDirectory());

                        System.out.println("타입 : "+part.getHeader("Content-Disposition"));
                        System.out.println("크기 : "+part.getSize());
                        System.out.println("이름 : "+part.getSubmittedFileName());
                        part.write(afterUploadPath+File.separator+part.getSubmittedFileName());
                        part.delete(); // 임시 파일 삭제

                        System.out.println("파일 저장 성공");
                        noticeRe = "file upload success";
                        System.out.println("경로 : "+afterUploadPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    noticeRe = "file upload fail";
                }
                // 성공
                result = "<script>alert('success : "+noticeRe+"');location.href='"+noticeBoard+"'</script>";
                simplePage(response, result);
                return true;
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }

        // 글 삭제하기
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardDelete)){
            try {
                var result = "";
                System.out.println("**** 게시글 삭제 시작 ****");
                var sid = Long.parseLong(request.getParameter("pageid"));
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(sid);

                if(notice == null) {
                    result = "<script>alert('do not exist.');location.href='"+noticeBoard+"'</script>";
                    simplePage(response, result);
                    return true;
                }

                var user = (User)request.getAttribute("user");
                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함 실패
                    System.out.println("글 작성자와 현재유저 일치 안함");
                    return false;
                }

                // 글 삭제
                if(!noticeDAO.deleteNotice(sid)){ // 삭제 실패
                    result = "<script>alert('fail');location.href='"+noticeBoard+"'</script>";
                    simplePage(response, result);
                    return true;
                }
                System.out.println("**** 게시글 삭제 끝 ****");

                // *** 파일 업로드 폴더 삭제 ***
                var folderName = notice.getGenTime()+"_"+notice.getSid();
                var uploadPath = getFilePath(request, folderName);
                var folder = new File(uploadPath);
                if(folder.exists()){
                    var files = folder.listFiles();
                    // 모든 파일 삭제
                    for(var file : files){
                        file.delete();
                    }
                    // 마지막으로 폴더 삭제
                    folder.delete();
                }
                // *** 파일 업로드 폴더 삭제 end ***

                result = "<script>alert('success');location.href='"+noticeBoard+"'</script>";
                simplePage(response, result);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 게시글 파일 다운로드
        if(uri.equals(noticeBoardFileDownload)){
            var result = "";
            System.out.println("**** 게시글 다운 시작 ****");
            var sid = Long.parseLong(request.getParameter("pageid"));
            var noticeDAO = new NoticeBoardDAO(getServletContext());
            var notice = noticeDAO.getNotice(sid);

            if(notice == null) {
                result = "<script>alert('do not exist.');location.href='"+noticeBoard+"'</script>";
                simplePage(response, result);
                return true;
            }
            var fileFolder = notice.getGenTime()+"_"+notice.getSid();
            
            var downlink = fileFolder+File.separatorChar+request.getParameter("downlink");
            var filePath = new String(getFilePath(request, downlink).getBytes("UTF-8")); // utf-8로 바꿔준다.

            var file = new File(filePath);
            if(!file.exists()){
                return false;
            }
            if(!file.isFile()){
                return false;
            }

            var filesize = file.length();

            var sMimeType = getServletContext().getMimeType(filePath); // 확장자에 따라 달라진다.
            if(sMimeType == null || sMimeType.length() == 0){
                sMimeType = "application/octet-stream";
            }
            BufferedInputStream fin = null;
            BufferedOutputStream outs = null;
            try {
                var fileName= downlink.split("/")[1];
                byte[] b = new byte[8192];

                response.setContentType(sMimeType+"; charset=utf-8");

                var userAgent = request.getHeader("User-Agent");
                System.out.println(userAgent);
                if(userAgent != null && userAgent.contains("MSIE 5.5")){ // MSIE 5.5 이하
                    return false;
                }else if(userAgent != null && userAgent.contains("MSIE")){ // MS IE
                    return false;
                }else{ // 모질라
                    response.setHeader("Content-Disposition", "attachment; filename="+ new String(fileName.getBytes("utf-8"), "latin1") + ";");
                }

                // 파일 사이즈 정확히 알아야함
                if(filesize > 0){
                    response.setHeader("Content-Length", ""+filesize);
                }
                
                fin = new BufferedInputStream(new FileInputStream(file));
                outs = new BufferedOutputStream(response.getOutputStream());
                int read = 0;
                while((read= fin.read(b)) != -1){
                    outs.write(b, 0, read);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                try{
                    fin.close();
                }catch(Exception e){}
                try{
                    outs.close();
                }catch(Exception e){}
            }
            return true;
        }


        // 파일 삭제
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardFileDelete)){
            var re = "fail";
            try{
                var noticeID = request.getParameter("noticeID");
                var filename = request.getParameter("filename");

                var sid = Long.parseLong(noticeID);
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(sid);

                if(notice == null) {
                    System.out.println("존재하지 않는 게시글");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                var user = (User)request.getAttribute("user");
                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함 실패
                    System.out.println("글 작성자와 현재유저 일치 안함");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                // 파일 존재 여부 확인
                var folderName = notice.getGenTime()+"_"+notice.getSid();
                var filePath = getFilePath(request, folderName+File.separatorChar+filename);
                var file = new File(filePath);
                if(!file.exists()){
                    System.out.println("파일 존재 안함!");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }
                if(!file.isFile()){
                    System.out.println("파일이 아님!");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                // 파일 삭제 하기.
                if(file.delete()){
                    re = "success";
                }
            }catch(Exception e){
                e.printStackTrace();
                re = "fail";
            }
            simplePage(response, "{\"result\":\""+re+"\"}");
            return true;
        }


        if(uri.matches(probURI+"[/_a-zA-Z0-9]*")){
            return (new Problem1(probURI, context, request, response)).connectPage();
        }
        if(uri.matches(sqlProbURI+"[/_a-zA-Z0-9]*")){
            return (new LoginAuthMethods(sqlProbURI, context, request, response)).connectPage();
        }
        return false;
    }
    private String getFilePath(ServletRequest request,String folderName){
        // var path = new File(request.getServletContext().getRealPath("")).getParent() + File.separatorChar+folderName;
        
        // 업로드 폴더 존재 확인 업로드 폴더 존재안하면 만들어주기. 
        // 링크를 통해 접근할 수 없는 위치에 만들어주기.
        var path = new File(request.getServletContext().getRealPath("")).getParentFile().getParent()+File.separatorChar+"upload_folder";
        var folder = new File(path);
        if(!folder.exists() || !folder.isDirectory()){
            folder.mkdir(); // 업로드 폴더 만들기.
        }
        path = path + File.separatorChar + folderName;
        return path;
    }
    private void simplePage(ServletResponse response, String result) throws IOException{
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
