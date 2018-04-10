package examples.IRAObjects;

public class Space implements Comparable<Space> {
	
	String hallName;
	Integer space;
	
	public Space(String hallName, int space) {
		super();
		this.hallName = hallName;
		this.space = space;
	}
	public String getHallName() {
		return hallName;
	}
	public void setHallName(String hallName) {
		this.hallName = hallName;
	}
	public Integer getSpace() {
		return space;
	}
	public void setSpace(int space) {
		this.space = space;
	}
	public int compareTo(Space o) {
		return this.getSpace().compareTo(o.getSpace());
	}

}
