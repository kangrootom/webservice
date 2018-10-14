package cn.itcast.person.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.itcast.model.Person;
import cn.itcast.person.dao.PersonDao;
import cn.itcast.person.service.PersonService;


@Service
//@Component适合于无法分层的类或接口
public class PersonServiceImpl implements PersonService {

	@Autowired
	//@Qualifier(value="")
	PersonDao personDao;
	
	@Override
	public void add(Person person) {
		personDao.add(person);
	}

}

