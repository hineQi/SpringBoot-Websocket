package com.online.spring.websocket.chat;


public class UserInfo {

	private Integer id;
	private String name;
	private String receive;

	public UserInfo() {
	}

	public UserInfo(Integer id) {
		this.id = id;
	}
	public UserInfo(Integer id,String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReceive() {
		return receive;
	}

	public void setReceive(String receive) {
		this.receive = receive;
	}
}
