package com.cumtenn.printerlib;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cumtenn.printer.utils.NetworkUtil;
import com.cumtenn.printerlib.databinding.ActivityPrinterConnectBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class PrinterConnectActivity extends AppCompatActivity implements PrinterAdapter.OnPrinterClickListener {

    private static final String TAG = "PrinterConnect_tag";

    private static final String PRINTER_SERVICE_TYPE = "_ipp._tcp.local.";

    private ActivityPrinterConnectBinding binding;

    private JmDNS jmdns;

    private List<Printer> printerList = new ArrayList<>();
    private PrinterAdapter printerAdapter;

    // 创建线程池
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 使用ViewBinding初始化视图
        binding = ActivityPrinterConnectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 设置返回按钮点击事件
        binding.back.setOnClickListener(v -> finish());

        // 初始化RecyclerView
        initRecyclerView();

        // 设置扫描按钮点击事件
        binding.btnScan.setOnClickListener(v -> {
            startPrinterDiscovery();
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        // 设置布局管理器
        binding.printerList.setLayoutManager(new LinearLayoutManager(this));

        // 添加ItemDecoration设置item间隔
        binding.printerList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                // 设置item之间的间隔为4dp
                int spacing = 12;
                outRect.bottom = spacing;
            }
        });
        // 初始化适配器
        printerAdapter = new PrinterAdapter(printerList, this);

        // 设置适配器
        binding.printerList.setAdapter(printerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        startPrinterDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 停止打印机发现
        stopPrinterDiscovery();

        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void startPrinterDiscovery() {
        // 在UI线程更新列表
        runOnUiThread(() -> {
            // 清空打印机列表
            printerList.clear();
            printerAdapter.notifyDataSetChanged();
        });

        // 使用线程池执行耗时操作
        executorService.execute(() -> {
            try {
                // 停止之前的扫描
                stopPrinterDiscovery();

                // 创建JmDNS实例
                jmdns = JmDNS.create();

                // 添加服务监听器
                jmdns.addServiceListener(PRINTER_SERVICE_TYPE, new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        // 请求服务信息
                        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent event) {
                        // 从列表中移除打印机
                        removePrinter(event.getName());
                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        // 添加打印机到列表
                        addPrinter(event.getInfo());
                    }
                });

                // 请求现有服务
                ServiceInfo[] services = jmdns.list(PRINTER_SERVICE_TYPE);
                for (ServiceInfo service : services) {
                    addPrinter(service);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "scan error: " + e);
            }
        });
    }

    /**
     * 停止打印机发现
     */
    private void stopPrinterDiscovery() {
        if (jmdns != null) {
            JmDNS jmDNSToClose = jmdns;
            jmdns = null;
            new Thread(() -> {
                try {
                    jmDNSToClose.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void addPrinter(ServiceInfo info) {
        runOnUiThread(() -> {
            Log.i(TAG, "service info: " + info);
            String ip = info.getHostAddress();
            if (!NetworkUtil.isIpAddressValid(ip)) {
                return;
            }

            // 构建打印机URL
            String url = "ipp://" + info.getHostAddress() + ":" + info.getPort() + "/ipp/print";

            // 创建打印机对象
            Printer printer = new Printer(
                    info.getName(),
                    ip,
                    url,
                    info.getNiceTextString(),
                    "IPP"
            );

            // 检查打印机是否已存在
            boolean exists = false;
            for (Printer p : printerList) {
                if (p.getUrl().equals(printer.getUrl())) {
                    exists = true;
                    break;
                }
            }

            // 如果不存在则添加到列表
            if (!exists) {
                printerList.add(printer);
                printerAdapter.notifyItemInserted(printerList.size() - 1);
            }
        });
    }

    private void removePrinter(String name) {
        runOnUiThread(() -> {
            for (int i = 0; i < printerList.size(); i++) {
                if (printerList.get(i).getName().equals(name)) {
                    printerList.remove(i);
                    printerAdapter.notifyItemRemoved(i);
                    break;
                }
            }
        });
    }

    @Override
    public void onPrinterClick(Printer printer) {
        // 先停止扫描和清理资源，避免在finish时卡顿
        stopPrinterDiscovery();

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        // 将打印机对象返回给上一个页面
        Intent intent = new Intent();
        intent.putExtra("selected_printer", printer);
        setResult(RESULT_OK, intent);

        finish();
    }

}