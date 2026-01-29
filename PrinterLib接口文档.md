# PrinterLib 接口文档

## 1. Android Studio 引入 AAR 文件

### 1.1 本地引入方式

1. 将 `printer.aar` 文件复制到项目的 `libs` 目录下（如果没有该目录，手动创建）

2. 在模块的 `build.gradle` 文件中添加以下配置：

```groovy
android {
    // 其他配置...
    repositories {
        flatDir {
            dirs 'libs' // 声明使用本地 libs 目录
        }
    }
}

dependencies {
    // 其他依赖...
    implementation(name: 'printer', ext: 'aar')
}
```

3. 同步项目（Sync Project with Gradle Files）

### 1.2 Maven 仓库引入方式

如果 AAR 文件已发布到 Maven 仓库，可直接在 `build.gradle` 中添加依赖：

```groovy
dependencies {
    // 其他依赖...
    implementation 'com.cumtenn:printer:版本号'
}
```

## 2. API 接口说明

### 2.1 IppManager

#### 2.1.1 类概述

- **功能**：处理 IPP（Internet Printing Protocol）打印相关操作
- **设计模式**：单例模式
- **包路径**：`com.cumtenn.printer.IppManager`

#### 2.1.2 核心方法

##### 获取实例

```java
IppManager manager = IppManager.getInstance();
```

##### 设置打印机 IP

```java
manager.setIp("192.168.1.100");
```

##### 设置打印机端口（可选，默认 631）

```java
manager.setPort(631);
```

##### 打印文件

```java
// 创建打印参数
PrintParams params = new PrintParams.Builder()
    .setCopies(1)
    .setSides("one-sided")
    .setMedia("iso_a4_210x297mm")
    .setDocumentFormat("application/pdf")
    .build();

// 调用打印方法
manager.printFile(context, filePath, params, new IppManager.PrinterCallBack() {
    @Override
    public void onPrinterError(String errorInfo) {
        // 打印错误回调
        Log.e("PrintError", errorInfo);
    }

    @Override
    public void onPrinterSuccess() {
        // 打印成功回调
        Log.i("PrintSuccess", "打印完成");
    }
});
```

##### 获取打印机支持的功能

```java
// 同步方式
PrinterSupported supported = manager.getPrinterSupported();
List<String> mediaList = supported.getMediaSupportedList();
List<String> sidesList = supported.getSidesSupportedList();
boolean isColorSupported = supported.isColorSupported();

// 异步方式
manager.getPrinterSupportedAsync(new IppManager.PrinterSupportedCallBack() {
    @Override
    public void onPrinterSupported(PrinterSupported supported) {
        // 处理打印机支持的功能
        Log.i("Print", "打印机支持的纸张类型: " + supported.getMediaSupportedList());
    }

    @Override
    public void onSupportedError(String errorInfo) {
        // 处理错误
        Log.e("Print", "获取支持功能失败: " + errorInfo);
    }
});
```

##### 获取打印机状态

```java
// 同步方式
PrinterStatus status = manager.getPrinterStatus();
PrinterState state = status.getState();
String stateMessage = status.getStateMessage();
boolean isError = status.isError();

// 异步方式
manager.getPrinterStatusAsync(new IppManager.PrinterStatusCallBack() {
    @Override
    public void onPrinterStatus(PrinterStatus status) {
        // 处理打印机状态
        Log.i("Print", "打印机状态: " + status.getStateMessage());
    }

    @Override
    public void onStatusError(String errorInfo) {
        // 处理错误
        Log.e("Print", "获取状态失败: " + errorInfo);
    }
});
```

##### 释放资源

```java
manager.release();
```

#### 2.1.3 PrintParams 类

用于配置打印参数，采用 Builder 模式：

```java
PrintParams params = new PrintParams.Builder()
    .setCopies(2)                    // 打印份数
    .setSides("two-sided-long-edge") // 双面打印，长边装订
    .setMedia("iso_a4_210x297mm")    // A4 纸张
    .setQuality(PrintQuality.High)   // 高质量打印
    .setColorMode(ColorMode.Color)   // 彩色打印
    .setOrientation(Orientation.Landscape) // 横向打印
    .setRange(new IntRange(1, 5))    // 打印 1-5 页
    .build();
```

#### 2.1.4 回调接口

