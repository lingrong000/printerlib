package com.cumtenn.printer.utils;

import android.util.Log;

import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    private static final String TAG = "FileUtil";
    public static final int CHUNK_PAGES = 100;

    public static List<File> splitPdf(File inputFile, File outputDir) throws IOException {
        if (inputFile == null || !inputFile.exists()) {
            throw new IllegalArgumentException("inputFile is null or does not exist");
        }

        if (outputDir == null) {
            outputDir = inputFile.getParentFile();
        }
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("Cannot create output directory: " + outputDir.getAbsolutePath());
            }
        }

        List<File> results = new ArrayList<>();
        try (PDDocument document = PDDocument.load(inputFile)) {
            int totalPages = document.getNumberOfPages();
            if (totalPages == 0) {
                throw new IOException("Empty PDF");
            }

            // 计算需要分割成多少个部分
            int parts = (totalPages + CHUNK_PAGES - 1) / CHUNK_PAGES;

            // 我们使用 PDFBox 的 Splitter，设置 splitAtPage
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(CHUNK_PAGES);
            // split 返回每个子文档的 List<PDDocument>
            List<PDDocument> docs = splitter.split(document);

            String baseName = getBaseName(inputFile.getName()); // 不带扩展名
            int idx = 1;
            for (PDDocument pd : docs) {
                String outName = String.format("%s_part%02d.pdf", baseName, idx);
                File outFile = new File(outputDir, outName);
                try (pd) {
                    pd.save(outFile.getAbsolutePath());
                    results.add(outFile);
                } catch (Exception e) {
                    Log.e(TAG, "save file error: " + e);
                }
                idx++;
            }

            return results;
        } catch (Exception e) {
            Log.e(TAG, "split file error: " + e);
        }
        return results;
    }

    public static int getPdfFilePages(File inputFile) throws IOException {
        if (inputFile == null || !inputFile.exists()) {
            throw new IllegalArgumentException("inputFile is null or does not exist");
        }
        try (PDDocument document = PDDocument.load(inputFile)) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            Log.e(TAG, "read file error: " + e);
        }
        return 0;
    }

    private static String getBaseName(String filename) {
        if (filename == null) return "output";
        int dot = filename.lastIndexOf('.');
        if (dot > 0) return filename.substring(0, dot);
        return filename;
    }
}
