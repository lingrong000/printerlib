package com.cumtenn.printerlib;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cumtenn.printer.IppManager;
import com.cumtenn.printer.PrintParams;
import com.cumtenn.printer.SnmpManager;
import com.cumtenn.printer.model.Orientation;
import com.cumtenn.printer.model.PrinterStatus;
import com.cumtenn.printer.model.PrinterSupported;
import com.cumtenn.printer.model.Quality;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlin.ranges.IntRange;

public class MainActivity extends AppCompatActivity {

    // 权限请求常量
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    
    // 文件选择常量
    private static final String PDF_MIME_TYPE = "application/pdf";

    private EditText etIp;
    private Button btnSetIp;
    private Button btnSnmpQuery;
    private EditText resultEditText;
    private SnmpManager snmpManager;
    private IppManager ippManager;
    private String currentIp;
    
    // 文件选择相关
    private Button btnSelectFile;
    private Button btnPrint;
    private TextView tvSelectedFile;
    private TextView tvPrintResult;
    private String selectedFileName;
    private String localFilePath;
    
    // ActivityResultLauncher 用于文件选择
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化 SNMP 管理器和 IPP 管理器
        snmpManager = SnmpManager.getInstance();
        ippManager = IppManager.getInstance();

        // 初始化 UI 组件
        etIp = findViewById(R.id.et_ip);
        btnSetIp = findViewById(R.id.btn_set_ip);
        btnSnmpQuery = findViewById(R.id.btn_snmp_query);
        resultEditText = findViewById(R.id.result_edittext);
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnPrint = findViewById(R.id.btn_print);
        tvSelectedFile = findViewById(R.id.tv_selected_file);
        tvPrintResult = findViewById(R.id.tv_print_result);

        // 初始化文件选择 launcher
        initFilePickerLauncher();

        // 检查权限
        checkAndRequestPermissions();

