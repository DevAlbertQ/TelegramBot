package br.com.albert.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.models.TelegramUser;
import br.com.albert.services.BotService;
import br.com.albert.util.RegexUtils;
import br.com.albert.util.UserStates;
import jakarta.annotation.PostConstruct;

@Configuration
public class TelegramBot extends TelegramLongPollingBot {

	public TelegramBot(@Value("${bot.token}") String token) {
		super(token);
	}

//	private HashMap<AskStates, Long> userStates;

	private Stack<UserStates> userActions;
	
	private UserStates actualAction;

	private List<UserStates> userStates = new ArrayList<>();
	
	@Autowired
	private TelegramUser user;

	@Value("${bot.token}")
	private String token;

	@Value("${bot.username}")
	private String username;

	@Autowired
	private BotService service;

	@PostConstruct
	public void init() {
		service.setBot(this);
	}

	private Update update = new Update();

	/**
	 * @implNote The method that controls the messages received by the bot
	 **/
	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {

			SendMessage sender = new SendMessage();
			User from = update.getMessage().getFrom();

			//Busca os atuais dados já preenchido do usuário, caso ainda não tivesse buscado
			if (userStates.isEmpty()) {
				userStates = service.getUserState(from.getId());

			}

			
			if (userActions.isEmpty()) {
				if (!userStates.contains(UserStates.REGISTER)) {
					sender.setText("Seu cadastro ainda não foi efetivado em nossa base de dados!"
							+ "Clique em /SIM para realizar o cadastro" + "Clique em /NÃO para finalizar");
					sender.setChatId(update.getMessage().getChatId());
					try {
						execute(sender);
					} catch (TelegramApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}else {
					getActions();
					actualAction = userActions.peek();
					registerUser(update);
				}
			}else {
				if(update.getMessage().getText().equals("/SIM")) {
					actualAction = UserStates.REGISTER;
					registerUser(update);
				}
			}

			String message = "";

			switch (update.getMessage().getText()) {
			case "/Sim":
				sender.setText("Seu cadastro ainda não foi efetivado em nossa base de dados!"
						+ "Clique em /SIM para realizar o cadastro" + "Clique em /NÃO para finalizar");
				sender.setChatId(update.getMessage().getChatId());
				getActions();
				break;
				
			case "/Cadastrar":
				sender.setChatId(update.getMessage().getChatId());
				service.register(update.getMessage().getFrom());
				sender.setText("Por favor digite seu nome:");
				userActions.add(UserStates.REGISTER);
				break;
			case "/Opcao1": {
				if (!userStates.isEmpty() && userActions.contains(UserStates.REGISTER)) {
					
				}
				message = "Realizar tarefa 2";
				break;
			}

			case "/Opcao2": {
				message = "Realizar tarefa 2";
				break;
			}

			default:
				if (this.userStates.isEmpty()) {
					message = "Olá %s! Eu sou o bot do Albert\n"
							+ "\nVocê ainda não é cadastrado em nossa base de dados"
							+ "\n/Cadastrar para receber atualizações";
				} else {
					message = "Olá %s! Eu sou o bot do Albert\n"
							+ "\n/Opção1: realizar tarefa 1"
							+ "\n/Opção2: realizar tarefa 2";
				}
			}

			sender.setText(String.format(message, update.getMessage().getFrom().getFirstName()));
			System.out.println(JsonFormatter.prettyPrint(update.toString()));
			System.out.println(String.format(message, update.getMessage().getFrom().getFirstName()));

			try {
				execute(sender);
			} catch (TelegramApiException e) {
				// TODO: handle exception
			}
		}
		if (this.update.getUpdateId() == null || this.update.getUpdateId() < update.getUpdateId()) {
			this.update = update;
		}

	}


	public Message sendMsg(SendMessage message) {
		Message msg = new Message();
		try {
			msg = execute(message);
		} catch (TelegramApiException e) {
			// TODO: handle exception
		}
		return msg;
	}

	public Message sendPoolQuestion(SendPoll pool) {
		Message msg = new Message();
		if (pool != null) {
			try {
				msg = execute(pool);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msg;
	}

	public void getActions(){
		for (UserStates state : UserStates.values()) {
			if(!this.userStates.contains(state)) {
				if(!state.equals(UserStates.REGISTER))
					this.userActions.add(state);
			}
		}
	}
	
	public void registerUser(Update update) {
		Message message = update.getMessage();
		String text = getMessageFromUserState(userActions.peek());
		SendMessage msg = new SendMessage();
		if(actualAction == UserStates.REGISTER) {
			msg.setText(text);
			msg.setChatId(update.getMessage().getFrom().getId());
			actualAction = userActions.peek();
			userStates.add(UserStates.REGISTER);
			sendMsg(msg);
		}
		actualAction = userActions.peek();
		switch (actualAction) {
		case EMAIL: {
			if(RegexUtils.isEmailValid(message.getText())) {
				user.setEmail(message.getText());
				user = service.save(user);
				userActions.pop();
				actualAction = userActions.peek();
				text = getMessageFromUserState(actualAction);
				userStates.add(UserStates.EMAIL);
			}
			msg.setChatId(message.getChatId());
			msg.setText(text);
			sendMsg(msg);
			break;
		}
		
		case USER_FULL_NAME: {
			if(!message.getText().isBlank()) {
				String name = message.getText();
				
				String[] split = name.split(" ");
				user.setFirstName(split[0].trim());
				user.setLastName(name.replaceFirst(split[0], "").trim());
				user = service.save(user);
				userActions.pop();
				actualAction = userActions.peek();
				text = getMessageFromUserState(actualAction);
				userStates.add(UserStates.USER_FULL_NAME);
			}
			msg.setChatId(message.getChatId());
			msg.setText(text);
			sendMsg(msg);
			break;
		}
		
		case USER_PHONE: {
			String tel = msg.getText().trim().replaceAll("+", "");
			if(RegexUtils.isCellNumberValid(tel)) {
				user.setCellNumber(Long.getLong(text));
				user = service.save(user);
				userActions.pop();
				actualAction = userActions.peek();
				text = getMessageFromUserState(actualAction);
				userStates.add(UserStates.USER_PHONE);
			}
			msg.setChatId(message.getChatId());
			msg.setText(text);
			sendMsg(msg);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + actualAction);
		}
		
	}
	
	private String getMessageFromUserState(UserStates state) {
		String msg;
		switch (state) {
		case EMAIL: {
			msg = "Por favor digite seu email: ";
			break;
		}
		case USER_FULL_NAME: {
			msg = "Por favor digite seu nome completo: ";
			break;
		}
		case USER_PHONE: {
			msg = "Por favor digite seu telefone registrado no Telegram no formato +5561012345678 (sem espaços ou outros caracteres)";
			break;
		}
		case USERNAME: {
			msg = "Por favor digite o nome de usuário do Telegram no formato @usuario";
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + actualAction);
		}
		return msg;
	}

	@Override
	public String getBotUsername() {
		return this.username;
	}

	@Override
	public String getBotToken() {
		return this.token;
	}

	public BotService getBotService() {
		return this.service;
	}
}
