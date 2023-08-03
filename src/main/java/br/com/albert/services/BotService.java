package br.com.albert.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;
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

	private static final String COMMAND_REGISTER = "/Registrar";
	private static final String COMMAND_UNICO = "/Unico";
	private static final String COMMAND_EXCLUDE = "/Excluir";
	private static final String COMMAND_FUNCTION_3 = "/Op03";
	private static final String COMMAND_YES = "/SIM";
	private static final String COMMAND_NO = "/NÃO";
	private static final String COMMAND_START = "/start";
	private static final String MESSAGE_WELCOME = "Olá %s!\nEscolha uma das funcionalidades abaixo clicando na desejada:"
			+ "\n%s : Abrir o Único" + "\n%s : Excluir seu usuário da base de dados" + "\n%s : Realizar tarefa 3";
	private static final String ILLEGAL_ARGUMENT_MESSAGE = "Nenhum dado encontrado para o parâmetro %s";
	private static final String MESSAGE_EXCLUDE_USER = "Após a exclusão você não poderá mais receber menságens ou realizar interações com o bot."
			+ "\nTem certeza que deseja excluir? Clique em %s para comfirmar a exclusão e em %s para voltar ao menu";
	private static final CharSequence COMMAND_DISCARD = "/Descartar";

	@Value("${bot.token}")
	private String token;

	@Value("${bot.username}")
	private String username;

	@Autowired
	private TelegramUserRepository repository;

	private AtomicReference<TelegramUser> user = new AtomicReference<>();
	private Stack<UserStatus> userStatus = new Stack<>();
