package com.chenlb.oauth2.server;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.oidc.IdTokenModel;
import cn.dev33.satoken.oauth2.scope.handler.OidcScopeHandler;
import com.chenlb.oauth2.server.mock.MockUser;
import com.chenlb.oauth2.server.mock.SaClientMockDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展 OIDC 权限处理器，返回更多字段
 */
@Component
public class CustomOidcScopeHandler extends OidcScopeHandler {

    private static Logger logger = LoggerFactory.getLogger(CustomOidcScopeHandler.class);

    protected ThreadLocal<IdTokenModel> localIdTokenModel = new ThreadLocal<>();

    @Autowired
    private SaClientMockDao saClientMockDao;

    @Override
    public void workAccessToken(AccessTokenModel at) {
        super.workAccessToken(at);

        IdTokenModel idToken = localIdTokenModel.get();

        if(idToken == null) {
            return;
        }

        Map<String, Object> idTokenExtData = workCustomExtraData(at);

        if(idTokenExtData != null && !idTokenExtData.isEmpty()) {
            // 额外信息放到 id-token 中，重新 id-token。
            idToken.extraData.putAll(idTokenExtData);

            // 构建 jwtIdToken
            String jwtIdToken = generateJwtIdToken(idToken);

            // 放入 AccessTokenModel
            at.extraData.put("id_token", jwtIdToken);
        }

        localIdTokenModel.remove();
    }

    protected Map<String, Object> workCustomExtraData(AccessTokenModel at) {
        SaManager.getLog().info("----- 为 idToken 追加扩展字段 ----- ");

        List<String> scopes = at.scopes;
        if(scopes == null) {
            logger.warn("----- 为 idToken 追加扩展字段，未找到 scopes ----- ");
            return null;
        }

        Object loginId = at.getLoginId();
        if(loginId == null) {
            logger.warn("----- 为 idToken 追加扩展字段，未找到 loginId: ----- ");
            return null;
        }

        MockUser mockUser = saClientMockDao.getUserByName(loginId.toString());
        if(mockUser == null) {
            logger.warn("----- 为 idToken 追加扩展字段，未找到 mockUser: {} ----- ", loginId);
            return null;
        }

        Map<String, Object> idTokenExtData = null;
        if(scopes.indexOf("profile") > 0) {
            idTokenExtData = new LinkedHashMap<>();

            idTokenExtData.put("name", mockUser.getUsername()); // 用户名
            idTokenExtData.put("nickname", mockUser.getNickname()); // 昵称
            idTokenExtData.put("picture", mockUser.getAvatar()); // 头像
        }

        if(scopes.indexOf("email") > 0) {
            if(idTokenExtData == null) {
                idTokenExtData = new LinkedHashMap<>();
            }
            idTokenExtData.put("email", mockUser.getEmail()); // 邮箱
        }

        // 更多字段 ...
        // 可参考：https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims

        return idTokenExtData;
    }

    @Override
    public IdTokenModel workExtraData(IdTokenModel idToken) {

        localIdTokenModel.set(idToken);

        return idToken;
    }
}