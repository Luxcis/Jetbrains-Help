package com.jetbrains.help.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.jetbrains.help.context.LicenseContextHolder;
import com.jetbrains.help.context.PluginsContextHolder;
import com.jetbrains.help.context.ProductsContextHolder;
import com.jetbrains.help.properties.JetbrainsHelpProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class OpenApiController {
    private final JetbrainsHelpProperties jetbrainsHelpProperties;

    @PostMapping("generateLicense")
    public String generateLicense(@RequestBody GenerateLicenseReqBody body) {
        String license;
        if ("Dbeaver".equalsIgnoreCase(body.productCode)) {
            license = LicenseContextHolder.generateDbeaverLicense(
                    jetbrainsHelpProperties.getProductId(),
                    jetbrainsHelpProperties.getProductVersion(),
                    jetbrainsHelpProperties.getDefaultLicenseName(),
                    jetbrainsHelpProperties.getDefaultAssigneeName(),
                    jetbrainsHelpProperties.getDefaultEmail()
            );
        } else if ("FinalShell".equals(body.productCode)) {
            license = LicenseContextHolder.generateFinalShellLicense(body.machineCode);
        } else {
            Set<String> productCodeSet;
            if (CharSequenceUtil.isBlank(body.getProductCode())) {
                List<String> productCodeList = ProductsContextHolder.productCacheList()
                        .stream()
                        .map(ProductsContextHolder.ProductCache::getProductCode)
                        .filter(StrUtil::isNotBlank)
                        .map(productCode -> CharSequenceUtil.splitTrim(productCode, ","))
                        .flatMap(Collection::stream)
                        .toList();
                List<String> pluginCodeList = PluginsContextHolder.pluginCacheList()
                        .stream()
                        .map(PluginsContextHolder.PluginCache::getProductCode)
                        .filter(StrUtil::isNotBlank)
                        .toList();
                productCodeSet = CollUtil.newHashSet(productCodeList);
                productCodeSet.addAll(pluginCodeList);
            } else {
                productCodeSet = CollUtil.newHashSet(CharSequenceUtil.splitTrim(body.getProductCode(), ','));
            }
            license = LicenseContextHolder.generateJetbrainsLicense(
                    body.getLicenseName(),
                    body.getAssigneeName(),
                    body.getExpiryDate(),
                    productCodeSet
            );
        }
        return license;
    }

    @GetMapping("refreshPlugins")
    public List<PluginsContextHolder.PluginCache> refreshPlugins() {
        PluginsContextHolder.refreshJsonFile();
        return PluginsContextHolder.pluginCacheList();
    }

    @Data
    public static class GenerateLicenseReqBody {
        private String licenseName;
        private String assigneeName;
        private String expiryDate;
        private String productCode;
        private String machineCode;
    }
}
