package com.example.vinayak.whatchat;

public class Conv {

    public boolean seen;
    public long timestamp;

    public Conv(){}

    public boolean isSeen(){return seen;}
    public long getTimestamp(){return timestamp;}

    public void setTimestamp(long timestamp){this.timestamp=timestamp;}
    public void setSeen(boolean seen){this.seen=seen;}

    public Conv(boolean seen,long timestamp){
        this.seen=seen;
        this.timestamp=timestamp;
    }
}
