package com.mingyu2.happyhackingmain.noticeboard;

public class Notice {
    private long sid;
    private long userSID;
    private String username;
    private long genTime;
    private String title;
    private String mainText;
    
    public Notice(
        long sid,
        long userSID,
        String username,
        long genTime,
        String title,
        String mainText
    ){
        this.sid = sid;
        this.userSID = userSID;
        this.username = username;
        this.genTime = genTime;
        this.title = title;
        this.mainText = mainText;
    }

    public long getSid() {
        return sid;
    }
    public long getUserSID() {
        return userSID;
    }
    public String getUsername() {
        return filter(username);
    }
    public long getGenTime() {
        return genTime;
    }
    public String getTitle() {
        return filter(title);
    }
    public String getMainText() {
        return filter(mainText);
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
