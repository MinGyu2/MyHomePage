package com.mingyu2.happyhackingmain.problems.authentication.database;

import java.sql.Connection;
import java.sql.DriverManager;

import jakarta.servlet.ServletContext;

public final class ProbMembersDAO {
    private String tableName = "members";
    private Connection conn = null;
    private String dbDriver = "", dbURL="", dbUser="", dbPWD="";
    public ProbMembersDAO(ServletContext context){
        dbDriver = (String)context.getAttribute("probDBDriver");
        dbURL = (String)context.getAttribute("probDBURL");
        dbUser = (String)context.getAttribute("probDBUser");
        dbPWD = (String)context.getAttribute("probDBPWD");
    }

    // 존재하는 id 인지 확인
    public Boolean isExistUserName(String username){
        Boolean re = false;
        try {
            connectDB();
            var query =String.format("select 1 from dual where exists (select 1 from %s where id=?)", tableName);
            var pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            var result = pstmt.executeQuery();
            re = result.next();
            conn.close();
        }catch(Exception e){
            re = false;
            e.printStackTrace();
        }finally {
            try{
                conn.close();
            }catch(Exception e) {
            }
        }
        return re;
    }
    public ProbMember getUser(String username){
        try {
            connectDB();
            var query = String.format("select * from %s where id=?",tableName);
            var pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            var result = pstmt.executeQuery();
            ProbMember member = null;
            if(result.next()){
                member = new ProbMember(
                    result.getInt(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4),
                    result.getString(5)
                );
            }
            conn.close();
            return member;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
    private Boolean connectDB(){
        try {
            var dbDriver = this.dbDriver;//"oracle.jdbc.driver.OracleDriver";
            var dbURL = this.dbURL;//"jdbc:oracle:thin:@221.27.0.8:1521:XE";
            var user = this.dbUser;//"C##MQDB";
            var pwd = this.dbPWD;//"1234";
            Class.forName(dbDriver);
            System.out.println("오라클 드라이버 로드 완료");
            conn = DriverManager.getConnection(dbURL, user, pwd);
            return true;
        }catch (Exception e){
            // 에러 발생
            return false;
        }
    }
    public Connection getConn() {
        connectDB();
        return conn;
    }
    public String getTableName() {
        return tableName;
    }
}
