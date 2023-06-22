package com.mingyu2.happyhackingmain;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.mingyu2.database.DBConnection;
import com.mingyu2.happyhackingmain.noMemberNoticeBoard.NoMemberNoticeBoardDAO;
import com.mingyu2.happyhackingmain.noMemberNoticeBoard.NoMemberNoticeBoardFileDAO;
import com.mingyu2.happyhackingmain.noticeboard.NoticeBoardDAO;
import com.mingyu2.happyhackingmain.noticeboard.NoticeFileDAO;
import com.mingyu2.happyhackingmain.noticeboard.NoticeLikeDAO;
import com.mingyu2.happyhackingmain.problems.problemauthentication.Problem1;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.LoginAuthMethods;
import com.mingyu2.login.SessionName.UserName;
import com.mingyu2.login.authentication.LoginAuthentication;
import com.mingyu2.login.authentication.database.User;
import com.mingyu2.login.authentication.database.UsersDAO;
import com.mingyu2.login.authentication.searchaddress.AddressDAO;

import jakarta.servlet.ServletException;
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
        DBConnection.addDBINFO(context, "noticeBoard", "/WEB-INF/db-info/main-db-info");
        // test
        // context.setAttribute("shareValue", "안녕 값 공유중이양!");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var uri = request.getRequestURI();

        if(noMemberSearch(request,response)){
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

        if(noMemberSearch(request,response)){
            return;
        }

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
    private boolean noMemberSearch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        var uri = request.getRequestURI();

        var noMemberNoticeBoard = "/no_member_notice_board";
        var noMemberNoticeWrite = "/no_member_notice_board/write";
        var noMemberNoticeSave = "/no_member_notice_board/save";
        var noMemberNoitceDetail = "/no_member_notice_board/notice_detail";
        var noMemberNoitceDelete = "/no_member_notice_board/delete";
        var noMemberNoitceModify = "/no_member_notice_board/modify";
        var noMemberNoitceModifySave = "/no_member_notice_board/modify_save";
        var noMemberNoticeFileDownload = "/no_member_notice_board/file_download";
        var noticeBoardFileDelete = "/no_member_notice_board/file_delete";

        if(uri.equals("/")){
            request.setAttribute("signInPage", "sign_in_page");
            request.setAttribute("noMemberNoticeBoard", noMemberNoticeBoard+"?page=1");
            gotoForwardPage(request, response, "/WEB-INF/no_login/first_page.jsp"); 
            return true;
        }

        // 비회원 문의 게시판 목록
        if(uri.equals(noMemberNoticeBoard)){
            var p = request.getParameter("page");
            var page = 1;
            try{
                page = Integer.parseInt(p);
                if(page < 1){
                    page = 1;
                }
            }catch(Exception e){
                e.printStackTrace();
                page = 1;
            }

            var noMemNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
            int cnt = noMemNoticeBoardDAO.getCount();
            if(cnt == 0){
                cnt = 1;
            }

            // ****** 클라이언트가 수정 가능 *******
            // 한 페이지 보여줄 데이터 갯수 정하기
            int number = 5;
            // ****** 클라이언트가 수정 가능 *******

            // 마지막 페이지
            int lastPage = (cnt/number)+((cnt%number == 0)?0:1);
            // 최대 페이지 수를 초과하면 가장 마지막 페이지로 이동한다.
            if(page > lastPage){
                response.sendRedirect(noMemberNoticeWrite+"?page="+lastPage);
                return true;
            }

            // 데이터 시작 위치
            int start = (page-1)*number;

            // 검색 데이터 가져오기.
            var array = noMemNoticeBoardDAO.getNoticeList(start, number);
            
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

            //***** 정보 넘겨주기 *****
            request.setAttribute("noticeBoard", array);

            // 페이징 번호 리스트
            request.setAttribute("pagination", pagination);

            // 현재 페이지 정보 page
            request.setAttribute("currentPage", page);
            // 마지막 Page
            request.setAttribute("lastPage", lastPage);

            request.setAttribute("noMemberNoitceDetail", noMemberNoitceDetail);

            request.setAttribute("noMemberNoticeWrite",noMemberNoticeWrite);
            request.setAttribute("noMemberNoticeBoard",noMemberNoticeBoard);
            gotoForwardPage(request, response, "/WEB-INF/no_login/notice_board/notice_board.jsp");
            return true;
        }
        // 비회원 문의 게시판 목록 end

        
        // 비회원 문의 게시판 글 쓰기
        if(uri.equals(noMemberNoticeWrite)){
            request.setAttribute("hasPassword",true); // 비회원 글쓰기
            request.setAttribute("noticeBoardURL", noMemberNoticeSave);
            gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_write.jsp");
            return true;
        }
        // 비회원 문의 게시판 글 쓰기 end
        
        // 비회원 문의 게시글 저장
        if(request.getMethod().equals("POST") && uri.equals(noMemberNoticeSave)){
            var charSet = "utf-8";
            request.setCharacterEncoding(charSet);
            if(!request.getContentType().toLowerCase().startsWith("multipart/form-data")){
                //404 에러 발생 
                // application/x-www-form-urlencoded 타입이 아니라.
                // 오직 multipart/form-data 타입만 받는다.
                return false;
            }

            var title = request.getParameter("title");
            var mainText = request.getParameter("main-text");
            var password = request.getParameter("password");
            // 실패
            if(title.equals("")) {
                simplePage(response, "<script>alert('title blink!');window.history.back();</script>");
                return true;
            }
            if(mainText.equals("")){
                simplePage(response, "<script>alert('main text blink!');window.history.back();</script>");
                return true;
            }
            if(password.equals("")){
                simplePage(response, "<script>alert('password blink!');window.history.back();</script>");
                return true;
            }

            var noMemberNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
            var noticeSid = noMemberNoticeBoardDAO.addNotice(title,mainText,password);
            if(noticeSid == -1){
                simplePage(response, "<script>alert('save fail!');window.history.back();</script>");
                return true;
            }

            // 게시글 저장 성공

            // ***** 파일 업로드 시작 *****
            var noticeRe = "no file";
            try{
                // 올바른 파일인지 확인하고 db에 저장한다.
                var parts = request.getParts();
                var fileSidList = new ArrayList<Long>();
                for(var part:parts){
                    if(!part.getHeader("Content-Disposition").contains("filename=")){
                        continue;
                    }
                    if(part.getSubmittedFileName().equals("")){
                        continue;
                    }
                    // 파일 이름에 금지된 문자가 사용되면 파일 업로드 안하기.
                    if(notCorrectFileName(part.getSubmittedFileName())){
                        continue;
                    }

                    if(fileSidList.size() > MAX_FILE_NUM){
                        noticeRe = "file max number";
                        break;
                    }

                    // 데이터 DB 테이블에 저장하기
                    var re = new NoMemberNoticeBoardFileDAO(getServletContext()).saveNoticeFile(noticeSid, part);
                    part.delete(); // 임시파일 삭제

                    if(re != -1){
                        System.out.println("업로드 성공");
                        noticeRe = "file upload success";
                        fileSidList.add(re);
                    }else{
                        System.out.println("파일 업로드 실패");
                        noticeRe = "file upload fail";
                    }
                }

                // 게시글 file list 와 저장된 파일 갯수 저장하기.
                noMemberNoticeBoardDAO.updateFileListAndCnt(fileSidList, noticeSid);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("파일 업로드 실패");
                noticeRe = "file upload fail";
            }
            // ***** 파일 업로드 end *****

            simplePage(response, "<script>alert('save success! "+noticeRe+" ');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
            return true;
        }
        // 비회원 문의 게시글 저장 end

        // 비회원 비번 입력 문의 글 읽기
        if(request.getMethod().equals("POST") && uri.equals(noMemberNoitceDetail)){
            try{
                var sid = Long.parseLong(request.getParameter("detailsid")); // 문의 게시글 sid
                var password = request.getParameter("password"); // 문의 게시글 비밀번호

                // 게시글 정보 가지고 오기.
                var noMemberNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
                var notice = noMemberNoticeBoardDAO.getNoitceBoard(sid, password);

                if(notice == null){
                    simplePage(response, "<script>alert('password diff!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
                    return true;
                }

                var noticeSID = notice.getSid();

                // *** 파일들 읽어 오기 ***
                // 파일 이름 찾기 및 href 링크 만들기
                var fileNameList = new ArrayList<Pair<String,String>>();
                var fileSidList = noMemberNoticeBoardDAO.getFileSidList(noticeSID);

                var noticeFileDAO = new NoMemberNoticeBoardFileDAO(getServletContext());
                for(var fileSid:fileSidList){
                    var fileName = noticeFileDAO.getFileName(fileSid);
                    fileNameList.add(new Pair<String,String>(fileName,fileSid.toString()));
                }

                request.setAttribute("fileNameList", fileNameList);
                request.setAttribute("noticeBoardFileDownload", noMemberNoticeFileDownload);
                // *** 파일들 읽어 오기 end ***

                request.setAttribute("notice", notice);
                request.setAttribute("password",password);
                request.setAttribute("noMemberNoitceDelete", noMemberNoitceDelete);
                request.setAttribute("noMemberNoitceModify", noMemberNoitceModify);
            
                gotoForwardPage(request, response, "/WEB-INF/no_login/notice_board/notice_detail.jsp");
            }catch(Exception e){
                e.printStackTrace();
                simplePage(response, "<script>alert('password diff!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
            }

            return true;
        }
        // 비회원 비번 입력 문의 글 읽기 end

        // 비회원 글 삭제
        if(request.getMethod().equals("POST") && uri.equals(noMemberNoitceDelete)){
            try {
                var sid = Long.parseLong(request.getParameter("pageid"));
                var password = request.getParameter("password");

                var noMemberNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
                if(!noMemberNoticeBoardDAO.noticeDelete(sid, password)){
                    simplePage(response, "<script>alert('delete fail!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                simplePage(response, "<script>alert('delete fail!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
                return true;
            }
            simplePage(response, "<script>alert('delete success!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
            return true;
        }
        // 비회원 글 삭제 end

        // 비회원 글 수정 페이지
        if(request.getMethod().equals("POST") && uri.equals(noMemberNoitceModify)){
            try{
                var sid = Long.parseLong(request.getParameter("pageid")); // 문의 게시글 sid
                var password = request.getParameter("password"); // 문의 게시글 비밀번호
    
                // 게시글 정보 가지고 오기.
                var noMemberNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
                var notice = noMemberNoticeBoardDAO.getNoitceBoard(sid, password);
    
                if(notice == null){
                    simplePage(response, "<script>alert('password diff!');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
                    return true;
                }
    

                // *** 파일들 읽어 오기 ***
                // 파일 이름 찾기 및 href 링크 만들기
                var noticeSID = notice.getSid();
                var fileNameList = new ArrayList<Pair<String,String>>();
                var fileSidList = noMemberNoticeBoardDAO.getFileSidList(noticeSID);

                var noticeFileDAO = new NoMemberNoticeBoardFileDAO(getServletContext());
                for(var fileSid:fileSidList){
                    var fileName = noticeFileDAO.getFileName(fileSid);
                    fileNameList.add(new Pair<String,String>(fileName,fileSid.toString()));
                }

                request.setAttribute("fileNameList", fileNameList);
                request.setAttribute("noticeBoardFileDownload", noMemberNoticeFileDownload);
                request.setAttribute("noticeBoardFileDelete", noticeBoardFileDelete);
                // *** 파일들 읽어 오기 end ***
                
                request.setAttribute("notice", notice);
                request.setAttribute("noticeID", notice.getSid());
                request.setAttribute("prePassword", password);
                request.setAttribute("title", notice.getTitle());
                request.setAttribute("mainText", notice.getMainText());
                request.setAttribute("fileNameList", fileNameList);
                request.setAttribute("noticeBoardURL", noMemberNoitceModifySave);
                request.setAttribute("hasPassword",true);
                request.setAttribute("modifyMode", true);
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            gotoForwardPage(request, response, "/WEB-INF/main_page/notice_board/notice_write.jsp");
            return true;
        }
        // 비회원 글 수정 페이지 end

        // 비회원 글 수정 저장하기
        if(request.getMethod().equals("POST") && uri.equals(noMemberNoitceModifySave)){
            try{
                var title = request.getParameter("title");
                var mainText = request.getParameter("main-text");
                var password = request.getParameter("password");

                var sid = Long.parseLong(request.getParameter("noticeID"));
                var prePassword = request.getParameter("pre-password");
                // 실패
                if(title.equals("")) {
                    simplePage(response, "<script>alert('title blink!');window.history.back();</script>");
                    return true;
                }
                if(mainText.equals("")){
                    simplePage(response, "<script>alert('main text blink!');window.history.back();</script>");
                    return true;
                }
                if(password.equals("")){
                    simplePage(response, "<script>alert('password blink!');window.history.back();</script>");
                    return true;
                }

                // 이전 비밀 번호 검사
                var noMemberNoticeBoardDAO = new NoMemberNoticeBoardDAO(getServletContext());
                var newNoticeId = noMemberNoticeBoardDAO.noticeModifySave(sid,prePassword,title,mainText,password);
                if(newNoticeId == -1){
                    simplePage(response, "<script>alert('modify fail!');window.history.back();</script>");
                    return true;
                }
                // ***** 파일 업로드 시작 *****
                var noticeRe = "no file";
                try{
                    // 올바른 파일인지 확인하고 db에 저장한다.
                    var parts = request.getParts();
                    var fileSidList = noMemberNoticeBoardDAO.getFileSidList(newNoticeId);
                    for(var part:parts){
                        if(!part.getHeader("Content-Disposition").contains("filename=")){
                            continue;
                        }
                        if(part.getSubmittedFileName().equals("")){
                            continue;
                        }
                        // 파일 이름에 금지된 문자가 사용되면 파일 업로드 안하기.
                        if(notCorrectFileName(part.getSubmittedFileName())){
                            continue;
                        }

                        if(fileSidList.size() > MAX_FILE_NUM){
                            noticeRe = "file max number";
                            break;
                        }

                        // 데이터 DB 테이블에 저장하기
                        var re = new NoMemberNoticeBoardFileDAO(getServletContext()).saveNoticeFile(newNoticeId, part);
                        part.delete(); // 임시파일 삭제

                        if(re != -1){
                            System.out.println("업로드 성공");
                            noticeRe = "file upload success";
                            fileSidList.add(re);
                        }else{
                            System.out.println("파일 업로드 실패");
                            noticeRe = "file upload fail";
                        }
                    }

                    // 게시글 file list 와 저장된 파일 갯수 저장하기.
                    noMemberNoticeBoardDAO.updateFileListAndCnt(fileSidList, newNoticeId);
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println("파일 업로드 실패");
                    noticeRe = "file upload fail";
                }
                // ***** 파일 업로드 end *****
                simplePage(response, "<script>alert('modify success!"+noticeRe+"');location.href='"+noMemberNoticeBoard+"?page=1"+"';</script>");
            }catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }
        // 비회원 글 수정 저장하기 end

        // 비회원 글 첨부파일 다운로드
        if(uri.equals(noMemberNoticeFileDownload)){
            long pageSid;
            long fileSid;
            try {
                pageSid = Long.parseLong(request.getParameter("pageid"));
                fileSid = Long.parseLong(request.getParameter("downlink"));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            var password = request.getParameter("password");
            var noticeDAO = new NoMemberNoticeBoardDAO(getServletContext());
            var notice = noticeDAO.getNoitceBoard(pageSid,password);
            if(notice == null) {
                return false;
            }

            var fileDAO = new NoMemberNoticeBoardFileDAO(getServletContext());
            var is = fileDAO.getFile(fileSid);
            if(is == null){
                return false;
            }
            var fileSize = is.available();
            var fileName = fileDAO.getFileName(fileSid);
            // 사용자에게 다운로드 보내기
            var sMimeType = getServletContext().getMimeType(fileName);
            if(sMimeType == null || sMimeType.length() == 0){
                sMimeType = "application/octet-stream";
            }

            BufferedOutputStream outs = null;
            try {
                response.setContentType(sMimeType+"; charset=utf-8");
                if(fileSize > 0){
                    response.setContentLength(fileSize);
                }

                var userAgent = request.getHeader("User-Agent");
                System.out.println(userAgent);
                if(userAgent != null && userAgent.contains("MSIE 5.5")){ // MSIE 5.5 이하
                    return false;
                }else if(userAgent != null && userAgent.contains("MSIE")){ // MS IE
                    return false;
                }else{ // 모질라
                    response.setHeader("Content-Disposition", "attachment; filename="+ new String(fileName.getBytes("utf-8"), "latin1") + ";");
                }

                // 사용자에게 파일을 전송한다.
                outs = new BufferedOutputStream(response.getOutputStream());
                int read = -1;
                byte[] b = new byte[8192];
                while((read = is.read(b)) != -1){
                    outs.write(b, 0, read);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    outs.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                // 다운로드 완료 된후 DB 연결을 해제해야한다.
                fileDAO.Close();
            }

            System.out.println("사용자 파일 다운로드 "+ pageSid + "  "+ fileSize);
            return true;
        }
        // 비회원 글 첨부파일 다운로드 end
        // 파일 삭제
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardFileDelete)){
            var re = "fail";
            try{
                var noticeSid = Long.parseLong(request.getParameter("noticeID"));
                var fileSid = Long.parseLong(request.getParameter("filename"));
                var password = request.getParameter("password");
                
                var noticeDAO = new NoMemberNoticeBoardDAO(getServletContext());
                
                var notice = noticeDAO.getNoitceBoard(noticeSid, password);
                if(notice == null){ // 비번 틀려서 다운 못받음
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                var fileDAO = new NoMemberNoticeBoardFileDAO(getServletContext());
                if(!fileDAO.deleteFile(fileSid)){
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }
                re = "success";
                var fileSidList = noticeDAO.getFileSidList(noticeSid);
                fileSidList.remove(fileSid);
                noticeDAO.updateFileListAndCnt(fileSidList, noticeSid);
                System.out.println("삭제 완료!");
            }catch(Exception e){
                e.printStackTrace();
                re = "fail";
            }
            simplePage(response, "{\"result\":\""+re+"\"}");
            return true;
        }
        // 파일 삭제 end

        return false;
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
        var noticeBoardLikes = "/main_page/notice_board/likes";

        // 마이페이지
        var myPage = "/main_page/my_page";
        var myPageChangeInfo = "/main_page/my_page/change_info";
        var findAddress = "/main_page/my_page/find_address_number";
        var changeUserInfo = "/main_page/my_page/change_user_info";
        var changePWD = "/main_page/my_page/change_user_pwd";
        
        if(uri.matches("/main_page[/]?")){
            // 메인페이지를 보여준다.
            request.setAttribute("authProblem1", probURI);
            request.setAttribute("loginMethods", sqlProbURI);

            // 메뉴
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("",true)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>(noticeBoard+baseNoticeBoardParameter(1,1,"","","",1), false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));

            request.setAttribute("menus", menus);

            return mainPage(request, response, 1);
        }

        // 마이페이지
        if(uri.equals(myPage)){
            // 메뉴들
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("/main_page",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>(noticeBoard+baseNoticeBoardParameter(1,1,"","","",1), false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            request.setAttribute("menus", menus);
            
            return mainPage(request, response, 0);
        }

        // 마이페이지 사용자 정보 변경 페이지
        if(uri.equals(myPageChangeInfo)){
            // 메뉴들
            var menus = new ArrayList<Pair<String,Pair<String,Boolean>>>();
            menus.add(new Pair<String,Pair<String,Boolean>>("Home",new Pair<String,Boolean>("/main_page",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("게시판",new Pair<String,Boolean>(noticeBoard+baseNoticeBoardParameter(1,1,"","","",1), false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            menus.add(new Pair<String,Pair<String,Boolean>>("버튼2",new Pair<String,Boolean>("",false)));
            request.setAttribute("menus", menus);

            request.setAttribute("signUpFindAddressNum", findAddress);
            
            return mainPage(request, response, 3);
        }

        // 우편번호 찾기
        if(uri.equals(findAddress)){
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
            return true;
        }

        // 사용자 정보 변경 요청을 처리
        if(request.getMethod().equals("POST") && uri.equals(changeUserInfo)){
            var email = request.getParameter("email");
            var addressNumber = request.getParameter("address-number");
            var address = request.getParameter("address");
            var addressSub = request.getParameter("address-sub");

            var user = (User)request.getAttribute("user");
            var isOK = new UsersDAO(getServletContext()).updateUserInfo(user.getSid(), email, addressNumber, address, addressSub);

            System.out.println("요청옴 "+email+"  "+addressNumber+"  "+address+"  "+addressSub+"  "+ user.getSid());

            var result = "";
            if(isOK){
                result = "<script>alert('change success!');location.href='"+myPage+"'</script>";
            }else{
                result = "<script>alert('change fail!');location.href='"+myPage+"'</script>";
            }
            simplePage(response, result);
            return true;
        }

        // 비밀번호 변경 요청을 처리
        if(request.getMethod().equals("POST") && uri.equals(changePWD)){
            var result = "";
            var pwd = request.getParameter("pwd");
            var newPwd = request.getParameter("new-pwd");
            var user = (User)request.getAttribute("user");

            var isOK = new UsersDAO(getServletContext()).updatePWD(user, pwd, newPwd);

            
            System.out.println("비번 변경 요청 : "+pwd+"   "+newPwd);
            if(isOK){
                result = "<script>alert('password change success!');location.href='/user-logout'</script>";
            }else{
                result = "<script>alert('password change fail!');location.href='/user-logout'</script>";
            }
            simplePage(response, result);
            return true;
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

            // 검색 옵션 option_val
            // 1 제목+내용
            // 2 작성자
            // 3 제목
            // 4 내용
            var ov = request.getParameter("option_val");
            var optionVal = 1;
            try {
                optionVal = Integer.parseInt(ov);
            }catch(Exception e){
                e.printStackTrace();
                optionVal = 1;
            }
            if(optionVal > 4 || optionVal < 1){
                optionVal = 1;
            }

            // *** 검색 날짜 범위 ***
            var dateFrom = request.getParameter("date_from");
            var dateTo = request.getParameter("date_to");
            if(dateFrom == null || dateTo == null){
                dateFrom = "";
                dateTo = "";
            }else if(dateFrom.equals("") || dateTo.equals("")){
                dateFrom = "";
                dateTo = "";
            }
            // 날짜 유효한지 검사
            var re = "(19|20)\\d{2}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";
            if(!dateFrom.matches(re) || !dateTo.matches(re)){
                dateFrom = "1980-01-01";
                dateTo = "2999-12-31";
            }

            var obj = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            obj.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long from = 0;
            long to = 0;
            // long type 변환
            try{
                from = obj.parse(dateFrom+" 00:0").getTime();
                to = obj.parse(dateTo+" 23:59").getTime();
            }catch(Exception e){
                from = 0;
                to = Long.MAX_VALUE;
                e.printStackTrace();
            }
            System.out.println(dateFrom+" ~ "+dateTo);
            System.out.println("검색 날짜 : "+from + " ~ "+to + " "+System.currentTimeMillis());
            // *** 검색 날짜 범위 END ***

            // *** 게시글 정렬 방법 ****
            // 0. 조회수 정렬
            // 1. 날짜순
            // 2. 제목 순
            // 3. 작성자 순
            // 4. 좋아요 순
            var orderBy = 1;
            try{
                orderBy = Integer.parseInt(request.getParameter("order_by"));
            }catch(Exception e){
                e.printStackTrace();
                orderBy = 1;
            }
            if(orderBy < 0 || orderBy > 4){
                orderBy = 1;
            }
            request.setAttribute("orderBy", orderBy);
            System.out.println("정렬 방법 : "+orderBy);
            // *** 게시글 정렬 방법 END ****
 
            // 현재 페이지
            var p = request.getParameter("page");

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
            int cnt = noticeBoardDAO.getCount(optionVal,q, from, to);
            System.out.println("검색 개시글 수 : "+cnt);

            if(cnt == 0){
                // 검색된 글 수
                request.setAttribute("countNotices", 0);
                cnt = 1;
            }else{
                // 검색된 글 수
                request.setAttribute("countNotices", cnt);
            }


            // ****** 클라이언트가 수정 가능 *******
            // 한 페이지 보여줄 데이터 갯수 정하기
            int number = 5;
            // ****** 클라이언트가 수정 가능 *******

            // 마지막 페이지
            int lastPage = (cnt/number)+((cnt%number == 0)?0:1);
            // 최대 페이지 수를 초과하면 가장 마지막 페이지로 이동한다.
            if(page > lastPage){
                // response.sendRedirect(noticeBoard+"?page="+lastPage+"&q="+q);
                response.sendRedirect(noticeBoard+baseNoticeBoardParameter(1,lastPage,q,"","",orderBy));
                return true;
            }
            // 데이터 시작 위치
            int start = (page-1)*number;
            

            // 검색 데이터 가져오기.
            var array = noticeBoardDAO.getNoticeList(optionVal,q,start,number, from, to, orderBy);


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
            // request.setAttribute("countNotices", cnt);
            // 검색 옵션 값 jsp 로 보내기
            request.setAttribute("optionVal", optionVal);
            // date from and date to
            request.setAttribute("dateFrom", dateFrom);
            request.setAttribute("dateTo", dateTo);

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
        // 새로운 글 저장
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
                // 올바른 파일인지 확인하고 db에 저장한다.
                var parts = request.getParts();
                var fileSidList = new ArrayList<Long>();
                for(var part:parts){
                    if(!part.getHeader("Content-Disposition").contains("filename=")){
                        continue;
                    }
                    if(part.getSubmittedFileName().equals("")){
                        continue;
                    }
                    // 파일 이름에 금지된 문자가 사용되면 파일 업로드 안하기.
                    if(notCorrectFileName(part.getSubmittedFileName())){
                        continue;
                    }

                    if(fileSidList.size() > MAX_FILE_NUM){
                        noticeRe = "file max number";
                        break;
                    }

                    // 데이터 DB 테이블에 저장하기
                    var re = new NoticeFileDAO(getServletContext()).saveNoticeFile(user.getSid(), noticeSid, part);
                    part.delete(); // 임시파일 삭제

                    if(re != -1){
                        System.out.println("업로드 성공");
                        noticeRe = "file upload success";

                        fileSidList.add(re);
                    }else{
                        System.out.println("파일 업로드 실패");
                        noticeRe = "file upload fail";
                    }
                }

                // 게시글 file list 와 저장된 파일 갯수 저장하기.
                notice.updateFileListAndCnt(fileSidList, noticeSid);
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("파일 업로드 실패");
                noticeRe = "file upload fail";
            }
            // ***** 파일 업로드 end *****

            // result = "<script>alert('success "+noticeRe+"');location.href='"+noticeBoard+"?page=1&q='</script>";
            result = "<script>alert('success "+noticeRe+"');location.href='"+noticeBoard+baseNoticeBoardParameter(1,1,"","","",1)+"'</script>";
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
                // 파일 이름 찾기 및 href 링크 만들기
                var fileNameList = new ArrayList<Pair<String,String>>();
                var fileSidList = noticeDAO.getFileSidList(noticeSID);

                var noticeFileDAO = new NoticeFileDAO(getServletContext());
                for(var fileSid:fileSidList){
                    var fileName = noticeFileDAO.getFileName(fileSid);
                    fileNameList.add(new Pair<String,String>(fileName,fileSid.toString()));
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
                // *** 파일들 읽어 오기 ***
                // 파일 이름 찾기 및 href 링크 만들기
                var fileNameList = new ArrayList<Pair<String,String>>();
                var fileSidList = noticeDAO.getFileSidList(noticeSID);

                var noticeFileDAO = new NoticeFileDAO(getServletContext());
                for(var fileSid:fileSidList){
                    var fileName = noticeFileDAO.getFileName(fileSid);
                    fileNameList.add(new Pair<String,String>(fileName,fileSid.toString()));
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

                // 1. 전 저장위치
                var noticeSID = notice.getSid();
                
                // 2. 새로운 글 저장
                var genTime = System.currentTimeMillis();
                var newNoticeSid = noticeDAO.saveNotice(user, title, mainText, genTime);
                if(newNoticeSid==0){ // 실패
                    result = "<script>alert('fail');location.href='"+noticeBoard+"'</script>";
                    simplePage(response, result);
                    return true;
                }

                // 3. 이전 게시글 파일 새로운 글로 이전
                // notice_board_sid 새로운 글 sid로 바꾸기.
                var fileSidList = noticeDAO.getFileSidList(noticeSID);
                noticeDAO.updateFileListAndCnt(fileSidList, newNoticeSid);

                var fileDAO = new NoticeFileDAO(getServletContext());
                for(var fileSid:fileSidList){
                    fileDAO.changeNoticeSid(fileSid, newNoticeSid);
                }

                // 4. 이전 글 삭제
                if(!noticeDAO.deleteNotice(sid)){ // 삭제 실패
                    return false;
                }
                
                // 5. ***** 파일 업로드 시작 *****
                var noticeRe = "no new file";
                try{
                    // 올바른 파일인지 확인하고 db에 저장한다.
                    var parts = request.getParts();
                    for(var part:parts){
                        if(!part.getHeader("Content-Disposition").contains("filename=")){
                            continue;
                        }
                        if(part.getSubmittedFileName().equals("")){
                            continue;
                        }
                        // 파일 이름에 금지된 문자가 사용되면 파일 업로드 안하기.
                        if(notCorrectFileName(part.getSubmittedFileName())){
                            continue;
                        }

                        if(fileSidList.size() > MAX_FILE_NUM){
                            noticeRe = "file max number";
                            break;
                        }

                        // 데이터 DB 테이블에 저장하기
                        var re = new NoticeFileDAO(getServletContext()).saveNoticeFile(user.getSid(), newNoticeSid, part);
                        part.delete(); // 임시파일 삭제

                        if(re != -1){
                            System.out.println("업로드 성공");
                            noticeRe = "file upload success";

                            fileSidList.add(re);
                        }else{
                            System.out.println("파일 업로드 실패");
                            noticeRe = "new file upload fail";
                        }
                    }

                    // 게시글 file list 와 저장된 파일 갯수 저장하기.
                    noticeDAO.updateFileListAndCnt(fileSidList, newNoticeSid);
                }catch(Exception e){
                    e.printStackTrace();
                    System.out.println("파일 업로드 실패");
                    noticeRe = "new file upload fail";
                }
                // ***** 파일 업로드 end *****
                
                // 성공
                result = "<script>alert('success : "+noticeRe+"');location.href='"+noticeBoard+baseNoticeBoardParameter(1, 1, "","","",1)+"'</script>";
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

                result = "<script>alert('success');location.href='"+noticeBoard+"'</script>";
                simplePage(response, result);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 게시글 파일 다운로드
        if(uri.equals(noticeBoardFileDownload)){
            long pageSid;
            long fileSid;
            try {
                pageSid = Long.parseLong(request.getParameter("pageid"));
                fileSid = Long.parseLong(request.getParameter("downlink"));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            var noticeDAO = new NoticeBoardDAO(getServletContext());
            var notice = noticeDAO.getNotice(pageSid);
            if(notice == null) {
                return false;
            }

            var fileDAO = new NoticeFileDAO(getServletContext());
            var is = fileDAO.getFile(fileSid);
            if(is == null){
                return false;
            }
            var fileSize = is.available();
            var fileName = fileDAO.getFileName(fileSid);
            
            // 사용자에게 다운로드 보내기
            var sMimeType = getServletContext().getMimeType(fileName);
            if(sMimeType == null || sMimeType.length() == 0){
                sMimeType = "application/octet-stream";
            }

            BufferedOutputStream outs = null;
            try {
                response.setContentType(sMimeType+"; charset=utf-8");
                if(fileSize > 0){
                    response.setContentLength(fileSize);
                }

                var userAgent = request.getHeader("User-Agent");
                System.out.println(userAgent);
                if(userAgent != null && userAgent.contains("MSIE 5.5")){ // MSIE 5.5 이하
                    return false;
                }else if(userAgent != null && userAgent.contains("MSIE")){ // MS IE
                    return false;
                }else{ // 모질라
                    response.setHeader("Content-Disposition", "attachment; filename="+ new String(fileName.getBytes("utf-8"), "latin1") + ";");
                }

                // 사용자에게 파일을 전송한다.
                outs = new BufferedOutputStream(response.getOutputStream());
                int read = -1;
                byte[] b = new byte[8192];
                while((read = is.read(b)) != -1){
                    outs.write(b, 0, read);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                try {
                    outs.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
                // 다운로드 완료 된후 DB 연결을 해제해야한다.
                fileDAO.Close();
            }

            System.out.println("사용자 파일 다운로드 "+ pageSid + "  "+ fileSize);
            return true;
        }


        // 파일 삭제
        if(request.getMethod().equals("GET") && uri.equals(noticeBoardFileDelete)){
            var re = "fail";
            try{
                var noticeSid = Long.parseLong(request.getParameter("noticeID"));
                var fileSid = Long.parseLong(request.getParameter("filename"));

                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(noticeSid);

                // 게시글 존재하는지 확인하기
                if(notice == null) {
                    System.out.println("존재하지 않는 게시글");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }
                // 글 작성자와 현재 유저가 일지하는지 확인
                var user = (User)request.getAttribute("user");
                if(user.getSid() != notice.getUserSID()){ // 글 작성자와 현재 유저와 일치안함 실패
                    System.out.println("글 작성자와 현재유저 일치 안함");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                // 업로드 파일 사용자 확인
                var fileDAO = new NoticeFileDAO(getServletContext());
                if(user.getSid() != fileDAO.getUploadUser(fileSid)){
                    System.out.println("글 작성자와 파일 업로드 사용자 명과 일치 안함");
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }

                // 파일 삭제
                if(!fileDAO.deleteFile(fileSid)){
                    simplePage(response, "{\"result\":\"fail\"}");
                    return true;
                }
                // 파일 삭제 end
                re = "success";

                var fileSidList = noticeDAO.getFileSidList(noticeSid);
                fileSidList.remove(fileSid);
                noticeDAO.updateFileListAndCnt(fileSidList, noticeSid);
                System.out.println("삭제 완료!");
            }catch(Exception e){
                e.printStackTrace();
                re = "fail";
            }
            simplePage(response, "{\"result\":\""+re+"\"}");
            return true;
        }

        // 좋아요 설정 및 해제
        if(request.getMethod().equals("POST") && uri.equals(noticeBoardLikes)){
            var re = true;
            long cnt = 0;

            // true : 좋아요 한 다음 갯수 알아보기.
            // false : 좋아요 취소한 다음 갯수 알아보기.
            // confirm : 오직 좋아요 갯수만 알아보기.
            var method = request.getParameter("like");
            var noticeId = request.getParameter("pageid");

            // like 검사, false 좋아요 해제
            if(!(method.equals("true") || method.equals("false") || method.equals("confirm"))){
                re = false;
                cnt = 0;
                simplePage(response, "{\"result\":\""+re+"\", \"likecount\":\""+cnt+"\"}");
                return true;
            }

            // 존재하는 게시글인지 확인하기 아니면 fail! false 좋아요 해제
            long noticeSid = 0;
            try{
                noticeSid = Long.parseLong(noticeId);
                var noticeDAO = new NoticeBoardDAO(getServletContext());
                var notice = noticeDAO.getNotice(noticeSid);
                if(notice == null){
                    re = false;
                    cnt = 0;
                    simplePage(response, "{\"result\":\""+re+"\", \"likecount\":\""+cnt+"\"}");
                    return true;
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            // 사용자 정보 가지고 오기
            var user = (User)request.getAttribute("user");
            
            var noticeLikeDAO = new NoticeLikeDAO(getServletContext());

            // 1. method 에 따른 계산 시작! 좋아요 증가 or 좋아요 삭제 등등
            if(method.equals("true")){ // 좋아요 추가
                noticeLikeDAO.addLike(user.getSid(), noticeSid);
            }else if(method.equals("false")) { // 좋아요 삭제
                noticeLikeDAO.deleteLike(user.getSid(), noticeSid);
            } //method 가 confirm 일 때는 pass

            // 2. 사용자 좋아요 싫어요 여부 확인
            var isLikeUser = noticeLikeDAO.isUserLike(user.getSid(), noticeSid);
            re = isLikeUser;

            // 3. 게시글 좋아요 갯수 카운트
            cnt = noticeLikeDAO.getCount(noticeSid);

            System.out.println("좋아용 "+method+" "+noticeId+"  like count : "+cnt);
            simplePage(response, "{\"result\":\""+re+"\", \"likecount\":\""+cnt+"\"}");
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
    // true : dont save file in server
    // false : save file in server
    private boolean notCorrectFileName(String fileNmae){
        var regex = "[/\\\\:*?<>|\"%]+";
        var pattern = Pattern.compile(regex);
        return pattern.matcher(fileNmae).find();
    }
    private String baseNoticeBoardParameter(int optionVal,int page,String q, String dateFrom, String dateTo, int orderBy){
        // TODO 수정
        return "?option_val="+optionVal+"&page="+page+"&q="+q+"&date_from="+dateFrom+"&date_to="+dateTo+"&order_by="+orderBy;
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
