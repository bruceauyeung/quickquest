package net.ubuntudaily.quickquest.fsobject;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(FSObject.class);
	private static final int MAX_NAME_LEN = 255;
	private long id;
	private String name;
	private int depth;
	private long size;
	private byte type;
	private Timestamp lmts;
	private long poid;
	private int rowNum;

	public FSObject() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.length() > MAX_NAME_LEN) {
			logger.debug("the length of name of file system object exceeds max threshold.");
			name = name.substring(0, MAX_NAME_LEN);
		}

		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public Timestamp getLmts() {
		return lmts;
	}

	public void setLmts(Timestamp lmts) {
		this.lmts = lmts;
	}

	public long getPoid() {
		return poid;
	}

	public void setPoid(long poid) {
		this.poid = poid;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * @return the row number returned by hsqldb rownum() function, starts with
	 *         1.
	 */
	public int getRowNum() {
		return rowNum;
	}

	/**
	 * @param rowNum
	 *            the row number returned by hsqldb rownum() function, starts
	 *            with 1.
	 */
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.poid).append(this.name).build();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FSObject other = (FSObject) obj;
		return new EqualsBuilder().append(this.poid, other.poid)
				.append(this.name, other.name).build();
	}

}
