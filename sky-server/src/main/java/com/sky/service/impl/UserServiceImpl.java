package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    private static final String WX_LOGIN_URL="https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //获取微信登录需要的密钥即四个参数
        String appId = weChatProperties.getAppid();
        String secret = weChatProperties.getSecret();
        String code = userLoginDTO.getCode();
        //调用微信接口，获取openid
        Map<String,String>map=new HashMap<>();
        map.put("appid",appId);
        map.put("secret",secret);
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        // 使用 HttpClient 调用微信接口，获取 openid，用于之后的jwts验证登录
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        if (openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user=userMapper.selectByOpenid(openid);
        if(user==null){
            user= User.builder().openid(openid).
                    createTime(LocalDateTime.now()).build();
        userMapper.insert(user);}
        return  user ;
    }
}
