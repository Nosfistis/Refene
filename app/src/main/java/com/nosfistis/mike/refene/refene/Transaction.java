package com.nosfistis.mike.refene.refene;

/**
 * Created by Mike on 1/5/2017.
 */
public class Transaction {
	
	private long id;
	private float price;
	private Person person;
	private String description;
	
	public Transaction(float price, Person person) {
		this.price = price;
		this.person = person;
	}
	
	public Transaction(long id, float price, String description) {
		this.id = id;
		this.price = price;
		this.description = description;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person person) {
		this.person = person;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
