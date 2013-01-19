/*
 * Copyright 2013 Mark Injerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pilot51.multi3d;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.pilot51.multi3d.object.*;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class TestActivity extends Activity {
	private GLSurfaceView mGLSurfaceView;
	private CubeRenderer mRenderer;
	private SensorManager mSensorManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGLSurfaceView = new CustomTouchSurfaceView(this);
		setContentView(mGLSurfaceView);
		mGLSurfaceView.requestFocus();
		mGLSurfaceView.setFocusableInTouchMode(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mRenderer.start();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mRenderer.stop();
		mGLSurfaceView.onPause();
	}
	
	class CustomTouchSurfaceView extends GLSurfaceView {
		private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
		private final float TRACKBALL_SCALE_FACTOR = 36.0f;
		private float mPreviousX;
		private float mPreviousY;
		
		public CustomTouchSurfaceView(Context context) {
			super(context);
			mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
			mRenderer = new CubeRenderer();
			setRenderer(mRenderer);
			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}

		@Override
		public boolean onTrackballEvent(MotionEvent e) {
			mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
			mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
			requestRender();
			return true;
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
					requestRender();
			}
			mPreviousX = x;
			mPreviousY = y;
			return true;
		}
	}
	
	private class CubeRenderer implements GLSurfaceView.Renderer, SensorEventListener {
		private Cube cube;
		private Sphere sphere;
		public float mAngleX;
		public float mAngleY;
		private Sensor mRotationVectorSensor;
		private final float[] mRotationMatrix = new float[16];
		
		public CubeRenderer() {
			mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			cube = new Cube();
			sphere = new Sphere(0.8f, 8);
			mRotationMatrix[0] = 1;
			mRotationMatrix[4] = 1;
			mRotationMatrix[8] = 1;
			mRotationMatrix[12] = 1;
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
			gl.glMultMatrixf(mRotationMatrix, 0);
			cube.draw(gl);
			gl.glLoadIdentity();
		    gl.glTranslatef(0, 0, -5); 
		    gl.glRotatef(mAngleX, 0, 1, 0);
		    gl.glRotatef(mAngleY, 1, 0, 0);
		    gl.glMultMatrixf(mRotationMatrix, 0);
			sphere.draw(gl);
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
		
		public void start() {
			mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
		}

		public void stop() {
			mSensorManager.unregisterListener(this);
		}
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
				mGLSurfaceView.requestRender();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	}
}
