package gamrGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import msi.gama.application.workspace.WorkspaceModelsManager;
import msi.gama.common.interfaces.IEventLayerDelegate;
import msi.gama.outputs.layers.EventLayerStatement;
import msi.gama.runtime.IScope;

public class Launcher implements IEventLayerDelegate {

	public static final Set<String> EVENTS =
			new HashSet<>(); 
	@Override
	public boolean acceptSource(IScope scope, Object source) {
		if (source.equals("launcher"))
			return true;
		return false;
	}

	private static void unzip(String zipFilePath, String destDir) {
		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists())
			dir.mkdirs();
		FileInputStream fis;
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
				System.out.println("Unzipping to " + newFile.getAbsolutePath());
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				// close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			// close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean createFrom(IScope scope, Object source, EventLayerStatement statement) {
		System.out.println("launcher");

		String filePath = source.toString();
		String dirPath = filePath.substring(0, filePath.length() - 5);
		if (filePath.endsWith(".gamr")) {
			unzip(filePath, dirPath);
			System.out.println("unzip");
		}

		final File ini = new File(dirPath+"/.metadata/launcher.ini");
		String model="";
		String exp="";
		try {
			// final File ini = new File("D:\\gamalauncher.ini");
			if (ini != null) {
				try (final FileInputStream stream = new FileInputStream(ini);
						final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));) {
					String s = reader.readLine();
					while (s != null) {
						if (s.startsWith("-model")) {
							s = reader.readLine();
							model = s;
						}
						if (s.startsWith("-experiment")) {
							s = reader.readLine();
							exp = s;
						} else {
							s = reader.readLine();
						}
					}
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		WorkspaceModelsManager.instance.openModelPassedAsArgument(dirPath+"/"+model+"#"+exp);
		return false;
	}

	@Override
	public Set<String> getEvents() {
		// TODO Auto-generated method stub
		return EVENTS;
	}

}
