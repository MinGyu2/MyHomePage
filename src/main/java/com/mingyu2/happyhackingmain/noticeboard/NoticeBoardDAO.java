package com.mingyu2.happyhackingmain.noticeboard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.mingyu2.database.DBConnection;
import com.mingyu2.login.authentication.database.User;

import jakarta.servlet.ServletContext;

public class NoticeBoardDAO {
    private String tableName = "notice_board";
    private String likeTableName = "likes";
    private Connection conn;

    private DBConnection dbConn;
    private ServletContext context;
    public NoticeBoardDAO(ServletContext context){
        this.context = context;
        dbConn = new DBConnection(context, "noticeBoard");
    }
    public Notice getNotice(long sid){
        Notice notice = null;
        var query = new StringBuilder();
        query.append("select * from ");
        query.append(tableName);
        query.append(" where sid=?");
        
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1,sid);
            var result = pstmt.executeQuery();
            if(result.next()){
                var noticeSid = result.getLong(1);
                long likes = new NoticeLikeDAO(context).getCount(noticeSid);
                notice = new Notice(
                    noticeSid, 
                    result.getLong(2), 
                    result.getString(3), 
                    result.getLong(4),
                    result.getString(5), 
                    result.getString(6),
                    result.getLong(7),
                    likes
                );
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally {
            Close();
        }
        return notice;
    }

