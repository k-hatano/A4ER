import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas {

	A4ER parent;
	ArrayList<HashMap<String, String>> lEREntities = new ArrayList<HashMap<String, String>>();
	HashMap<String, HashMap<String, String>> lERFields = new HashMap<String, HashMap<String, String>>();

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
	}

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		grp.setColor(Color.black);
		int y = 0;
		for (HashMap<String, String> item : lEREntities) {
			grp.drawString(item.get("name"), 
				Integer.parseInt(item.get("x")),
				Integer.parseInt(item.get("y")));

			y += 16;
		}

		g.drawImage(img, 0, 0, this);
	}

	public void showImportFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileFilter(new FileNameExtensionFilter("ERダイアグラム", "a5er"));
		int res = chooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file=chooser.getSelectedFile();
			importA5ER(file.getAbsolutePath());
		}
	}

	public void importA5ER(String path) {
		File file = new File(path);
		parent.setTitle(file.getName());
		try {
			lEREntities = new ArrayList<HashMap<String, String>>();
			lERFields = new HashMap<String, HashMap<String, String>>();

			ArrayList<String> list = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}


			HashMap<String, String> entity = new HashMap<String, String>();
			HashMap<String, HashMap<String, String>> fields = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> field = new HashMap<String, String>();
			entity.put("name", "");

			Collections.reverse(list);
			for (String str : list) {
				final Pattern p1 = Pattern.compile("^Position=\\\"(.*)\\\",(\\d+),(\\d+)");
				Matcher m1 = p1.matcher(str);
				if (m1.find()) {
					entity.put("x", m1.group(2));
					entity.put("y", m1.group(3));
					continue;
				}

				final Pattern p2 = Pattern.compile("^Field=\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\"");
				Matcher m2 = p2.matcher(str);
				if (m2.find()) {
					field.put("name", m2.group(1));
					field.put("key", m2.group(2));
					field.put("type", m2.group(3));
					fields.put(entity.get("name"), field);
					continue;
				}

				final Pattern p3 = Pattern.compile("^PName=(.+)");
				Matcher m3 = p3.matcher(str);
				if (m3.find()) {
					entity.put("name", m3.group(1));
					continue;
				}

				final Pattern p4 = Pattern.compile("^LName=(.+)");
				Matcher m4 = p4.matcher(str);
				if (m4.find()) {
					entity.put("key", m4.group(1));
					continue;
				}

				final Pattern p5 = Pattern.compile("^\\[Entity\\]");
				Matcher m5 = p5.matcher(str);
				if (m5.find()) {
					lEREntities.add(entity);
					lERFields.putAll(fields);
					entity = new HashMap<String, String>();
					fields = new HashMap<String, HashMap<String, String>>();
					field = new HashMap<String, String>();
					entity.put("name", "");
					continue;
				}
			}

			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}