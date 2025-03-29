package com.chenlb.oauth2.server;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动：Sa-OAuth2 Server端
 */
@SpringBootApplication
public class SaOAuth2ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaOAuth2ServerApplication.class, args);
		SaManager.getLog().info("Sa-Token-OAuth2 Server端启动成功，配置如下：\n{}", SaOAuth2Manager.getServerConfig());
	}
	
}
