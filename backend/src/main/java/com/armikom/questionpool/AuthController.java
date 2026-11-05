package com.armikom.questionpool;

import java.time.Duration;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

	private final AuthService auth;
	private final AppProps props;

	public AuthController(AuthService auth, AppProps props) {
		this.auth = auth;
		this.props = props;
	}

	record EmailIn(String email) {
	}

	record TokenIn(String token) {
	}

	@PostMapping("/auth/request")
	Map<String, Object> request(@RequestBody EmailIn in) {
		boolean onRoster = auth.requestLink(in.email());
		return Map.of("ok", true, "on_roster", onRoster);
	}

	@PostMapping("/auth/verify")
	Map<String, Object> verify(@RequestBody TokenIn in, HttpServletResponse response) {
		String session = auth.verify(in.token()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"This link is not valid any more. Ask for a new link."));
		response.addHeader(HttpHeaders.SET_COOKIE, cookie(session, Duration.ofDays(30)).toString());
		return Map.of("ok", true);
	}

	@PostMapping("/auth/logout")
	Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
		auth.logout(WebConfig.sessionToken(request));
		response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
		return Map.of("ok", true);
	}

	@GetMapping("/me")
	Map<String, Object> me(@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		return Map.of("id", user.id(), "nickname", user.nickname(), "full_name", user.fullName(), "role", user.role(),
				"questions_per_chapter", props.questionsPerChapter());
	}

	private ResponseCookie cookie(String value, Duration maxAge) {
		return ResponseCookie.from(WebConfig.SESSION_COOKIE, value).httpOnly(true).secure(props.secureCookies())
				.sameSite("Lax").path("/").maxAge(maxAge).build();
	}
}
