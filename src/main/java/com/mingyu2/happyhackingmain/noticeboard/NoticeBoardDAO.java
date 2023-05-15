package com.mingyu2.happyhackingmain.noticeboard;

import java.sql.Connection;
import java.util.ArrayList;

import com.mingyu2.database.DBConnection;
import com.mingyu2.login.authentication.database.User;

import jakarta.servlet.ServletContext;

public class NoticeBoardDAO {
    private String tableName = "notice_board";
    private Connection conn;

    private DBConnection dbConn;
    public NoticeBoardDAO(ServletContext context){
        dbConn = new DBConnection(context, "noticeBoard");
    }
    public Notice getNotice(long sid){
        Notice notice = null;
        var query = "select * from notice_board where sid=?";
        
        try{
            Connection();
            var pstmt = conn.prepareStatement(query);
            pstmt.setLong(1,sid);
            var result = pstmt.executeQuery();
            if(result.next()){
                notice = new Notice(
                    result.getLong(1), 
                    result.getLong(2), 
                    result.getString(3), 
                    result.getLong(4),
                    result.getString(5), 
                    result.getString(6)
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
    public ArrayList<Notice> getNoticeList(String question, int start, int number){
        var arr = new ArrayList<Notice>();

        try{
            Connection();
            var query = new StringBuilder();
            query.append("select * from ");
            query.append(tableName);
            query.append(" where title like '%'||?||'%' or main_text like '%'||?||'%'");
            query.append(" order by gen_time desc ");
            // 0번째 부터 5개의 자료만 출력해라.
            query.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");


            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, question);
            pstmt.setString(2, question);
            pstmt.setInt(3, start);
            pstmt.setInt(4, number);
            var result = pstmt.executeQuery();

            var maxLen = 50;
            while(result.next()){
                var mainText = result.getString(6);
                var notice = new Notice(
                    result.getLong(1), 
                    result.getLong(2), 
                    result.getString(3), 
                    result.getLong(4),
                    result.getString(5), 
                    mainText.substring(0, (mainText.length() < maxLen)?mainText.length():maxLen)
                );
                arr.add(notice);
            }
        }catch(Exception e){
        }finally {
            Close();
        }
        
        return arr;
    }

    
    // 게시판 저장
    public boolean saveNotice(User user, String title, String mainText){
        var re = false;

        var sid = genSid();
        var userSid = user.getSid();
        var username = user.getUsername();
        var genTime = System.currentTimeMillis();
        // insert into 
        // notice_board (sid,user_sid,username,gen_time,title,main_text)
        // values (1,1,'mq',0,'제목','본문');

        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (sid,user_sid,username,gen_time,title,main_text) ");
        query.append("values (?,?,?,?,?,?)");
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

            System.out.println("update "+ pstmt.executeUpdate());
            re = true;
        }catch(Exception e){
            System.out.println(e.getMessage());
            re = false;
        }finally{
            Close();
        }
        return re;
    }
    // 삭제
    public boolean deleteNotice(long sid){
        var re = false;
        // delete from notice_board where sid=13;
        var query = "delete from notice_board where sid=?";
        try {
            Connection();
            var pstmt = conn.prepareStatement(query);
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
    public int getCount(String question){
        // var query = "select count(sid) from notice_board";
        var query = new StringBuilder();
        query.append("select count(sid) from ");
        query.append(tableName);
        query.append(" where title like '%'||?||'%' or main_text like '%'||?||'%'");
        int re = 0;
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setString(1, question);
            pstmt.setString(2, question);
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
