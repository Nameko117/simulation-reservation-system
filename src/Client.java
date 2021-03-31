import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Client {

	private String serverName = null;
	private int serverPort = 0;
	
	InputStream inputStream = null;
	OutputStream outputStream = null;
	GUI gui = null;
	
	private int coldTime;
	private int cancelTime;
	private String lastReserve = null;
	
	public Client(String name, int port) {
		serverName = name;
		serverPort = port;
	}
	
	private void start() {
		// set server address
		SocketAddress severSocketAddress = new InetSocketAddress(serverName, serverPort);
		
		// create a client socket object
		try(Socket clientSocket = new Socket()) {
			// 和 server 連線
			System.out.println("Connect to server " + serverName + ":" + serverPort);
			clientSocket.connect(severSocketAddress, 3000);
			
			// 顯示一下 client 和 server 的資訊
			InetSocketAddress socketAddress = (InetSocketAddress)clientSocket.getLocalSocketAddress();
			String clientAddress = socketAddress.getAddress().getHostAddress();
			int clientPort = socketAddress.getPort();
			System.out.println("Client " + clientAddress + ":" + clientPort);
			System.out.println("Connecting to server " + serverName + ":" + serverPort);
			
			try {
				// 定義輸入輸出串流
				inputStream = clientSocket.getInputStream();
				outputStream = clientSocket.getOutputStream();
				// 執行 GUI、往 server 傳訊息
				gui = new GUI();
				Timer timer = new Timer();
				Timer timer2 = new Timer();
				// 持續接收server訊息
				byte[] buf = new byte[1024];
				String str = null;
				int length = inputStream.read(buf);
				while(length > 0) {
					str = new String(buf, 0, length);
					switch(str.split(",")[0]) {
					case "success":
						gui.msg.setText("預約成功");
						gui.ResetData(str.substring(8));
						gui.ResetTime();
						// 取消功能
						cancelTime = 60;
						gui.cancelButton.setVisible(true);
						timer.schedule(new CancelTimer(), 1000, 1000);
						// 冷卻功能
						coldTime = 3600;
						timer2.schedule(new ColdTimer(), 1000, 1000);
						break;
					case "exist":
						gui.msg.setText("該時段已被預約");
						gui.ResetData(str.substring(6));
						gui.ResetTime();
						break;
					case "askData":
						System.out.println(str.substring(8));
						gui.ResetData(str.substring(8));
						gui.ResetTime();
						break;
					case "cancel":
						gui.msg.setText("預約已取消");
						gui.cancelButton.setVisible(false);
						coldTime = 0;
						gui.ResetData(str.substring(7));
						gui.ResetTime();
						break;
					}
					//System.out.write(buf, 0, length);
					length = inputStream.read(buf);
				}
				/*
				// 收發訊息工具
				inputStream = clientSocket.getInputStream();
				outputStream = clientSocket.getOutputStream();
				// 一個新 thread 用於接收 server 訊息
				Thread task = new Thread(new ListeningTask(inputStream));
				task.start();
				
				byte[] buf = new byte[1024];
				// 改成視窗的部分
				int length = System.in.read(buf);
				while(length > 0) {
					outputStream.write(buf, 0, length);
					outputStream.flush();
					length = System.in.read(buf);
				}
				*/
			} catch(IOException e) {
				e.printStackTrace();
			}
		} catch(IOException e1) {
			e1.printStackTrace();
		} finally {
			System.out.println("Connection shutdown");
		}
	}
	
	private class GUI {
		JLabel time = null;
		JPanel panel = null;
		JLabel msg = null;
		JTable jtable = null;
		JScrollPane jscrollpane = null;
		JButton enterButton = null;
		JButton cancelButton = null;
		JLabel cancelMsg = null;
		
		public GUI() {
			JFrame demo = new JFrame();
	        demo.setTitle("假裝這是一個洗衣機預約系統");
	        demo.setSize(500, 600);
	        demo.setResizable(false);
	        demo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        demo.setLocationRelativeTo(null);
	        
	        panel = new JPanel();
	        demo.add(panel);
	        
	        SetPanel();
	        
	        demo.setVisible(true);
		}
		
		private void ResetTime() {
			SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
			nowdate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			String sdate = nowdate.format(new java.util.Date());
			time.setText("最後更新時間：" + sdate);
		}
		
		private void SetPanel() {
			panel.setLayout(null);
			
			// 當前時間
			SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
			nowdate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			String sdate = nowdate.format(new java.util.Date());
			time = new JLabel("最後更新時間：" + sdate);
			time.setBounds(10, 10, 300, 15);
			panel.add(time);
			// 刷新按鈕
			JButton resetButton = new JButton("刷新");
			resetButton.setBounds(250, 7, 80, 20);
			resetButton.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		// 訊息
	    			msg.setText("");
	    			// 表格
	        		String str = "askData";
	    			try {
	        			outputStream.write(str.getBytes(), 0, str.getBytes().length);
	    				outputStream.flush();
	        		} catch(IOException e1) {
	    				e1.printStackTrace();
	    			}
	        	}
			});
			panel.add(resetButton);
			
			// 預約欄目
			JLabel reserve = new JLabel("【預約表單】");
			reserve.setBounds(10, 40, 300, 15);
			panel.add(reserve);
			// 暱稱
			JLabel userLabel = new JLabel("暱稱：");
			userLabel.setBounds(10, 60, 45, 20);
			panel.add(userLabel);
			JTextField userText = new JTextField(20);
	        userText.setBounds(55, 60, 100, 20);
	        panel.add(userText);
	        // 時間
	        JLabel timeLabel = new JLabel("預約時間：");
	        timeLabel.setBounds(180, 60, 75, 20);
			panel.add(timeLabel);
	        JComboBox timeInput = new JComboBox();
	        int hour = Integer.parseInt(sdate.substring(12, 14));
	        int minute = Integer.parseInt(sdate.substring(15, 17));
	        /*
	        for(int i=6;i<24;i++) {
	        	if(i==hour) {
	        		if(minute < 30) timeInput.addItem(i+":30");
	        	}
	        	else if(i > hour) {
	        		timeInput.addItem(i+":00");
	        		timeInput.addItem(i+":30");
	        	}
	        }*/
	        for(int i=6;i<24;i++) {
	        	timeInput.addItem(i+":00");
        		timeInput.addItem(i+":30");
	        }
	        timeInput.setBounds(250, 60, 100, 20);
	        panel.add(timeInput);
	        // 輸入按鈕
	        enterButton = new JButton("預約");
	        enterButton.setBounds(385, 60, 80, 20);
	        enterButton.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		// 冷卻中
	        		if(coldTime > 0) {
	        			msg.setText("預約失敗。" + coldTime/60 + "分" + coldTime%60 + "秒後即可再次預約");
	        			return;
	        		}
	        		
	        		// 暱稱
	        		String userName = userText.getText();
	        		if(userName.equals("") || userName.equals("NAN")) {
	        			msg.setText("暱稱不得為空");
	        			return;
	        		}
	        		if(userName.split(",").length > 1) {
	        			msg.setText("暱稱中不得包含\",\"");
	        			return;
	        		}
	        		
	        		// 預約時間
	        		String data = "reserveData," + userName + "," + (String) timeInput.getSelectedItem();
	        		lastReserve = (String) timeInput.getSelectedItem();
	        		try {
	        			outputStream.write(data.getBytes(), 0, data.getBytes().length);
						outputStream.flush();
	        		} catch(IOException e1) {
	    				e1.printStackTrace();
	    			}
	        	}
	        });
	        panel.add(enterButton);
	        // 訊息
	        msg = new JLabel("");
	        if(timeInput.getItemCount() == 0) msg.setText("非常抱歉，現在已超過洗衣機使用時間"); 
	        msg.setBounds(10, 80, 500, 20);
	        msg.setForeground(Color.RED);
			panel.add(msg);
			
			// 取消按鈕
			cancelButton = new JButton("取消");
			cancelButton.setBounds(10, 100, 80, 20);
			cancelButton.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		String str = "cancel," + lastReserve;
	    			try {
	        			outputStream.write(str.getBytes(), 0, str.getBytes().length);
	    				outputStream.flush();
	        		} catch(IOException e1) {
	    				e1.printStackTrace();
	    			}
	        	}
			});
			cancelButton.setVisible(false);
			panel.add(cancelButton);
			// 取消訊息
			cancelMsg = new JLabel("");
			cancelMsg.setBounds(100, 100, 300, 20);
			panel.add(cancelMsg);
			
			// 表格
			JLabel tableLabel = new JLabel("【當前預約情況】");
			tableLabel.setBounds(30, 140, 200, 20);
			panel.add(tableLabel);
			// 要資料做表格
			String str = "askData";
			try {
    			outputStream.write(str.getBytes(), 0, str.getBytes().length);
				outputStream.flush();
    		} catch(IOException e1) {
				e1.printStackTrace();
			}
		}
		
		private void ResetData(String str) {			
			String[][] data = new String[36][2];
			String[] database = str.split(",");
			if(database.length != 36) return;
			
			for(int i=0;i<36;i+=2) {
				data[i][0] = (i/2+6) + ":00";
				data[i][1] = database[i].equals("NAN")?"":database[i];
				data[i+1][0] = (i/2+6) + ":30";
				data[i+1][1] = database[i+1].equals("NAN")?"":database[i+1];
			}
			
			// 已預約資料
			if(jscrollpane!=null)	panel.remove(jscrollpane);
	        String[] dataTitle = {"預約時間", "預約者"};
	        jtable = new JTable(data, dataTitle);
	        jtable.setEnabled(false);
	        jscrollpane = new JScrollPane(jtable);
	        jscrollpane.setBounds(40, 160, 400, 350);
	        panel.add(jscrollpane);
		}
	}
	
	private class CancelTimer extends TimerTask {
		@ Override
		public void run() {
			if(--cancelTime <= 0 || !gui.cancelButton.isVisible()) {
				gui.cancelMsg.setText("");
				gui.cancelButton.setVisible(false);
				cancel();
				return;
			}
			gui.cancelMsg.setText("如需取消，請在" + cancelTime + "秒內完成");
		}
	}
	
	private class ColdTimer extends TimerTask {
		@ Override
		public void run() {
			if(--coldTime <= 0) cancel();
		}
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
		
		Client client = new Client(serverName, serverPort);
		client.start();
	}
}
