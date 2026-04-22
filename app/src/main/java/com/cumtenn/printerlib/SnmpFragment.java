package com.cumtenn.printerlib;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cumtenn.printer.SnmpManager;
import com.cumtenn.printerlib.databinding.FragmentSnmpBinding;

public class SnmpFragment extends Fragment {

    private FragmentSnmpBinding binding;
    private SharedViewModel sharedViewModel;
    private SnmpManager snmpManager;

    public static SnmpFragment newInstance() {
        return new SnmpFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snmpManager = SnmpManager.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSnmpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getIp().observe(getViewLifecycleOwner(), ip -> {
            if (ip != null && !ip.isEmpty()) {
                snmpManager.setIp(ip);
            }
        });
        setupButtonListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupButtonListeners() {
        binding.btnSnmpReady.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询就绪状态...\n");
            querySnmpOid(SnmpManager.READY, SnmpManager.OID_MAP.get(SnmpManager.READY));
        });

        binding.btnSnmpIdle.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询空闲状态...\n");
            querySnmpOid(SnmpManager.IDLE, SnmpManager.OID_MAP.get(SnmpManager.IDLE));
        });

        binding.btnSnmpPrintTotal.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询打印总计数...\n");
            querySnmpOid(SnmpManager.PRINT_TOTAL_COUNT, SnmpManager.OID_MAP.get(SnmpManager.PRINT_TOTAL_COUNT));
        });

        binding.btnSnmpWakeState.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询唤醒状态...\n");
            querySnmpOid(SnmpManager.WAKE_STATE_SET, SnmpManager.OID_MAP.get(SnmpManager.WAKE_STATE_SET));
        });

        binding.btnSnmpYellowFull.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询黄色耗材满值...\n");
            querySnmpOid(SnmpManager.YELLOW_FULL, SnmpManager.OID_MAP.get(SnmpManager.YELLOW_FULL));
        });

        binding.btnSnmpYellowRemain.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询黄色耗材剩余...\n");
            querySnmpOid(SnmpManager.YELLOW_REMAIN, SnmpManager.OID_MAP.get(SnmpManager.YELLOW_REMAIN));
        });

        binding.btnSnmpRedFull.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询红色耗材满值...\n");
            querySnmpOid(SnmpManager.RED_FULL, SnmpManager.OID_MAP.get(SnmpManager.RED_FULL));
        });

        binding.btnSnmpRedRemain.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询红色耗材剩余...\n");
            querySnmpOid(SnmpManager.RED_REMAIN, SnmpManager.OID_MAP.get(SnmpManager.RED_REMAIN));
        });

        binding.btnSnmpCyanFull.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询青色耗材满值...\n");
            querySnmpOid(SnmpManager.CYAN_FULL, SnmpManager.OID_MAP.get(SnmpManager.CYAN_FULL));
        });

        binding.btnSnmpCyanRemain.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询青色耗材剩余...\n");
            querySnmpOid(SnmpManager.CYAN_REMAIN, SnmpManager.OID_MAP.get(SnmpManager.CYAN_REMAIN));
        });

        binding.btnSnmpBlackFull.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询黑色耗材满值...\n");
            querySnmpOid(SnmpManager.BLACK_FULL, SnmpManager.OID_MAP.get(SnmpManager.BLACK_FULL));
        });

        binding.btnSnmpBlackRemain.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询黑色耗材剩余...\n");
            querySnmpOid(SnmpManager.BLACK_REMAIN, SnmpManager.OID_MAP.get(SnmpManager.BLACK_REMAIN));
        });

        binding.btnSnmpCurrentJob.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询当前作业 ID...\n");
            querySnmpOid(SnmpManager.CURRENT_JOB_ID, SnmpManager.OID_MAP.get(SnmpManager.CURRENT_JOB_ID));
        });

        binding.btnSnmpCancelJob.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("执行取消作业...\n");
            querySnmpOid(SnmpManager.CANCEL_JOB, SnmpManager.OID_MAP.get(SnmpManager.CANCEL_JOB));
        });

        binding.btnSnmpOutOfPaper.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            appendResult("查询缺纸状态...\n");
            querySnmpOid(SnmpManager.OUT_OF_PAPER, SnmpManager.OID_MAP.get(SnmpManager.OUT_OF_PAPER));
        });
    }

    private void querySnmpOid(String key, String oid) {
        snmpManager.getByOid(oid, new SnmpManager.SnmpCallback() {
            @Override
            public void onSuccess(String result) {
                String message = key + " (" + oid + "): " + result + "\n";
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> appendResult(message));
                }
            }

            @Override
            public void onError(String error) {
                String message = key + " (" + oid + "): 错误 - " + error + "\n";
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> appendResult(message));
                }
            }
        });
    }

    private void appendResult(String text) {
        binding.resultEdittext.append(text);
        binding.resultScrollview.post(() -> binding.resultScrollview.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
