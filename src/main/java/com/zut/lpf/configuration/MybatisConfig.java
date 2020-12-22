package com.zut.lpf.configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class MybatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor(DataSource dataSource) throws SQLException {

    return new PaginationInterceptor();
    }
}
