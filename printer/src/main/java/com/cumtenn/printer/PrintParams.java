package com.cumtenn.printer;

import com.cumtenn.printer.model.Orientation;
import com.cumtenn.printer.model.Quality;

import kotlin.ranges.IntRange;

public class PrintParams {
    // 打印份数
    private int copies;

    // 任务名称
    private String jobName;

    // 单双面
    private String sides;

    // 文档格式
    private String documentFormat;

    // 压缩格式
    private String compression;

    // 纸张
    private String media;

    // 打印范围
    private IntRange range;

    // 颜色
    private String colorMode;

    // 方向
    private Orientation orientation;

    // 打印质量
    private Quality quality;

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public void setRange(IntRange range) {
        this.range = range;
    }

    // 私有构造函数，只能通过Builder创建
    private PrintParams(Builder builder) {
        this.copies = builder.copies;
        this.jobName = builder.jobName;
        this.sides = builder.sides;
        this.documentFormat = builder.documentFormat;
        this.compression = builder.compression;
        this.media = builder.media;
        this.range = builder.range;
        this.colorMode = builder.colorMode;
        this.orientation = builder.orientation;
        this.quality = builder.quality;
    }

    // Builder内部类
    public static class Builder {
        // 打印份数
        private int copies = 1;

        // 任务名称
        private String jobName;

        // 单双面
        private String sides = "one-sided";

        // 文档格式
        private String documentFormat = "application/pdf";

        // 压缩格式
        private String compression = "none";

        // 纸张
        private String media = "iso_a4_210x297mm";

        // 打印范围
        private IntRange range;

        // 颜色
        private String colorMode = "auto";

        // 方向
        private Orientation orientation = Orientation.Portrait;

        // 打印质量
        private Quality quality = Quality.Normal;

        // 构造函数
        public Builder() {
        }

        // 设置打印份数
        public Builder setCopies(int copies) {
            this.copies = copies;
            return this;
        }

        // 设置任务名称
        public Builder setJobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        // 设置单双面
        public Builder setSides(String sides) {
            this.sides = sides;
            return this;
        }

        // 设置打印质量
        public Builder setQuality(Quality quality) {
            this.quality = quality;
            return this;
        }

        // 设置文档格式
        public Builder setDocumentFormat(String documentFormat) {
            this.documentFormat = documentFormat;
            return this;
        }

        // 设置压缩格式
        public Builder setCompression(String compression) {
            this.compression = compression;
            return this;
        }

        // 设置纸张
        public Builder setMedia(String media) {
            this.media = media;
            return this;
        }

        // 设置打印范围
        public Builder setRange(IntRange range) {
            this.range = range;
            return this;
        }

        // 设置颜色
        public Builder setColorMode(String colorMode) {
            this.colorMode = colorMode;
            return this;
        }

        // 设置方向
        public Builder setOrientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        // 构建PrintParams实例
        public PrintParams build() {
            return new PrintParams(this);
        }
    }

    // Getter方法
    public int getCopies() {
        return copies;
    }

    public String getJobName() {
        return jobName;
    }

    public String getSides() {
        return sides;
    }

    public String getDocumentFormat() {
        return documentFormat;
    }

    public String getCompression() {
        return compression;
    }

    public String getMedia() {
        return media;
    }

    public IntRange getRange() {
        return range;
    }

    public String getColorMode() {
        return colorMode;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Quality getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return "PrintParams{" +
                "copies=" + copies +
                ", jobName='" + jobName + '\'' +
                ", sides='" + sides + '\'' +
                ", documentFormat='" + documentFormat + '\'' +
                ", compression=" + compression +
                ", media='" + media + '\'' +
                ", range=" + range +
                ", colorMode=" + colorMode +
                ", orientation=" + orientation +
                '}';
    }
}
