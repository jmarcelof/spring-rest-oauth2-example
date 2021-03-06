package com.poc.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.*;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;

import javax.sql.DataSource;

@Configuration
public class OAuth2Config {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private DataSource dataSource;

        @Autowired
        @Qualifier("userDetailsService")
        private UserDetailsService userDetailsService;

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer configurer) throws Exception {
            configurer.authenticationManager(authenticationManager);
            configurer.userDetailsService(userDetailsService);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.jdbc(dataSource);
        }
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .requestMatchers().antMatchers("/user", "/**")
            .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/user").access("hasRole('ROLE_SIGNUP') or hasRole('ROLE_ADMIN')")
                    .antMatchers("/user/all").access("hasRole('ROLE_ADMIN')")
                    .antMatchers("/**").access("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')");
        }
    }
}
