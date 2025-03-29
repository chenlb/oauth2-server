package com.chenlb.oauth2.server.mock;

import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SaClientModel 模拟查询操作
 */
@Component
public class SaClientMockDao {

    private static Logger logger = LoggerFactory.getLogger(SaClientMockDao.class);

    public List<SaClientModel> clientList = new ArrayList<>();

    private Map<String, MockUser> mockUsers = new HashMap<>();

    public SaClientMockDao(){

    }

    /**
     * 根据应用 id 查找对应的应用，找不到则返回 null
     * @param clientId 应用 id
     * @return 应用对象
     */
    public SaClientModel getClientModel(String clientId) {
        return clientList.stream()
                .filter(e -> e.getClientId().equals(clientId))
                .findFirst()
                .orElse(null);
    }

    public MockUser getUserByName(String username) {
        return mockUsers.get(username);
    }

    @PostConstruct
    public void loadMockUsers() {
        // mock 数据只用于测试。
        String user_file = "mock/users.json";
        try {
            JSONArray users = new JSONArray(IoUtil.readUtf8(new FileInputStream(user_file)));
            for(Object user : users) {
                JSONObject userObj = (JSONObject) user;
                MockUser mockUser = new MockUser();

                // porfile
                mockUser.setUsername(userObj.getStr("username"));
                mockUser.setPassword(userObj.getStr("password"));
                mockUser.setNickname(userObj.getStr("nickname"));
                mockUser.setAvatar(userObj.getStr("avatar"));

                // email
                mockUser.setEmail(userObj.getStr("email"));

                logger.info("load mock user => [{}]", mockUser);
                mockUsers.put(mockUser.getUsername(), mockUser);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void loadMockClients() {
        // mock 数据只用于测试。
        // 真实环境需要从数据库查询
        String client_file = "mock/clients.json";
        try {
            JSONArray clients = new JSONArray(IoUtil.readUtf8(new FileInputStream(client_file)));
            for(Object client : clients) {
                JSONObject clientObj = (JSONObject) client;
                SaClientModel clientModel = new SaClientModel();
                clientModel.setClientId(clientObj.getStr("client_id"));    // client id
                clientModel.setClientSecret(clientObj.getStr("client_secret"));    // client 秘钥
                clientModel.addAllowRedirectUris(clientObj.getJSONArray("allow_redirect_uris").toArray(new String[0]));    // 所有允许授权的 url
                clientModel.addContractScopes(clientObj.getJSONArray("contract_scopes").toArray(new String[0]));    // 所有签约的权限
                clientModel.setSubjectId(clientObj.getStr("allow_grant_types"));   // 主体 id (可选)
                clientModel.addAllowGrantTypes(clientObj.getJSONArray("allow_grant_types").toArray(new String[0]));


                clientList.add(clientModel);
                logger.info("load mock client => [{}]", clientModel.clientId);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}