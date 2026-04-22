package com.cumtenn.printerlib;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cumtenn.printer.IppManager;
import com.cumtenn.printer.PrintParams;
import com.cumtenn.printer.model.Orientation;
import com.cumtenn.printer.model.PrinterStatus;
import com.cumtenn.printer.model.PrinterSupported;
import com.cumtenn.printer.model.Quality;
import com.cumtenn.printerlib.databinding.FragmentPrintBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.ranges.IntRange;

public class PrintFragment extends Fragment {

    private FragmentPrintBinding binding;
    private SharedViewModel sharedViewModel;
    private IppManager ippManager;
    private String selectedFileName;
    private String localFilePath;
    private ExecutorService executorService;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private static final String PDF_MIME_TYPE = "application/pdf";

    public static PrintFragment newInstance() {
        return new PrintFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ippManager = IppManager.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        initFilePickerLauncher();
    }

    private void initFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            handleSelectedFile(uri);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrintBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getIp().observe(getViewLifecycleOwner(), ip -> {
            if (ip != null && !ip.isEmpty()) {
                ippManager.setIp(ip);
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
        binding.btnSelectFile.setOnClickListener(v -> openFilePicker());

        binding.btnPrintSupported.setOnClickListener(v -> {
            ippManager.getPrinterSupportedAsync(new IppManager.PrinterSupportedCallBack() {
                @Override
                public void onPrinterSupported(PrinterSupported supported) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                binding.tvPrintResult.setText("supported: " + supported));
                    }
                }

                @Override
                public void onSupportedError(String errorInfo) {
                }
            });

            ippManager.getPrinterStatusAsync(requireContext(), new IppManager.PrinterStatusCallBack() {
                @Override
                public void onPrinterStatus(PrinterStatus status) {
                }

                @Override
                public void onStatusError(String errorInfo) {
                }
            });
        });

        binding.btnPrint.setOnClickListener(v -> {
            String ip = sharedViewModel.getIpValue();
            if (ip == null || ip.isEmpty()) {
                Toast.makeText(requireContext(), "请先设置 IP 地址", Toast.LENGTH_SHORT).show();
                return;
            }
            if (localFilePath == null || localFilePath.isEmpty()) {
                Toast.makeText(requireContext(), "请先选择 PDF 文件", Toast.LENGTH_SHORT).show();
                return;
            }
            printSelectedFile();
        });

        binding.btnCancelPrint.setOnClickListener(v -> {
            ippManager.cancelPrint();
            binding.btnCancelPrint.setEnabled(false);
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(PDF_MIME_TYPE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    private void handleSelectedFile(Uri uri) {
        binding.tvSelectedFile.setText("正在处理文件...");
        executorService.execute(() -> {
            try {
                selectedFileName = getFileNameFromUri(uri);
                localFilePath = saveFileToPrivateDir(uri, selectedFileName);
                long fileSize = new File(localFilePath).length();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.tvSelectedFile.setText("已选择文件: " + selectedFileName + " (" + fileSize + " bytes)");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "文件保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "unknown.pdf";
        ContentResolver resolver = requireContext().getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }

    private String saveFileToPrivateDir(Uri uri, String fileName) throws IOException {
        ContentResolver resolver = requireContext().getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("无法打开输入流");
        }

        File privateDir = requireContext().getFilesDir();
        File outputFile = new File(privateDir, fileName);
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        return outputFile.getAbsolutePath();
    }

    private void printSelectedFile() {
        binding.tvPrintResult.setText("正在打印...");

        PrintParams params = new PrintParams.Builder()
                .setJobName("Test Print")
                .setCopies(1)
//                .setRange(new IntRange(1, 16))
                .setSides("one-sided")
                .setMedia("iso_a4_210x297mm") // iso_a4_210x297mm iso_a5_148x210mm
                .setDocumentFormat("application/pdf")
                .setColorMode("monochrome")
                .setOrientation(Orientation.Portrait)
                .setQuality(Quality.Normal)
                .setCompression("none")
                .build();

        ippManager.getPrinterStatusAsync(requireContext(), new IppManager.PrinterStatusCallBack() {
            @Override
            public void onPrinterStatus(PrinterStatus status) {
            }

            @Override
            public void onStatusError(String errorInfo) {
            }
        });

        ippManager.printFile(requireContext(), localFilePath, params, new IppManager.PrinterCallBack() {
            @Override
            public void onPrinterError(String errorInfo) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            binding.tvPrintResult.setText("打印结果：打印失败 - " + errorInfo));
                }
            }

            @Override
            public void onPrinterSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            binding.tvPrintResult.setText("打印结果：打印成功"));
                }
            }

            @Override
            public void onPrinterStart() {
            }
        });
    }
}
