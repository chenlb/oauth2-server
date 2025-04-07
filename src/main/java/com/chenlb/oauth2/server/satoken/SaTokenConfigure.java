package com.chenlb.oauth2.server.satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.SaJwtTemplate;
import cn.dev33.satoken.jwt.SaJwtUtil;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.PemUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;


/**
 * [Sa-Token 权限认证] 配置类
 *
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

	private static Logger logger = LoggerFactory.getLogger(SaTokenConfigure.class);
	
	/**
	 * 注册 Sa-Token 拦截器打开注解鉴权功能  
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 注册 Sa-Token 拦截器打开注解鉴权功能 
		registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
	}

	/**
	 * 注册 [Sa-Token 全局过滤器]
	 */
	@Bean
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter()

				// 指定 [拦截路由] 与 [放行路由]
				.addInclude("/**").addExclude("/favicon.ico")

				// 认证函数: 每次请求执行
				.setAuth(obj -> {
					SaManager.getLog().debug("----- 请求path={}  提交token={}, \n\tAuthorization={} \n\t参数={}"
							, SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue()
							, SaHolder.getRequest().getHeader("Authorization")
							, SaHolder.getRequest().getParamMap()
					);
					// ...
				})

				// 异常处理函数：每次认证函数发生异常时执行此函数
				.setError(e -> {
					return SaResult.error(e.getMessage());
				})

				// 前置函数：在每次认证函数之前执行
				.setBeforeAuth(obj -> {
					SaHolder.getResponse()

							// ---------- 设置跨域响应头 ----------
							// 允许指定域访问跨域资源
							.setHeader("Access-Control-Allow-Origin", "*")
							// 允许所有请求方式
							.setHeader("Access-Control-Allow-Methods", "*")
							// 允许的header参数
							.setHeader("Access-Control-Allow-Headers", "*")
							// 有效时间
							.setHeader("Access-Control-Max-Age", "3600")
					;

					// 如果是预检请求，则立即返回到前端
					SaRouter.match(SaHttpMethod.OPTIONS)
							.free(r -> SaManager.getLog().info("--------OPTIONS预检请求，不做处理"))
							.back();
				})
				;
	}

	public static class RS256SaJwtTemplate extends SaJwtTemplate {
		private static PrivateKey privateKey = null;

		public RS256SaJwtTemplate() {
            try {
				String private_key_file = "pem/rs256_private_key.pem";
				logger.info("load privateKey file = [{}] for sign id_token", private_key_file);
                privateKey = PemUtil.readPemPrivateKey(new FileInputStream(private_key_file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

		@Override
		public JWTSigner createSigner (String keyt) {
			return JWTSignerUtil.rs256(privateKey);
		}

	}

	/**
	 * 按 pem/rs256_public_key.pem 生成 jwks.json 文件
	 */
	@PostConstruct
	public void createJwks() {
		File jwksFile = new File("web/oidc/jwks.json");
		if(jwksFile.exists()) {
			logger.info("jwks file = [{}] exists, skip for create", jwksFile);
			return;
		}
		String public_key_file = "pem/rs256_public_key.pem";
		try(
				FileInputStream pkIn = new FileInputStream(public_key_file);
				FileOutputStream jwksOut = new FileOutputStream(jwksFile);
		) {
			logger.info("load publicKey file = [{}] for openid connect jwks", public_key_file);
			PublicKey publicKey = PemUtil.readPemPublicKey(pkIn);

			// https://connect2id.com/products/nimbus-jose-jwt/examples/pem-encoded-objects
			// https://connect2id.com/products/nimbus-jose-jwt/examples/jwk-generation
			RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) publicKey);
			builder.algorithm(new Algorithm("RS256"));
			builder.keyID(String.valueOf(publicKey.hashCode()));
			builder.keyUse(KeyUse.SIGNATURE);

			RSAKey rsakey = builder.build();

			IoUtil.writeUtf8(jwksOut, true, "{\"keys\":[", rsakey.toJSONString(), "]}");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 自定义 SaJwtUtil 生成 token 的算法 （使用 RS256）
	 */
	@PostConstruct
	public void setSaJwtTemplate() {
		SaJwtUtil.setSaJwtTemplate(new RS256SaJwtTemplate());
	}

}
