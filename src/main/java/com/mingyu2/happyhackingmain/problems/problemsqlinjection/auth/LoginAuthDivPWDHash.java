package com.mingyu2.happyhackingmain.problems.problemsqlinjection.auth;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;

import com.mingyu2.happyhackingmain.problems.authentication.database.ProbMember;
import com.mingyu2.happyhackingmain.problems.problemsqlinjection.LoginAuth;

import jakarta.servlet.ServletContext;

public class LoginAuthDivPWDHash implements LoginAuth{
    private String tableName = "members";
    private Connection conn = null;
    private String dbDriver = "", dbURL="", dbUser="", dbPWD="";
    private String sqlQuery = "";

    public LoginAuthDivPWDHash(ServletContext context){
        dbDriver = (String)context.getAttribute("probDBDriver");
        dbURL = (String)context.getAttribute("probDBURL");
        dbUser = (String)context.getAttribute("probDBUser");
        dbPWD = (String)context.getAttribute("probDBPWD");
    }
    
    @Override
    public ProbMember authAndMember(String username, String pwd) {
        var id = auth(username, pwd);
        ProbMember member = null;
        if(!id.equals("")){
            member = getMember(id);
        }
        // member = new ProbMember(0, id, "pwd", "username", "pwd");
        return member;
    }

    @Override
    public String auth(String username, String pwd) {
        connectDB();
        String id = "";
        String password = "";
        try {
            var query = new StringBuilder()
                .append("select id,hashpwd from ")
                .append(tableName)
                .append(" where id='")
                .append(username)
                .append("'");

            sqlQuery = query.toString();

            var pstmt = conn.prepareStatement(sqlQuery);
            var result = pstmt.executeQuery();
            if(result.next()){
                id = result.getString(1);
                password = result.getString(2);
            }
        }catch(Exception e){
            id = "";
        }finally {
            try {
                conn.close();
            } catch (Exception e) {
                id = "";
            }
        }
        var userPWD = pwdSHA512(pwd);
        // 대소문자 상관없이 비교한다.
        if(userPWD == null || !userPWD.toUpperCase().equals(password.toUpperCase())){
            id = "";
        }
        // id = password + "    " + userPWD + "   " + userPWD.toUpperCase().equals(password);
        return id;
    }
    // SHA 512 알고리즘으로 pwd 를 해시 암호화 한다.
    private String pwdSHA512(String pwd){
        String password = null;
        try {
            var md = MessageDigest.getInstance("SHA-512");
            md.update(pwd.getBytes());
            password = String.format("%064x", new BigInteger(1,md.digest()));
        }catch(Exception e){
            password = null;
        }
        return password;
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
