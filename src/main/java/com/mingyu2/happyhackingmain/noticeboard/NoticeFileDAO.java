package com.mingyu2.happyhackingmain.noticeboard;

import java.io.InputStream;
import java.sql.Connection;

import com.mingyu2.database.DBConnection;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

public class NoticeFileDAO {
    private String tableName = "notice_file";
    private Connection conn;

    private DBConnection dbConn;
    public NoticeFileDAO(ServletContext context){
        dbConn = new DBConnection(context, "noticeBoard");
    }

    public long saveNoticeFile(long userSid, long noticeBoardSid, Part part){
        long re = -1;
        var sid = genSid();

        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (sid, user_sid, notice_board_sid, file_name, file_content) values(?,?,?,?,?)");

        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, sid);
            pstmt.setLong(2, userSid);
            pstmt.setLong(3, noticeBoardSid);
            pstmt.setString(4, part.getSubmittedFileName());
            pstmt.setBinaryStream(5, part.getInputStream(), part.getSize());
            // pstmt.setBlob(5, part.getInputStream());
            pstmt.executeUpdate();
            
            re = sid;
        } catch (Exception e) {
            e.printStackTrace();
            re = -1;
        } finally {
            Close();
        }
        return re;
    }

    // 게시글 에 속한 파일 삭제하기.
    public void deleteNoticeFile(long noticeSid){
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

    public String getFileName(long sid){
        String fileName = "none";
        var query = new StringBuilder();
        query.append("select file_name from ");
        query.append(tableName);
        query.append(" where sid=?");

        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, sid);
            var re = pstmt.executeQuery();
            if(re.next()){
                fileName = re.getString(1);
            }
        }catch(Exception e){
            e.printStackTrace();
            fileName="none";
        }finally {
            Close();
        }
        return fileName;
    }

    // 파일 찾기
    public InputStream getFile(long fileSid){
        InputStream re = null;

        var query = new StringBuilder();
        query.append("select file_content from ");
        query.append(tableName);
        query.append(" where sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, fileSid);
            var result = pstmt.executeQuery();
            if(result.next()){
                var blob = result.getBlob(1);
                re = blob.getBinaryStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
            re = null;
        }
        // 다운로드가 완료 되기 전까지 DB연결이 해제되어야 한다.
        // finally {
        //     Close();
        // }
        return re;
    }


    // 파일이 속하는 게시글 번호 바꾸기
    public boolean changeNoticeSid(long fileSid, long newNoticeSid){
        var re = false;
        var query = new StringBuilder();
        query.append("update ");
        query.append(tableName);
        query.append(" set notice_board_sid=? where sid=?");

        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, newNoticeSid);
            pstmt.setLong(2, fileSid);
            pstmt.executeUpdate();

            re = true;
        }catch(Exception e){
            e.printStackTrace();
            re = false;
        }finally {
            Close();
        }
        return re;
    }

    // 파일 sid 로 삭제
    public boolean deleteFile(long fileSid){
        var re = false;
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where sid=?");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, fileSid);
            pstmt.executeUpdate();
            re = true;
        } catch (Exception e) {
            e.printStackTrace();
            re = false;
        }finally{
            Close();
        }
        return re;
    }

    // 파일 업로드 사용자 sid
    public long getUploadUser(long fileSid){
        long re = -1;
        var query = new StringBuilder();
        query.append("select user_sid from ");
        query.append(tableName);
        query.append(" where sid=?");

        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, fileSid);
            var result = pstmt.executeQuery();
            if(result.next()){
                re = result.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            re = -1;
        } finally {
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
    public void Close(){
        try{
            conn.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
