package cict.gaml.extensions.VR.file;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class TestImport {
public static void main(String[] args){
	try {
		String path="D:\\OneDrive\\Gama1.7.1\\P0\\models\\";
		for(int i=0; i<100; i++){
			
			PrintWriter writer = new PrintWriter(path+"m"+i+".gaml", "UTF-8");
			writer.println("model m"+i+"");
			writer.println("global{");
			writer.println("	file img<-file(\"../images/images/soil.jpg\");");
			writer.println("}");
			writer.println("species A"+i+"{");
			writer.println("}");
			
			writer.close();
		}
		for (int j=0; j<6; j++){
			path="D:\\OneDrive\\Gama1.7.1\\P0\\models\\modules"+j+"\\";
			for(int i=0; i<100; i++){				
				PrintWriter writer = new PrintWriter(path+"mo"+j+"_"+i+".gaml", "UTF-8");
				writer.println("model mo"+j+"_"+i+"");
				writer.println("global{");
				writer.println("	file img<-file(\"../../images/images/soil.jpg\");");
				writer.println("}");
				writer.println("species Ao"+j+"_"+i+"{");
				writer.println("}");
				
				writer.close();
			}
		}
		
		path="D:\\OneDrive\\Gama1.7.1\\TestRelativePath\\models2\\modules\\";
		

		PrintWriter writer = new PrintWriter(path+"mi.gaml", "UTF-8");
		writer.println("model mi");
		for(int i=0; i<100; i++){
			writer.println("import \"../../../P0/models/m"+i+".gaml\"");
		}

		for (int j=0; j<6; j++){
			for(int i=0; i<100; i++){
				writer.println("import \"../../../P0/models/modules"+j+"/mo"+j+"_"+i+".gaml\"");
			}
		}
		writer.println("global{");
		writer.println("}");
		writer.println("experiment expi type:gui{");
		writer.println("	output{");
		writer.println("	}	");
		writer.println("}");
		
		writer.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
/*

//			writer.println("experiment exp"+i+" type:gui{");
//			writer.println("	output{");
//			writer.println("		display \"exp"+i+"_disp\"{");
//			writer.println("			image img.path ;");
//			writer.println("		}");
//			writer.println("	}	");
//			writer.println("}");
*/