package com.jetbrains.help.route;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.jetbrains.help.auth.AuthStrategy;
import com.jetbrains.help.context.AgentContextHolder;
import com.jetbrains.help.context.PluginsContextHolder;
import com.jetbrains.help.context.ProductsContextHolder;
import com.jetbrains.help.properties.AuthProperties;
import com.jetbrains.help.properties.JetbrainsHelpProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final JetbrainsHelpProperties jetbrainsHelpProperties;
    private final AuthProperties authProperties;

    @SaIgnore
    @GetMapping("auth.do")
    public void auth(HttpServletResponse resp) throws IOException {
        AuthStrategy auth = SpringUtil.getBean(authProperties.getType(), AuthStrategy.class);
        auth.redirect(resp);
    }

    @GetMapping
    public String index(Model model) {
        List<ProductsContextHolder.ProductCache> productCacheList = ProductsContextHolder.productCacheList();
        List<PluginsContextHolder.PluginCache> pluginCacheList = PluginsContextHolder.pluginCacheList();
        model.addAttribute("products", productCacheList);
        model.addAttribute("plugins", pluginCacheList);
        model.addAttribute("defaults", jetbrainsHelpProperties);
        return "index";
    }

    @GetMapping("search")
    public String index(@RequestParam(required = false) String search, Model model) {
        List<ProductsContextHolder.ProductCache> productCacheList = ProductsContextHolder.productCacheList();
        List<PluginsContextHolder.PluginCache> pluginCacheList = PluginsContextHolder.pluginCacheList();
        if (CharSequenceUtil.isNotBlank(search)) {
            productCacheList = productCacheList.stream()
                    .filter(productCache -> CharSequenceUtil.containsIgnoreCase(productCache.getName(), search))
                    .toList();
            pluginCacheList = pluginCacheList.stream()
                    .filter(pluginCache -> CharSequenceUtil.containsIgnoreCase(pluginCache.getName(), search))
                    .toList();
        }
        model.addAttribute("products", productCacheList);
        model.addAttribute("plugins", pluginCacheList);
        model.addAttribute("defaults", jetbrainsHelpProperties);
        return "index::product-list";
    }

    @GetMapping("ja-netfilter")
    @ResponseBody
    public ResponseEntity<Resource> downloadJaNetfilter() {
        File jaNetfilterZipFile = AgentContextHolder.jaNetfilterZipFile();
        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment;filename=" + jaNetfilterZipFile.getName())
                .contentType(APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(FileUtil.getInputStream(jaNetfilterZipFile)));
    }
}
