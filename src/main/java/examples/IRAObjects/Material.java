package examples.IRAObjects;

public class Material {
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	String label;

	public Material(String label) {
		super();
		this.label = label;
	}
	
	public int compareTo(Time o) {
		return this.getLabel().compareTo(o.getLabel());
	}
}
