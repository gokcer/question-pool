package com.armikom.questionpool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Instructor screens. WebConfig lets only users with the INSTRUCTOR role reach /api/instructor/**. */
@RestController
@RequestMapping("/api/instructor")
public class InstructorController {

	private final JdbcClient db;
	private final AuthService auth;
	private final AiService ai;

	public InstructorController(JdbcClient db, AuthService auth, AiService ai) {
		this.db = db;
		this.auth = auth;
		this.ai = ai;
	}

	record RosterIn(String csv) {
	}

	record DecisionIn(String action, String note) {
	}

	record ChapterIn(String title, String docEn, String docTr, Boolean open) {
	}

	// ---------- roster ----------

	/** One student per line: e-mail, full name, student number (optional). Comma, semicolon or tab between them. */
	@PostMapping("/roster")
	Map<String, Object> importRoster(@RequestBody RosterIn in) {
		int added = 0;
		int updated = 0;
		List<String> skipped = new ArrayList<>();
		for (String line : (in.csv() == null ? "" : in.csv()).split("\\R")) {
			if (line.isBlank()) {
				continue;
			}
			String[] cells = line.split("[,;\\t]");
			String email = AuthService.normalize(cells[0]);
			String name = cells.length > 1 ? cells[1].trim() : "";
			String number = cells.length > 2 ? cells[2].trim() : null;
			if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+") || name.isEmpty()) {
				skipped.add(line.trim());
				continue;
			}
			int changed = db.sql("""
					update app_user set full_name = :n, student_no = :s, active = true
					where email = :e and role = 'STUDENT'""").param("n", name).param("s", number).param("e", email)
					.update();
			if (changed > 0) {
				updated++;
				continue;
			}
			boolean exists = db.sql("select count(*) from app_user where email = :e").param("e", email)
					.query(Integer.class).single() > 0;
			if (exists) {
				skipped.add(line.trim());
				continue;
			}
			db.sql("insert into app_user (email, full_name, student_no, nickname) values (:e, :n, :s, :nick)")
					.param("e", email).param("n", name).param("s", number).param("nick", auth.newNickname()).update();
			added++;
		}
		return Map.of("added", added, "updated", updated, "skipped", skipped);
	}

	/** One row per student with the numbers needed to judge participation. */
	@GetMapping("/students")
	List<Map<String, Object>> students() {
		return db.sql("""
				select u.id, u.full_name, u.student_no, u.email, u.nickname, u.active, u.last_login_at,
				    (select count(*) from question q where q.author_id = u.id) as questions,
				    (select count(*) from question q where q.author_id = u.id and q.status = 'APPROVED') as approved,
				    (select count(distinct q.chapter_id) from question q
				        where q.author_id = u.id and q.status <> 'REJECTED') as chapters,
				    (select count(*) from attempt a where a.user_id = u.id) as attempts,
				    (select count(*) from attempt a where a.user_id = u.id and a.correct) as correct,
				    (select count(*) from comment c where c.author_id = u.id and not c.deleted) as comments,
				    (select count(*) from flag f where f.user_id = u.id) as flags
				from app_user u where u.role = 'STUDENT' order by u.full_name""").query().listOfRows();
	}

	/** Everything one student wrote, with links back to the questions. */
	@GetMapping("/students/{id}")
	Map<String, Object> student(@PathVariable long id) {
		Map<String, Object> out = new LinkedHashMap<>(db.sql("""
				select id, full_name, student_no, email, nickname, last_login_at from app_user where id = :id""")
				.param("id", id).query().listOfRows().stream().findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found.")));
		out.put("questions", db.sql("""
				select q.id, q.stem, q.status, q.created_at, c.number as chapter_number,
				    q.ai_review::text as ai_review from question q
				join chapter c on c.id = q.chapter_id where q.author_id = :id order by c.number, q.id""")
				.param("id", id).query().listOfRows());
		out.put("comments", db.sql("""
				select m.id, m.question_id, m.body, m.created_at, m.deleted, q.stem from comment m
				join question q on q.id = m.question_id where m.author_id = :id order by m.id""").param("id", id)
				.query().listOfRows());
		out.put("flags", db.sql("""
				select f.id, f.question_id, f.reason, f.resolved, f.created_at from flag f
				where f.user_id = :id order by f.id""").param("id", id).query().listOfRows());
		out.put("attempts_by_chapter", db.sql("""
				select c.number as chapter_number, count(*) as attempts, count(*) filter (where a.correct) as correct
				from attempt a join question q on q.id = a.question_id join chapter c on c.id = q.chapter_id
				where a.user_id = :id group by c.number order by c.number""").param("id", id).query().listOfRows());
		return out;
	}

	// ---------- review ----------

	@GetMapping("/questions")
	List<Map<String, Object>> questions(@RequestParam(defaultValue = "PENDING") String status,
			@RequestParam(required = false) Integer chapterId) {
		return db.sql("""
				select q.id, q.chapter_id, c.number as chapter_number, q.stem, q.option_a, q.option_b, q.option_c,
				    q.option_d, q.correct, q.rationale_a, q.rationale_b, q.rationale_c, q.rationale_d, q.reference,
				    q.status, q.ai_status, q.ai_review::text as ai_review, q.instructor_note, q.explanation,
				    q.created_at, u.nickname as author, u.full_name as author_name, u.id as author_id,
				    (select count(*) from attempt a where a.question_id = q.id) as attempts,
				    (select count(*) from attempt a where a.question_id = q.id and a.correct) as correct_attempts,
				    (select count(*) from flag f where f.question_id = q.id and not f.resolved) as open_flags
				from question q
				join app_user u on u.id = q.author_id
				join chapter c on c.id = q.chapter_id
				where q.status = :status and (cast(:chapter as int) is null or q.chapter_id = :chapter)
				order by c.number, q.id""").param("status", status).param("chapter", chapterId).query().listOfRows();
	}

	/** How many questions wait in each state, for the headings of the review screen. */
	@GetMapping("/questions/counts")
	List<Map<String, Object>> counts() {
		return db.sql("select status, count(*) as count from question group by status").query().listOfRows();
	}

	@PostMapping("/questions/{id}/decision")
	Map<String, Object> decide(@PathVariable long id, @RequestBody DecisionIn in) {
		String note = in.note() == null || in.note().isBlank() ? null : in.note().trim();
		String status = switch (in.action() == null ? "" : in.action()) {
			case "APPROVE" -> "APPROVED";
			case "REJECT" -> "REJECTED";
			case "REVISE" -> "NEEDS_REVISION";
			default -> throw QuestionController.bad("Unknown action.");
		};
		if (!"APPROVED".equals(status) && note == null) {
			throw QuestionController.bad("Write a note so the student knows what to change.");
		}
		int changed = db.sql("""
				update question set status = :s, instructor_note = :n,
				    approved_at = case when :s = 'APPROVED' then now() else null end
				where id = :id""").param("s", status).param("n", note).param("id", id).update();
		if (changed == 0) {
			throw QuestionController.notFound();
		}
		if ("APPROVED".equals(status)) {
			ai.explainAsync(id);
		}
		return Map.of("ok", true);
	}

	/** The instructor can fix wording at any time. The state of the question does not change. */
	@PutMapping("/questions/{id}")
	Map<String, Object> edit(@PathVariable long id, @RequestBody QuestionController.QuestionIn raw) {
		QuestionController.QuestionIn in = raw.checked();
		int changed = db.sql(QuestionController.UPDATE_FIELDS + " where id = :id").param("id", id)
				.params(QuestionController.fields(in)).update();
		if (changed == 0) {
			throw QuestionController.notFound();
		}
		return Map.of("ok", true);
	}

	@PostMapping("/questions/{id}/ai-check")
	Map<String, Object> rerunCheck(@PathVariable long id) {
		ai.checkAsync(id);
		return Map.of("ok", true);
	}

	@PostMapping("/questions/{id}/explain")
	Map<String, Object> rerunExplanation(@PathVariable long id) {
		ai.explainAsync(id);
		return Map.of("ok", true);
	}

	// ---------- flags and moderation ----------

	@GetMapping("/flags")
	List<Map<String, Object>> flags() {
		return db.sql("""
				select f.id, f.question_id, f.reason, f.resolved, f.created_at, q.stem, q.status,
				    u.full_name as student_name, u.nickname
				from flag f join question q on q.id = f.question_id join app_user u on u.id = f.user_id
				order by f.resolved, f.id desc""").query().listOfRows();
	}

	@PostMapping("/flags/{id}/resolve")
	Map<String, Object> resolveFlag(@PathVariable long id) {
		db.sql("update flag set resolved = true where id = :id").param("id", id).update();
		return Map.of("ok", true);
	}

	/** Hides a comment from students. The text stays in the database for the participation report. */
	@PostMapping("/comments/{id}/hide")
	Map<String, Object> hideComment(@PathVariable long id) {
		db.sql("update comment set deleted = true where id = :id").param("id", id).update();
		return Map.of("ok", true);
	}

	// ---------- chapters ----------

	@PutMapping("/chapters/{id}")
	Map<String, Object> updateChapter(@PathVariable int id, @RequestBody ChapterIn in) {
		int changed = db.sql("""
				update chapter set title = coalesce(:t, title), doc_en = coalesce(:en, doc_en),
				    doc_tr = coalesce(:tr, doc_tr), open = coalesce(:o, open)
				where id = :id""").param("t", in.title()).param("en", in.docEn()).param("tr", in.docTr())
				.param("o", in.open()).param("id", id).update();
		if (changed == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found.");
		}
		return Map.of("ok", true);
	}
}
