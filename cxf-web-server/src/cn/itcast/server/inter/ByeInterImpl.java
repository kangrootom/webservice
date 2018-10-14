package cn.itcast.server.inter;

public class ByeInterImpl implements ByeInter {

	
	@Override
	public String sayBye(String name) {
		return name + " bye";
	}

}
