package gamrGenerator;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class GamaLauncher extends JFrame {
	String gama = "";
	String model = "";
	String exp = "";
	private String OS = System.getProperty("os.name").toLowerCase();

	private boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	private boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	private boolean isUnix() {
		return (OS.indexOf("nux") >= 0);
	}

	final File ini = new File("gamalauncher.ini");

	public void init() {
		try {
			// final File ini = new File("D:\\gamalauncher.ini");
			if (ini != null) {
				try (final FileInputStream stream = new FileInputStream(ini);
						final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));) {
					String s = reader.readLine();
					while (s != null) {
						if (s.startsWith("-gama")) {
							s = reader.readLine();
							gama = s;
						} else if (s.startsWith("-model")) {
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
	}

	public void check_gama() {
		File g = new File(gama);
		if (!g.exists()) {
			FileDialog dialog = new FileDialog(this, "Cannot find Gama in ini! Select GAMA binary file");
			dialog.setMode(FileDialog.LOAD);
			dialog.setVisible(true);
			String file = dialog.getFile();
			if (file == null) {
				System.exit(ABORT);
			}
			gama = dialog.getDirectory() + file;

			try {
				final List<String> contents = new ArrayList<>();
				if (ini != null) {
					try (final FileInputStream stream = new FileInputStream(ini);
							final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));) {
						String s = reader.readLine();
						while (s != null) {
							if (s.startsWith("-gama")) {
								contents.add(s);
								s = reader.readLine();
								s = gama;
							} else {
								contents.add(s);
								s = reader.readLine();
							}
						}
					}
					try (final FileOutputStream os = new FileOutputStream(ini);
							final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));) {
						for (final String line : contents) {
							writer.write(line);
							writer.newLine();
						}
						writer.flush();
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void launch() {
		check_gama();
//		String s = ""+gama + "\" " + model + "#" + exp;
//		if (s == null || s.isEmpty())
//			return;
		final List<String> commands = new ArrayList<>();
//		commands.add(isWindows() ? "cmd.exe" : "/bin/bash");
//		commands.add(isWindows() ? "/C" : "-c");
//		commands.add(s.trim());
		commands.add(gama);
		System.out.println(System.getProperty("user.dir")+"\\"+model);
		commands.add(System.getProperty("user.dir")+"\\"+model+ "#" + exp);
		// commands.addAll(Arrays.asList(s.split(" ")));
		final boolean nonBlocking = commands.get(commands.size() - 1).endsWith("&");
		if (nonBlocking) {
			// commands.(commands.size() - 1);
		}
		final ProcessBuilder b = new ProcessBuilder(commands);
		// b.redirectErrorStream(true);
		// b.directory(new File(gama));
		// b.environment().putAll(environment);
		try {
			final Process p = b.start();
			return;
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		GamaLauncher g = new GamaLauncher();
		g.setDefaultCloseOperation(EXIT_ON_CLOSE);
		g.init();
		g.launch();
		// g.setVisible(true);
	}

}
