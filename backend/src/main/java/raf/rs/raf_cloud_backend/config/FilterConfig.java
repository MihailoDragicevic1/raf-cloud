package raf.rs.raf_cloud_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import raf.rs.raf_cloud_backend.service.JwtAuthenticationFilter;

@Configuration
public class FilterConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("jwtAuthenticationFilter");
        return registrationBean;
    }
}


