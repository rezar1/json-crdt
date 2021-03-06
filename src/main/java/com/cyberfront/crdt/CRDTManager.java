/*
 * Copyright (c) 2018 Cybernetic Frontiers LLC
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
package com.cyberfront.crdt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyberfront.crdt.operation.Operation.OperationType;
import com.cyberfront.crdt.operation.Operation;
import com.cyberfront.crdt.operation.OperationManager;
import com.cyberfront.crdt.sample.manager.GenericManager;
import com.cyberfront.crdt.sample.manager.JsonManager;
import com.cyberfront.crdt.sample.simulation.SimCRDTManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.diff.JsonDiff;	// Use this with jsonpatch
//import com.flipkart.zjsonpatch.JsonDiff;		// Use this with zjsonpatch

/**
 * The CRDTManager class is used to wrap a CRDT instance so as to interact with it.  The intent of this class is to 
 * provide an interface to manage JSON documents with the CRDT types provided.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @Type(value = GenericCRDTManager.class, name = "GenericCRDTManager"),
    @Type(value = JsonManager.class, name = "JsonManager"),
    @Type(value = GenericManager.class, name = "GenericManager"),
    @Type(value = SimCRDTManager.class, name = "SimCRDTManager")
    })
public class CRDTManager {
	protected static final String CRDT = "crdt";
	
	/** The Constant logger used to generate log entries */
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(CRDTManager.class);

	/** The Constant mapper */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/** The CRDT containing the updates for the JSON object being managed. */
	@JsonProperty(CRDT)
	private LastWriteWins crdt;
	
	/**
	 * Default constructor; performs no initialization of components
	 */
	public CRDTManager() {}

	/**
	 * Constructor specifying the CRDT to manage the LastWriteWins CRDT provided
	 * @param crdt LastWriteWins CRDT to manage with this instance
	 */
	public CRDTManager(@JsonProperty(CRDT) LastWriteWins crdt) {
		this.crdt = new LastWriteWins(crdt);
	}
	
	/**
	 * Gets the CRDT this manager is managing
	 * @return the CRDT this manager is managing
	 */
	@JsonProperty(CRDT)
	public LastWriteWins getCrdt() {
		if (null == this.crdt) {
			this.crdt = new LastWriteWins();
		}
		return crdt;
	}

	/**
	 * Get the static class ObjectMapper for performing JSON conversions
	 * @return The static ObjectMapper instance for performing JSON conversions
	 */
	protected static ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Checks if the CRDT being managed includes an operation with a Type.CREATE operation type
	 *
	 * @return True if and only if the CRDT includes an operation with a Type.CREATE operation type
	 */
	@JsonIgnore
	public boolean isCreated() {
		return this.getCrdt().isCreated();
	}
	
	/**
	 * Checks if the CRDT being managed includes an operation with a Type.READ operation type
	 *
	 * @return True if and only if the CRDT includes an operation with a Type.READ operation type
	 */
	@JsonIgnore
	public boolean isRead() {
		return this.getCrdt().isRead();
	}
	
	/**
	 * Checks if the CRDT being managed includes an operation with a Type.UPDATE operation type
	 *
	 * @return True if and only if the CRDT includes an operation with a Type.UPDATE operation type
	 */
	@JsonIgnore
	public boolean isUpdated() {
		return this.getCrdt().isUpdated();
	}
	
	/**
	 * Checks if the CRDT being managed includes an operation with a Type.DELETE operation type
	 *
	 * @return True if and only if the CRDT includes an operation with a Type.DELETE operation type
	 */
	@JsonIgnore
	public boolean isDeleted() {
		return this.getCrdt().isDeleted();
	}

	/**
	 * Clear all of the operations in the CRDT
	 */
	public void clear() {
		this.getCrdt().clear();
	}

	/**
	 * Deliver the operation, which has the effect of inserting the operation into the AddOperation set
	 * @param op Operation to deliver to the CRDT
	 */
	private void pushAdd(Operation op) {
		this.getCrdt().addOperation(op);
	}

	/**
	 * Cancel an operation which currently is, or potentially in the future will be, included in the RemOperation set  
	 * @param op The AbstractOperation instance to include in the RemoveOperation list
	 */
	private void pushRemove(Operation op) {
		this.getCrdt().remOperation(op);
	}

	/**
	 * Deliver an operation embedded in the OperationManager and based upon the StatusType of that OperationManager
	 * @param op OperationsManager instance wrapping the operation to persist in this CRDT
	 */
	protected void push(OperationManager op) {
		switch(op.getStatus()) {
		case APPROVED:
		case PENDING:
			this.pushAdd(op.getOperation());
			break;
		case REJECTED:
			this.pushRemove(op.getOperation());
			break;
		default:
			break;
		}
	}
	
	/**
	 * Generate a CreateOperation given a JsonNode and timestamp
	 * @param timestamp Effective timestamp for the create operation
	 * @return The new CreateOperation
	 */
	public static Operation generateCreate(long timestamp) {
		return new Operation(OperationType.CREATE, timestamp);
	}
	
	/**
	 * Generate a ReadOperation with the given time stamp value
	 * @param timestamp Effective timestamp for the read operation
	 * @return The read operation with the given timestamp
	 */
	public static Operation generateRead(long timestamp) {
		return new Operation(OperationType.READ, timestamp);
	}
	
	/**
	 * Generate an UpdateOperation given an original and update value and a timestamp value.
	 * @param source The original JsonNode to update with a new value
	 * @param target The new JsonNode which the update will produce given the original state 
	 * @param timestamp Effective time stamp for the update operations
	 * @return The update operation resulting from transforming from the source to target JsonNode values
	 */
	public static Operation generateUpdate(JsonNode source, JsonNode target, long timestamp) {
		return new Operation(JsonDiff.asJson(source, target), timestamp);
	}

	/**
	 * Generate a DeleteOperation with the given timestamp
	 * @param timestamp Effective timestamp for the delete operations
	 * @return A DeleteOperation with the given timestamp 
	 */
	public static Operation generateDelete(long timestamp) {
		return new Operation(OperationType.DELETE, timestamp);
	}

	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (null == obj || !(obj instanceof CRDTManager) || !super.equals(obj)) {
			return false;
		}
		
		CRDTManager mgr = (CRDTManager) obj;
		
		return this.getCrdt().equals(mgr.getCrdt());
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#hashCode()
	 */
	@Override
	public int hashCode() {
		return 31 * super.hashCode() + this.getCrdt().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see com.cyberfront.cmrdt.support.BaseManager#getSegment()
	 */
	protected String getSegment() {
		StringBuilder sb = new StringBuilder();

		sb.append("\"crdt\":" + (null == this.getCrdt() ? "null" : this.getCrdt().toString()));
		
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.getSegment() + "}";
	}
}
