package com.armikom.questionpool;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProps(
		String baseUrl,
		String mailFrom,
		String ewsUrl,
		List<String> instructorEmails,
		List<String> instructorNames,
		boolean secureCookies,
		int questionsPerChapter,
		int askAiPerDay,
		String chapterDocsDir,
		String openrouterUrl,
		String openrouterKey,
		String openrouterModel) {
}
