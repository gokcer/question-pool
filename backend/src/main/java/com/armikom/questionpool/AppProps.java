package com.armikom.questionpool;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProps(
		String baseUrl,
		String mailFrom,
		String instructorEmail,
		String instructorName,
		boolean secureCookies,
		int questionsPerChapter,
		int askAiPerDay,
		String chapterDocsDir,
		String openrouterUrl,
		String openrouterKey,
		String openrouterModel) {
}
