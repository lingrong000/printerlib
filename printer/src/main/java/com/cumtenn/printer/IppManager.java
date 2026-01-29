package com.cumtenn.printer;

import static com.cumtenn.printer.utils.NetworkUtil.isIpAddressValid;
import static de.gmuth.ipp.attributes.TemplateAttributes.copies;
import static de.gmuth.ipp.attributes.TemplateAttributes.jobName;
import static de.gmuth.ipp.attributes.TemplateAttributes.orientationRequested;
import static de.gmuth.ipp.attributes.TemplateAttributes.pageRanges;

import android.content.Context;
import android.util.Log;

import com.cumtenn.printer.model.PrinterStatus;
import com.cumtenn.printer.model.PrinterSupported;
import com.cumtenn.printer.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import de.gmuth.ipp.attributes.ColorMode;
import de.gmuth.ipp.attributes.Compression;
import de.gmuth.ipp.attributes.DocumentFormat;
import de.gmuth.ipp.attributes.Media;
import de.gmuth.ipp.attributes.Orientation;
import de.gmuth.ipp.attributes.PrintQuality;
import de.gmuth.ipp.attributes.PrinterState;
import de.gmuth.ipp.attributes.Sides;
import de.gmuth.ipp.client.IppJob;
import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.ipp.core.IppAttributeBuilder;
import kotlin.ranges.IntRange;

public class IppManager {
    private static final String TAG = "IppManager";

    private static volatile IppManager instance;

    private String ip;
    private int port = 631;

    private String printUri;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private IppManager() {
    }

    public static IppManager getInstance() {
        if (instance == null) {
            synchronized (IppManager.class) {
                if (instance == null) {
                    instance = new IppManager();
                }
            }
        }
        return instance;
    }


    public void printFile(Context context, String filePath, PrintParams params, PrinterCallBack callBack) {
        if (!isIpAddressValid(ip)) {
            throw new IllegalArgumentException("invalid ip：" + ip);
        }

        if (callBack == null) {
            throw new IllegalArgumentException("callback is null");
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "start print. ip: " + ip + " file: " + filePath + " params: " + params);

                // 参数检查
                PrinterSupported supported = getPrinterSupported();
                List<String> mediaList = supported.getMediaSupportedList();
                if (!mediaList.isEmpty() && !mediaList.contains(params.getMedia())) {
                    callBack.onPrinterError("Media: " + params.getMedia() + " is not supported");
                    return;
                }
                List<String> sidesList = supported.getSidesSupportedList();
                if (!sidesList.isEmpty() && !sidesList.contains(params.getSides())) {
                    callBack.onPrinterError("Sides: " + params.getSides() + " is not supported");
                    return;
                }
                List<String> documentList = supported.getDocumentFormatSupportedList();
                if (!documentList.isEmpty() && !documentList.contains(params.getDocumentFormat())) {
                    callBack.onPrinterError("DocumentFormat: " + params.getDocumentFormat() + " is not supported");
                    return;
                }
                if (!supported.isColorSupported() && params.getColorMode().equals("color")) {
                    callBack.onPrinterError("Color is not supported");
                    return;
                }

                // 打印机状态检查
                PrinterStatus status = getPrinterStatus();
                if (status.getState() != PrinterStatus.State.Idle) {
                    callBack.onPrinterError("Printing, please wait");
                    return;
                }
                if (status.isError()) {
                    callBack.onPrinterError(status.getReasonList().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(", "))
                    );
                    return;
                }

