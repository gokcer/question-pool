package com.armikom.questionpool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * What students do with questions: write, answer, discuss. The main rule is "attempt first": the correct answer, the
 * reasons, the explanation and the thread stay hidden until the student has answered the question.
 */
@RestController
@RequestMapping("/api")
public class QuestionController {

	static final String SELECT_FULL = """
			select q.id, q.chapter_id, q.author_id, q.stem, q.option_a, q.option_b, q.option_c, q.option_d, q.correct,
			    q.rationale_a, q.rationale_b, q.rationale_c, q.rationale_d, q.reference, q.status, q.ai_status,
			    q.ai_review::text as ai_review, q.instructor_note, q.explanation, q.created_at, u.nickname as author
			from question q join app_user u on u.id = q.author_id
			""";
	private static final List<String> HIDDEN_BEFORE_ATTEMPT = List.of("correct", "rationale_a", "rationale_b",
			"rationale_c", "rationale_d", "reference", "explanation");
	private static final List<String> AUTHOR_ONLY = List.of("ai_status", "ai_review", "instructor_note");

	private final JdbcClient db;
	private final AppProps props;
	private final AiService ai;

	public QuestionController(JdbcClient db, AppProps props, AiService ai) {
		this.db = db;
		this.props = props;
		this.ai = ai;
	}

	record QuestionIn(Integer chapterId, String stem, String optionA, String optionB, String optionC, String optionD,
			String correct, String rationaleA, String rationaleB, String rationaleC, String rationaleD,
			String reference) {

		/** Returns a trimmed copy, or stops the request with a message the student can act on. */
		QuestionIn checked() {
			QuestionIn q = new QuestionIn(chapterId, text("The question text", stem, 15, 800),
					text("Option A", optionA, 1, 300), text("Option B", optionB, 1, 300),
					text("Option C", optionC, 1, 300), text("Option D", optionD, 1, 300),
					correct == null ? "" : correct.trim().toUpperCase(), text("The reason for A", rationaleA, 10, 600),
					text("The reason for B", rationaleB, 10, 600), text("The reason for C", rationaleC, 10, 600),
					text("The reason for D", rationaleD, 10, 600), text("The reference", reference, 3, 200));
			if (!List.of("A", "B", "C", "D").contains(q.correct)) {
				throw bad("Choose the correct option (A, B, C or D).");
			}
			if (List.of(q.optionA, q.optionB, q.optionC, q.optionD).stream().map(String::toLowerCase).distinct()
					.count() < 4) {
				throw bad("The four options must be different.");
			}
			return q;
		}

		private static String text(String name, String value, int min, int max) {
			String v = value == null ? "" : value.trim();
			if (v.length() < min) {
				throw bad(name + " is too short (at least " + min + " characters).");
			}
			if (v.length() > max) {
				throw bad(name + " is too long (at most " + max + " characters).");
			}
			return v;
		}
	}

	record AttemptIn(String selected) {
	}

	record CommentIn(String body, Long parentId) {
	}

	record FlagIn(String reason) {
	}

	// ---------- reading ----------

	/** Question stems of a chapter. Stems of waiting questions are listed too, so students can avoid duplicates. */
	@GetMapping("/chapters/{chapterId}/questions")
	List<Map<String, Object>> list(@PathVariable int chapterId,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		return db.sql("""
				select q.id, q.stem, q.status, (q.author_id = :u) as mine, u.nickname as author,
				    (a.id is not null) as attempted, a.correct as my_correct,
				    (select count(*) from comment c where c.question_id = q.id and not c.deleted) as comments
				from question q
				join app_user u on u.id = q.author_id
				left join attempt a on a.question_id = q.id and a.user_id = :u
				where q.chapter_id = :c and (q.status in ('APPROVED', 'PENDING', 'NEEDS_REVISION') or q.author_id = :u)
				order by q.id""").param("u", user.id()).param("c", chapterId).query().listOfRows();
	}

	@GetMapping("/questions/mine")
	List<Map<String, Object>> mine(@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		return db.sql("""
				select q.id, q.stem, q.status, q.ai_status, q.instructor_note, q.created_at,
				    c.id as chapter_id, c.number as chapter_number, c.title as chapter_title,
				    (select count(*) from attempt a where a.question_id = q.id) as attempts,
				    (select count(*) from comment m where m.question_id = q.id and not m.deleted) as comments
				from question q join chapter c on c.id = q.chapter_id
				where q.author_id = :u order by c.number, q.id""").param("u", user.id()).query().listOfRows();
	}

