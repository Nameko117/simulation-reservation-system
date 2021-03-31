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
			// �M server �s�u
			System.out.println("Connect to server " + serverName + ":" + serverPort);
			clientSocket.connect(severSocketAddress, 3000);
			
			// ��ܤ@�U client �M server ����T
			InetSocketAddress socketAddress = (InetSocketAddress)clientSocket.getLocalSocketAddress();
			String clientAddress = socketAddress.getAddress().getHostAddress();
			int clientPort = socketAddress.getPort();
			System.out.println("Client " + clientAddress + ":" + clientPort);
			System.out.println("Connecting to server " + serverName + ":" + serverPort);
			
			try {
				// �w�q��J��X��y
				inputStream = clientSocket.getInputStream();
				outputStream = clientSocket.getOutputStream();
				// ���� GUI�B�� server �ǰT��
				gui = new GUI();
				Timer timer = new Timer();
				Timer timer2 = new Timer();
				// ���򱵦�server�T��
				byte[] buf = new byte[1024];
				String str = null;
				int length = inputStream.read(buf);
				while(length > 0) {
					str = new String(buf, 0, length);
					switch(str.split(",")[0]) {
					case "success":
						gui.msg.setText("�w�����\");
						gui.ResetData(str.substring(8));
						gui.ResetTime();
						// �����\��
						cancelTime = 60;
						gui.cancelButton.setVisible(true);
						timer.schedule(new CancelTimer(), 1000, 1000);
						// �N�o�\��
						coldTime = 3600;
						timer2.schedule(new ColdTimer(), 1000, 1000);
						break;
					case "exist":
						gui.msg.setText("�Ӯɬq�w�Q�w��");
						gui.ResetData(str.substring(6));
						gui.ResetTime();
						break;
					case "askData":
						System.out.println(str.substring(8));
						gui.ResetData(str.substring(8));
						gui.ResetTime();
						break;
					case "cancel":
						gui.msg.setText("�w���w����");
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
				// ���o�T���u��
				inputStream = clientSocket.getInputStream();
				outputStream = clientSocket.getOutputStream();
				// �@�ӷs thread �Ω󱵦� server �T��
				Thread task = new Thread(new ListeningTask(inputStream));
				task.start();
				
				byte[] buf = new byte[1024];
				// �令����������
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
	        demo.setTitle("���˳o�O�@�Ӭ~����w���t��");
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
			SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyy�~MM��dd�� HH:mm:ss");
			nowdate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			String sdate = nowdate.format(new java.util.Date());
			time.setText("�̫��s�ɶ��G" + sdate);
		}
		
		private void SetPanel() {
			panel.setLayout(null);
			
			// ��e�ɶ�
			SimpleDateFormat nowdate = new java.text.SimpleDateFormat("yyyy�~MM��dd�� HH:mm:ss");
			nowdate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			String sdate = nowdate.format(new java.util.Date());
			time = new JLabel("�̫��s�ɶ��G" + sdate);
			time.setBounds(10, 10, 300, 15);
			panel.add(time);
			// ��s���s
			JButton resetButton = new JButton("��s");
			resetButton.setBounds(250, 7, 80, 20);
			resetButton.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		// �T��
	    			msg.setText("");
	    			// ���
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
			
			// �w�����
			JLabel reserve = new JLabel("�i�w�����j");
			reserve.setBounds(10, 40, 300, 15);
			panel.add(reserve);
			// �ʺ�
			JLabel userLabel = new JLabel("�ʺ١G");
			userLabel.setBounds(10, 60, 45, 20);
			panel.add(userLabel);
			JTextField userText = new JTextField(20);
	        userText.setBounds(55, 60, 100, 20);
	        panel.add(userText);
	        // �ɶ�
	        JLabel timeLabel = new JLabel("�w���ɶ��G");
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
	        // ��J���s
	        enterButton = new JButton("�w��");
	        enterButton.setBounds(385, 60, 80, 20);
	        enterButton.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		// �N�o��
	        		if(coldTime > 0) {
	        			msg.setText("�w�����ѡC" + coldTime/60 + "��" + coldTime%60 + "���Y�i�A���w��");
	        			return;
	        		}
	        		
	        		// �ʺ�
	        		String userName = userText.getText();
	        		if(userName.equals("") || userName.equals("NAN")) {
	        			msg.setText("�ʺ٤��o����");
	        			return;
	        		}
	        		if(userName.split(",").length > 1) {
	        			msg.setText("�ʺ٤����o�]�t\",\"");
	        			return;
	        		}
	        		
	        		// �w���ɶ�
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
	        // �T��
	        msg = new JLabel("");
	        if(timeInput.getItemCount() == 0) msg.setText("�D�`��p�A�{�b�w�W�L�~����ϥήɶ�"); 
	        msg.setBounds(10, 80, 500, 20);
	        msg.setForeground(Color.RED);
			panel.add(msg);
			
			// �������s
			cancelButton = new JButton("����");
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
			// �����T��
			cancelMsg = new JLabel("");
			cancelMsg.setBounds(100, 100, 300, 20);
			panel.add(cancelMsg);
			
			// ���
			JLabel tableLabel = new JLabel("�i��e�w�����p�j");
			tableLabel.setBounds(30, 140, 200, 20);
			panel.add(tableLabel);
			// �n��ư����
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
			
			// �w�w�����
			if(jscrollpane!=null)	panel.remove(jscrollpane);
	        String[] dataTitle = {"�w���ɶ�", "�w����"};
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
			gui.cancelMsg.setText("�p�ݨ����A�Цb" + cancelTime + "������");
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
