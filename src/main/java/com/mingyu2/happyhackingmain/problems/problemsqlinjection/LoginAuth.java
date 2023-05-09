package com.mingyu2.happyhackingmain.problems.problemsqlinjection;

import com.mingyu2.happyhackingmain.problems.authentication.database.ProbMember;

public interface LoginAuth {
    public ProbMember authAndMember(String username, String pwd);
    public String auth(String username, String pwd);
    public ProbMember getMember(String username);
    public String getSqlQuery();
    public String getMSG();
}
