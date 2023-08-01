package br.com.albert.services;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.models.TelegramUser;
import br.com.albert.repositories.TelegramUserRepository;
import br.com.albert.util.StringUtils;
import br.com.albert.util.UserStatus;

@Service
public class BotService extends TelegramLongPollingBot {

	public BotService(@Value("${bot.token}") String token) {
		super(token);
	}

	@Value("${bot.token}")
	private String token;

	@Value("${bot.username}")
	private String username;

	@Autowired
	private TelegramUserRepository repository;

	private Stack<UserStatus> userStatus = new Stack<>();
	private TelegramUser user = new TelegramUser();

	private Logger log = Logger.getLogger(BotService.class.getName());

	public Message sendMessage(SendMessage message) {
		try {
			return execute(message);
		} catch (TelegramApiException e) {
			log.throwing(BotService.class.getName(), "sendMessage", e);
		}
		return new Message();
	}

	public SendMessage getJsonMessage() {
		SendMessage message = new SendMessage();
		message.setChatId(1l);
		message.setText("Message");
		List<MessageEntity> entities = new ArrayList<>();
		MessageEntity entity = new MessageEntity();
		entity.setType("bot_command");
		entity.setOffset(0);
		entity.setLength(7);
		entity.setText("Message");
		entities.add(entity);
		message.setEntities(entities);
		System.out.println(JsonFormatter.prettyPrint(message.toString()));
		return message;
	}

	public String getJson() {
		String string = "" + "{\r\n" + "	\"chat_id\": \"856471610\",\r\n"
				+ "	\"text\": \"/Message is a command\",\r\n" + "	\"entities\": [\r\n" + "		{\r\n"
				+ "		   \"type\": \"bot_command\",\r\n" + "		   \"offset\": \"0\",\r\n"
				+ "		   \"length\": \"7\",\r\n" + "		   \"text\": \"/Message\"\r\n" + "	   }\r\n" + "	],\r\n"
				+ "	\"method\": \"sendmessage\"\r\n" + "}";
		return JsonFormatter.prettyPrint(string);
	}

	@Override
	public void onUpdateReceived(Update update) {

		log.info("Recived an update");
		if (update.hasMessage() && update.getMessage().hasText()) {
			log.info("update has message and text");
			Message message = update.getMessage();
			user = repository.findById(update.getMessage().getFrom().getId()).orElse(null);
			log.info("Get an user from DB: " + user);
			if (user == null) {
				log.info("User is null");
				user = new TelegramUser();
				try {
					handleNewUser(update.getMessage());
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleNewUser", e);
					e.printStackTrace();
				}
			} else if (message.getText().contains("/Cadastrar")) {
				log.info("Message contains /Cadastrar");
				try {
					handleNewUser(message);
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleNewUser", e);
					e.printStackTrace();
				}
			} else if (user == null || !user.isAllFilled()) {
				log.info("User is not all filled");
				try {
					handleNewUser(message);
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleNewUser", e);
					e.printStackTrace();
				}
			} else if (user.isAllFilled()) {
				try {
					handleFunctions(message);
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleFunctions", e);
				}
			}
		}

	}

	private void handleNewUser(Message message) throws Throwable {
		if (this.user != null && this.user.isAllFilled()) {
			handleFunctions(message);
			return;
		}
		log.info("handleNewUser method");
		if (this.userStatus.isEmpty()) {
			this.userStatus = user.getUserStatus();
		}
		UserStatus status = userStatus.peek();

		if (status == UserStatus.REGISTER) {
			log.info("Status == REGISTER");
			sendMessage(message.getChatId(), status.getMsg());
			userStatus.pop();
		} else if (message.getText().contains("/Registrar")) {
			sendMessage(message.getChatId(), status.getMsg());
		} else if (StringUtils.isValid(message.getText(), status)) {
			userStatus.pop();
			log.info("Text valid: " + message.getText() + " " + status);
			user.setX(status, message.getText());
			user.setIdUser(message.getChatId());
			if (user.getAddDate() == null) {
				user.setAddDate(new Date());
			}
			repository.save(user);
			sendMessage(message.getChatId(), userStatus.peek().getMsg());
		} else {
			log.info("Text invalid " + message.getText() + " " + status);
			userStatus.add(status);
			String msg = "Não entendi!\n" + status.getMsg();
			sendMessage(message.getChatId(), msg);
		}

	}

	private void handleFunctions(Message message) throws Throwable {
		List<MessageEntity> entities = message.getEntities();
		String msg = "";
		if (entities.isEmpty()) {
			msg = "Olá " + user.getFullName() + "!" + "\nEscolha uma das funcionalidades abaixo clicando na desejada:"
					+ "\n/Op01 : Realizar tarefa 1" + "\n/Op02 : Realizar tarefa 2" + "\n/Op03 : Realizar tarefa 3";
			sendMessage(message.getChatId(), msg);
			return;
		} else {
			List<MessageEntity> functions = new ArrayList<>();
			entities.stream().forEach(e -> {
				if (e.getType() == "bot_command")
					functions.add(e);
			});

			if (!functions.isEmpty()) {
				switch (functions.get(0).getText()) {
				case "/Op01": {
					sendMessage(message.getChatId(), "Abrindo o Único...");
					openUnico();
					break;
				}
				case "/Op02": {
					sendMessage(message.getChatId(), "Executando Op02...");
					break;
				}
				case "/Op03": {
					sendMessage(message.getChatId(), "Executando Op03...");
					break;
				}
				default:
					sendMessage(message.getChatId(),
							"Desculpe, não entendi." + "\nEscolha uma das funcionalidades abaixo clicando na desejada:"
									+ "\n/Op01 : Abrir o Único" + "\n/Op02 : Realizar tarefa 2"
									+ "\n/Op03 : Realizar tarefa 3");
				}
			}
		}
	}

	public void openUnico() {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new URI("https://novoportal.mpf.mp.br/unico"));
		} catch (IOException | URISyntaxException e) {
			log.throwing(BotService.class.getName(), "openUnico", e);
		}
	}

	public Message sendMessage(Long id, String message) throws TelegramApiException {
		SendMessage msg = new SendMessage();
		if (id != null) {
			msg.setChatId(id);
			msg.setText(message);
			return execute(msg);
		}
		throw new IllegalArgumentException("Id must be set!");
	}

	public void sendPool() throws Throwable {
		SendPoll poll = new SendPoll();
		poll.setChatId(0l);
		List<String> options = new ArrayList<>();
		options.add("Sim");
		options.add("Não");
		poll.setOptions(options);
		poll.setQuestion("Poll question");
		execute(poll);

	}

	@Override
	public String getBotUsername() {
		return this.username;
	}

}
