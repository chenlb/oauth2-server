package com.chenlb.oauth2.server;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.dev33.satoken.util.SaResult;
import com.chenlb.oauth2.server.mock.MockUser;
import com.chenlb.oauth2.server.mock.SaClientMockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sa-Token OAuth2 Resources 端 Controller
 *
 * <p> Resources 端：OAuth2 资源端，允许 Client 端根据 Access-Token 置换相关资源 </p>
 *
 * <p> 在 OAuth2 中，认证端和资源端：
 *  1、可以在一个 Controller 中，也可以在不同的 Controller 中
 *  2、可以在同一个项目中，也可以在不同的项目中（在不同项目中时需要两端连同一个 Redis ）
 * </p>
 *
 */
@RestController
public class SaOAuth2ResourcesController {

    @Autowired
    private SaClientMockDao saClientMockDao;

    // 示例：获取 userinfo 信息：昵称、头像、性别等等
    @RequestMapping("/oauth2/userinfo")
    public SaResult userinfo() {
        // 获取 Access-Token 对应的账号id
        SaManager.getLog().info("------- 进入请求: {}", SaHolder.getRequest().getUrl());
        SaManager.getLog().info("------- UserInfo Authorization: {}", SaHolder.getRequest().getHeader("Authorization"));

        String accessToken = SaOAuth2Manager.getDataResolver().readAccessToken(SaHolder.getRequest());
        Object loginId = SaOAuth2Util.getLoginIdByAccessToken(accessToken);
        SaManager.getLog().info("-------- 此Access-Token对应的账号id: {}", loginId);

        // 校验 Access-Token 是否具有权限: userinfo
        SaOAuth2Util.checkAccessTokenScope(accessToken, "profile");

        // 模拟账号信息 （真实环境需要查询数据库获取信息）
        Map<String, Object> map = new LinkedHashMap<>();

        // 真实项目，从数据库读取。
        MockUser mockUser = saClientMockDao.getUserByName(loginId.toString());
        if(mockUser == null) {
            return SaResult.error("账号 ["+loginId+"] 不存在");
        }

        // 与 oidc 返回的内容保持一致，增加 sub 返回。
        map.put("sub", loginId);
        map.put("name", mockUser.getUsername());
        map.put("nickname", mockUser.getNickname());
        map.put("picture", mockUser.getAvatar());

        if(SaOAuth2Util.hasAccessTokenScope(accessToken, "email")) {
            map.put("email", mockUser.getEmail());
        }
        return SaResult.ok().setMap(map);
    }

}