package com.jetbrains.help.context;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jetbrains.help.util.FileTools;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j(topic = "插件上下文")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginsContextHolder {

    private static final String PLUGIN_BASIC_URL = "https://plugins.jetbrains.com";

    private static final String PLUGIN_LIST_URL = PLUGIN_BASIC_URL + "/api/searchPlugins?max=10000&offset=0&orderBy=id&pricingModels=PAID&pricingModels=FREEMIUM&excludeTags=Profiling";

    private static final String PLUGIN_INFO_URL = PLUGIN_BASIC_URL + "/api/plugins/";

    private static final String PLUGIN_JSON_FILE_NAME = "external/data/plugin.json";

    private static List<PluginCache> pluginCacheList;

    private static File pluginsJsonFile;

    public static void init() {
        log.info("初始化中...");
        pluginsJsonFile = FileTools.getFileOrCreat(PLUGIN_JSON_FILE_NAME);

        String pluginJsonArray;
        try {
            pluginJsonArray = IoUtil.readUtf8(FileUtil.getInputStream(pluginsJsonFile));
        } catch (IORuntimeException e) {
            throw new IllegalArgumentException(CharSequenceUtil.format("{} 文件读取失败!", PLUGIN_JSON_FILE_NAME), e);
        }
        if (CharSequenceUtil.isBlank(pluginJsonArray) || !JSONUtil.isTypeJSON(pluginJsonArray)) {
            refreshJsonFile();
        } else {
            pluginCacheList = JSONUtil.toList(pluginJsonArray, PluginCache.class);
            log.info("初始化成功!");
            refreshJsonFile();
        }
    }

    public static List<PluginCache> pluginCacheList() {
        return PluginsContextHolder.pluginCacheList;
    }

    public static void refreshJsonFile() {
        log.info("从'JetBrains.com'刷新中...");
        CompletableFuture
                .supplyAsync(PluginsContextHolder::pluginList)
                .thenApply(PluginsContextHolder::pluginConversion)
                .thenAccept(PluginsContextHolder::overrideJsonFile)
                .thenRun(() -> log.info("刷新成功!"))
                .exceptionally(throwable -> {
                    log.error("刷新失败!", throwable);
                    return null;
                });
    }

    public static void overrideJsonFile(List<PluginCache> pluginCaches) {
        pluginCacheList = pluginCaches;
        String jsonStr = JSONUtil.toJsonStr(pluginCacheList);
        try {
            FileUtil.writeString(JSONUtil.formatJsonStr(jsonStr), pluginsJsonFile, StandardCharsets.UTF_8);
            log.info("Json文件已覆写!");
        } catch (IORuntimeException e) {
            throw new IllegalArgumentException(CharSequenceUtil.format("{} 文件写入失败!", PLUGIN_JSON_FILE_NAME), e);
        }
    }

    public static PluginList pluginList() {
        String body = HttpUtil.get(PLUGIN_LIST_URL);
        return JSONUtil.toBean(body, PluginList.class);
    }

    public static List<PluginCache> pluginConversion(PluginList pluginList) {
        List<PluginCache> list = pluginList
                .getPlugins()
                .stream()
                .parallel()
                .map(plugin -> {
                    String productCode = pluginInfo(plugin.getId()).getPurchaseInfo().getProductCode();
                    return new PluginCache()
                            .setId(plugin.getId())
                            .setProductCode(productCode)
                            .setName(plugin.getName())
                            .setPricingModel(plugin.getPricingModel())
                            .setIcon(StrUtil.isNotBlank(plugin.getIcon()) ? PLUGIN_BASIC_URL + plugin.getIcon() : null);
                })
                .toList();
        log.info("转换后大小 => [{}]", list.size());
        return list;
    }

    public static PluginInfo pluginInfo(Long pluginId) {
        String body = HttpUtil.get(PLUGIN_INFO_URL + pluginId);
        return JSONUtil.toBean(body, PluginInfo.class);
    }

    @Data
    public static class PluginCache {

        private Long id;
        private String productCode;
        private String name;
        private String pricingModel;
        private String icon;

        @Override
        public final boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PluginCache that))
                return false;

            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    @Data
    public static class PluginInfo {

        private Long id;

        private PurchaseInfo purchaseInfo;

        @Data
        public static class PurchaseInfo {

            private String productCode;
        }
    }

    @Data
    public static class PluginList {

        private List<Plugin> plugins;
        private Long total;


        @Data
        public static class Plugin {

            private Long id;
            private String name;
            private String preview;
            private Integer downloads;
            private String pricingModel;
            private String organization;
            private String icon;
            private String previewImage;
            private Double rating;
            private VendorInfo vendorInfo;
        }

        @Data
        public static class VendorInfo {
            private String name;
            private Boolean isVerified;
        }
    }
}
