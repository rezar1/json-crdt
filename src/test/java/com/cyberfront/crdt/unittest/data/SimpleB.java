/*
 * Copyright (c) 2017 Cybernetic Frontiers LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
package com.cyberfront.crdt.unittest.data;

import com.cyberfront.crdt.unittest.data.Factory.TYPE;
import com.cyberfront.crdt.unittest.support.WordFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleB.
 */
public class SimpleB extends AbstractDataType {
	
	/** The int value. */
	private Integer intValue;

	/**
	 * Instantiates a new simple B.
	 */
	public SimpleB() {
		super();

		this.setIntValue(WordFactory.getRandom().nextInt());
	}

	/**
	 * Instantiates a new simple B.
	 *
	 * @param src the src
	 */
	public SimpleB(SimpleB src) {
		super(src);
		this.intValue = src.intValue;
	}

	/**
	 * Gets the int value.
	 *
	 * @return the int value
	 */
	public Integer getIntValue() {
		return intValue;
	}

	/**
	 * Sets the int value.
	 *
	 * @param value the new int value
	 */
	public void setIntValue(Integer value) {
		this.intValue = value;
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#update(java.lang.Double)
	 */
	@Override
	public void update(Double prob) {
		super.update(prob);
		
		if (WordFactory.getRandom().nextDouble() < prob) {
			this.setIntValue(WordFactory.getRandom().nextInt());
			this.incrementVersion();
		}
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object target) {
		if (target == this) {
			return true;
		} else if (!(target instanceof SimpleB)) {
			return false;
		} else {
			SimpleB castOther = (SimpleB) target;
			boolean intDifference = this.getIntValue() == castOther.getIntValue();
			return super.equals(castOther) && intDifference;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() * 71 + this.getIntValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		sb.append(super.toString());
		sb.append("\"intValue\":" + this.getIntValue());
		sb.append("}");
		
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.data.DataType#getType()
	 */
	@Override
	public TYPE getType() {
		return TYPE.SIMPLE_B;
	}
}