                // 开始打印
                File file = new File(filePath);
                int pages = FileUtil.getPdfFilePages(file);
                Log.d(TAG, "pages: " + pages);
                if (pages == 0) {
                    callBack.onPrinterError("Empty PDF");
                    return;
                }
                if (pages < FileUtil.CHUNK_PAGES) {
                    printSingleFile(file, params);
                } else {
                    printLargeFile(context, file, pages, params);
                }
                // 打印完成后，检查打印机是否有报错
                status = getPrinterStatus();
                if (status.isError()) {
                    String error = status.getReasonList().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(", "));
                    callBack.onPrinterError(error);
                } else{
                    callBack.onPrinterSuccess();
                }

            } catch (Exception e) {
                e.printStackTrace();
                callBack.onPrinterError("Print error: " + e);
            }
        });
    }

    private void printSingleFile(File file, PrintParams params) {
        IppAttributeBuilder[] builders = new IppAttributeBuilder[]{
                copies(params.getCopies()),
                jobName(file.getName()),
                pageRanges(params.getRange()),
                orientationRequested(Orientation.fromInt(params.getOrientation().getCode())),
                Sides.fromKeyword(params.getSides()),
                new ColorMode(params.getColorMode()),
                PrintQuality.fromInt(params.getQuality().getCode()),
                new DocumentFormat(params.getDocumentFormat()),
                Compression.fromString(params.getCompression()),
                new Media(params.getMedia())};

        IppPrinter ippPrinter = new IppPrinter(printUri);
        IppJob job = ippPrinter.printJob(file, builders, null);
        job.waitForTermination();
    }

    private void printLargeFile(Context context, File file, int pages, PrintParams params) throws IOException {
        File outDir = new File(context.getFilesDir(), "split_output");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        List<File> fileList = FileUtil.splitPdf(file, outDir);
        IntRange totalRange = params.getRange();

        int copies = params.getCopies();
        for (int i = 0; i < copies; i++) {
            // 文件分割后，还是需要按顺序一份份打印
            for (int j = 0; j < fileList.size(); j++) {
                // 重新计算range
                IntRange partRange;
                if (j == fileList.size() - 1) {
                    partRange = new IntRange(j * FileUtil.CHUNK_PAGES, pages - 1);
                } else {
                    partRange = new IntRange(j * FileUtil.CHUNK_PAGES, (j + 1) * FileUtil.CHUNK_PAGES - 1);
                }

                IntRange interRange = getIntersectionRange(totalRange, partRange);
                if (interRange == null) {
                    continue;
                }
                IntRange printRange = new IntRange(interRange.getStart() - j * FileUtil.CHUNK_PAGES,
                        interRange.getEndInclusive() - j * FileUtil.CHUNK_PAGES);

                params.setCopies(1);
                params.setRange(printRange);

                Log.i(TAG, "print: " + fileList.get(j).getAbsolutePath() + " range: " + printRange);

                printSingleFile(fileList.get(j), params);
            }
        }

        // 打印完成后，删除临时的分割文件
        for (File f : fileList) {
            f.delete();
        }
    }

    public PrinterSupported getPrinterSupported() {
        if (!isIpAddressValid(ip)) {
            throw new IllegalArgumentException("invalid ip：" + ip);
        }
        IppPrinter ippPrinter = new IppPrinter(printUri);

        PrinterSupported supported = new PrinterSupported();
        supported.setMediaSupportedList(ippPrinter.getMediaSupported());
        supported.setSidesSupportedList(ippPrinter.getSidesSupported());
        supported.setDocumentFormatSupportedList(ippPrinter.getDocumentFormatSupported());
        supported.setColorSupported(ippPrinter.getColorSupported());

        supported.setCompressList(ippPrinter.getCompressionSupported());
        supported.setQualityList(ippPrinter.getQualitySupported());
        supported.setOrientationList(ippPrinter.getOrientationSupported());

        return supported;
    }

    public void getPrinterSupportedAsync(PrinterSupportedCallBack callBack) {
        if (callBack == null) {
            throw new IllegalArgumentException("callback is null");
        }

        executor.execute(() -> {
            try {
                PrinterSupported supported = getPrinterSupported();
                callBack.onPrinterSupported(supported);
            } catch (Exception e) {
                e.printStackTrace();
                callBack.onSupportedError(e.toString());
            }
        });
    }

    public PrinterStatus getPrinterStatus() {
        if (!isIpAddressValid(ip)) {
            throw new IllegalArgumentException("invalid ip：" + ip);
        }
        IppPrinter ippPrinter = new IppPrinter(printUri);

        PrinterStatus status = new PrinterStatus();
        status.setState(ippPrinter.getState());
        status.setStateMessage(Objects.requireNonNull(ippPrinter.getStateMessage()).getText());
        List<String> reasonList = ippPrinter.getStateReasons();
        status.setReasonList(reasonList);
        if (!reasonList.isEmpty()) {
            status.setError(!reasonList.get(0).equals("none"));
        }

        return status;
    }

    public void getPrinterStatusAsync(PrinterStatusCallBack callBack) {
        if (callBack == null) {
            throw new IllegalArgumentException("callback is null");
        }

        executor.execute(() -> {
            try {
                PrinterStatus status = getPrinterStatus();
                callBack.onPrinterStatus(status);
            } catch (Exception e) {
                e.printStackTrace();
                callBack.onStatusError(e.toString());
            }
        });
    }


    public void setIp(String ip) {
        this.ip = ip;
        printUri = "ipp://" + ip + ":" + port + "/ipp/print";
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getPrintUri() {
        return printUri;
    }

    public void setPort(int port) {
        this.port = port;
        printUri = "ipp://" + ip + ":" + port + "/ipp/print";
    }

    public void release() {
        executor.shutdown();
    }

    private IntRange getIntersectionRange(IntRange range1, IntRange range2) {
        int start = Math.max(range1.getStart(), range2.getStart());
        int end = Math.min(range1.getEndInclusive(), range2.getEndInclusive());
        if (end < start) {
            return null;
        }
        return new IntRange(start, end);
    }

    public interface PrinterCallBack {
        void onPrinterError(String errorInfo);

        void onPrinterSuccess();
    }

    public interface PrinterSupportedCallBack {
        void onPrinterSupported(PrinterSupported supported);

        void onSupportedError(String errorInfo);
    }

    public interface PrinterStatusCallBack {
        void onPrinterStatus(PrinterStatus status);

        void onStatusError(String errorInfo);
    }
}
