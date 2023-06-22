package com.mingyu2.happyhackingmain.noMemberNoticeBoard;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.mingyu2.database.DBConnection;

import jakarta.servlet.ServletContext;

public class NoMemberNoticeBoardDAO {
    private String tableName = "no_member_notice_board";
    private Connection conn;

    private DBConnection dbConn;
    private ServletContext context;
    public NoMemberNoticeBoardDAO(ServletContext context){
        this.context = context;
        dbConn = new DBConnection(context, "noticeBoard");
    }
    public long addNotice(String title, String mainText, String password){
        long re = -1;
        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (sid, gen_time, title, main_text, salt, password) ");
        query.append("select ");
        query.append(" ?,?,?,?,salt,standard_hash(salt||?,'SHA512') from (select DBMS_RANDOM.STRING('P', 30) as salt from dual)");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            re = getSid();
            pstmt.setLong(1, re);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, title);
            pstmt.setString(4, mainText);
            pstmt.setString(5, password);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            re = -1;
        } finally {
            Close();
        }
        return re;
    }
    public long addNotice2(String title, String mainText, String password,ArrayList<Long> arr){
        long re = -1;
        var query = new StringBuilder();
        query.append("insert into ");
        query.append(tableName);
        query.append(" (sid, gen_time, title, main_text, salt, password, file_cnt,file_sids) ");
        query.append("select ");
        query.append(" ?,?,?,?,salt,standard_hash(salt||?,'SHA512'),?,? from (select DBMS_RANDOM.STRING('P', 30) as salt from dual)");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            re = getSid();
            pstmt.setLong(1, re);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, title);
            pstmt.setString(4, mainText);
            pstmt.setString(5, password);
            pstmt.setLong(6, arr.size());
            pstmt.setString(7, arr.toString());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            re = -1;
        } finally {
            Close();
        }
        return re;
    }

    public ArrayList<NoMemberNoticeBoard> getNoticeList(long start, long number){
        var re = new ArrayList<NoMemberNoticeBoard>();
        var query = new StringBuilder();
        query.append("select * from ");
        query.append(tableName);
        query.append(" order by gen_time desc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, start);
            pstmt.setLong(2, number);
            var result = pstmt.executeQuery();
            while(result.next()){
                re.add(
                    new NoMemberNoticeBoard(
                        result.getLong(1), 
                        result.getLong(2), 
                        result.getString(3), 
                        result.getString(4)
                    )
                );
            }
        }catch(Exception e){
            e.printStackTrace();
            re = new ArrayList<NoMemberNoticeBoard>();
        }finally{
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
    
    public int getCount(){
        var cnt = 0;
        var query = new StringBuilder();
        query.append("select count(sid) from ");
        query.append(tableName);
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            var result = pstmt.executeQuery();
            if(result.next()){
                cnt = result.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            cnt = 0;
        } finally { 
            Close();
        }
        return cnt;
    }

    public NoMemberNoticeBoard getNoitceBoard(long sid, String password){
        NoMemberNoticeBoard noMemberNoticeBoard = null;
        var query = new StringBuilder();
        query.append("select * from ");
        query.append(tableName);
        query.append(" where sid=? and password=standard_hash(salt||?,'SHA512')");
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, sid);
            pstmt.setString(2, password);

            var result = pstmt.executeQuery();
            if(result.next()){
                noMemberNoticeBoard = new NoMemberNoticeBoard(
                    result.getLong(1), 
                    result.getLong(2), 
                    result.getString(3),
                    result.getString(4)
                );
            }
        }catch(Exception e){
            noMemberNoticeBoard = null;
        }finally {
            Close();
        }
        return noMemberNoticeBoard;
    }

    public boolean noticeDelete(long sid, String password){
        // 첨부 파일 삭제
        new NoMemberNoticeBoardFileDAO(context).deleteNoticeFile(sid);

        var re = false;
        var query = new StringBuilder();
        query.append("delete from ");
        query.append(tableName);
        query.append(" where sid=? and password=standard_hash(salt||?,'SHA512')");
        try {
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            pstmt.setLong(1, sid);
            pstmt.setString(2, password);
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

    public long noticeModifySave(long sid, String prePassword, String title, String mainText, String newPassword){
        // 비번 일치 확인
        long newSid = -1;
        var preNotice = getNoitceBoard(sid, prePassword);
        System.out.println("일치 확인"+preNotice+" "+sid+" "+prePassword);
        if(preNotice == null){ // 비번 일치 안함
            return -1;
        }
        var arr = getFileSidList(sid);

        // 새로운 문의 글 생성
        newSid = addNotice2(title, mainText, newPassword,arr);
        if(newSid == -1){
            return -1;
        }

        // 이전에 저장된 파일 새로운 글로 이전
        new NoMemberNoticeBoardFileDAO(context).changeNoticeSid(sid, newSid);
        
        // 이전 문의 글 삭제
        System.out.println("이전 글 삭제");
        if(!noticeDelete(sid,prePassword)){
            return -1;
        }
        return newSid;
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
    private long getSid(){
        long re = -1;
        var query = new StringBuilder();
        query.append("select coalesce(max(sid),-1)+1 from ");
        query.append(tableName);
        try{
            Connection();
            var pstmt = conn.prepareStatement(query.toString());
            var result = pstmt.executeQuery();
            if(result.next()){
                re = result.getLong(1);
            }
        }catch(Exception e){
            e.printStackTrace();
            re = -1;
        }finally {
            Close();
        }
        return re;
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
