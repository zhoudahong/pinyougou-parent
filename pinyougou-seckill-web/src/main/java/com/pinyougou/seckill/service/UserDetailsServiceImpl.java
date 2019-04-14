package com.pinyougou.seckill.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义认证类
 * security和cas 集成后 认证类 只做一件事 ,对登录的用户 授予 相应的角色
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     *
     * @param username 用户输入的登录名称
     * @return  UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("经过了认证类UserDetailsServiceImpl");
        List<GrantedAuthority> grantAuths=new ArrayList<>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username,"",grantAuths);
    }
}
