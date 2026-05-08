package com.atguigu.exam.service.impl;

import com.atguigu.exam.config.properties.JwtProperties;
import com.atguigu.exam.constant.JwtClaimsConstant;
import com.atguigu.exam.entity.User;
import com.atguigu.exam.mapper.UserMapper;
import com.atguigu.exam.service.UserService;
import com.atguigu.exam.utils.JwtUtils;
import com.atguigu.exam.vo.LoginRequestVo;
import com.atguigu.exam.vo.LoginResponseVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Service实现类
 * 实现用户相关的业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public LoginResponseVo login(LoginRequestVo loginRequestVo) {
        // 1. 根据用户名查询用户信息
        User user = this.getOne(new QueryWrapper<User>().eq("username", loginRequestVo.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(loginRequestVo.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());

        String token = JwtUtils.createJWt(jwtProperties.getSecretKey(), jwtProperties.getTtlMillis(), claims);

        LoginResponseVo loginResponseVo = new LoginResponseVo();
        BeanUtils.copyProperties(user, loginResponseVo);
        loginResponseVo.setToken(token);

        return loginResponseVo;
    }
}