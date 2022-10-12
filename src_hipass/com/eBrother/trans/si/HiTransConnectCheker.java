package com.eBrother.trans.si;

import com.eBrother.trans.kafka.KafkaProducerManager2;
import com.google.gson.Gson;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Properties;

public class HiTransConnectCheker implements Runnable {
	Hashtable _connectchekerst_this;
	SimpleDateFormat sys_time_format;
	KafkaProducerManager2 kafka = null;
	String CheckInterval = null;
	String CheckRetry = null;
	public HiTransConnectCheker() {
		// 초기화
		_connectchekerst_this = new Hashtable<String, Socket>();
		sys_time_format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try {
			CheckInterval = System.getenv("ConnectCheck_Interval");
			CheckRetry = System.getenv("ConnectCheck_Retry");
		} catch (Exception e) {

		}
	}

	@Override
	public synchronized void run() {
		try {
			while (true) {
				if (CheckInterval != null) {
					int Interval = (Integer.parseInt(CheckInterval) * 1000);
					Thread.sleep(Interval);
				} else {
					Thread.sleep(60000); // default interval
				}
				_connectchekerst_this = HiTransWorker632._connectchekerst;
				Enumeration en = _connectchekerst_this.keys();
				while (en.hasMoreElements()) {
					String key = en.nextElement().toString();
					String[] headersplit = _connectchekerst_this.get(key).toString().split("\\.");
					long time1 = System.currentTimeMillis();
					long time2 = sys_time_format.parse(headersplit[5]).getTime();
					System.out.println("\n connect_fail time check :" + headersplit[5]); // 시간
					System.out.print("\n connect_fail :" + _connectchekerst_this.get(key).toString()); // 키값
					System.out.println("\n connect_fail time gap : " + (time1 - time2) / 1000.0);
					if ((time1 - time2) / 1000.0 > 60) {
						int checkretry = 3; // default 횟수 = 3회

						if (CheckRetry != null) {
							checkretry = Integer.parseInt(CheckRetry);
						}
						if (Integer.parseInt(headersplit[6]) / checkretry == 1) // 횟수 checkretry 에 도달하면 보내고 삭제
						{
							this.make_send_message(key, headersplit[5]);
							HiTransWorker632._connectchekerst.remove(key);
						} else // 그밖에 것들은 보내고 횟수 1 증가
						{
							this.make_send_message(key, headersplit[5]);
							int count = Integer.parseInt(headersplit[6]);
							count++; // 횟수 계산
							System.out.println("HiTransWorker632._connectchekerst.put : " + Integer.toString(count));
							headersplit[6] = Integer.toString(count);
							String conhash = _connectchekerst_this.get(key).toString().substring(0,
									_connectchekerst_this.get(key).toString().length() - 1);
							HiTransWorker632._connectchekerst.put(key, conhash + headersplit[6]);
						}
					}
				}
				System.out.println("\n Checker Time");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				kafka.close();
			} catch (Exception e_kafka) {
			}
			kafka = null;
		}
	}

	public void make_send_message(String _header_data, String sys_time) {
		try {
			Gson gson = null;
			LinkedHashMap<String, Object> socket_close = null;
			LinkedHashMap<String, Object> msg_type_map = null;
			String szmsgtr = null;
			Properties _config;
			socket_close = new LinkedHashMap<>();
			msg_type_map = new LinkedHashMap<>();
			gson = new Gson();
			String[] headersplit = _header_data.split("\\.");
			socket_close.put("DATA_TYPE", "COLLECT");
			socket_close.put("SYS_TYPE", headersplit[0]);
			socket_close.put("SEND_TIME", sys_time);
			socket_close.put("IC_CODE", headersplit[1]);
			socket_close.put("LANE_NO", headersplit[2]);
			socket_close.put("BD_NAME", headersplit[3]);
			socket_close.put("MSG_TYPE", "BOSU_DATA");
			socket_close.put("MAKER_NAME", headersplit[4]);
			socket_close.put("INTERFACE_VERSION", "1.0");
			socket_close.put("BOSU_DATA", "");
			if (headersplit[0].equals("IMAGE")) {
				msg_type_map.put("DEVICE_NAME", "MainConnection");
				msg_type_map.put("DEVICE_INDEX", "ImageServer");
				msg_type_map.put("REPAIR_EVENT", "이상발생");
			} else {
				msg_type_map.put("OCC_DATETIME", sys_time);
				msg_type_map.put("WORK_TYPE", "4");
				msg_type_map.put("MACH_KIND", "99");
				msg_type_map.put("OCCUR_KIND", "0");
				msg_type_map.put("ERR_CODE", "01");
				msg_type_map.put("WORK_NO", "9999");
			}
			socket_close.put("BOSU_DATA", msg_type_map);
			szmsgtr = gson.toJson(socket_close);
			System.out.println("socket_close_mesage" + szmsgtr);
			try {
				if (kafka == null)
					kafka = new KafkaProducerManager2(HiTransServer632._config);
				kafka.send("hipass_succreal", szmsgtr);
				kafka.flush();
			} catch (Exception f) {
				System.out.println("kafka error" + f.getMessage());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}