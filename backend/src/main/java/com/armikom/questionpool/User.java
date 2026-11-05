package com.armikom.questionpool;

public record User(long id, String email, String fullName, String role, String nickname) {

	public boolean instructor() {
		return "INSTRUCTOR".equals(role);
	}
}
