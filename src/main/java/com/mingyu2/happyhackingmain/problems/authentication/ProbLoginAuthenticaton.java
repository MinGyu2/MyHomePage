package com.mingyu2.happyhackingmain.problems.authentication;

import com.mingyu2.happyhackingmain.problems.authentication.database.ProbMember;
import com.mingyu2.happyhackingmain.problems.authentication.database.ProbMembersDAO;

import jakarta.servlet.ServletContext;

public class ProbLoginAuthenticaton {
    public static ProbMember userAuthenticatoin(ServletContext servletContext, String username, String password){
        var probMembersDAO = new ProbMembersDAO(servletContext);
        // 1. 존재하는 유저인지 확인하기.
        if(!probMembersDAO.isExistUserName(username)){
            return null;
        }
        // 2. 아이디에 해당하는 pwd 가져오기
        var member = probMembersDAO.getUser(username);
        // 3. 비번 코드 상에서 검사하기
        if(!password.equals(member.getPassword())) { // 비번 비교하기
            return null;
        }
        // 비번 일치
        return member;
    }
}
