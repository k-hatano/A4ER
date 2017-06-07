import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas implements MouseListener, MouseMotionListener {

	A4ER parent;
	ArrayList<Entity> lEREntities = new ArrayList<Entity>();
	HashMap<String, ArrayList<Field>> lERFields = new HashMap<String, ArrayList<Field>>();
	ArrayList<String> lPages = new ArrayList<String>();

	int originalX, originalY;
	int scrollX, scrollY;
	int clickedX, clickedY;
	boolean dragging = false;

	File lastFile = null;

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();

		int maxWidth = 0;
		int maxHeight = 0;
		int level = parent.cbLevel.getSelectedIndex();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		String currentPage = (String) parent.cbPage.getSelectedItem();
		FontMetrics metrics = grp.getFontMetrics();

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		grp.setColor(Color.black);
		for (Entity item : lEREntities) {
			Position position = item.positionInPage(currentPage);
			if (position == null) {
				continue;
			}

			grp.drawString(item.logicalName, 
				position.x + scrollX,
				position.y + scrollY);

			if (item.physicalNameWidth == 0) {
				int physicalNameWidth = metrics.getStringBounds(item.physicalName, grp).getBounds().width;
				if (item.physicalNameWidth < physicalNameWidth) {
					item.physicalNameWidth = physicalNameWidth;
				}
				for (Field field : lERFields.get(item.physicalName)) {
					physicalNameWidth = metrics.getStringBounds(field.key, grp).getBounds().width;
					if (item.physicalNameWidth < physicalNameWidth) {
						item.physicalNameWidth = physicalNameWidth;
					}
				}
			}

			if (item.logicalNameWidth == 0) {
				int logicalNameWidth = metrics.getStringBounds(item.logicalName, grp).getBounds().width;
				if (item.logicalNameWidth < logicalNameWidth) {
					item.logicalNameWidth = logicalNameWidth;
				}
				for (Field field : lERFields.get(item.physicalName)) {
					logicalNameWidth = metrics.getStringBounds(field.name, grp).getBounds().width;
					if (item.logicalNameWidth < logicalNameWidth) {
						item.logicalNameWidth = logicalNameWidth;
					}
				}
			}

			if (item.typeWidth == 0) {
				int typeWidth = 0;
				for (Field field : lERFields.get(item.physicalName)) {
					typeWidth = metrics.getStringBounds(field.type, grp).getBounds().width;
					if (item.typeWidth < typeWidth) {
						item.typeWidth = typeWidth;
					}
				}
			}

			int left = position.x + scrollX;
			int top = position.y + scrollY;
			int width = item.logicalNameWidth;
			int height = lERFields.get(item.physicalName).size() * 16;
			if (level == 1) {
				width = item.logicalNameWidth + item.physicalNameWidth;
			} else if (level == 2) {
				width = item.logicalNameWidth + item.typeWidth;
			}

			grp.setColor(Color.white);
			grp.fillRect(left, top, width, height);
			grp.setColor(Color.black);
			grp.drawRect(left, top, width, height);

			if (maxWidth < left + width) {
				maxWidth = left + width;
			}

			if (maxHeight < top + height) {
				maxHeight = top + height;
			}
		}

		for (String key : lERFields.keySet()) {
			Entity entity = null;
			Position position = null;
			for (Entity anEntity : lEREntities) {
				position = anEntity.positionInPage(currentPage);
				if (position == null) {
					continue;
				}

				if (anEntity.physicalName.equals(key)) {
					entity = anEntity;
					break;
				}
			}
			if (entity == null || position == null) {
				continue;
			}

			ArrayList<Field> fields = lERFields.get(key);
			int x = position.x + scrollX;
			int y = position.y + scrollY;
			for (Field item : fields) {
				y += 16;
				grp.drawString(item.name, x, y);
				if (level == 1) {
					grp.drawString(item.key, x + entity.logicalNameWidth, y);
				} else if (level == 2) {
					grp.drawString(item.type, x + entity.logicalNameWidth, y);
				}
			}
		}

		if (dragging) {
			grp.setColor(Color.red);
			grp.drawRect(scrollX, scrollY, maxWidth - scrollX, maxHeight - scrollY);
		}

		g.drawImage(img, 0, 0, this);
	}

	public void showImportFileDialog() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setFileFilter(new FileNameExtensionFilter("ERダイアグラム", "a5er"));
		if (lastFile != null) {
			chooser.setSelectedFile(lastFile);
		}
		int res = chooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastFile = file;
			importA5ER(file.getAbsolutePath());
		}
	}

	public void importA5ER(String path) {
		File file = new File(path);
		parent.setTitle(file.getName());
		try {
			lEREntities = new ArrayList<Entity>();
			lERFields = new HashMap<String, ArrayList<Field>>();
			lPages = new ArrayList<String>();

			ArrayList<String> list = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				list.add(line);
				line = reader.readLine();
			}


			Entity entity = new Entity();
			ArrayList<Field> fields = new ArrayList<Field>();
			Field field = new Field();
			entity.logicalName = "";
			entity.physicalName = "";

			Collections.reverse(list);
			for (String str : list) {
				final Pattern p1 = Pattern.compile("^Position=\\\"(.*)\\\",(\\d+),(\\d+)");
				Matcher m1 = p1.matcher(str);
				if (m1.find()) {
					String page = m1.group(1);

					Position position = new Position();
					position.x = Integer.parseInt(m1.group(2));
					position.y = Integer.parseInt(m1.group(3));
					position.page = page;
					entity.positions.add(position);
					if (lPages.indexOf(page) < 0) {
						lPages.add(0, page);
					}

					continue;
				}

				final Pattern p2 = Pattern.compile("^Field=\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\"");
				Matcher m2 = p2.matcher(str);
				if (m2.find()) {
					field.name = m2.group(1);
					field.key = m2.group(2);
					field.type = m2.group(3);
					fields.add(0, field);
					field = new Field();
					continue;
				}

				final Pattern p3 = Pattern.compile("^PName=(.+)");
				Matcher m3 = p3.matcher(str);
				if (m3.find()) {
					entity.physicalName = m3.group(1);
					continue;
				}

				final Pattern p4 = Pattern.compile("^LName=(.+)");
				Matcher m4 = p4.matcher(str);
				if (m4.find()) {
					entity.logicalName = m4.group(1);
					continue;
				}

				final Pattern p5 = Pattern.compile("^\\[Entity\\]");
				Matcher m5 = p5.matcher(str);
				if (m5.find()) {
					lEREntities.add(0, entity);
					lERFields.put(entity.physicalName ,fields);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					entity.logicalName = "";
					entity.physicalName = "";
					continue;
				}
			}

			parent.cbPage.removeAllItems();
			for (String page : lPages) {
				parent.cbPage.addItem(page);
			}
			repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		originalX = scrollX;
		originalY = scrollY;
		clickedX = arg0.getX();
		clickedY = arg0.getY();
		dragging = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		dragging = false;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (dragging) {
			scrollX = originalX + (arg0.getX() - clickedX);
			scrollY = originalY + (arg0.getY() - clickedY);
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

}