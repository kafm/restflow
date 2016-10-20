package com.platum.restflow.auth;

public class AuthDetails {

	private String userName;
	
	private String password;
	
	private String newPassword;
	
	private String passwordConfirm;

	public String getUserName() {
		return userName;
	}

	public AuthDetails setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public AuthDetails setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public AuthDetails setNewPassword(String newPassword) {
		this.newPassword = newPassword;
		return this;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public AuthDetails setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
		return this;
	}

	@Override
	public String toString() {
		return "AuthDetails [userName=" + userName + ", password=" + password + ", newPassword=" + newPassword
				+ ", passwordConfirm=" + passwordConfirm + "]";
	}
	
}
