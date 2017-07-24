import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetClient {
	TankClient tc;
	private int udpPort;
	String IP;

	public int getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

	DatagramSocket ds = null;

	public NetClient(TankClient tc) {
		this.tc = tc;
		/*
		 * try { ds = new DatagramSocket(udpPort); } catch (SocketException e) {
		 * // TODO 自动生成的 catch 块 e.printStackTrace(); }
		 */

	}

	public void connect(String IP, int port) {
		this.IP = IP;
		try {
			ds = new DatagramSocket(udpPort);
		} catch (SocketException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		Socket s = null;
		try {
			s = new Socket(IP, port);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeInt(udpPort);
			DataInputStream dis = new DataInputStream(s.getInputStream());
			int id = dis.readInt();
			tc.myTank.id = id;
			if (id % 2 == 0) {
				tc.myTank.good = false;
			} else {
				tc.myTank.good = true;
			}
			System.out.println("Connected to Server!"
					+ "And server give me ID:" + id);
		} catch (UnknownHostException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
					s = null;
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		TankNewMeg msg = new TankNewMeg(tc.myTank);
		send(msg);
		new Thread(new UDPRecvThread()).start();
	}

	public void send(Msg msg) {
		msg.send(ds, IP, TankServer.UDP_PORT);
	}

	private class UDPRecvThread implements Runnable {
		byte[] buf = new byte[1024];

		@Override
		public void run() {
			while (ds != null) {
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				try {
					ds.receive(dp);
					parse(dp);
					System.out.println("a packet received from server!");
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}

		private void parse(DatagramPacket dp) {
			ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0,
					dp.getLength());
			DataInputStream dis = new DataInputStream(bais);
			int msgType = 0;
			Msg msg = null;
			try {
				msgType = dis.readInt();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			switch (msgType) {
			case Msg.TNAK_NEW_MSG:
				/*
				 * TankNewMeg tnMsg = new TankNewMeg(tc.myTank); send(tnMsg);
				 */
				msg = new TankNewMeg(tc);
				msg.parse(dis);
				break;
			case Msg.TNAK_MOVE_MSG:
				msg = new TankMoveMsg(tc);
				msg.parse(dis);
				break;
			case Msg.MISSILE_NEW_MSG:
				msg = new MissileNewMsg(tc);
				msg.parse(dis);
				break;
			case Msg.TANK_DEAD_MSG:
				msg = new TankDeadMsg(tc);
				msg.parse(dis);
				break;
			case Msg.MISSILE_DEAD_MSG:
				msg = new MissileDeadMsg(tc);
				msg.parse(dis);
				break;
			}

		}

	}
}
