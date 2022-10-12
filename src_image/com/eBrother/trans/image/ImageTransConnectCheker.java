package com.eBrother.trans.image;

import com.eBrother.trans.image.model.PlResponse;
import com.eBrother.trans.image.model.TimeSyncResponse;
import com.eBrother.trans.kafka.KafkaProducerManager2;
import com.eBrother.util.SocketUtil;
import com.eBrother.util.UtilExt;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;


/*
   @@ 서버에서 client ( 차로) 로 메시지 push 하기 위함.
   @@ time sync 등을 전송하기 위한 전용 thread 임.
 */
public class ImageTransConnectCheker implements Runnable {

	static Logger _log = Logger.getLogger(ImageTransConnectCheker.class.getName());

	Hashtable<String, Socket> _connectchekerst_this;

	SimpleDateFormat sys_time_format;

	KafkaProducerManager2 kafka = null;
	String CheckInterval = null;
	String CheckRetry = null;

	public ImageTransConnectCheker () {

		// 초기화

		_log.info( "ImageTransConnectCheker init " );

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

		_log.info( "ImageTransConnectCheker start " );

		ImageTransHelper transHandler = ImageTransHelper.getInstance();

		SocketUtil socUtil = new SocketUtil();
		try {

			PlResponse resp = new PlResponse ();

			resp.setRespCmdType( ImageTransConst._PL_MSG_TIMESYNC_ );
			resp.setLenPayload( ImageTransConst._PL_RESP_TIMESYNC_LEN_ );
			while (true) {

				if (CheckInterval != null) {
					int Interval = (Integer.parseInt(CheckInterval) * 1000);
					Thread.sleep(Interval);
				} else {
					Thread.sleep(10000); // default interval
				}

				// _log.info( "ImageTransConnectCheker Loop check " );
				_connectchekerst_this = ImageTransWorker632._controlHsocket_;

				Enumeration en = ImageTransWorker632._controlHsocket_.keys();

				while (en.hasMoreElements()) {

					String key = en.nextElement().toString();

//					_log.info( "ImageTransConnectCheker sync client :" + key );

					try {

						Socket theSocket = ImageTransWorker632._controlHsocket_.get( key);

						if ( theSocket.isClosed() || ! theSocket.isConnected()) {
							// this socket is closed.
							// skip
							continue;
						}

						byte [] msgout = new byte [ resp.getLenPayload() +  ImageTransConst._PL_RESP_HEAD_LEN_V2];

						ByteBuffer msg_resp = transHandler.getTimeSyncOut ( resp );

						msg_resp.flip();
						msg_resp.get( msgout, 0, msg_resp.capacity() );

//						_log.info("timesync = " + UtilExt.print( msgout ));

						socUtil.setClientMsg2(theSocket, msgout, msgout.length);

					}
					catch ( Exception e_socloop ) {

						e_socloop.printStackTrace();

					}

				}

				// _log.info("Checker Time");

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
		_log.info( "ImageTransConnectCheker end " );

	}

	public void make_send_message(String _header_data, String sys_time) {

		try {



		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}