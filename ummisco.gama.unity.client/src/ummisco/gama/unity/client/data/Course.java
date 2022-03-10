package ummisco.gama.unity.client.data;

public class Course {
	private int code;
	private String name;
	private int term;

	public Course() {
	}

	public Course(int code, String name, int term) {
		this.code = code;
		this.name = name;
		this.term = term;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public String ToString() {
		return "code: " + code + ", name: " + name + ", term: " + term;
	}
}
