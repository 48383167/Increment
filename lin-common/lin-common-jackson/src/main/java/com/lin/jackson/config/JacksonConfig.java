package com.lin.jackson.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Slf4j
@Configuration
public class JacksonConfig {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            log.info("初始化 Jackson 全局序列化配置");
            
            // 1. 全局配置时区为系统默认时区
            builder.timeZone(TimeZone.getDefault());

            // 2. 解决前端 Long 和 BigInteger 精度丢失问题 (转成 String)
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);

            // 3. 将 BigDecimal 转为 String，避免前端精度丢失或出现科学计数法
            builder.serializerByType(BigDecimal.class, ToStringSerializer.instance);

            // 4. 全局时间格式化 (LocalDateTime)，去掉恶心的 'T'
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            
            // java.util.Date 的全局格式化
            builder.simpleDateFormat(DATETIME_PATTERN);
        };
    }
}