        // 设置 IP 按钮点击事件
        btnSetIp.setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            if (!ip.isEmpty()) {
                currentIp = ip;
                snmpManager.setIp(ip);
                ippManager.setIp(ip);
                Toast.makeText(MainActivity.this, "IP 设置成功: " + ip, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "请输入有效的 IP 地址", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置 SNMP 查询按钮点击事件
        btnSnmpQuery.setOnClickListener(v -> {
            if (currentIp == null || currentIp.isEmpty()) {
                Toast.makeText(MainActivity.this, "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }

            // 清空之前的结果
            resultEditText.setText("");
            appendResult("开始 SNMP 查询...\n\n");

            // 遍历所有预定义的 OID 并执行查询
            for (Map.Entry<String, String> entry : SnmpManager.OID_MAP.entrySet()) {
                String key = entry.getKey();
                String oid = entry.getValue();
                querySnmpOid(key, oid);
            }
        });

        // 设置文件选择按钮点击事件
        btnSelectFile.setOnClickListener(v -> {
            openFilePicker();
        });

        findViewById(R.id.btn_print_supported).setOnClickListener(v -> {
            ippManager.getPrinterSupportedAsync(new IppManager.PrinterSupportedCallBack() {
                @Override
                public void onPrinterSupported(PrinterSupported supported) {

                }

                @Override
                public void onSupportedError(String errorInfo) {

                }
            });
            
            ippManager.getPrinterStatusAsync(new IppManager.PrinterStatusCallBack() {
                @Override
                public void onPrinterStatus(PrinterStatus status) {
                    Log.i("printer_test", "status: " + status);
                }

                @Override
                public void onStatusError(String errorInfo) {

                }
            });
        });

        // 设置打印按钮点击事件
        btnPrint.setOnClickListener(v -> {
            if (currentIp == null || currentIp.isEmpty()) {
                Toast.makeText(MainActivity.this, "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            if (localFilePath == null || localFilePath.isEmpty()) {
                Toast.makeText(MainActivity.this, "请先选择 PDF 文件", Toast.LENGTH_SHORT).show();
                return;
            }
            printSelectedFile();
        });
    }

    /**
     * 初始化文件选择 launcher
     */
    private void initFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            handleSelectedFile(uri);
                        }
                    }
                }
        );
    }

    /**
     * 打开文件选择器
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(PDF_MIME_TYPE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    /**
     * 处理选择的文件
     * @param uri 文件 URI
     */
    private void handleSelectedFile(Uri uri) {
        try {
            // 获取文件名
            selectedFileName = getFileNameFromUri(uri);
            tvSelectedFile.setText("已选择文件: " + selectedFileName);
            
            // 保存文件到私有目录
            localFilePath = saveFileToPrivateDir(uri, selectedFileName);
            Toast.makeText(this, "文件已保存到私有目录", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "文件保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从 URI 获取文件名
     * @param uri 文件 URI
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = "unknown.pdf";
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }

    /**
     * 将文件保存到私有目录
     * @param uri 文件 URI
     * @param fileName 文件名
     * @return 保存后的文件路径
     * @throws IOException
     */
    private String saveFileToPrivateDir(Uri uri, String fileName) throws IOException {
        ContentResolver resolver = getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("无法打开输入流");
        }

        // 创建私有目录文件
        File privateDir = getFilesDir();
        File outputFile = new File(privateDir, fileName);
        OutputStream outputStream = new FileOutputStream(outputFile);

        // 复制文件
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // 关闭流
        outputStream.close();
        inputStream.close();

        return outputFile.getAbsolutePath();
    }

    /**
     * 打印选择的文件
     */
    private void printSelectedFile() {
        tvPrintResult.setText("正在打印...");
        
        // 创建打印参数
        PrintParams params = new PrintParams.Builder()
                // 任务名
                .setJobName("Test Print")
                // 打印份数。可选设置，默认1
                .setCopies(2)
                // 打印范围。必须设置，打印所有，则设置new IntRange(0, 文件页数-1)
                .setRange(new IntRange(0, 16))
                // 单双面。可选设置，默认单面，可选项需要获取打印机支持类型，PrinterSupported.sidesSupportedList
                .setSides("one-sided")
                // 纸张样式。可选设置，默认A4，可选项需要获取打印机支持类型，PrinterSupported.mediaSupportedList
                .setMedia("iso_a4_210x297mm")
                // 文件类型。可选设置，默认pdf，可选项需要获取打印机支持类型，PrinterSupported.documentFormatSupportedList
                .setDocumentFormat("application/pdf")
                // 打印颜色。可选设置，默认Auto，可选项: auto、color、monochrome，如果需要选择color，则需要先获取打印机支持类型，PrinterSupported.colorSupported为true才可以
                .setColorMode("auto")
                // 方向。可选设置，默认Portrait，可选项需要获取打印机支持类型，PrinterSupported.orientationList
                .setOrientation(Orientation.Portrait)
                // 打印质量。可选设置，默认Normal，可选项需要获取打印机支持类型，PrinterSupported.qualityList
                .setQuality(Quality.Normal)
                // 压缩。可选设置，默认none，可选项需要获取打印机支持类型，PrinterSupported.compressList
                .setCompression("none")
                .build();

        ippManager.getPrinterStatusAsync(new IppManager.PrinterStatusCallBack() {
            @Override
            public void onPrinterStatus(PrinterStatus status) {

            }

            @Override
            public void onStatusError(String errorInfo) {

            }
        });

        // 调用打印方法
        ippManager.printFile(this, localFilePath, params, new IppManager.PrinterCallBack() {
            @Override
            public void onPrinterError(String errorInfo) {
                runOnUiThread(() -> {
                    tvPrintResult.setText("打印结果：打印失败 - " + errorInfo);
                });
            }

            @Override
            public void onPrinterSuccess() {
                runOnUiThread(() -> {
                    tvPrintResult.setText("打印结果：打印成功");
                });
            }
        });
    }

    /**
     * 检查并请求所需的权限
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // 检查 INTERNET 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.INTERNET);
        }

        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用 READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12- 使用 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        // 请求所需的权限
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }

        // 检查并请求所有文件访问权限（Android 11+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkAndRequestManageExternalStoragePermission();
        }
    }

    /**
     * 检查并请求所有文件访问权限（Android 11+）
     */
    private void checkAndRequestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle("需要所有文件访问权限")
                        .setMessage("为了正常使用打印机功能，需要您授予应用所有文件访问权限")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            Toast.makeText(MainActivity.this, "未授予所有文件访问权限，部分功能可能无法正常使用", Toast.LENGTH_LONG).show();
                        })
                        .show();
            }
        }
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "部分权限未获取，可能影响功能使用", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 处理所有文件访问权限请求结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (android.os.Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "所有文件访问权限获取成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "未授予所有文件访问权限，部分功能可能无法正常使用", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 查询指定的 SNMP OID
     * @param key 预定义的 OID 键
     * @param oid SNMP OID 值
     */
    private void querySnmpOid(String key, String oid) {
        snmpManager.getByOid(oid, new SnmpManager.SnmpCallback() {
            @Override
            public void onSuccess(String result) {
                String message = key + " (" + oid + "): " + result + "\n";
                runOnUiThread(() -> appendResult(message));
            }

            @Override
            public void onError(String error) {
                String message = key + " (" + oid + "): 错误 - " + error + "\n";
                runOnUiThread(() -> appendResult(message));
            }
        });
    }

    /**
     * 追加结果到 EditText
     * @param text 要追加的文本
     */
    private void appendResult(String text) {
        resultEditText.append(text);
        // 自动滚动到底部
        resultEditText.post(() -> {
            int scrollAmount = resultEditText.getLayout().getLineTop(resultEditText.getLineCount()) - resultEditText.getHeight();
            if (scrollAmount > 0) {
                resultEditText.scrollTo(0, scrollAmount);
            } else {
                resultEditText.scrollTo(0, 0);
            }
        });
    }
}