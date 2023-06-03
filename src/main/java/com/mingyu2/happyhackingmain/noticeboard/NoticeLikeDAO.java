package com.mingyu2.happyhackingmain.noticeboard;

import java.sql.Connection;

import com.mingyu2.database.DBConnection;

import jakarta.servlet.ServletContext;

public class NoticeLikeDAO {
    private String tableName = "likes";
    private Connection conn;

    private DBConnection dbConn;
    public NoticeLikeDAO(ServletContext context){
        dbConn = new DBConnection(context, "noticeBoard");
    }
    public long getCount(long noticeSid){
        long likes = 0;
        var query = new StringBuilder();
        query.append("select count(*) from ");
        query.append(tableName);
        query.append(" where notice_board_sid=?");
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, noticeSid);
            var result = pstmt.executeQuery();
            if(result.next()){
                likes = result.getLong(1);
            }
        }catch(Exception e){
            e.printStackTrace();
            likes = 0;
        }finally{
            Close();
        }
        return likes;
    }

    // 사용자 좋아요 했는지 안했는지 판단한다.
    public boolean isUserLike(long userSid,long noticeSid){
        long cnt = 0;
        var query = new StringBuilder();
        query.append("select count(*) from ");
        query.append(tableName);
        query.append(" where user_sid=? and notice_board_sid=?");
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, userSid);
            pstmt.setLong(2, noticeSid);
            var result = pstmt.executeQuery();
            if(result.next()){
                cnt = result.getLong(1);
            }
        }catch(Exception e){
            e.printStackTrace();
            cnt = 0;
        }finally {
            Close();
        }
        return cnt == 1;
    }

    // 사용자 게시글 좋아요 추가하기.
    public void addLike(long userSid, long noticeSid){
        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (user_sid, notice_board_sid) values (?, ?)");
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, userSid);
            pstmt.setLong(2, noticeSid);
            pstmt.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            Close();
        }
    }

    // 사용자 게시글 좋아요 삭제하기.
    public void deleteLike(long userSid,long noticeSid){
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where user_sid=? and notice_board_sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, userSid);
            pstmt.setLong(2, noticeSid);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            Close();
        }
    }

    // 게시글 삭제할 때 좋아요 테이블에서 게시글과 관련된 데이터 모두 지우기.
    public void deleteNoticeLike(long noticeSid){
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where notice_board_sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, noticeSid);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            Close();
        }
    }

    // 사용자가 탈퇴할 때 좋아요 테이블에서 사용자와 관련된 데이터 모두 지우기.
    public void deleteUserLike(long userSid){
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where user_sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, userSid);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            Close();
        }
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
