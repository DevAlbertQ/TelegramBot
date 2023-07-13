package br.com.albert.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.albert.models.TelegramUser;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long>{

}
