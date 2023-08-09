package br.com.albert.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
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
public class TelegramBotService extends TelegramLongPollingBot {

	private static final String COMMAND_REGISTER = "/Registrar";
	private static final String COMMAND_UNICO = "/Unico";
	private static final String COMMAND_EXCLUDE = "/Excluir";
	private static final String COMMAND_FUNCTION_3 = "/Op03";
	private static final String COMMAND_YES = "/SIM_EXCLUIR";
	private static final String COMMAND_NO = "/NAO";
	private static final String MESSAGE_WELCOME = "Olá %s!\nEscolha uma das funcionalidades abaixo clicando na desejada:\n"
			+ COMMAND_UNICO + " : Abrir o Único\n" + COMMAND_EXCLUDE + " : Excluir seu usuário da base de dados\n"
			+ COMMAND_FUNCTION_3 + " : Realizar tarefa 3";
	private static final String MESSAGE_EXCLUDE_USER = "Após a exclusão você não poderá mais receber menságens ou realizar interações com o bot."
			+ "\nTem certeza que deseja excluir? Clique em %s para comfirmar a exclusão e em %s para voltar ao menu";
	private static final CharSequence COMMAND_DISCARD = "/Descartar";
	private static final String MESSAGE_DISCARD = "As funcionalidades do bot são apenas para usuários cadastrados, para cadastrar clique em "
			+ COMMAND_REGISTER;
	private static final String[] COMMANDS_ALLOWED = new String[] { COMMAND_UNICO, COMMAND_EXCLUDE,
			COMMAND_FUNCTION_3 };

	public TelegramBotService(@Value("${bot.token}") String token) {
		super(token);
	}

	@Autowired
	private TelegramUserRepository userRepository;

	@Value("${bot.username}")
	private String botUsername;

	@Value("${bot.token}")
	private String botToken;

	private AtomicReference<TelegramUser> user = new AtomicReference<>();

	private Stack<UserStatus> userStatus = new Stack<>();

	private Logger log = Logger.getLogger(TelegramBotService.class.getName());

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			Message recivedMessage = update.getMessage();
			SendMessage sendMessage = null;

			sendMessage = Optional.ofNullable(handleNewUser(recivedMessage)).orElse(handleMessage(recivedMessage));

