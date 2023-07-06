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
        return filter(title);
    }
    public String getMainText() {
        return mainText;
    }
    public String getMainSumaryText() {
        return mainText.substring(0, (mainText.length() > 20)?20:mainText.length());
    }
    public String getGenDate(){
        var obj = new SimpleDateFormat("dd MMM yyy HH:mm");
        obj.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 타임존 한국
        var res = new Date(genTime);
        return obj.format(res);
    }
    // xss 방지
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
