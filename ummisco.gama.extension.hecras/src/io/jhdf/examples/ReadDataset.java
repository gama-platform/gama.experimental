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
import io.jhdf.api.Dataset;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Example application for reading a dataset from HDF5
 *
 * @author James Mudd
 */
public class ReadDataset {
	public static void main(String[] args) {
		File file = new File("E:\\Downloads\\HWC\\HelloWorldCoupling.p04.hdf");

		try (HdfFile hdfFile = new HdfFile(file)) {
			Dataset dataset = hdfFile.getDatasetByPath("/Results/Unsteady/Output/Output Blocks/Base Output/Unsteady Time Series/2D Flow Areas/Hello 2D Area/Depth");
			// data will be a java array of the dimensions of the HDF5 dataset
			float[][] data = (float[][]) dataset. getData();
			try (PrintWriter p = new PrintWriter(new FileOutputStream("E:\\Depth.csv", false))) {
				int x=dataset.getDimensions()[0];
				int y=dataset.getDimensions()[1];
//				p.println("ncols 40");//"+x);1118216
//				p.println("nrows 20");//"+y);
//				p.println("xllcorner     0.0");
//				p.println("yllcorner     0.0");
//				p.println("cellsize      2.0");

				  float[] oneDArray = new float[(int) dataset.getSize()];
				  for(int i = 0; i < x; i ++)
				  {
				    for(int s = 0; s < y; s ++)
				    {
				      oneDArray[(i * y) + s] = data[i][s];
				    }
				  }
				  
				  float[][][] frame=new float[2000][][];
				  int f =0; int fi=0;
				  while (fi<oneDArray.length -800) {
					  
				      frame[f]=new float[20][40];
					  for(int i = 0; i < 20; i ++)
					  {
					    for(int s = 0; s < 40; s ++)
					    {
					      frame[f][i][s] = oneDArray[fi];
					      fi++;
					    }
					  }
					  f++;
				  }
				  
				  
			    p.println(ArrayUtils.toString(frame[100]).replace("},{", "\n").replace("{{", "").replace("}}", ""));
			    p.close();
			} catch (Exception e1) {
			    e1.printStackTrace();
			}
			//System.out.println(ArrayUtils.toString(data));
		}
	}
}
