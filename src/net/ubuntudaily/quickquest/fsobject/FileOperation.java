package net.ubuntudaily.quickquest.fsobject;

import java.io.File;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FileOperation {

	private FileOperationType type;
	private File beforeOperated;
	private File afterOperated;

	/**
	 * construct a {@linkplain FileOperation} instance.
	 * @param type the type of file operation.
	 * @param beforeOperated the file before file operation is performed. should be set to <code>null</code> when type is {@linkplain FileOperationType#CREATE CREATE}
	 * @param afterOperated the file after file operation is performed. should be set to null when type is {@linkplain FileOperationType#DELETE DELETE}
	 */
	public FileOperation(FileOperationType type, File beforeOperated,
			File afterOperated) {
		super();
		this.type = type;
		this.beforeOperated = beforeOperated;
		this.afterOperated = afterOperated;
	}

	public FileOperationType getType() {
		return type;
	}

	public File getBeforeOperated() {
		return beforeOperated;
	}

	public File getAfterOperated() {
		return afterOperated;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(type).append(beforeOperated)
				.append(afterOperated).build();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileOperation other = (FileOperation) obj;

		return new EqualsBuilder().append(this.type, other.type)
				.append(this.beforeOperated, other.beforeOperated)
				.append(this.afterOperated, other.afterOperated).build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(this.type).append(this.beforeOperated).append(this.afterOperated).build();
	}

}
