package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.properties.BaiduAkProperties;
import com.sky.utils.AliOssUtil;
import com.sky.utils.SnCalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class BaiduConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SnCalUtil baaiduAkUtil(BaiduAkProperties baiduAkProperties){
        log.info("开始创建百度地图ak工具类对象：{}",baiduAkProperties);
        return new SnCalUtil(baiduAkProperties.getAk(), baiduAkProperties.getSk());
    }
}