##### PrinterCallBack

用于打印操作的回调：

```java
public interface PrinterCallBack {
    void onPrinterError(String errorInfo);              // 打印错误回调
    void onPrinterSuccess();                            // 打印成功回调
}
```

##### PrinterSupportedCallBack

用于获取打印机支持功能的回调：

```java
public interface PrinterSupportedCallBack {
    void onPrinterSupported(PrinterSupported supported); // 打印机支持功能回调
    void onSupportedError(String errorInfo);             // 获取支持功能错误回调
}
```

##### PrinterStatusCallBack

用于获取打印机状态的回调：

```java
public interface PrinterStatusCallBack {
    void onPrinterStatus(PrinterStatus status);         // 打印机状态回调
    void onStatusError(String errorInfo);               // 获取状态错误回调
}
```

### 2.2 SnmpManager

#### 2.2.1 类概述

- **功能**：处理 SNMP（Simple Network Management Protocol）相关操作
- **设计模式**：单例模式
- **包路径**：`com.cumtenn.printer.SnmpManager`

#### 2.2.2 预定义常量

```java
// 打印机状态相关
SnmpManager.READY              // 就绪状态 OID
SnmpManager.IDLE               // 空闲状态 OID
SnmpManager.PRINT_TOTAL_COUNT  // 打印总计数 OID
SnmpManager.WAKE_STATE_SET     // 唤醒状态设置 OID

// 耗材相关
SnmpManager.YELLOW_FULL        // 黄色耗材满值 OID
SnmpManager.YELLOW_REMAIN      // 黄色耗材剩余 OID
SnmpManager.RED_FULL           // 红色耗材满值 OID
SnmpManager.RED_REMAIN         // 红色耗材剩余 OID
SnmpManager.CYAN_FULL          // 青色耗材满值 OID
SnmpManager.CYAN_REMAIN        // 青色耗材剩余 OID
SnmpManager.BLACK_FULL         // 黑色耗材满值 OID
SnmpManager.BLACK_REMAIN       // 黑色耗材剩余 OID

// 其他
SnmpManager.CURRENT_JOB_ID     // 当前作业 ID OID
SnmpManager.CANCEL_JOB         // 取消作业 OID
SnmpManager.OUT_OF_PAPER       // 缺纸状态 OID
```

#### 2.2.3 核心方法

##### 获取实例

```java
SnmpManager manager = SnmpManager.getInstance();
```

##### 设置 SNMP 参数

```java
// 必选：设置打印机 IP
manager.setIp("192.168.1.100");

// 可选：设置 SNMP 社区名（默认 "public"）
manager.setCommunity("public");

// 可选：设置 SNMP 端口（默认 161）
manager.setPort(161);

// 可选：设置超时时间（默认 2000ms）
manager.setTimeout(3000);

// 可选：设置重试次数（默认 2 次）
manager.setRetries(3);
```

##### 通过预定义 Key 获取打印机状态

```java
manager.getByKey(SnmpManager.READY, new SnmpManager.SnmpCallback() {
    @Override
    public void onSuccess(@NonNull String result) {
        // 处理成功结果
        Log.i("SNMP", "打印机就绪状态: " + result);
    }

    @Override
    public void onError(@NonNull String error) {
        // 处理错误
        Log.e("SNMP", "获取状态失败: " + error);
    }
});
```

##### 通过 OID 直接获取打印机状态

```java
String oid = "1.3.6.1.2.1.25.3.5.1.2.1";
manager.getByOid(oid, new SnmpManager.SnmpCallback() {
    @Override
    public void onSuccess(@NonNull String result) {
        // 处理成功结果
        Log.i("SNMP", "OID " + oid + " 结果: " + result);
    }

    @Override
    public void onError(@NonNull String error) {
        // 处理错误
        Log.e("SNMP", "获取 OID 失败: " + error);
    }
});
```

##### 释放资源

```java
manager.release();
```

#### 2.2.4 SnmpCallback 接口

```java
public interface SnmpCallback {
    void onSuccess(@NonNull String result); // 成功回调，返回 SNMP 查询结果
    void onError(@NonNull String error);    // 错误回调，返回错误信息
}
```

## 3. 使用示例

### 3.1 IPP 打印示例

