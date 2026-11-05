package com.armikom.questionpool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

	private static final Logger log = LoggerFactory.getLogger(ChapterController.class);

	private final JdbcClient db;
	private final AppProps props;

	public ChapterController(JdbcClient db, AppProps props) {
		this.db = db;
		this.props = props;
	}

	/**
	 * First-time fill: when a chapter has no text yet, it is read from week<N>.en.md and week<N>.tr.md in the folder
	 * CHAPTER_DOCS_DIR. Later changes are made on the "Chapter texts" screen and are never overwritten from files.
	 */
	@EventListener(ApplicationReadyEvent.class)
	void seedDocs() throws IOException {
		if (props.chapterDocsDir() == null || props.chapterDocsDir().isBlank()) {
			return;
		}
		for (Map<String, Object> c : db.sql("select id, number, doc_en, doc_tr from chapter").query().listOfRows()) {
			for (String lang : List.of("en", "tr")) {
				Path file = Path.of(props.chapterDocsDir(), "week" + c.get("number") + "." + lang + ".md");
				if (c.get("doc_" + lang).toString().isEmpty() && Files.isRegularFile(file)) {
					db.sql("update chapter set doc_" + lang + " = :d where id = :id")
							.param("d", Files.readString(file)).param("id", c.get("id")).update();
					log.info("Chapter {} ({}) text loaded from {}", c.get("number"), lang, file);
				}
			}
		}
	}

	/** Chapters with the signed-in student's own progress. */
	@GetMapping
	List<Map<String, Object>> list(@RequestAttribute(WebConfig.USER_ATTRIBUTE) User user) {
		return db.sql("""
				select c.id, c.number, c.title, c.open,
				    (c.doc_en <> '') as has_doc_en, (c.doc_tr <> '') as has_doc_tr,
				    (select count(*) from question q where q.chapter_id = c.id and q.status = 'APPROVED') as approved,
				    (select count(*) from question q where q.chapter_id = c.id and q.author_id = :u
				        and q.status <> 'REJECTED') as my_questions,
				    (select count(*) from attempt a join question q on q.id = a.question_id
				        where q.chapter_id = c.id and a.user_id = :u and q.status = 'APPROVED') as my_attempts,
				    (select count(*) from attempt a join question q on q.id = a.question_id
				        where q.chapter_id = c.id and a.user_id = :u and a.correct and q.status = 'APPROVED') as my_correct
				from chapter c order by c.number""").param("u", user.id()).query().listOfRows();
	}

	/** One chapter with its reference document in both languages. */
	@GetMapping("/{id}")
	Map<String, Object> get(@PathVariable int id) {
		return db.sql("select id, number, title, open, doc_en, doc_tr from chapter where id = :id").param("id", id)
				.query().listOfRows().stream().findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found."));
	}
}
