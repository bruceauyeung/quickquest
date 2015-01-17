package us.suselinks.quickquest.fsobject;

import java.sql.Timestamp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FSObjectVO {

	private String name;
	private String path;
	private String Size;
	private Timestamp ltms;
	private int rowNum;
	private FSObject fso;
	private long poid;
	public FSObjectVO() {
		// TODO Auto-generated constructor stub
	}
	public FSObjectVO(String name, String path, int size, Timestamp ltms) {
		super();
		this.name = name;
		this.path = path;
		Size = FileUtils.byteCountToDisplaySize(size);
		this.ltms = ltms;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getSize() {
		return Size;
	}
	public void setSize(long size) {
		Size = FileUtils.byteCountToDisplaySize(size);
	}
	public Timestamp getLtms() {
		return ltms == null ? new Timestamp(System.currentTimeMillis()) : ltms;
	}
	public void setLtms(Timestamp ltms) {
		this.ltms = ltms;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	public int getRowNum() {
		return rowNum;
	}
	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}
	public FSObject getFSObject() {
		return fso;
	}
	public void setFSObject(FSObject fSObject) {
		fso = fSObject;
	}
	public long getPoid() {
		return poid;
	}
	public void setPoid(long poid) {
		this.poid = poid;
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).append(this.poid).build();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FSObjectVO other = (FSObjectVO) obj;
		return new EqualsBuilder().append(this.poid, other.poid).append(this.name, other.name).build();
	}
	
	
}