```java
// 初始化 IppManager
IppManager ippManager = IppManager.getInstance();
ippManager.setIp("192.168.1.100");

// 创建打印参数
PrintParams params = new PrintParams.Builder()
    .setCopies(1)
    .setSides("one-sided")
    .setMedia("iso_a4_210x297mm")
    .setDocumentFormat("application/pdf")
    .build();

// 打印文件
String filePath = "sdcard/Download/test.pdf";
ippManager.printFile(getApplicationContext(), filePath, params, new IppManager.PrinterCallBack() {
    @Override
    public void onPrinterError(String errorInfo) {
        Log.e("Print", "打印失败: " + errorInfo);
    }

    @Override
    public void onPrinterSuccess() {
        Log.i("Print", "打印成功");
    }
});

// 异步获取打印机状态
ippManager.getPrinterStatusAsync(new IppManager.PrinterStatusCallBack() {
    @Override
    public void onPrinterStatus(PrinterStatus status) {
        Log.i("Print", "打印机状态: " + status.getStateMessage());
    }

    @Override
    public void onStatusError(String errorInfo) {
        Log.e("Print", "获取状态失败: " + errorInfo);
    }
});

// 异步获取打印机支持功能
ippManager.getPrinterSupportedAsync(new IppManager.PrinterSupportedCallBack() {
    @Override
    public void onPrinterSupported(PrinterSupported supported) {
        Log.i("Print", "打印机支持的纸张类型: " + supported.getMediaSupportedList());
    }

    @Override
    public void onSupportedError(String errorInfo) {
        Log.e("Print", "获取支持功能失败: " + errorInfo);
    }
});
```

### 3.2 SNMP 查询示例

```java
// 初始化 SnmpManager
SnmpManager snmpManager = SnmpManager.getInstance();
snmpManager.setIp("192.168.1.100");
snmpManager.setCommunity("public");

// 查询打印机总打印计数
snmpManager.getByKey(SnmpManager.PRINT_TOTAL_COUNT, new SnmpManager.SnmpCallback() {
    @Override
    public void onSuccess(@NonNull String result) {
        Log.i("SNMP", "总打印页数: " + result);
    }

    @Override
    public void onError(@NonNull String error) {
        Log.e("SNMP", "查询失败: " + error);
    }
});

// 查询黑色耗材剩余
snmpManager.getByKey(SnmpManager.BLACK_REMAIN, new SnmpManager.SnmpCallback() {
    @Override
    public void onSuccess(@NonNull String result) {
        Log.i("SNMP", "黑色耗材剩余: " + result);
    }

    @Override
    public void onError(@NonNull String error) {
        Log.e("SNMP", "查询失败: " + error);
    }
});
```

## 4. 注意事项

1. **权限要求**：
   - 网络权限：`android.permission.INTERNET`
   - 访问存储权限（如果需要打印本地文件）：`android.permission.READ_EXTERNAL_STORAGE` 或 `android.permission.READ_MEDIA_IMAGES`（Android 13+）

2. **线程安全**：
   - 所有异步方法都在内部线程池中执行，回调会在后台线程返回，如需更新UI，需切换到主线程

3. **资源释放**：
   - 在不再使用管理器时，调用 `release()` 方法释放资源，避免内存泄漏

4. **异常处理**：
   - 同步方法可能抛出异常，建议使用 try-catch 块捕获
   - 异步方法的异常会通过回调的 `onError` 方法返回

5. **版本兼容性**：
   - 建议使用 Android 6.0（API 23）及以上版本
   - 如需支持更低版本，需自行处理权限请求等兼容性问题

## 5. 故障排查

1. **连接失败**：
   - 检查打印机 IP 是否正确
   - 检查网络连接是否正常
   - 检查打印机是否开启了对应协议（IPP/SNMP）

2. **打印失败**：
   - 检查文件格式是否被打印机支持
   - 检查打印参数是否符合打印机要求
   - 检查打印机是否处于正常状态

3. **SNMP 查询失败**：
   - 检查 SNMP 社区名是否正确
   - 检查打印机是否开启了 SNMP 服务
   - 检查 OID 是否被打印机支持

## 6. 更新日志

- **版本 1.0.0**：
  - 初始版本
  - 支持 IPP 打印功能
  - 支持 SNMP 状态查询功能
