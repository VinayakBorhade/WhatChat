package com.example.vinayak.whatchat;

public class Messages {

    private String message, type, from;
    private long time;
    private boolean seen;

    public Messages(){}

    public Messages(String message, Boolean seen, String type, long time, String from){
        this.message=message;
        this.seen=seen;
        this.type=type;
        this.time=time;
        this.from=from;
    }

    public String getMessage() {return message;}
    public boolean getSeen() {return seen;}
    public long getTime() {return time;}
    public String getType() {return type;}
    public String getFrom() {return from;}

    public void setMessage(String message) {this.message = message;}
    public void setSeen(boolean seen) {this.seen = seen;}
    public void setTime(long time) {this.time = time;}
    public void setType(String type) {this.type = type;}
    public void setFrom(String from) {this.from = from;}
}
