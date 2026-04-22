package com.cumtenn.printerlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.cumtenn.printer.IppManager;
import com.cumtenn.printer.SnmpManager;
import com.cumtenn.printerlib.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;

    private ActivityMainBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        checkAndRequestPermissions();
        setupViewPager();
        setupIpButton();
    }

    private void setupViewPager() {
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return SnmpFragment.newInstance();
                } else {
                    return PrintFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        };

        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("SNMP 查询");
            } else {
                tab.setText("打印");
            }
        }).attach();
    }

    private void setupIpButton() {
        binding.btnSetIp.setOnClickListener(v -> {
            String ip = binding.etIp.getText().toString().trim();
            if (!ip.isEmpty()) {
                sharedViewModel.setIp(ip);
                SnmpManager.getInstance().setIp(ip);
                IppManager.getInstance().setIp(ip);
                Toast.makeText(MainActivity.this, "IP 设置成功: " + ip, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "请输入有效的 IP 地址", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.INTERNET);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkAndRequestManageExternalStoragePermission();
        }
    }

    private void checkAndRequestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle("需要所有文件访问权限")
                        .setMessage("为了正常使用打印机功能，需要您授予应用所有文件访问权限")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            android.content.Intent intent = new android.content.Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            Toast.makeText(MainActivity.this, "未授予所有文件访问权限，部分功能可能无法正常使用", Toast.LENGTH_LONG).show();
                        })
                        .show();
            }
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
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
}
