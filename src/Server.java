import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Server {
	
	private String serverName = null;
	private int serverPort = 0;
	private Map<String, String> data;
	
	public Server(String name, int port) {
		serverName = name;
		serverPort = port;
	}
	
	public void start() {
		// 資料初始化
		data = new HashMap<>();
		
		// 根據主機名和埠號號創建Socket位址
		InetSocketAddress serverSocketAddress = new InetSocketAddress(serverName, serverPort);		
		String localAddress = serverSocketAddress.getAddress().getHostAddress();
		
		// create an server socket object
		try(ServerSocket serverSocket = new ServerSocket()) {
			// bind the server socket to the socket address
			System.out.println("Bind server socekt to " + localAddress + ":" + serverPort);
			serverSocket.bind(serverSocketAddress);
			System.out.println("Multithreading server binding success");
			
			// listen whether any connections come
			while(true) {
				// accept new connections
				Socket socket = serverSocket.accept();
				//Create a thread to serve the client and execute ClientHandlingTask(socket)
				Thread thread = new Thread(new ClientHandlingTask(socket));
				thread.start();
			}
		} catch(IOException e) {
			// socket 出事
			e.printStackTrace();
		}finally {
			// close socket (connection)
			System.out.println("Server shutdown.");
		}
	}
	
	private class ClientHandlingTask implements Runnable {
		
		private Socket clientSocket = null;
		public ClientHandlingTask(Socket socket) {
			clientSocket = socket;
		}
		
		@Override
		public void run() {
			// 顯示一下連到誰了
			InetSocketAddress clientSocketAddress = (InetSocketAddress)clientSocket.getRemoteSocketAddress();
			String clientAddress = clientSocketAddress.getAddress().getHostAddress();
			int clientPort = clientSocketAddress.getPort();
			System.out.println("Connecting to " + clientAddress + ":" + clientPort);
			
			String str = null;
			String[] strs = null;
			String userName = null; 
			try {
				// 收發訊息工具
				InputStream inputStream = clientSocket.getInputStream();
				OutputStream outputStream = clientSocket.getOutputStream();
				byte[] buf = new byte[1024];
				
				// 從客戶端接收訊息
				int length = inputStream.read(buf);
				int randomNum;
				while(length > 0) {
					str = new String(buf, 0, length);
					strs = str.split(",");
					switch(strs[0]) {
					case "reserveData":
						if(data.containsKey(strs[2])) {
							str = "exist" + GetData();
						}
						else {
							data.put(strs[2], strs[1]);
							str = "success" + GetData();
						}
						break;
					case "askData":
						str = "askData" + GetData();
						break;
					case "cancel":
						if(data.containsKey(strs[1])) data.remove(strs[1]);
						str = "cancel" + GetData();
						break;
					}
					
					System.out.println(str);
					//str = str.toUpperCase();
					
					// 發送訊息到客戶端
					outputStream.write(str.getBytes());
					outputStream.flush();
					length = inputStream.read(buf);
				}
			} catch(IOException e1) {
				// client 斷連
				// e1.printStackTrace();
			} finally {
				// 記得結束時關閉連接
				try{
					clientSocket.close();
				} catch(IOException e2){}
				System.out.println("Disconnecting to " + clientAddress + ":" + clientPort);
			}
		}
	}
	
	private String GetData() {
		String result = "";

		System.out.println(data.toString());
		
		for(int i=6;i<24;i++) {
			if(data.containsKey(i+":"+"00")) {
				result += "," + data.get(i+":"+"00");
			}
			else {
				result += "," + "NAN";
			}
			if(data.containsKey(i+":"+"30")) {
				result += "," + data.get(i+":"+"30");
			}
			else {
				result += "," + "NAN";
			}
		}
		
		return result;
	}

	public static void main(String[] args) {
		String serverName = "127.0.0.1";
		int serverPort = 12000;
		
		if(args.length >= 2) {
			serverName = args[0];
			try {
				serverPort = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {} 
		}
			
		Server server = new Server(serverName, serverPort);
		server.start();
	}
}
