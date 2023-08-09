package br.com.albert.services;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import br.com.albert.models.TelegramUser;
import br.com.albert.repositories.TelegramUserRepository;

@Service
public class TelegramUserService {

	private static final String ILLEGAL_ARGUMENT_MESSAGE = "Nenhum dado encontrado para o parâmetro %s";
	@Autowired
	private TelegramUserRepository repository;
	private Logger log = Logger.getLogger(TelegramUserService.class.getName());


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
	
	public List<TelegramUser> findUsersByExample(TelegramUser user){
		Example<TelegramUser> example = Example.of(user);
		return repository.findAll(example);
	}


}
