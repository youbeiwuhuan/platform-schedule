package com.courage.platform.rpc.remoting.netty.protocol;

public enum PlatformRemotingCommandFormat {

    REQUESET_ONEWAY(0, "请求无需返回"),
    REQUESET(1, "请求"),
    RESPONSE(2, "响应");

    private int code;

    private String content;

    PlatformRemotingCommandFormat(int code, String content) {
        this.code = code;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static String getContent(int code) {
        try {
            for (PlatformRemotingCommandFormat format : PlatformRemotingCommandFormat.values()) {
                if (format.getCode() == code){
                    return format.getContent();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
