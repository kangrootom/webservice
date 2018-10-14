package cn.itcast.person.ws.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.itcast.constant.Constants;
import cn.itcast.model.Person;
import cn.itcast.person.dao.PersonDao;
import cn.itcast.person.ws.service.PersonServiceWS;


@Service
public class PersonServiceWSImpl implements PersonServiceWS {

	@Autowired
	PersonDao personDao;
	
	@Override
	public String add(Person person, String password) {
		String flag = "success";
		if(Constants.ws_pass.equals(password)){
			personDao.add(person);
		}else{
			flag = "pass_error";
		}
		return flag;
		
	}

	@Override
	public List<Person> getPersonAll() {
		return personDao.getPersonAll();
	}

	@Override
	public Person getPersonByName(String pname) {
		// TODO Auto-generated method stub
		return personDao.getPersonByName(pname);
	}

}
