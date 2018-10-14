package cn.itcast.server.inter;


import java.util.List;

import javax.jws.WebService;

import cn.itcast.entity.Person;
@WebService
public interface PersonService {
	
	public void addPerson(Person p);
	
	public List<Person> getPersonAll();

}
