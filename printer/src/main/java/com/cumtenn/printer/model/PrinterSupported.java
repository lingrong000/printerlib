package com.cumtenn.printer.model;

import java.util.List;

public class PrinterSupported {
    private List<String> mediaSupportedList;

    private List<String> sidesSupportedList;

    private List<String> documentFormatSupportedList;

    private List<String> compressList;

    private List<Integer> qualityList;

    private List<Integer> orientationList;

    private boolean colorSupported;

    public List<String> getMediaSupportedList() {
        return mediaSupportedList;
    }

    public void setMediaSupportedList(List<String> mediaSupportedList) {
        this.mediaSupportedList = mediaSupportedList;
    }

    public List<String> getSidesSupportedList() {
        return sidesSupportedList;
    }

    public void setSidesSupportedList(List<String> sidesSupportedList) {
        this.sidesSupportedList = sidesSupportedList;
    }

    public List<String> getDocumentFormatSupportedList() {
        return documentFormatSupportedList;
    }

    public void setDocumentFormatSupportedList(List<String> documentFormatSupportedList) {
        this.documentFormatSupportedList = documentFormatSupportedList;
    }

    public boolean isColorSupported() {
        return colorSupported;
    }

    public void setColorSupported(boolean colorSupported) {
        this.colorSupported = colorSupported;
    }

    public List<String> getCompressList() {
        return compressList;
    }

    public void setCompressList(List<String> compressList) {
        this.compressList = compressList;
    }

    public List<Integer> getQualityList() {
        return qualityList;
    }

    public void setQualityList(List<Integer> qualityList) {
        this.qualityList = qualityList;
    }

    public List<Integer> getOrientationList() {
        return orientationList;
    }

    public void setOrientationList(List<Integer> orientationList) {
        this.orientationList = orientationList;
    }

    @Override
    public String toString() {
        return "PrinterSupported{" +
                "mediaSupportedList=" + mediaSupportedList +
                ", sidesSupportedList=" + sidesSupportedList +
                ", documentFormatSupportedList=" + documentFormatSupportedList +
                ", compressList=" + compressList +
                ", qualityList=" + qualityList +
                ", orientationList=" + orientationList +
                ", colorSupported=" + colorSupported +
                '}';
    }
}
