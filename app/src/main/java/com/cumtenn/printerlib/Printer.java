package com.cumtenn.printerlib;

import android.os.Parcel;
import android.os.Parcelable;

import com.cumtenn.printer.model.PrinterStatus;

public class Printer implements Parcelable {
    private String name;
    private String ip;
    private String url;
    private String info;
    private String type;

    private boolean isConnected = true;

    private PrinterStatus status;

    public Printer(String name, String ip, String url, String info, String type) {
        this.name = name;
        this.ip = ip;
        this.url = url;
        this.info = info;
        this.type = type;
    }

    protected Printer(Parcel in) {
        name = in.readString();
        ip = in.readString();
        url = in.readString();
        info = in.readString();
        type = in.readString();
        isConnected = in.readByte() != 0;
    }

    public static final Creator<Printer> CREATOR = new Creator<Printer>() {
        @Override
        public Printer createFromParcel(Parcel in) {
            return new Printer(in);
        }

        @Override
        public Printer[] newArray(int size) {
            return new Printer[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public PrinterStatus getStatus() {
        return status;
    }

    public void setStatus(PrinterStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Printer{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", url='" + url + '\'' +
                ", info='" + info + '\'' +
                ", type='" + type + '\'' +
                ", isConnected=" + isConnected +
                ", status=" + status +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeString(url);
        dest.writeString(info);
        dest.writeString(type);
        dest.writeByte((byte) (isConnected ? 1 : 0));
    }
}