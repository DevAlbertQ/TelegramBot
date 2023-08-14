package br.com.albert.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.albert.enums.UserPosition;
import br.com.albert.models.TelegramUser;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long>{
	
	public List<TelegramUser> findByUserPosition(UserPosition userPosition);

}
