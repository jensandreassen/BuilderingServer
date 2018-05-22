package YappCom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
	
	/**
	 * Constructor
	 * @param serverGui for output to user
	 * @param port port to listen to
	 */
	public Server(int port, int threads) {
		ConcurrentLinkedQueue<Request> queIn = new ConcurrentLinkedQueue<Request>();
		ConcurrentLinkedQueue<Request> queOut = new ConcurrentLinkedQueue<Request>();
		new Accepter(port, queIn, (ThreadPoolExecutor) Executors.newFixedThreadPool(threads)).start();
		
		new Sender(queOut).start();
	}
	/**
	 * Listener for new connections, adds them to ThreadpoolExecutor
	 * @author Jens Andreassen
	 *
	 */
	private class Sender extends Thread {
		private ConcurrentLinkedQueue<Request> queOut;
		private ObjectOutputStream oos;
		
		public Sender(ConcurrentLinkedQueue<Request> queOut) {
			this.queOut = queOut;
		}
		
		public void run() {
			while(true) {
				Request req = queOut.poll();
				if(req!=null) {
					Socket socket = (Socket)req.getSocket();
					try {
						oos = new ObjectOutputStream(socket.getOutputStream());
						oos.writeObject(req);
						oos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("Error sending response: " + req.getText());
						//Take Action
					}
				}
				
			}
			
		}
	}
	private class Accepter extends Thread {
		private ServerSocket serverSocket = null;
		private ThreadPoolExecutor executor;
		private int count;
		private int port;
		private ConcurrentLinkedQueue<Request> queIn;
		
		public Accepter(int port, ConcurrentLinkedQueue<Request> queIn, ThreadPoolExecutor executor) {
			this.port = port;
			this.queIn = queIn;
			this.executor = executor;
		}
		
		public void close() {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		/**
		 * Run-method for thread, listens for new connections and adds them
		 * to threadpool
		 */
		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(port);
				
				while (!Thread.interrupted()) {
					try {
						socket = serverSocket.accept();
						executor.execute(new Reciever(socket, queIn, ++count));
					} catch (SocketException e) {
						serverSocket = null;
						System.out.println("ServerSocket closed");
					} catch (IOException e) {
						if (socket != null) {
							socket.close();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ServerSocket error");
			} finally {
				close();
			}
		}
	}
	/**
	 * Task handling the connections in the threadpool
	 * @author Jens Andreassen
	 *
	 */
	private class Reciever implements Runnable{
		private Socket socket;
		private int number;
		private ConcurrentLinkedQueue<Request> queIn;
		
		public Reciever(Socket socket, ConcurrentLinkedQueue<Request> queIn, int number) {
			this.socket = socket;
			this.queIn = queIn;
			this.number = number;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ObjectInputStream ois;
			System.out.println("Client #"+ number + " connected");
			try {
				ois = new ObjectInputStream(socket.getInputStream());
				Request req = (Request)ois.readObject();
				System.out.println("Request recieved from number " + number + ": " + req.getText());
				queIn.add(req);
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Client #"+ number + " disconnected");
			}
		}
	}
}

