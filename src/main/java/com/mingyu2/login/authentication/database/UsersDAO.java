package com.mingyu2.login.authentication.database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Base64;

import com.mingyu2.database.DBConnection;

import jakarta.servlet.ServletContext;

public class UsersDAO {
    private String tableName = "USERS"; // 테이블 이름
    private Connection conn = null;

    // private String dbDriver = "", dbURL="", dbUser="", dbPWD="";
    private DBConnection connection;

    public UsersDAO(ServletContext context){
        // dbDriver = (String)context.getAttribute("mainDBDriver");
        // dbURL = (String)context.getAttribute("mainDBURL");
        // dbUser = (String)context.getAttribute("mainDBUser");
        // dbPWD = (String)context.getAttribute("mainDBPWD");
        connection = new DBConnection(context, "main");
    }

    // 존재하는 id 인지 확인
    public Boolean isExistUserName(String username){
        Boolean re = false;
        try {
            connectDB();
            var query =String.format("select 1 from dual where exists (select 1 from %s where username=?)", tableName);
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
    public User getUser(String username){
        try {
            connectDB();
            var query = String.format("select * from %s where username=?",tableName);
            var pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            var result = pstmt.executeQuery();
            User user = null;
            if(result.next()){
                user = new User(
                    result.getLong(1),
                    result.getString(2),
                    result.getString(3),
                    result.getString(4),
                    result.getString(5), 
                    result.getLong(6), 
                    result.getLong(7),
                    result.getString(8), 
                    result.getString(9).equals("1")
                );
            }
            conn.close();
            return user;
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
    // version 2
    public boolean createUser(String username, String email, String pwd1, String pwd2, String addrNumber, String address, String addressPlus){
        var regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"+"[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if(isExistUserName(username) || !email.matches(regexPattern) || !pwd1.equals(pwd2)){ // 존재하는 유저이다.
            return false;
        }
        var re = false;
        try {
            // salt 만들기
            var random = SecureRandom.getInstanceStrong();
            var bytes = new byte[16];
            random.nextBytes(bytes);
            var salt = new String(Base64.getEncoder().encode(bytes));

            // 비번 sha-512 로 해시 암호화 하기
            var md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            md.update(pwd1.getBytes());
            var password = String.format("%064x",new BigInteger(1,md.digest()));

            // db 연결
            connectDB();
            var sid = getMaxSid()+1;
            var query = "insert into users (sid, username, email, salt, password, gen_time, modifi_time, algorithm, authority, 우편번호, 주소, 주소2) values (?,?,?,?,?,?,?,?,'0',?,?,?)";
            var pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, sid);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, salt);
            pstmt.setString(5, password);
            pstmt.setLong(6, System.currentTimeMillis());;
            pstmt.setLong(7, System.currentTimeMillis());;
            pstmt.setString(8, "SHA-512");
            // 유저 정보에 주소 추가하기
            pstmt.setString(9, addrNumber);
            pstmt.setString(10, address);
            pstmt.setString(11, addressPlus);
            pstmt.executeQuery();
            
            re = true;
        }catch(Exception e){
            re = false;
        }finally{
            try {
                conn.close();
            } catch (Exception e) {
            }
        }

        return re;
    }
    private long getMaxSid() throws Exception{
        var query = "select max(sid) from users";
        var pstmt = conn.prepareStatement(query);
        var re = pstmt.executeQuery();
        if(re.next()){
            return re.getLong(1);
        }
        return 0;
    }
    private void connectDB(){
        conn = connection.connectDB();
        // try {
        //     var dbDriver = this.dbDriver;//"oracle.jdbc.driver.OracleDriver";
        //     var dbURL = this.dbURL;//"jdbc:oracle:thin:@221.27.0.8:1521:XE";
        //     var user = this.dbUser;//"C##MQDB";
        //     var pwd = this.dbPWD;//"1234";
        //     Class.forName(dbDriver);
        //     System.out.println("오라클 드라이버 로드 완료");
        //     conn = DriverManager.getConnection(dbURL, user, pwd);
        //     return true;
        // }catch (Exception e){
        //     // 에러 발생
        //     return false;
        // }
    }
}
