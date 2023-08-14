package br.com.albert.util;

import java.util.Arrays;

import br.com.albert.enums.UserPosition;
import br.com.albert.enums.UserStatus;

public class StringUtils {
	
	private static String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
	private static String TELEPHONE_REGEX = "^\\+55\\s\\d{2}\\s9?\\d{4}-\\d{4}$";

	public static boolean isEmailValid(String email) {
		return email.matches(EMAIL_REGEX);
	}
	
	public static boolean isPhoneValid(String phone) {
		return phone.matches(TELEPHONE_REGEX);
	}

	public static boolean isNameValid(String text) {
		return !text.isBlank() && !text.trim().startsWith("/");
	}

	public static boolean isUsernameValid(String text) {
		return text.trim().startsWith("@");
	}
	
	public static boolean isValid(String text, UserStatus status) {
		switch (status) {
		case EMAIL: {
			return isEmailValid(text);
		}
		case PHONE_NUMBER: {
			return isPhoneValid(text);
		}
		case NAME: {
			return isNameValid(text);
		}
		case USERNAME: {
			return isUsernameValid(text);
		}
		case USER_POSITION: {
			return isPositinValid(text);
		}
		default:
			return false;
		}
	}

	private static boolean isPositinValid(String text) {
		try {
		Integer id = Integer.parseInt(text.trim());
		return Arrays.stream(UserPosition.values()).anyMatch(UserPosition.valueOf(id)::equals);
		}catch (NumberFormatException e) {
			return false;
		}
	}
}
