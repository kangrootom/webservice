package cn.itcast.person.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import cn.itcast.model.Person;
import cn.itcast.person.dao.PersonDao;

@Repository
public class PersonDaoImpl implements PersonDao{

	List<Person> pList = new ArrayList<Person>();

	@Override
	public void add(Person person) {
		pList.add(person);
	}

	@Override
	public List<Person> getPersonAll() {
		return pList;
	}

	@Override
	public Person getPersonByName(String pname) {
		// TODO Auto-generated method stub
		if(StringUtils.isNotBlank(pname)) {
			for (Person person : pList) {
				if(person.getName().equals(pname)) {
					return person;
				}
			}
		}
		return null;
	}

}
