package com.armikom.questionpool;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exam builder (instructor only). An exam is a random draw of approved questions, a number per chapter. Each form
 * (A, B, ...) has the same questions; the order inside a chapter and the order of the options are shuffled per form.
 * The shuffle depends only on the exam's seed, the form and the question, so a form and its answer key always agree.
 */
@RestController
@RequestMapping("/api/instructor/exams")
public class ExamController {

	private static final List<String> LETTERS = List.of("A", "B", "C", "D");
	private static final String DRAW = """
			select q.id from question q
			where q.chapter_id = :c and q.status = 'APPROVED'
			  and q.id not in (select question_id from exam_item where exam_id = :e)
			  and (not :skip or not exists (select 1 from flag f where f.question_id = q.id and not f.resolved))
			order by random() limit :n""";

	private final JdbcClient db;

	public ExamController(JdbcClient db) {
		this.db = db;
	}

	record ExamIn(String title, Integer forms, Map<Integer, Integer> counts, Boolean skipFlagged) {
	}

	@GetMapping
	List<Map<String, Object>> list() {
		return db.sql("""
				select e.id, e.title, e.forms, e.created_at,
				    (select count(*) from exam_item i where i.exam_id = e.id) as questions
				from exam e order by e.id desc""").query().listOfRows();
	}

	/** Draws the questions. Fails, and saves nothing, when a chapter does not have enough approved questions. */
	@PostMapping
	@Transactional
	public Map<String, Object> create(@RequestBody ExamIn in) {
		String title = in.title() == null ? "" : in.title().trim();
		if (title.isEmpty() || title.length() > 120) {
			throw QuestionController.bad("Write a title (at most 120 characters).");
		}
		int forms = in.forms() == null ? 2 : in.forms();
		if (forms < 1 || forms > 4) {
			throw QuestionController.bad("Choose 1 to 4 forms.");
		}
		long examId = db.sql("insert into exam (title, forms, seed) values (:t, :f, :s) returning id")
				.param("t", title).param("f", forms).param("s", new SecureRandom().nextLong()).query(Long.class)
				.single();
		int total = 0;
		for (Map<String, Object> chapter : db.sql("select id, number from chapter order by number").query()
				.listOfRows()) {
			int wanted = in.counts() == null ? 0 : in.counts().getOrDefault((Integer) chapter.get("id"), 0);
			if (wanted <= 0) {
				continue;
			}
			List<Long> drawn = draw(examId, (Integer) chapter.get("id"), wanted, !Boolean.FALSE.equals(in.skipFlagged()));
			if (drawn.size() < wanted) {
				throw QuestionController.bad("Week " + chapter.get("number") + " has only " + drawn.size()
						+ " usable approved questions, but you asked for " + wanted + ".");
			}
			drawn.forEach(q -> addItem(examId, q));
			total += drawn.size();
		}
		if (total == 0) {
			throw QuestionController.bad("Ask for at least one question.");
		}
		return Map.of("id", examId);
	}

	/** The exam with its questions in chapter order, with the original (unshuffled) options and key. */
	@GetMapping("/{id}")
	Map<String, Object> get(@PathVariable long id) {
		Map<String, Object> exam = new LinkedHashMap<>(exam(id));
		exam.remove("seed");
		exam.put("items", items(id));
		return exam;
	}

	@DeleteMapping("/{id}")
	Map<String, Object> delete(@PathVariable long id) {
		db.sql("delete from exam where id = :id").param("id", id).update();
		return Map.of("ok", true);
	}

	/** Replaces one question with another random approved question of the same chapter. */
	@PostMapping("/{id}/items/{itemId}/swap")
	@Transactional
	public Map<String, Object> swap(@PathVariable long id, @PathVariable long itemId) {
		Integer chapterId = db.sql("""
				select q.chapter_id from exam_item i join question q on q.id = i.question_id
				where i.id = :i and i.exam_id = :e""").param("i", itemId).param("e", id).query(Integer.class)
				.optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
		List<Long> drawn = draw(id, chapterId, 1, true);
		if (drawn.isEmpty()) {
			throw QuestionController.bad("This chapter has no other approved question to use.");
		}
		db.sql("update exam_item set question_id = :q where id = :i").param("q", drawn.get(0)).param("i", itemId)
				.update();
		return Map.of("ok", true);
	}

