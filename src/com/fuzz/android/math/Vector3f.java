/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fuzz.android.math;

public final class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f() {

    }

    public Vector3f(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3f(Vector3f vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public void set(Vector3f vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f add(Vector3f vector) {
        x += vector.x;
        y += vector.y;
        z += vector.z;
        return this;
    }

    public Vector3f subtract(Vector3f vector) {
        x -= vector.x;
        y -= vector.y;
        z -= vector.z;
        return this;
    }

    public boolean equals(Vector3f vector) {
        if (x == vector.x && y == vector.y && z == vector.z)
            return true;
        return false;
    }

    @Override
    public String toString() {
        return (new String("(" + x + ", " + y + ", " + z + ")"));
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void scale(float spreadValueX, float spreadValueY, float spreadValueZ) {
        x *= spreadValueX;
        y *= spreadValueY;
        z *= spreadValueZ;
    }

	
    public Vector3f multiply(float f) {
		// TODO Auto-generated method stub
    	  x *= f;
          y *= f;
          z *= f;
          return this;
	}

	public Vector3f normalize() {
		// TODO Auto-generated method stub
		double length = Math.sqrt((x*x) + (y * y) + (z * z));
		if(length!=0){
			x = (float) (x/length);
			y = (float) (y/length);
			z = (float) (z/length);
		}else{
			x=0;
			y=0;
			z=0;
		}
		return this;
	}

	public float dot(Vector3f v) {
		// TODO Auto-generated method stub
		return (x*v.x) + (y*v.y) + (z*v.z);
	}

	
	public float getLength() {
		// TODO Auto-generated method stub
		return (float) Math.sqrt((x*x) + (y*y) + (z*z));
	}

	
	public Vector3f multiplyVector(Vector3f vector) {
		// TODO Auto-generated method stub
		x *= vector.x;
		y *= vector.y;
		z *= vector.z;
		return this;
	}

	
	public float cross(Vector3f v) {
		// TODO Auto-generated method stub
		//x y z
		//x y z
		//return (y*v.z - z*v.y) - (x*v.z - z*v.x) + (x*v.y - y*v.x);
		
		return (x*v.y) + (y*v.z) + (z*v.x) - (z*v.y) - (x*v.z) - (y*v.x);
	}
}