	@GetMapping("/questions/{id}")
	Map<String, Object> get(@PathVariable long id, @RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		Map<String, Object> q = load(id);
		boolean mine = isAuthor(q, user);
		boolean privileged = mine || user.instructor();
		if (!privileged && !"APPROVED".equals(q.get("status"))) {
			throw notFound();
		}
		Optional<String> selected = db.sql("select selected from attempt where question_id = :q and user_id = :u")
				.param("q", id).param("u", user.id()).query(String.class).optional();
		boolean revealed = privileged || selected.isPresent();
		q.remove("author_id");
		q.put("mine", mine);
		q.put("revealed", revealed);
		q.put("my_selected", selected.orElse(null));
		if (!privileged) {
			AUTHOR_ONLY.forEach(q::remove);
		}
		if (!revealed) {
			HIDDEN_BEFORE_ATTEMPT.forEach(q::remove);
		}
		return q;
	}

	// ---------- answering and discussing ----------

	@PostMapping("/questions/{id}/attempt")
	Map<String, Object> attempt(@PathVariable long id, @RequestBody AttemptIn in,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		Map<String, Object> q = load(id);
		if (!"APPROVED".equals(q.get("status"))) {
			throw notFound();
		}
		if (isAuthor(q, user) || user.instructor()) {
			throw bad("You cannot answer this question.");
		}
		String selected = in.selected() == null ? "" : in.selected().trim().toUpperCase();
		if (!List.of("A", "B", "C", "D").contains(selected)) {
			throw bad("Choose one option.");
		}
		// Only the first answer counts: a second request does not change it.
		db.sql("""
				insert into attempt (question_id, user_id, selected, correct) values (:q, :u, :s, :ok)
				on conflict (question_id, user_id) do nothing""").param("q", id).param("u", user.id())
				.param("s", selected).param("ok", selected.equals(q.get("correct"))).update();
		return get(id, user);
	}

	@GetMapping("/questions/{id}/comments")
	List<Map<String, Object>> comments(@PathVariable long id, @RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		requireRevealed(id, user);
		return db.sql("""
				select c.id, c.parent_id, c.body, c.is_ai, c.created_at, (c.author_id = :u) as mine,
				    case when c.is_ai then 'AI helper' when u.role = 'INSTRUCTOR' then 'Instructor'
				         else u.nickname end as author,
				    (c.author_id = q.author_id) as wrote_question,
				    case when :instructor then u.full_name end as real_name
				from comment c
				join question q on q.id = c.question_id
				left join app_user u on u.id = c.author_id
				where c.question_id = :q and not c.deleted order by c.id""").param("u", user.id())
				.param("instructor", user.instructor()).param("q", id).query().listOfRows();
	}

	@PostMapping("/questions/{id}/comments")
	Map<String, Object> comment(@PathVariable long id, @RequestBody CommentIn in,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		requireRevealed(id, user);
		return Map.of("id", addComment(id, user.id(), false, in.parentId(), commentBody(in.body())));
	}

