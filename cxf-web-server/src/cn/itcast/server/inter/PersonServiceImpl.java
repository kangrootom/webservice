package cn.itcast.server.inter;

import java.util.ArrayList;
import java.util.List;

import cn.itcast.entity.Person;

public class PersonServiceImpl implements PersonService {

	//存储Person的集合
	List<Person> pList = new ArrayList<Person>();
	
	
	@Override
	public void addPerson(Person p) {
		pList.add(p);
	}

	@Override
	public List<Person> getPersonAll() {
		return pList;
	}

}
