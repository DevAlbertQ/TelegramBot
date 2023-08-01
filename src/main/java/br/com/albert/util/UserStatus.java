package br.com.albert.util;

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
	PHONE_NUMBER ("Digite seu telefone cadastrado no Telegram (no formato +55 00 00000-0000): ");
	
	private String msg;
	
	UserStatus(String msg) {
		this.msg = msg;
	}
	
	UserStatus(){
		
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

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
