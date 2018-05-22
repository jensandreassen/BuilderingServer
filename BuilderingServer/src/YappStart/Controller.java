package YappStart;

import YappCom.Server;
import YappLog.Logic;

public class Controller {
	private Server server;
	private Logic logic;
	
	public Controller(int port) {
		this.logic = new Logic();
		this.server = new Server(port,20);
	}
	public static void main(String[] args) {
		Controller cont = new Controller(5000);
	}
}
