package com.armikom.questionpool;

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	public static final String SESSION_COOKIE = "qp_session";
	public static final String USER_ATTRIBUTE = "user";

	private final AuthService auth;

	public WebConfig(AuthService auth) {
		this.auth = auth;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new HandlerInterceptor() {
			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
					throws Exception {
				Optional<User> user = auth.userForSession(sessionToken(request));
				if (user.isEmpty()) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please sign in.");
					return false;
				}
				if (request.getRequestURI().startsWith("/api/instructor/") && !user.get().instructor()) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only the instructor can do this.");
					return false;
				}
				request.setAttribute(USER_ATTRIBUTE, user.get());
				return true;
			}
		}).addPathPatterns("/api/**").excludePathPatterns("/api/auth/request", "/api/auth/verify", "/api/auth/logout");
	}

	static String sessionToken(HttpServletRequest request) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (SESSION_COOKIE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/** The Vue app handles these paths itself, so the server always answers with index.html. */
	@Controller
	static class SpaController {

		@GetMapping({ "/login", "/chapters/**", "/questions/**", "/mine", "/instructor/**" })
		String index() {
			return "forward:/index.html";
		}
	}
}
