package com.armikom.questionpool;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Passwordless login: a single-use link is mailed to an address that is on the roster. */
@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private static final SecureRandom RNG = new SecureRandom();
	private static final String[] ADJECTIVES = { "Brave", "Calm", "Clever", "Curious", "Eager", "Gentle", "Happy",
			"Honest", "Kind", "Lucky", "Patient", "Quick", "Quiet", "Sharp", "Steady", "Sunny", "Swift", "Wise",
			"Bold", "Bright" };
	private static final String[] ANIMALS = { "Otter", "Falcon", "Panda", "Fox", "Heron", "Lynx", "Dolphin", "Owl",
			"Tiger", "Koala", "Badger", "Crane", "Gecko", "Hawk", "Ibex", "Robin", "Seal", "Wolf", "Bee", "Turtle" };

	private final JdbcClient db;
	private final AppProps props;
	private final ObjectProvider<JavaMailSender> mailSender;
	private final ExecutorService background;
	private final String smtpUser;
	private final String smtpHost;

	public AuthService(JdbcClient db, AppProps props, ObjectProvider<JavaMailSender> mailSender,
			ExecutorService background, @Value("${spring.mail.username:}") String smtpUser,
			@Value("${spring.mail.host:}") String smtpHost) {
		this.db = db;
		this.props = props;
		this.mailSender = mailSender;
		this.background = background;
		this.smtpUser = smtpUser;
		this.smtpHost = smtpHost;
	}

	@EventListener(ApplicationReadyEvent.class)
	void ensureInstructor() {
		String email = normalize(props.instructorEmail());
		if (email.isEmpty()) {
			log.warn("INSTRUCTOR_EMAIL is not set: nobody can open the instructor screens.");
			return;
		}
		int updated = db.sql("update app_user set role = 'INSTRUCTOR', active = true where email = :e")
				.param("e", email).update();
		if (updated == 0) {
			db.sql("insert into app_user (email, full_name, role, nickname) values (:e, :n, 'INSTRUCTOR', 'Instructor')")
					.param("e", email).param("n", props.instructorName()).update();
		}
	}

	/** Always returns quietly, so the caller cannot learn which addresses are on the roster. */
	public void requestLink(String rawEmail) {
		String email = normalize(rawEmail);
		Optional<Long> userId = db.sql("select id from app_user where email = :e and active").param("e", email)
				.query(Long.class).optional();
		if (userId.isEmpty()) {
			return;
		}
		int recent = db.sql("""
				select count(*) from login_token
				where user_id = :u and created_at > now() - interval '15 minutes'""").param("u", userId.get())
				.query(Integer.class).single();
		if (recent >= 3) {
			return;
		}
		String token = randomToken();
		db.sql("""
				insert into login_token (token_hash, user_id, expires_at)
				values (:h, :u, now() + interval '15 minutes')""").param("h", sha256(token))
				.param("u", userId.get()).update();
		String link = props.baseUrl() + "/login?token=" + token;
		background.submit(() -> sendLink(email, link));
	}

	private void sendLink(String email, String link) {
		JavaMailSender sender = mailSender.getIfAvailable();
		if (sender == null || smtpHost.isBlank()) {
			log.info("SMTP is not configured. Login link for {}: {}", email, link);
			return;
		}
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			String from = props.mailFrom() == null || props.mailFrom().isBlank() ? smtpUser : props.mailFrom();
			msg.setFrom(from);
			msg.setTo(email);
			msg.setSubject("SENG 403 Question Pool - your login link");
			msg.setText("""
					Hello,

					Open this link to sign in to the SENG 403 Question Pool:
					%s

					The link works one time and only for 15 minutes.
					If you did not ask for it, you can ignore this e-mail.

					---

					Merhaba,

					SENG 403 Soru Havuzu'na girmek için bu bağlantıyı aç:
					%s

					Bağlantı yalnızca bir kez ve 15 dakika boyunca çalışır.
					Bu isteği sen yapmadıysan bu e-postayı dikkate alma.
					""".formatted(link, link));
			sender.send(msg);
		}
		catch (Exception e) {
			log.error("Could not send the login mail to {}", email, e);
		}
	}

	/** Exchanges a mailed token for a session token. Empty when the token is wrong, used or too old. */
	public Optional<String> verify(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		Optional<Long> userId = db.sql("""
				update login_token set used_at = now()
				where token_hash = :h and used_at is null and expires_at > now()
				returning user_id""").param("h", sha256(token)).query(Long.class).optional();
		if (userId.isEmpty()) {
			return Optional.empty();
		}
		String session = randomToken();
		db.sql("""
				insert into user_session (token_hash, user_id, expires_at)
				values (:h, :u, now() + interval '30 days')""").param("h", sha256(session))
				.param("u", userId.get()).update();
		db.sql("update app_user set last_login_at = now() where id = :u").param("u", userId.get()).update();
		return Optional.of(session);
	}

	public Optional<User> userForSession(String sessionToken) {
		if (sessionToken == null || sessionToken.isBlank()) {
			return Optional.empty();
		}
		return db.sql("""
				select u.id, u.email, u.full_name, u.role, u.nickname
				from user_session s join app_user u on u.id = s.user_id
				where s.token_hash = :h and s.expires_at > now() and u.active""").param("h", sha256(sessionToken))
				.query(User.class).optional();
	}

	public void logout(String sessionToken) {
		if (sessionToken != null && !sessionToken.isBlank()) {
			db.sql("delete from user_session where token_hash = :h").param("h", sha256(sessionToken)).update();
		}
	}

	/** A random name such as "CalmOtter42" that other students see instead of the real name. */
	public String newNickname() {
		for (int i = 0; i < 200; i++) {
			String nick = ADJECTIVES[RNG.nextInt(ADJECTIVES.length)] + ANIMALS[RNG.nextInt(ANIMALS.length)]
					+ (10 + RNG.nextInt(90));
			boolean taken = db.sql("select count(*) from app_user where nickname = :n").param("n", nick)
					.query(Integer.class).single() > 0;
			if (!taken) {
				return nick;
			}
		}
		return "Student" + Long.toHexString(RNG.nextLong());
	}

	static String normalize(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}

	private static String randomToken() {
		byte[] bytes = new byte[32];
		RNG.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String sha256(String value) {
		try {
			return HexFormat.of()
					.formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
