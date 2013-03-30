package net.ubuntudaily.quickquest.fsobject;

public class ViewModelNotice {

	private FileOperationType type;
	private FSObjectVO fSObjectVO;
	private FSObject fSObject;
	public ViewModelNotice(FileOperationType type,
			FSObjectVO fSObjectVO,
			FSObject fSObject) {
		super();
		this.type = type;
		this.fSObjectVO = fSObjectVO;
		this.fSObject = fSObject;
	}
	public FileOperationType getType() {
		return type;
	}
	public FSObjectVO getFSObjectVO() {
		return fSObjectVO;
	}
	public FSObject getFSObject() {
		return fSObject;
	}
	
}
