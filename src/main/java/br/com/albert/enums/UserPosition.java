package br.com.albert.enums;

public enum UserPosition {

	MEMBRO(1), SERVIDOR(2), ESTAGIARIO(3), COLABORADOR(4);

	private final Integer code;

	UserPosition(Integer id) {
		this.code = id;
	}

	public Integer getCode() {
		return this.code;
	}

	public static UserPosition valueOf(Integer code) {
		for (UserPosition userPosition : UserPosition.values()) {
			if (code == userPosition.getCode()) {
				return userPosition;
			}
		}
		throw new IllegalArgumentException("Invalid UserPosition code");
	}

}
