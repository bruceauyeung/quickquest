package us.suselinks.quickquest.fsobject;

import java.sql.Timestamp;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ViewableFSObject {
	private StringProperty name;

	public StringProperty nameProperty() {
		if (name == null)
			name = new SimpleStringProperty(this, "name");
		return name;
	}

	private StringProperty path;

	public StringProperty pathProperty() {
		if (path == null)
			path = new SimpleStringProperty(this, "path");
		return path;
	}

	private StringProperty size;

	public StringProperty sizeProperty() {
		if (size == null)
			size = new SimpleStringProperty(this, "size");
		return size;
	}

	private SimpleObjectProperty<Timestamp> lmts;

	public SimpleObjectProperty<Timestamp> lmtsProperty() {
		if (lmts == null)
			lmts = new SimpleObjectProperty<Timestamp>(this, "lmts");
		return lmts;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getPath() {
		return path.get();
	}

	public void setPath(String path) {
		this.path.set(path);
	}

	public String getSize() {
		return size.get();
	}

	public void setSize(String size) {
		this.size.set(size);
	}

	public Timestamp getLtms() {
		return lmts.get();
	}

	public void setLtms(Timestamp lmts) {
		this.lmts.set(lmts);
	}
}
