package com.mingyu2.database;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

import jakarta.servlet.ServletContext;

public class DBConnection {
    private String dbDriver = "", dbURL="", dbUser="", dbPWD="";
    public DBConnection(ServletContext context, String name){
        dbDriver = (String)context.getAttribute(name+"DBDriver");
        dbURL = (String)context.getAttribute(name+"DBURL");
        dbUser = (String)context.getAttribute(name+"DBUser");
        dbPWD = (String)context.getAttribute(name+"DBPWD");
    }
    public Connection connectDB(){
        Connection conn = null;
        try {
            var dbDriver = this.dbDriver;
            var dbURL = this.dbURL;
            var user = this.dbUser;
            var pwd = this.dbPWD;
            Class.forName(dbDriver);
            System.out.println(this.dbUser+"오라클 드라이버 로드 완료");
            conn = DriverManager.getConnection(dbURL, user, pwd);
            return conn;
        }catch (Exception e){
            // 에러 발생
            return null;
        }
    }

    public static void addDBINFO(ServletContext context, String name, String dbInfoPath){
        var istream = context.getResourceAsStream(dbInfoPath);
        var reader = new BufferedReader(new InputStreamReader(istream));
        String dbDriver, dbURL, dbUser, dbPWD;
        dbDriver = readLine(reader);
        dbURL = readLine(reader);
        dbUser = readLine(reader);
        dbPWD = readLine(reader);

        context.setAttribute(name+"DBDriver",dbDriver);
        context.setAttribute(name+"DBURL",dbURL);
        context.setAttribute(name+"DBUser",dbUser);
        context.setAttribute(name+"DBPWD",dbPWD);

        try{
            istream.close();
        }catch(Exception e){

        }
    }
    private static String readLine(BufferedReader reader){
        var re = "";
        try {
            if((re = reader.readLine()) == null){
                re = "";
            }
        }catch(Exception e){
            re = "";
        }
        return re;
    }
}