    // 전체 목록 가져오기 or 검색 목록 가져오기
    // 1 <= optionVal <= 4
    // 1 제목 + 내용
    // 2 작성자
    // 3 제목
    // 4 내용
    // 날짜 범위
    public ArrayList<Notice> getNoticeList(int optionVal,String question, int start, int number, long from, long to, int orderBy){
        if(optionVal > 4 || optionVal < 1){
            optionVal = 1;
        }
        if(orderBy > 4 || orderBy < 0){
            orderBy = 1;
        }
        
        var arr = new ArrayList<Notice>();

        try{
            Connection();
            var query = new StringBuilder();
            
            // 정렬 방법 : 4 
            if(orderBy == 4){
                query.append("select sid, user_sid, username, gen_time,title,main_text,views,coalesce(b.cnt,0) as count from ");
                query.append(tableName);
                query.append(" left join ");
                query.append("(select notice_board_sid,count(notice_board_sid) as cnt from ");
                query.append(likeTableName);
                query.append(" group by notice_board_sid) b on sid = notice_board_sid");
            }else{
                // 정렬 방법 : 1,2,3
                query.append("select * from ");
                query.append(tableName);
            }

            if(optionVal == 1){
                query.append(" where (title like '%'||?||'%' or main_text like '%'||?||'%')");
            }else if(optionVal == 2){
                query.append(" where (username like '%'||?||'%')");
            }else if(optionVal == 3){
                query.append(" where (title like '%'||?||'%')");
            }else if(optionVal == 4){
                query.append(" where (main_text like '%'||?||'%')");
            }
            // 게시글 범위
            query.append(" and (gen_time > ? and gen_time < ?)");
            
            if(orderBy == 0){
                // 0. 조회수 정렬
                query.append(" order by views desc, gen_time desc ");
            }else if(orderBy == 1){
                // 1. 생성일 순
                query.append(" order by gen_time desc ");
            }else if(orderBy == 2){
                // 2. 제목 순
                query.append(" order by title,gen_time desc ");
            }else if(orderBy == 3){
                // 3. 작성자 순
                query.append(" order by username, gen_time desc ");
            }else if(orderBy == 4){
                // 4. 좋아요 순
                query.append(" order by count desc, gen_time desc ");
            }

            
            // 0번째 부터 5개의 자료만 출력해라.
            query.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");


            var pstmt = conn.prepareStatement(query.toString());
            if(optionVal == 1){
                pstmt.setString(1, question);
                pstmt.setString(2, question);
                pstmt.setLong(3, from);
                pstmt.setLong(4, to);
                pstmt.setInt(5, start);
                pstmt.setInt(6, number);
            }else{
                pstmt.setString(1, question);
                pstmt.setLong(2, from);
                pstmt.setLong(3, to);
                pstmt.setInt(4, start);
                pstmt.setInt(5, number);
            }
            
            var result = pstmt.executeQuery();

            System.out.println("no?");
            var maxLen = 50;
            while(result.next()){
                var noticeSid = result.getLong(1);
                long likes = new NoticeLikeDAO(context).getCount(noticeSid);

                var mainText = result.getString(6);
                var notice = new Notice(
                    noticeSid, 
                    result.getLong(2), 
                    result.getString(3), 
                    result.getLong(4),
                    result.getString(5), 
                    mainText.substring(0, (mainText.length() < maxLen)?mainText.length():maxLen),
                    result.getLong(7),
                    likes
                );
                arr.add(notice);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            Close();
        }
        
        return arr;
    }

    
    // 게시판 저장
    public long saveNotice(User user, String title, String mainText, long genTime){ // 0 이면 false
        long re = 0;

        var sid = genSid();
        var userSid = user.getSid();
        var username = user.getUsername();
        // var genTime = System.currentTimeMillis();
        // insert into 
        // notice_board (sid,user_sid,username,gen_time,title,main_text)
        // values (1,1,'mq',0,'제목','본문');

        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (sid,user_sid,username,gen_time,title,main_text,views) ");
        query.append("values (?,?,?,?,?,?,?)");
        System.out.println("저장전 " + query.toString());

        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, sid);
            pstmt.setLong(2, userSid);
            pstmt.setString(3, username);
            pstmt.setLong(4, genTime);
            pstmt.setString(5, title);
            pstmt.setString(6, mainText);
            pstmt.setLong(7, 0);
            System.out.println("update "+ pstmt.executeUpdate());
            re = sid;
        }catch(Exception e){
            System.out.println(e.getMessage());
            re = 0;
        }finally{
            Close();
        }
        return re;
    }
    // 삭제
    public boolean deleteNotice(long sid){
        // 좋아요 테이블에서 게시글과 관련된 좋아요 기록 모두 삭제
        new NoticeLikeDAO(context).deleteNoticeLike(sid);

        // 업로드 파일 부터 삭제한다.
        new NoticeFileDAO(context).deleteNoticeFile(sid);

        var re = false;
        // delete from notice_board where sid=13;
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1,sid);
            pstmt.executeUpdate();
            System.out.println("삭제 완료");
            re = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Close();
        }
        return re;
    }

    // 게시판 목록 수 가져오기
    // 1 <= optionVal <= 4
    // 1 제목 + 내용
    // 2 작성자
    // 3 제목
    // 4 내용
    public int getCount(int optionVal, String question, long from, long to){
        if(optionVal > 4 || optionVal < 1){
            optionVal = 1;
        }
        // var query = "select count(sid) from notice_board";
        var query = new StringBuilder();
        query.append("select count(sid) from ");
        query.append(tableName);
        if(optionVal == 1){
            query.append(" where (title like '%'||?||'%' or main_text like '%'||?||'%')");
        }else if(optionVal == 2){
            query.append(" where (username like '%'||?||'%')");
        }else if(optionVal == 3){
            query.append(" where (title like '%'||?||'%')");
        }else if(optionVal == 4){
            query.append(" where (main_text like '%'||?||'%')");
        }
        query.append("  and (gen_time > ? and gen_time < ?)");
        int re = 0;
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, question);
            var cnt = 2;
            if(optionVal == 1){
                pstmt.setString(cnt, question);
                cnt+=1;
            }
            pstmt.setLong(cnt, from);
            cnt +=1;
            pstmt.setLong(cnt, to);

            var result = pstmt.executeQuery();
            if(result.next()){
                re = result.getInt(1);
            }
        }catch(Exception e){
            e.printStackTrace();
            re = 0;
        }finally{
            Close();
        }
        return re;
    }

    // 조회수 1 카운트 증가시키기
    public long incressViews(long sid, long currentViews){
        var query = new StringBuilder();
        query.append("update ");
        query.append(tableName);
        query.append(" set views = ");
        query.append(currentViews+1);
        query.append(" where sid=");
        query.append(sid);
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            var re = pstmt.executeUpdate();
            System.out.println(sid+" 글 조회수 업데이트 : "+re);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            Close();
        }
        return sid;
    }

    // file list 저장 및 저장된 파일 수 저장
    public boolean updateFileListAndCnt(ArrayList<Long> fileSidList, long noticeSid){
        var re = false;
        var query = new StringBuilder();
        query.append("update ");
        query.append(tableName);
        query.append(" set file_cnt=?,file_sids=? where sid=?");

        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1,fileSidList.size());
            pstmt.setString(2, fileSidList.toString());
            pstmt.setLong(3, noticeSid);
            pstmt.executeUpdate();
            re = true;
        } catch (Exception e) {
            e.printStackTrace();
            re = false;
        } finally { 
            Close();
        }

        return re;
    }


    // get file sid list
    public ArrayList<Long> getFileSidList(long noticeSid){
        ArrayList<Long> fileSidList = null;
        var query = new StringBuilder();
        query.append("select file_sids from ");
        query.append(tableName);
        query.append(" where sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, noticeSid);
            var re = pstmt.executeQuery();

            String listStr = "[]";
            if(re.next()){
                listStr = re.getString(1);
            }
            fileSidList = strToArrayList(listStr);
        } catch (Exception e) {
            e.printStackTrace();
            fileSidList = new ArrayList<Long>();
        } finally {
            Close();
        }
        return fileSidList;
    }

    private ArrayList<Long> strToArrayList(String listStr){
        var arr = new ArrayList<Long>();
        if(listStr.length() > 2){
            arr.addAll(Arrays.stream(listStr.substring(1,listStr.length()-1).split(", ")).map(Long::parseLong).collect(Collectors.toList()));
        }
        return arr;
    }

    private long genSid(){
        long sid = -2;
        Connection();
        try {
            var query = new StringBuilder();
            query.append("select coalesce(max(sid),-1) from ");
            query.append(tableName);
            var pstmt = conn.prepareStatement(query.toString());
            var result = pstmt.executeQuery();
            if(result.next()){
                sid = result.getLong(1);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            Close();
        }
        return sid+1;
    }
    private void Connection(){
        conn = dbConn.connectDB();
    }
    private void Close(){
        try{
            conn.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
