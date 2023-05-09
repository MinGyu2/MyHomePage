package com.mingyu2.login.authentication.searchaddress;

import com.mingyu2.database.DBConnection;

import jakarta.servlet.ServletContext;

public class AddressDAO {
    private String tableName = "KO_ADDRESS"; // 테이블 이름
    private DBConnection dbConn;
    public AddressDAO(ServletContext context){
        dbConn = new DBConnection(context,"main");
    }
    public String getAddressNumber(String address){
        var split = address.split(" ");
        var re = "";
        var conn = dbConn.connectDB();
        var address1 = "(시도||' '||시군구||' '||도로명||' '||건물번호본번||'-'||건물번호부번 ||' '||시군구용건물명)";
        var address2 = "(시도||' '||시군구||' '||법정동명||' '||지번본번||'-'||지번부번||' '||시군구용건물명)";
        try {
            var query = new StringBuilder();
            query.append("select 우편번호, ")
                .append(address1+" as 도로명1, ")
                .append(address2+" as 주소 ")
                .append("from "+tableName)
                .append(" where (");
            for(int i = 0;i<split.length-1;i++){
                query.append(address1+" like '%'||?||'%' and ");
            }
            query.append(address1+" like '%'||?||'%' ) or ( ");
            for(int i = 0;i<split.length-1;i++){
                query.append(address2+" like '%'||?||'%' and ");
            }
            query.append(address2+" like '%'||?||'%' )");
            

            // 사용자 입력 넣기!
            var pstmt = conn.prepareStatement(query.toString());
            for(int i = 0;i<split.length;i++){
                pstmt.setString(i+1, split[i]);
            }
            for(int i = split.length;i<2*split.length;i++){
                pstmt.setString(i+1, split[i-split.length]);
            }

            var result = pstmt.executeQuery();
            int max = 49;
            int cnt = 0;
            while(result.next()){
                re += result.getString(1)
                    +"\n"+result.getString(2).replaceAll("-0", "")
                    +"\n"+result.getString(3).replaceAll("-0", "")
                    +"\n";
                cnt++;
                if(cnt > max) {
                    break;
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                conn.close();
            }catch(Exception e){
            }
        }
        return re;
    }
}
