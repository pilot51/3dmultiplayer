package com.pilot51.multi3d;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import com.pilot51.multi3d.object.Cube;

public class TestMultiActivity extends Activity {
	private GLSurfaceView mGLSurfaceView;
	private CubeRenderer mRenderer;
	private float rAngleX, rAngleY;
	private DatagramSocket socket;
	private InetAddress ipRemote;
	private static final int PORT = 12345;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLSurfaceView = new CustomTouchSurfaceView(this);
		setContentView(mGLSurfaceView);
		mGLSurfaceView.requestFocus();
		mGLSurfaceView.setFocusableInTouchMode(true);
		initSocket();
		connect(getIntent().getStringExtra("address"));
		new Thread(new Runnable() {
			@Override
			public void run() {
				do {
					String msg = receive();
					if (msg != null) {
						String[] rCoords = msg.split(" ");
						rAngleX = Float.parseFloat(rCoords[0]);
						rAngleY = Float.parseFloat(rCoords[1]);
						mGLSurfaceView.requestRender();
					}
				} while (!socket.isClosed());
			}
		}).start();
	}
	
	private void initSocket() {
		try {
			socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void connect(String address) {
		if (address.equals("")) return;
		try {
			ipRemote = InetAddress.getByName(address);
			connect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	private void connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				socket.connect(ipRemote, PORT);
				send("connect");
			}
		}).start();
	}
	
	private void send(String msg) {
		if (!socket.isConnected()) {
			return;
		}
		try {
			socket.send(new DatagramPacket(msg.getBytes(), msg.length()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String receive() {
		byte[] message = new byte[24];
		DatagramPacket packet = new DatagramPacket(message, message.length);
		try {
			socket.receive(packet);
			String msg = new String(message, 0, packet.getLength());
			if (!socket.isConnected() | !packet.getAddress().equals(ipRemote)) {
				ipRemote = packet.getAddress();
				connect();
			}
			if (msg.equals("connect")) return null;
			if (msg.equals("disconnect")) {
				socket.disconnect();
				return null;
			}
			return msg;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		send("disconnect");
		socket.close();
	}
	
	private class CustomTouchSurfaceView extends GLSurfaceView {
		private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
		private float mPreviousX;
		private float mPreviousY;
		
		private CustomTouchSurfaceView(Context context) {
			super(context);
			mRenderer = new CubeRenderer();
			setRenderer(mRenderer);
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			switch (e.getAction()) {
				case MotionEvent.ACTION_MOVE:
					float dx = x - mPreviousX;
					float dy = y - mPreviousY;
					mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
					mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
					send(mRenderer.mAngleX + " " + mRenderer.mAngleY);
					requestRender();
			}
			mPreviousX = x;
			mPreviousY = y;
			return true;
		}
	}
	
	private class CubeRenderer implements GLSurfaceView.Renderer {
		private Cube cube;
		private float mAngleX, mAngleY;
		
		private CubeRenderer() {
			cube = new Cube();
		}

		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(0, 2, -5.0f);
			gl.glRotatef(mAngleX, 0, 1, 0);
			gl.glRotatef(mAngleY, 1, 0, 0);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			cube.draw(gl);
			gl.glLoadIdentity();
			gl.glTranslatef(0, -2, -5.0f);
			gl.glRotatef(rAngleX, 0, 1, 0);
			gl.glRotatef(rAngleY, 1, 0, 0);
			cube.draw(gl);
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			gl.glViewport(0, 0, width, height);
			float ratio = (float)width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glDisable(GL10.GL_DITHER);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
			gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
			gl.glEnable(GL10.GL_CULL_FACE);
			gl.glShadeModel(GL10.GL_SMOOTH);
			gl.glEnable(GL10.GL_DEPTH_TEST);
		}
	}
}
