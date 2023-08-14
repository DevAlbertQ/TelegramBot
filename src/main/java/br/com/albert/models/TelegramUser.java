package br.com.albert.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Stack;

import br.com.albert.enums.UserPosition;
import br.com.albert.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_user")
public class TelegramUser implements Serializable {

	public TelegramUser() {

	}

	public TelegramUser(Long idUser, String fullName, String lastName, String username, Date addDate, String cellNumber,
			String email, UserPosition userPosition) {
		super();
		this.idUser = idUser;
		this.fullName = fullName;
		this.username = username;
		this.addDate = addDate;
		this.cellNumber = cellNumber;
		this.email = email;
		this.userPosition = userPosition;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id_user", nullable = false)
	private Long idUser;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "username")
	private String username;

	@Column(name = "add_date")
	private Date addDate;

	@Column(name = "cell_number")
	private String cellNumber;

	@Column(name = "email")
	private String email;

	@Column(name = "user_position")
	private UserPosition userPosition;

	public Long getIdUser() {
		return idUser;
	}

	public void setIdUser(Long idUser) {
		this.idUser = idUser;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String firstName) {
		this.fullName = firstName;
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

	public String getCellNumber() {
		return cellNumber;
	}

	public void setCellNumber(String cellNumber) {
		this.cellNumber = cellNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserPosition getUserPosition() {
		return userPosition;
	}

	public void setUserPosition(UserPosition userPosition) {
		this.userPosition = userPosition;
	}

	/**
	 * @implNote Sets a TelegramUser property through his status
	 * @param x
	 * @param value
	 */
	public void setX(UserStatus x, String value) {
		switch (x) {
		case NAME: {
			this.fullName = value;
			break;
		}
		case EMAIL: {
			this.email = value;
			break;
		}
		case PHONE_NUMBER: {
			this.cellNumber = value;
			break;
		}
		case USERNAME: {
			this.username = value;
			break;
		}
		case USER_POSITION: {
			this.userPosition = UserPosition.valueOf(Integer.valueOf(value));
			break;
		}

		default:
			throw new IllegalArgumentException("Unexpected value: " + x);
		}
	}

	/**
	 * @implNote Returns a UserStatus Stack of the TelegramUser's current state
	 * @return Stack<UserStatus>
	 */
	public Stack<UserStatus> getUserStatus() {
		Stack<UserStatus> status = new Stack<>();
		if (this.isAllFilled()) {
			return status;
		}
		if (this.getUserPosition() == null || this.getUserPosition().getCode() == null)
			status.add(UserStatus.USER_POSITION);
		if (this.cellNumber == null || this.cellNumber.isBlank())
			status.add(UserStatus.PHONE_NUMBER);
		if (this.email == null || this.email.isBlank())
			status.add(UserStatus.EMAIL);
		if (this.username == null || this.username.isBlank())
			status.add(UserStatus.USERNAME);
		if (this.fullName == null || this.fullName.isBlank())
			status.add(UserStatus.NAME);
		if (this.getIdUser() == null || this.getIdUser() == null)
			status.add(UserStatus.REGISTER);
		return status;
	}

	public boolean isAllFilled() {
		return this.cellNumber != null && !this.cellNumber.isBlank() && this.fullName != null
				&& !this.fullName.isBlank() && this.email != null && !this.email.isBlank() && this.username != null
				&& !this.username.isBlank() && this.userPosition != null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(addDate, cellNumber, email, fullName, idUser, username, userPosition);
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
				&& Objects.equals(email, other.email) && Objects.equals(fullName, other.fullName)
				&& Objects.equals(idUser, other.idUser) && Objects.equals(username, other.username)
				&& Objects.equals(userPosition, other.userPosition);
	}

}
