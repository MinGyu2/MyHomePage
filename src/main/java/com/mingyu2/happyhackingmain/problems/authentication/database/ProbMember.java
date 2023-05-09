package com.mingyu2.happyhackingmain.problems.authentication.database;

public final class ProbMember {
    private int sid;
    private String id;
    private String password;
    private String email;
    private String info;
    public ProbMember(int sid, String id, String password, String email, String info){
        this.sid = sid;
        this.id = id;
        this.password = password;
        this.email = email;
        this.info = info;
    }
    public int getSid() {
        return sid;
    }
    public String getId() {
        return id;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public String getInfo() {
        return info;
    }
}
