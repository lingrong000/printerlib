package com.cumtenn.printer.model;

import java.util.List;

import de.gmuth.ipp.attributes.PrinterState;

public class PrinterStatus {
    private State state;
    private String stateMessage;
    private List<String> reasonList;

    private boolean isError;

    public boolean canPrint() {
        return state == State.Idle && !isError;
    }

    public State getState() {
        return state;
    }

    public void setState(PrinterState state) {
        this.state = State.fromInt(state.getCode());
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public void setStateMessage(String stateMessage) {
        this.stateMessage = stateMessage;
    }

    public List<String> getReasonList() {
        return reasonList;
    }

    public void setReasonList(List<String> reasonList) {
        this.reasonList = reasonList;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    // 内部枚举类 State
    public enum State {
        Idle(3),
        Processing(4),
        Stopped(5);

        private final int code;

        State(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        // 通过code获取enum
        public static State fromInt(int code) {
            for (State state : values()) {
                if (state.code == code) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Invalid State code: " + code);
        }
    }

    @Override
    public String toString() {
        return "PrinterStatus{" +
                "state=" + state +
                ", stateMessage='" + stateMessage + '\'' +
                ", reasonList=" + reasonList +
                ", isError=" + isError +
                '}';
    }
}