			try {
				execute(sendMessage);
			} catch (TelegramApiException e) {
				log.throwing(TelegramBotService.class.getName(), "onUpdateReceived", e);
			}
		}

	}

	private SendMessage handleNewUser(Message recivedMessage) {
		SendMessage sendMessage = null;
		if (user.get() == null || user.get().getAddDate() == null) {
			user.set(userRepository.findById(recivedMessage.getChatId()).orElse(new TelegramUser()));
			if (user.get().getAddDate() == null) {
				user.get().setAddDate(new Date());
			}
		}
		if (user.get() != null && user.get().isAllFilled()) {
			return null;
		}
		if (recivedMessage.getText().contains(COMMAND_DISCARD)) {
			sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()), MESSAGE_DISCARD);
			return sendMessage;
		}
		if (getUserStatus().isEmpty() && !user.get().isAllFilled()) {
			setUserStatus(user.get().getUserStatus());
		}
		if (recivedMessage.getText().contains(COMMAND_REGISTER)) {
			if (getUserStatus().peek().equals(UserStatus.REGISTER)) {
				getUserStatus().pop();
				sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()),
						getUserStatus().peek().getMsg());
				return sendMessage;
			}
			sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()),
					getUserStatus().peek().getMsg());
			return sendMessage;
		}
		if (Arrays.stream(COMMANDS_ALLOWED).anyMatch(recivedMessage.getText().trim()::contains)) {
			sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()), MESSAGE_DISCARD);
			return sendMessage;
		}
		if (StringUtils.isValid(recivedMessage.getText(), getUserStatus().peek())) {
			user.get().setX(getUserStatus().peek(), recivedMessage.getText().trim());
			getUserStatus().pop();

			if (user.get().getAddDate() == null) {
				user.get().setAddDate(new Date());
			}
			if (getUserStatus().isEmpty()) {
				user.get().setIdUser(recivedMessage.getChatId());
				user.set(userRepository.save(user.get()));
				recivedMessage.setText(MESSAGE_WELCOME);
				return handleMessage(recivedMessage);
			}
			sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()),
					getUserStatus().peek().getMsg());

			return sendMessage;
		} else {
			sendMessage = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()),
					"Não entendi!\n" + getUserStatus().peek().getMsg());
			return sendMessage;
		}

	}

	/**
	 * @implNote This method handle a message when a saved user calls the bot,
	 *           showing the preprogrammed functions and processing the user inputs
	 * @param message
	 */
	private SendMessage handleMessage(Message recivedMessage) {
		SendMessage sendMsg = null;
		if (user.get() == null || !user.get().isAllFilled()) {
			sendMsg = new SendMessage(String.valueOf(recivedMessage.getFrom().getId()), MESSAGE_DISCARD);
			return sendMsg;
		}

		List<MessageEntity> entities = recivedMessage.getEntities();
		String msg = "";
		if (entities == null || entities.isEmpty()) {
			msg = MESSAGE_WELCOME.formatted(user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
					COMMAND_FUNCTION_3);
			sendMsg = new SendMessage(String.valueOf(recivedMessage.getChatId()), msg);
			return sendMsg;
		} else {
			List<MessageEntity> functions = new ArrayList<>();
			entities.stream().forEach(e -> {
				if ("bot_command".equals(e.getType()))
					functions.add(e);
			});

			if (!functions.isEmpty()) {

				switch (functions.get(0).getText()) {
				case COMMAND_UNICO: {
					return sendUnicoURL(recivedMessage);
				}
				case COMMAND_EXCLUDE: {
					sendMsg = new SendMessage(String.valueOf(recivedMessage.getChatId()),
							MESSAGE_EXCLUDE_USER.formatted(COMMAND_YES, COMMAND_NO));
					return sendMsg;
				}
				case COMMAND_FUNCTION_3: {
					break;
				}
				case COMMAND_YES: {
					return deleteUser(recivedMessage.getFrom().getId());
				}
				default:
					msg = MESSAGE_WELCOME.formatted(user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
							COMMAND_FUNCTION_3);
					sendMsg = new SendMessage(String.valueOf(recivedMessage.getChatId()), msg);
					return sendMsg;
				}
			}
		}
		msg = MESSAGE_WELCOME.formatted(user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE, COMMAND_FUNCTION_3);
		sendMsg = new SendMessage(String.valueOf(recivedMessage.getChatId()), msg);
		return sendMsg;
	}

	private SendMessage deleteUser(Long id) {
		userRepository.deleteById(id);
		user.set(null);
		return new SendMessage(String.valueOf(id), "Dados excluidos");
	}

	private SendMessage sendUnicoURL(Message recivedMessage) {
		String msg = "<a href='https://novoportal.mpf.mp.br/unico'>Clique para abrir o Único</a>";
		SendMessage sendMsg = new SendMessage();
		sendMsg.setChatId(recivedMessage.getChatId());
		sendMsg.setText(msg);
		sendMsg.setParseMode("HTML");
		return sendMsg;
	}

	/**
	 * Sends a String message to one user
	 */
	public void sendStringMessage(String message, Long userId) {
		SendMessage msg = new SendMessage(String.valueOf(userId), message);
		try {
			execute(msg);
		} catch (TelegramApiException e) {
			log.throwing(TelegramBotService.class.getName(), "sendStringMessage", e);
		}
	}

	/**
	 * Sends a formatted message to all users in db setting their names on the message 
	 * @param message
	 */
	public void sendMessageToAllUsers(String message) {
		List<TelegramUser> users = userRepository.findAll();
		for (TelegramUser telegramUser : users) {
			sendStringMessage(message.formatted(telegramUser.getFullName()), telegramUser.getIdUser());
		}
	}
	
	
	/**
	 * @implNote This method return a Json example of a Message object
	 * @return String
	 */
	public String getJson() {
		String string = "" + "{\r\n" + "	\"chat_id\": \"856471610\",\r\n"
				+ "	\"text\": \"/Message is a command\",\r\n" + "	\"entities\": [\r\n" + "		{\r\n"
				+ "		   \"type\": \"bot_command\",\r\n" + "		   \"offset\": \"0\",\r\n"
				+ "		   \"length\": \"7\",\r\n" + "		   \"text\": \"/Message\"\r\n" + "	   }\r\n" + "	],\r\n"
				+ "	\"method\": \"sendmessage\"\r\n" + "}";
		return JsonFormatter.prettyPrint(string);
	}
	
	/**
	 * @implNote - returns the bot username to the Telegram API
	 */
	@Override
	public String getBotUsername() {
		return this.botUsername;
	}

	/**
	 * @implNote - Ensures that the userStatus field is accessed safely by the
	 *           multiple threads
	 * @return Stack<UserStatus>
	 */
	private synchronized Stack<UserStatus> getUserStatus() {
		return this.userStatus;
	}

	/**
	 * @implNote - Ensures that the userStatus field is accessed safely by the
	 *           multiple threads
	 * @param status
	 */
	private synchronized void setUserStatus(Stack<UserStatus> userStatus) {
		this.userStatus = userStatus;
	}

	public Message sendMessage(SendMessage message) {
		try {
			return execute(message);
		} catch (TelegramApiException e) {
			log.throwing(TelegramBotService.class.getName(), "sendStringMessage", e);
			throw new RuntimeException(e.getMessage());
		}
	}

}
