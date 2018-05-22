package YappCom;

import java.net.Socket;

public class Request {
	private String text;
	private Socket socket;
	
	public Request(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
