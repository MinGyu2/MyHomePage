package com.mingyu2.happyhackingmain.noMemberNoticeBoard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class NoMemberNoticeBoard {
    private long sid;
    private long genTime;
    private String title;
    private String mainText;
    public NoMemberNoticeBoard(long sid, long genTime, String title, String mainText){
        this.sid = sid;
        this.genTime = genTime;
        this.title = title;
        this.mainText = mainText;
    }
    public long getSid() {
        return sid;
    }
    public long getGenTime() {
        return genTime;
    }
    public String getTitle() {
        return title;
    }
    public String getMainText() {
        return mainText;
    }
    public String getGenDate(){
        var obj = new SimpleDateFormat("dd MMM yyy HH:mm");
        obj.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 타임존 한국
        var res = new Date(genTime);
        return obj.format(res);
    }
}