	@PostMapping("/{id}/items/{itemId}/remove")
	Map<String, Object> remove(@PathVariable long id, @PathVariable long itemId) {
		db.sql("delete from exam_item where id = :i and exam_id = :e").param("i", itemId).param("e", id).update();
		return Map.of("ok", true);
	}

	/** One printable form: numbered questions with shuffled options, and the key for this form. */
	@GetMapping("/{id}/forms/{form}")
	Map<String, Object> form(@PathVariable long id, @PathVariable int form) {
		Map<String, Object> exam = exam(id);
		if (form < 1 || form > (Integer) exam.get("forms")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This exam does not have that form.");
		}
		long seed = ((Number) exam.get("seed")).longValue();
		List<Map<String, Object>> items = new ArrayList<>(items(id));
		// Shuffle the questions, then put them back in chapter order: the order inside a chapter is what changes.
		Collections.shuffle(items, new Random(mix(seed, form, 0)));
		items.sort((a, b) -> Integer.compare((Integer) a.get("chapter_number"), (Integer) b.get("chapter_number")));
		List<Map<String, Object>> questions = new ArrayList<>();
		for (Map<String, Object> item : items) {
			List<String> order = new ArrayList<>(LETTERS);
			Collections.shuffle(order, new Random(mix(seed, form, ((Number) item.get("question_id")).longValue())));
			Map<String, Object> q = new LinkedHashMap<>();
			q.put("number", questions.size() + 1);
			q.put("question_id", item.get("question_id"));
			q.put("chapter_number", item.get("chapter_number"));
			q.put("stem", item.get("stem"));
			q.put("options", order.stream().map(l -> item.get("option_" + l.toLowerCase())).toList());
			q.put("correct", LETTERS.get(order.indexOf(item.get("correct"))));
			questions.add(q);
		}
		return Map.of("title", exam.get("title"), "form", String.valueOf((char) ('A' + form - 1)), "questions",
				questions);
	}

	/**
	 * Turns (seed, form, question) into a well-spread seed. Without this, java.util.Random gives almost the same
	 * first numbers for seeds that are close together, and most correct answers land on the same letter.
	 */
	private static long mix(long seed, long form, long questionId) {
		long z = seed + form * 0x9E3779B97F4A7C15L + questionId * 0xD1B54A32D192ED03L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	private Map<String, Object> exam(long id) {
		return db.sql("select id, title, forms, seed, created_at from exam where id = :id").param("id", id).query()
				.listOfRows().stream().findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found."));
	}

	private List<Map<String, Object>> items(long examId) {
		return db.sql("""
				select i.id as item_id, q.id as question_id, c.number as chapter_number, q.stem, q.option_a,
				    q.option_b, q.option_c, q.option_d, q.correct, q.status, u.full_name as author_name,
				    (select count(*) from attempt a where a.question_id = q.id) as attempts,
				    (select count(*) from attempt a where a.question_id = q.id and a.correct) as correct_attempts,
				    (select count(*) from flag f where f.question_id = q.id and not f.resolved) as open_flags
				from exam_item i
				join question q on q.id = i.question_id
				join chapter c on c.id = q.chapter_id
				join app_user u on u.id = q.author_id
				where i.exam_id = :e order by c.number, i.id""").param("e", examId).query().listOfRows();
	}

	private List<Long> draw(long examId, int chapterId, int count, boolean skipFlagged) {
		return db.sql(DRAW).param("c", chapterId).param("e", examId).param("skip", skipFlagged).param("n", count)
				.query(Long.class).list();
	}

	private void addItem(long examId, long questionId) {
		db.sql("insert into exam_item (exam_id, question_id) values (:e, :q)").param("e", examId)
				.param("q", questionId).update();
	}
}
