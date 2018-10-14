package cn.itcast.person.dao;

import java.util.List;

import cn.itcast.model.Person;


public interface PersonDao {
	
	public void add(Person person);
	
	public List<Person> getPersonAll();
	
	public Person getPersonByName(String pname);

}
