package br.com.albert.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public enum UserStatus {

	REGISTER (
			"As funcionalidades são apenas para usuários registrados. "
			+ "\nDeseja registrar? Clique na opção desejada:"
			+ "\n/Registrar para sim;"
			+ "\n/Descartar para não;"),
	NAME ("Digite seu nome completo: "),
	USERNAME ("Digite seu username do Telegram (@seunome): "),
	EMAIL ("Digite seu email: "), 
	PHONE_NUMBER ("Digite seu telefone cadastrado no Telegram \n(no formato +55 00 00000-0000): "),
	USER_POSITION ("Digite o número referente a sua posição no órgão (apenas o número):\n"
			+ "1 - Membro\n"
			+ "2 - Servidor\n"
			+ "3 - Estagiário\n"
			+ "4 - Colaborador");
	
	private final String msg;
	
	UserStatus(String msg) {
		this.msg = msg;
	}
	

	
	public static Stack<UserStatus> getAllStatusStack(){
		Stack<UserStatus> stack = new Stack<>();
		stack.add(PHONE_NUMBER);
		stack.add(EMAIL);
		stack.add(USERNAME);
		stack.add(NAME);
		stack.add(REGISTER);
		return stack;
	}
	
	public static List<UserStatus> getAllStatusList(){
		List<UserStatus> list = new ArrayList<>();
		for (UserStatus userStatus : UserStatus.values()) {
			list.add(userStatus);
		}
		return list;
	}

	public String getMsg() {
		return msg;
	}

}
