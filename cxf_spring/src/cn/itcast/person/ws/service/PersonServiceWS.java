package cn.itcast.person.ws.service;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import cn.itcast.model.Person;


@WebService(serviceName="PersonServices",name="PersonServiceWSTest",portName="PersonServiceWSTestPort",targetNamespace="person.itcast.cn")
public interface PersonServiceWS {
	
	public @WebResult(name="person") String add(@WebParam(name="person")Person person, @WebParam(name="password")String password);
	
	public @WebResult(name="personList")List<Person> getPersonAll();
	
	public @WebResult(name="person")Person getPersonByName(@WebParam(name="pname")String pname);
}
