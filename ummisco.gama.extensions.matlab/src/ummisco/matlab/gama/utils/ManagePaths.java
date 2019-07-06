package ummisco.matlab.gama.utils;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ManagePaths {
	
	/**
	 * Sets the java library path to the specified path
	 * from http://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html
	 * 
	 * @param path the new library path
	 * @throws Exception
	 */
	public static void setLibraryPath(String path) throws Exception {
		System.setProperty("java.library.path", path);

		//set sys_paths to null
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);
	}	
	
	
	/**
	* Adds the specified path to the java library path
	* from http://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html
	* 
	* @param pathToAdd the path to add
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	* @throws Exception
	*/
	public static void addLibraryPath(String pathToAdd) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		//get array of paths
		final String[] paths = (String[])usrPathsField.get(null);
	
		//check if the path to add is already present
		for(String path : paths) {
			if(path.equals(pathToAdd)) {
				return;
			}
		}
		
		//add the new path
		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length-1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}
}
