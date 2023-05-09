package com.mingyu2.login.authentication.database;

public class User {
    private long sid; // primary key
    private String username;
    private String email;
    private String salt;
    private String password;
    private long genTime; // 생성 시각
    private long modifiTime; // 수정 시각
    private String algorithm;
    private boolean authority = false;
    public User(long sid, String username, String email, String salt, String password, long getTime, long modifiTime, String algorithm, boolean authority){
        this.sid = sid;
        this.username = username;
        this.email = email;
        this.salt = salt;
        this.password = password;
        this.genTime = getTime;
        this.modifiTime = modifiTime;
        this.algorithm = algorithm;
        this.authority = authority;
    }
    public long getSid() {
        return sid;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getSalt() {
        return salt;
    }
    public String getPassword() {
        return password;
    }
    public long getGenTime() {
        return genTime;
    }
    public long getModifiTime() {
        return modifiTime;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public boolean getAuthority() {
        return authority;
    }
    
    @Override
    public String toString() {
        return getUsername();
    }



    public String getUsernameFilter() {
        return filter(username);
    }
    // xss 방지
    // 안되네
    private String filter(String in){
        // 순서데로 해야한다.
        // & &amp;  이게 무조건 첫번째로 와야한다.
        // < &lt;
        // > &gt;
        // ' &#x27;
        // " &quot;
        // ( &#40;
        // ) &#41;
        // / &#x2F;
        var re = in.replaceAll("[&]", "&amp;")
                .replaceAll("[<]", "&lt;")
                .replaceAll("[>]", "&gt;")
                .replaceAll("[']", "&#x27;")
                .replaceAll("[\"]", "&quot;")
                .replaceAll("[(]", "&#40;")
                .replaceAll("[)]", "&#41;")
                .replaceAll("[/]", "&#x2F;");
        return re;
    }
}
