package com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth;

import java.sql.Connection;
import java.sql.DriverManager;

import com.mingyu2.happyhackingmain.problems.authentication.database.ProbMember;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.LoginAuth;

import jakarta.servlet.ServletContext;

public final class LoginAuthSamePWDHash implements LoginAuth{
    private String tableName = "members";
    private Connection conn = null;
    private String dbDriver = "", dbURL="", dbUser="", dbPWD="";
    private String sqlQuery = "";
    public LoginAuthSamePWDHash(ServletContext context){
        dbDriver = (String)context.getAttribute("probDBDriver");
        dbURL = (String)context.getAttribute("probDBURL");
        dbUser = (String)context.getAttribute("probDBUser");
        dbPWD = (String)context.getAttribute("probDBPWD");
    }
    @Override
    public ProbMember authAndMember(String username, String pwd) {
        String id =auth(username, pwd);
        ProbMember member = null;
        if(!id.equals("")){
            member = getMember(id);
        }
        // member = new ProbMember(0, "id "+id, "username", "pwd", "id");
        return member;
    }
    @Override
    public String auth(String username, String pwd) {
        connectDB();
        String id = "";
        try {
            var query = new StringBuilder()
                .append("select id from ")
                .append(tableName)
                .append(" where id='")
                .append(username)
                .append("' and hashpwd=standard_hash('")
                .append(pwd)
                .append("','SHA512')");

            sqlQuery = query.toString();

            var pstmt = conn.prepareStatement(sqlQuery);
            var result = pstmt.executeQuery();
            if(result.next()){
                id = result.getString(1);
            }
        }catch(Exception e){
            id = "";//"e1 : "+e.toString();
        }finally {
            try {
                conn.close();
            } catch (Exception e) {
                id ="";// "e2";
            }
        }
        return id;
    }
    @Override
    public ProbMember getMember(String id) {
        ProbMember member = null;
        connectDB();
        try {
            // memeber 정보 가죠오기
            var query = new StringBuilder();
            query
                .append("select * from ")
                .append(tableName)
                .append(" where id='").append(id).append("'");
            var pstmt = conn.prepareStatement(query.toString());
            var result = pstmt.executeQuery();
            if(result.next()){
                member = new ProbMember(
                    result.getInt(1),
                    result.getString(2),
                    result.getString(6),
                    result.getString(4),
                    result.getString(5)
                );
            }
            // member = new ProbMember(0, "id", "id", "id", "id");
        }catch(Exception e){
            member = null;
        }finally {
            try {
                conn.close();
            }catch(Exception e){
                member = null;
            }
        }
        return member;
    }

    @Override
    public String getSqlQuery() {
        return sqlQuery;
    }

    private Boolean connectDB(){
        try {
            Class.forName(dbDriver);
            System.out.println("오라클 드라이버 로드 완료");
            conn = DriverManager.getConnection(dbURL, dbUser, dbPWD);
            return true;
        }catch (Exception e){
            // 에러 발생
            return false;
        }
    }
    @Override
    public String getMSG() {
        return null;
    }
}
