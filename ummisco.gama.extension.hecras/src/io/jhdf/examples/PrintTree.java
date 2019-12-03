/*
 * This file is part of jHDF. A pure Java library for accessing HDF5 files.
 *
 * http://jhdf.io
 *
 * Copyright 2019 James Mudd
 *
 * MIT License see 'LICENSE' file
 */
package io.jhdf.examples;

import io.jhdf.HdfFile;
import io.jhdf.api.Group;
import io.jhdf.api.Node;

import java.io.File;
import java.util.Collections;

/**
 * An example of recursively parsing a HDF5 file tree and printing it to the
 * console.
 *
 * @author James Mudd
 */
public class PrintTree {

	public static void main(String[] args) {
		File file = new File("E:\\Downloads\\HWC\\HelloWorldCoupling.p04.hdf");
		System.out.println(file.getName());

		try (HdfFile hdfFile = new HdfFile(file)) {
			recursivePrintGroup(hdfFile, 0);
		}
	}

	private static void recursivePrintGroup(Group group, int level) {
		level++;
		String indent = String.join("", Collections.nCopies(level, "    "));
		for (Node node : group) {
			System.out.println(indent +node.getPath()+" "+ node.getType()+" "+ node.getName());
			if (node instanceof Group) {
				recursivePrintGroup((Group) node, level);
			}
		}
	}

}
