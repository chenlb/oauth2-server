package com.chenlb.oauth2.server;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.chenlb.oauth2.server.mock.MockUser;
import com.chenlb.oauth2.server.mock.SaClientMockDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token-OAuth2 Server 认证端 Controller
 */
@RestController
public class SaOAuth2ServerController {

	@Autowired
	private SaClientMockDao saClientMockDao;

	// OAuth2-Server 端：处理所有 OAuth2 相关请求
	@RequestMapping("/oauth2/*")
	public Object request() {
		SaManager.getLog().info("------- 进入请求: {}", SaHolder.getRequest().getUrl());
		SaManager.getLog().info("------- Authorization: {}", SaHolder.getRequest().getHeader("Authorization"));
		Object obj = SaOAuth2ServerProcessor.instance.dister();
		if(obj instanceof SaResult) {
			SaResult sa_obj = (SaResult) obj;
			SaManager.getLog().info("------- 返回: {}", new HashMap<>(sa_obj));
		}
		return obj;
	}

	// Sa-Token OAuth2 定制化配置
	@Autowired
	public void configOAuth2Server(SaOAuth2ServerConfig oauth2Server) {
		// 未登录的视图
		oauth2Server.notLoginView = ()->{
			return new ModelAndView("login.html");
		};

		// 登录处理函数
		oauth2Server.doLoginHandle = (name, pwd) -> {
			// 真实项目，从数据库读取。
			MockUser storeUser = saClientMockDao.getUserByName(name);
			if(storeUser != null && storeUser.isMatchPassword(pwd)) {
				StpUtil.login(name);
				return SaResult.ok().set("satoken", StpUtil.getTokenValue());
			}
			return SaResult.error("账号名或密码错误");
		};

		// 授权确认视图
		oauth2Server.confirmView = (clientId, scopes)->{
			Map<String, Object> map = new HashMap<>();
			map.put("clientId", clientId);
			map.put("scope", scopes);
			return new ModelAndView("confirm.html", map);
		};

		// 设置返回 TokenType 修改为全是大写开头
		// 方便 client 端识别，直接使用 token_type 加到 http 的 Authorization 头部
		SaOAuth2Consts.TokenType.basic = SaOAuth2Consts.TokenType.Basic;
		SaOAuth2Consts.TokenType.bearer = SaOAuth2Consts.TokenType.Bearer;
		SaOAuth2Consts.TokenType.digest = SaOAuth2Consts.TokenType.Digest;
	}
}
