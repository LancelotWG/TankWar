import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MissileDeadMsg implements Msg {
	int msgType = Msg.MISSILE_DEAD_MSG;
	int id;
	int tankID;
	TankClient tc;

	public MissileDeadMsg(int tankID, int id) {
		super();
		this.id = id;
		this.tankID = tankID;
	}

	public MissileDeadMsg(TankClient tc) {
		super();
		this.tc = tc;
	}

	@Override
	public void send(DatagramSocket ds, String IP, int udpPort) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(msgType);
			dos.writeInt(tankID);
			dos.writeInt(id);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		byte[] buf = baos.toByteArray();

		try {
			DatagramPacket dp = new DatagramPacket(buf, buf.length,
					new InetSocketAddress(IP, udpPort));
			ds.send(dp);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void parse(DataInputStream dis) {
		try {
			int tankID = dis.readInt();
			/*if (tc.myTank.id == id) {
				return;
			}
			boolean exist = false;*/
			int myId=dis.readInt();
			for (int i = 0; i < tc.missiles.size(); i++) {
				Missile m = tc.missiles.get(i);
				if (m.tankID==tankID&&m.id == myId) {
					m.live=false;
					tc.explodes.add(new Explode(m.x, m.y, tc));
					//exist = true;
					break;
				}
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}

}
