package br.com.albert.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_user")
public class TelegramUser implements Serializable {
	
	public TelegramUser() {
		
	}
	
	
	public TelegramUser(Long idUser, String firstName, String lastName, String username, Date addDate, Long cellNumber,
			String email) {
		super();
		this.idUser = idUser;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.addDate = addDate;
		this.cellNumber = cellNumber;
		this.email = email;
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id_user", nullable = false)
	private Long idUser;
	
	@Column(name = "first_name")
	private String firstName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "add_date")
	private Date addDate;
	
	@Column(name = "cell_number")
	private Long cellNumber;
	
	@Column(name = "email")
	private String email;

	public Long getIdUser() {
		return idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public Long getCellNumber() {
		return cellNumber;
	}

	public void setCellNumber(Long cellNumber) {
		this.cellNumber = cellNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		return Objects.hash(addDate, cellNumber, email, firstName, idUser, lastName, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TelegramUser other = (TelegramUser) obj;
		return Objects.equals(addDate, other.addDate) && Objects.equals(cellNumber, other.cellNumber)
				&& Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName)
				&& Objects.equals(idUser, other.idUser) && Objects.equals(lastName, other.lastName)
				&& Objects.equals(username, other.username);
	}
	
	

}

