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

package com.pilot51.multi3d.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Sphere {
	private static FloatBuffer sphereVertex;
	private static final double DEG = Math.PI / 180;
	private double mRaduis, mStep;
	private int mPoints;

	/**
	 * The value of step will define the size of each facet as well as the number of facets
	 * 
	 * @param radius
	 * @param step
	 */

	public Sphere(float radius, double step) {
		this.mRaduis = radius;
		this.mStep = step;
		sphereVertex =
			ByteBuffer.allocateDirect(40000).order(ByteOrder.nativeOrder()).asFloatBuffer();
		/**
		 * x = p * sin(phi) * cos(theta) y = p * sin(phi) * sin(theta) z = p * cos(phi)
		 */
		double dTheta = mStep * DEG;
		double dPhi = dTheta;
		for (double phi = -(Math.PI); phi <= Math.PI; phi += dPhi) {
			//for each stage calculating the slices
			for (double theta = 0.0; theta <= (Math.PI * 2); theta += dTheta) {
				sphereVertex.put((float)(mRaduis * Math.sin(phi) * Math.cos(theta)));
				sphereVertex.put((float)(mRaduis * Math.sin(phi) * Math.sin(theta)));
				sphereVertex.put((float)(mRaduis * Math.cos(phi)));
				mPoints++;
			}
		}
		sphereVertex.position(0);
	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CW);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sphereVertex);
		gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, mPoints);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
