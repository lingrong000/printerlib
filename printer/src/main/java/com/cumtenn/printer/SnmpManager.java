package com.cumtenn.printer;

import static com.cumtenn.printer.utils.NetworkUtil.isIpAddressValid;

import androidx.annotation.NonNull;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SnmpManager {

    public static final String READY = "READY";                     // 0x80 就绪
    public static final String IDLE = "IDLE";                       // 3 idle 就绪
    public static final String PRINT_TOTAL_COUNT = "PRINT_TOTAL_COUNT"; // 打印机总计数
    public static final String WAKE_STATE_SET = "WAKE_STATE_SET";   // 1 待机, 2 睡眠；set 1 唤醒打印机
    public static final String YELLOW_FULL = "YELLOW_FULL";         // 黄色耗材满的数值
    public static final String YELLOW_REMAIN = "YELLOW_REMAIN";     // 黄色耗材还剩的数值
    public static final String RED_FULL = "RED_FULL";               // 红色耗材满的数值
    public static final String RED_REMAIN = "RED_REMAIN";           // 红色耗材还剩的数值
    public static final String CYAN_FULL = "CYAN_FULL";             // 青色耗材满的数值
    public static final String CYAN_REMAIN = "CYAN_REMAIN";         // 青色耗材还剩的数值
    public static final String BLACK_FULL = "BLACK_FULL";           // 黑色耗材满的数值
    public static final String BLACK_REMAIN = "BLACK_REMAIN";       // 黑色耗材还剩的数值
    public static final String CURRENT_JOB_ID = "CURRENT_JOB_ID";   // 获取当前打印作业 id
    public static final String CANCEL_JOB = "CANCEL_JOB";           // 取消打印作业
    public static final String OUT_OF_PAPER = "OUT_OF_PAPER";       // 是否缺纸


    public interface SnmpCallback {
        void onSuccess(@NonNull String result);
        void onError(@NonNull String error);
    }


    private static volatile SnmpManager INSTANCE;

    public static SnmpManager getInstance() {
        if (INSTANCE == null) {
            synchronized (SnmpManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SnmpManager();
                }
            }
        }
        return INSTANCE;
    }


    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile String ip = null;
    private volatile String community = "public";
    private volatile int port = 161;
    private volatile int timeout = 2000;
    private volatile int retries = 2;

    public static final Map<String, String> OID_MAP;
    static {
        Map<String, String> m = new HashMap<>();
        m.put(READY, "1.3.6.1.2.1.25.3.5.1.2.1");
        m.put(IDLE, "1.3.6.1.2.1.25.3.5.1.1.1");
        m.put(PRINT_TOTAL_COUNT, "1.3.6.1.2.1.43.10.2.1.4.1.1");
        m.put(WAKE_STATE_SET, "1.3.6.1.4.1.11.2.3.9.4.2.1.1.1.2.0");
        m.put(YELLOW_FULL, "1.3.6.1.2.1.43.11.1.1.8.1.1");
        m.put(YELLOW_REMAIN, "1.3.6.1.2.1.43.11.1.1.9.1.1");
        m.put(RED_FULL, "1.3.6.1.2.1.43.11.1.1.8.1.2");
        m.put(RED_REMAIN, "1.3.6.1.2.1.43.11.1.1.9.1.2");
        m.put(CYAN_FULL, "1.3.6.1.2.1.43.11.1.1.8.1.3");
        m.put(CYAN_REMAIN, "1.3.6.1.2.1.43.11.1.1.9.1.3");
        m.put(BLACK_FULL, "1.3.6.1.2.1.43.11.1.1.8.1.4");
        m.put(BLACK_REMAIN, "1.3.6.1.2.1.43.11.1.1.9.1.4");
        m.put(CURRENT_JOB_ID, "1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.2.1.1.0");
        m.put(CANCEL_JOB, "1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.2.0");
        m.put(OUT_OF_PAPER, "1.3.6.1.4.1.11.2.3.9.4.2.1.4.1.3.2.2.0");
        OID_MAP = Collections.unmodifiableMap(m);
    }

    private SnmpManager() { }


    public void setIp(@NonNull String ip) {
        this.ip = ip.trim();
    }

    public void setCommunity(@NonNull String community) {
        this.community = community.trim();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeoutMs) {
        this.timeout = timeoutMs;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void getByOid(@NonNull final String oid, @NonNull final SnmpCallback callback) {
        if (!isIpAddressValid(ip)) {
            callback.onError("invalid ip: " + ip);
            return;
        }

        executor.execute(() -> {
            try {
                String res = performSnmpGet(ip, port, community, oid, timeout, retries);
                callback.onSuccess(res);
            } catch (Exception e) {
                callback.onError(e.toString());
            }
        });
    }


    public void getByKey(@NonNull final String key, @NonNull final SnmpCallback callback) {
        String oid = OID_MAP.get(key);
        if (oid == null) {
            callback.onError("Unknown key: " + key);
            return;
        }
        getByOid(oid, callback);
    }

    /**
     * 关闭管理器（释放线程池）
     */
    public void release() {
        executor.shutdownNow();
    }

    private String performSnmpGet(String address, int port, String community,
                                  String oidStr, int timeoutMs, int retries) throws IOException {
        TransportMapping<?> transport = null;
        Snmp snmp = null;
        try {
            String addr = "udp:" + address + "/" + port;
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(GenericAddress.parse(addr));
            target.setRetries(retries);
            target.setTimeout(timeoutMs);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oidStr)));
            pdu.setType(PDU.GET);

            ResponseEvent event = snmp.get(pdu, target);
            if (event != null && event.getResponse() != null && event.getResponse().size() > 0) {
                VariableBinding vb = event.getResponse().get(0);
                return "" + vb.getVariable();
            } else {
                throw new IOException("No response (timeout) or empty PDU");
            }
        } finally {
            try {
                if (snmp != null) snmp.close();
            } catch (Exception ignored) {
            }
            try {
                if (transport != null) transport.close();
            } catch (Exception ignored) {
            }
        }
    }
}