//	private TelegramUser user = new TelegramUser();

	private Logger log = Logger.getLogger(BotService.class.getName());

	/**
	 * 
	 * @param SendMessage message - the message to send. Must have at least
	 *                    messge.chatId and message.text set
	 * @return Message
	 * @implNote This method uses execute to send the message set.
	 */
	public Message sendMessage(SendMessage message) {
		try {
			return execute(message);
		} catch (TelegramApiException e) {
			log.throwing(BotService.class.getName(), "sendMessage", e);
		}
		return new Message();
	}

	/**
	 * @implNote This method return a SendMessage example to use the sendMessage
	 *           method.
	 * @return SendMessage
	 */
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
	 * @implNote method that process the received message from the bot.
	 * @param Update - the object send by the API when receive a message
	 */
	@Override
	public void onUpdateReceived(Update update) {

		log.info("Recived an update");
		if (update.hasMessage() && update.getMessage().hasText()) {
			log.info("update has message and text");
			Message message = update.getMessage();
			user.set(repository.findById(update.getMessage().getFrom().getId()).orElse(null));
			log.info("Get an user from DB: " + user);
			if(message.getText().contains(COMMAND_DISCARD))
				return;
			if (user.get() == null) {
				log.info("User is null");
				user.set(new TelegramUser());
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
			} else if (user.get() == null || !user.get().isAllFilled()) {
				log.info("User is not all filled");
				try {
					handleNewUser(message);
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleNewUser", e);
					e.printStackTrace();
				}
			} else if (user.get().isAllFilled()) {
				try {
					handleFunctions(message);
				} catch (Throwable e) {
					log.throwing(BotService.class.getName(), "handleFunctions", e);
				}
			}
		}

	}

	/**
	 * @implNote This method handle a message when a new user calls the bot, getting
	 *           his data and saving on database.
	 * @param message
	 * @throws Throwable
	 */
	private void handleNewUser(Message message) throws Throwable {
		
		if(message.getText().contains(COMMAND_DISCARD)) {
			return;
		}
		if (this.user.get() != null && this.user.get().isAllFilled()) {
			handleFunctions(message);
			return;
		}
		log.info("handleNewUser method");
		if (getUserStatus().isEmpty()) {
			setUserStatus(user.get().getUserStatus());
		}
		UserStatus status = getUserStatus().peek();

		if (status == UserStatus.REGISTER) {
			log.info("Status == REGISTER");
			sendMessage(message.getChatId(), status.getMsg());
			getUserStatus().pop();
		} else if (message.getText().contains(COMMAND_REGISTER)) {
			sendMessage(message.getChatId(), status.getMsg());
		} else if (StringUtils.isValid(message.getText(), status)) {
			getUserStatus().pop();
			log.info("Text valid: " + message.getText() + " " + status);
			user.get().setX(status, message.getText());
			user.get().setIdUser(message.getChatId());
			if (user.get().getAddDate() == null) {
				user.get().setAddDate(new Date());
			}
			repository.save(user.get());
			sendMessage(message.getChatId(), getUserStatus().peek().getMsg());
		} else {
			log.info("Text invalid " + message.getText() + " " + status);
			getUserStatus().add(status);
			String msg = "Não entendi!\n" + status.getMsg();
			sendMessage(message.getChatId(), msg);
		}

	}

	/**
	 * @implNote This method handle a message when a saved user calls the bot,
	 *           showing the preprogrammed functions and processing the user inputs
	 * @param message
	 * @throws Throwable
	 */
	private void handleFunctions(Message message) throws Throwable {
		List<MessageEntity> entities = message.getEntities();
		String msg = "";
		if (entities.isEmpty()) {
			msg = String.format(MESSAGE_WELCOME, user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
					COMMAND_FUNCTION_3);
			sendMessage(message.getChatId(), msg);
			return;
		} else {
			List<MessageEntity> functions = new ArrayList<>();
			entities.stream().forEach(e -> {
				if ("bot_command".equals(e.getType()))
					functions.add(e);
			});

			if (!functions.isEmpty()) {
				switch (functions.get(0).getText()) {
				case COMMAND_UNICO: {
					openUnico(message.getChatId());
					break;
				}
				case COMMAND_EXCLUDE: {
					sendMessage(message.getChatId(), String.format(MESSAGE_EXCLUDE_USER, COMMAND_YES, COMMAND_NO));
					break;
				}
				case COMMAND_FUNCTION_3: {
					sendMessage(message.getChatId(), "Executando Op03...");
					break;
				}
				case COMMAND_YES:
					deleteUser(message.getFrom().getId());
					break;
				case COMMAND_NO:
					msg = String.format(MESSAGE_WELCOME, user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
							COMMAND_FUNCTION_3);
					sendMessage(message.getChatId(), msg);
					break;
				case COMMAND_START:
					sendMessage(message.getChatId(),
							String.format(MESSAGE_WELCOME, user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
									COMMAND_FUNCTION_3));
					break;
				default:
					sendMessage(message.getChatId(),
							String.format("Desculpe, não entendi." + MESSAGE_WELCOME, user.get().getFullName(), COMMAND_UNICO, COMMAND_EXCLUDE,
									COMMAND_FUNCTION_3));
				}
			}
		}
	}

	/**
	 * @implNote Opens MPF-Unico in default web browser
	 */
	public void openUnico(Long chatId) {
		String msg = "<a href='https://novoportal.mpf.mp.br/unico'>Clique para abrir o Único</a>";
		SendMessage sendMsg = new SendMessage(); 
		sendMsg.setChatId(chatId);
		sendMsg.setText(msg);
		sendMsg.setParseMode("HTML");
		try {
			execute(sendMsg);
		} catch (TelegramApiException e) {
			log.throwing(BotService.class.getName(), "openUnico", e);
		}
	}
	
	/**
	 * @implNote Deletes one user by its id
	 * @param id
	 */
	public void deleteUser(Long id) {
		repository.deleteById(id);
	}

	/**
	 * @implNote Send an String message to a Telegram user
	 * @param Long   id not null - The Telegram user Id to whom the message is intended
	 * @param String message - The message to be sent
	 * @return Message
	 * @throws TelegramApiException
	 */
	public Message sendMessage(Long id, String message) throws TelegramApiException {
		SendMessage msg = new SendMessage();
		if (id != null) {
			msg.setChatId(id);
			msg.setText(message);
			return execute(msg);
		}
		throw new IllegalArgumentException("Id must be set!");
	}

	/**
	 * @implNote To be implemented
	 * @throws Throwable
	 */
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

	/**
	 * @implNote - Returns a list of all users in the database
	 * @return List<TelegramUser>
	 */
	public List<TelegramUser> listAllUsers() {
		return repository.findAll();
	}

	/**
	 * @implNote - Returns a TelegramUser for the given id
	 * @param Long id
	 * @return TelegramUser
	 */
	public TelegramUser findOneUser(Long id) {
		return repository.findById(id).orElseThrow(() -> {
			log.severe(ILLEGAL_ARGUMENT_MESSAGE);
			return new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_MESSAGE, "id"));
		});
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
	private synchronized void setUserStatus(Stack<UserStatus> status) {
		this.userStatus = status;
	}

	/**
	 * @implNote - returns the bot username to the Telegram API
	 */
	@Override
	public String getBotUsername() {
		return this.username;
	}

}
