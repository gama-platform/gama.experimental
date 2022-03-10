package ummisco.gama.unity.client.data;

import java.util.HashMap;

public class Student {

	private String name;
	private int registrationNumber;
	private Course[] courses;
	private HashMap<Integer, Course> mapCourse = new HashMap<Integer, Course>();

	public Student() {

	}

	public Student(String name, int registrationNumber, Course[] courses, HashMap<Integer, Course> map) {
		this.name = name;
		this.registrationNumber = registrationNumber;
		this.courses = courses;
		this.mapCourse = map;
	}

	public String ToString() {
		return ("name: " + (this.name + (", registrationNumber, "
				+ (this.registrationNumber + (", courses: \n" + this.printArray(this.courses))))));
	}

	public final String printArray(Object[] ob) {
		String coursesStr = "";
		if ((this.courses == null)) {
			return coursesStr;
		} else {
			for (int i = 0; (i < ob.length); i++) {
				coursesStr = (coursesStr + (ob[i] + "\n"));
			}

			return coursesStr;
		}

	}

	public String printStudent() {
		String str = "";
		str += " -- * -- Print the Student Data -- * -- \n";
		str += " name : " + name + " \n";
		str += " registrationNumber : " + registrationNumber + "\n";
		str += " courses number : " + courses.length + "\n";
		for (Course course : courses) {
			str += "		-- -- -- -- -- -- -- \n";
			str += "	1code : " + course.getCode() + "\n";
			str += "	name : " + course.getName() + "\n";
			str += "	term : " + course.getTerm() + "\n";
			str += "		-- -- -- -- -- -- -- \n";
		}

		str += " mapCourse number : " + mapCourse.size() + " \n";

		for (Integer mapKey : mapCourse.keySet()) {
			str += "		-- -- -- -- -- -- -- \n";
			str += "	code : " + mapCourse.get(mapKey).getCode() + "\n";
			str += "	name : " + mapCourse.get(mapKey).getName() + "\n";
			str += "	term : " + mapCourse.get(mapKey).getTerm() + "\n";
			str += "		-- -- -- -- -- -- -- \n";
		}

		str += " \n";

		return str;
	}

	public static Student getNewStudent() {

		Course[] courses = { new Course(6756, "XML and Related Technologies", 2),
				new Course(9865, "Object Oriented Programming", 2), new Course(1134, "E-Commerce Programming", 3) };

		HashMap map = new HashMap();
		map.put(6756, new Course(6756, "XML and Related Technologies", 3));
		map.put(9865, new Course(9865, "Object Oriented Programming", 2));
		map.put(1134, new Course(1134, "E-Commerce Programming", 2));
		map.put(4598, new Course(4598, "Enterprise Component Architecture", 3));

		Student student = new Student("Carlos Jaimez", 76453, courses, map);

		return student;
	}
}
