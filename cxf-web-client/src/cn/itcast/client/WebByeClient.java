package cn.itcast.client;

import cn.itcast.byecode.ByeInter;
import cn.itcast.byecode.ByeInterService;

public class WebByeClient {
	public static void main(String[] args) {
		ByeInterService bs = new ByeInterService();
		ByeInter bi = bs.getByeInterPort();
		String result = bi.sayBye("八戒");
		System.out.println(result);
	}
}