	/** Posts the student's message and then the AI helper's answer as a reply to it. */
	@PostMapping("/questions/{id}/ask-ai")
	Map<String, Object> askAi(@PathVariable long id, @RequestBody CommentIn in,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		requireRevealed(id, user);
		if (!ai.enabled()) {
			throw bad("The AI helper is switched off.");
		}
		String body = commentBody(in.body());
		int today = db.sql("""
				select count(*) from comment a join comment p on p.id = a.parent_id
				where a.is_ai and p.author_id = :u and a.created_at > now() - interval '1 day'""")
				.param("u", user.id()).query(Integer.class).single();
		if (!user.instructor() && today >= props.askAiPerDay()) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"You can ask the AI helper " + props.askAiPerDay() + " times a day. Try again tomorrow.");
		}
		String answer;
		try {
			answer = ai.answerInThread(id, body);
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
					"The AI helper did not answer. Try again in a minute.");
		}
		long mine = addComment(id, user.id(), false, in.parentId(), body);
		addComment(id, null, true, mine, answer);
		return Map.of("id", mine);
	}

	@PostMapping("/questions/{id}/flag")
	Map<String, Object> flag(@PathVariable long id, @RequestBody FlagIn in,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		requireRevealed(id, user);
		String reason = in.reason() == null ? "" : in.reason().trim();
		if (reason.length() < 5 || reason.length() > 500) {
			throw bad("Write what is wrong (5 to 500 characters).");
		}
		db.sql("insert into flag (question_id, user_id, reason) values (:q, :u, :r)").param("q", id)
				.param("u", user.id()).param("r", reason).update();
		return Map.of("ok", true);
	}

	// ---------- writing ----------

	@PostMapping("/questions")
	Map<String, Object> create(@RequestBody QuestionIn raw, @RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		QuestionIn in = raw.checked();
		if (in.chapterId() == null) {
			throw bad("Choose a chapter.");
		}
		boolean open = db.sql("select open from chapter where id = :c").param("c", in.chapterId())
				.query(Boolean.class).optional().orElseThrow(() -> bad("Chapter not found."));
		if (!open && !user.instructor()) {
			throw bad("This chapter is closed for new questions.");
		}
		int count = db.sql("""
				select count(*) from question where chapter_id = :c and author_id = :u and status <> 'REJECTED'""")
				.param("c", in.chapterId()).param("u", user.id()).query(Integer.class).single();
		if (!user.instructor() && count >= props.questionsPerChapter()) {
			throw bad("You already have " + props.questionsPerChapter() + " questions in this chapter.");
		}
		long id = db.sql("""
				insert into question (chapter_id, author_id, stem, option_a, option_b, option_c, option_d, correct,
				    rationale_a, rationale_b, rationale_c, rationale_d, reference)
				values (:chapter, :author, :stem, :a, :b, :c, :d, :correct, :ra, :rb, :rc, :rd, :reference)
				returning id""").param("chapter", in.chapterId()).param("author", user.id())
				.params(fields(in)).query(Long.class).single();
		ai.checkAsync(id);
		return Map.of("id", id);
	}

	/** The author can change a question until the instructor approves or rejects it. */
	@PutMapping("/questions/{id}")
	Map<String, Object> update(@PathVariable long id, @RequestBody QuestionIn raw,
			@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		QuestionIn in = raw.checked();
		Map<String, Object> q = load(id);
		if (!isAuthor(q, user)) {
			throw notFound();
		}
		if (!List.of("PENDING", "NEEDS_REVISION").contains(q.get("status"))) {
			throw bad("This question is already " + q.get("status").toString().toLowerCase() + " and cannot change.");
		}
		db.sql(UPDATE_FIELDS + ", status = 'PENDING' where id = :id").param("id", id).params(fields(in)).update();
		ai.checkAsync(id);
		return Map.of("id", id);
	}

	@DeleteMapping("/questions/{id}")
	Map<String, Object> delete(@PathVariable long id, @RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		Map<String, Object> q = load(id);
		if (!isAuthor(q, user)) {
			throw notFound();
		}
		if ("APPROVED".equals(q.get("status"))) {
			throw bad("An approved question cannot be deleted.");
		}
		db.sql("delete from question where id = :id").param("id", id).update();
		return Map.of("ok", true);
	}

	// ---------- shared helpers (also used by the instructor screens) ----------

	static final String UPDATE_FIELDS = """
			update question set stem = :stem, option_a = :a, option_b = :b, option_c = :c, option_d = :d,
			    correct = :correct, rationale_a = :ra, rationale_b = :rb, rationale_c = :rc, rationale_d = :rd,
			    reference = :reference, updated_at = now()""";

	static Map<String, Object> fields(QuestionIn in) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("stem", in.stem());
		m.put("a", in.optionA());
		m.put("b", in.optionB());
		m.put("c", in.optionC());
		m.put("d", in.optionD());
		m.put("correct", in.correct());
		m.put("ra", in.rationaleA());
		m.put("rb", in.rationaleB());
		m.put("rc", in.rationaleC());
		m.put("rd", in.rationaleD());
		m.put("reference", in.reference());
		return m;
	}

	private Map<String, Object> load(long id) {
		return db.sql(SELECT_FULL + " where q.id = :id").param("id", id).query().listOfRows().stream().findFirst()
				.map(LinkedHashMap::new).orElseThrow(QuestionController::notFound);
	}

	private static boolean isAuthor(Map<String, Object> q, User user) {
		return ((Number) q.get("author_id")).longValue() == user.id();
	}

	/** The thread is open to the author, the instructor, and students who answered an approved question. */
	private void requireRevealed(long id, User user) {
		if (!Boolean.TRUE.equals(get(id, user).get("revealed"))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Answer the question first.");
		}
	}

	private long addComment(long questionId, Long authorId, boolean isAi, Long parentId, String body) {
		if (parentId != null) {
			boolean sameQuestion = db.sql("select count(*) from comment where id = :p and question_id = :q")
					.param("p", parentId).param("q", questionId).query(Integer.class).single() == 1;
			if (!sameQuestion) {
				throw bad("The comment you answer to does not exist.");
			}
		}
		return db.sql("""
				insert into comment (question_id, author_id, is_ai, parent_id, body)
				values (:q, :a, :ai, :p, :b) returning id""").param("q", questionId).param("a", authorId)
				.param("ai", isAi).param("p", parentId).param("b", body).query(Long.class).single();
	}

	private static String commentBody(String raw) {
		String body = raw == null ? "" : raw.trim();
		if (body.isEmpty() || body.length() > 2000) {
			throw bad("Write a comment (at most 2000 characters).");
		}
		return body;
	}

	static ResponseStatusException bad(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found.");
	}
}
