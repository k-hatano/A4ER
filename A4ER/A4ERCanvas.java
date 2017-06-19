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
	ArrayList<Relation> lERReleations = new ArrayList<Relation>();
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
				int physicalNameWidth = 0;
				for (Field field : lERFields.get(item.physicalName)) {
					physicalNameWidth = metrics.getStringBounds(field.key, grp).getBounds().width;
					if (item.physicalNameWidth < physicalNameWidth) {
						item.physicalNameWidth = physicalNameWidth;
					}
				}
			}

			if (item.logicalNameWidth == 0) {
				int logicalNameWidth = 0;
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
			item.tmpWidth = width;
			item.tmpHeight = height;

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
			Entity entity = Entity.entityNamed(lEREntities, key);
			if (entity == null) {
				continue;
			}
			Position position = entity.positionInPage(currentPage);
			if (position == null) {
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

		for (Relation relation : lERReleations) {
			Entity entity1 = Entity.entityNamed(lEREntities, relation.entity1);
			Entity entity2 = Entity.entityNamed(lEREntities, relation.entity2);
			if (entity1 == null || entity2 == null) {
				continue;
			}
			Position position1 = entity1.positionInPage(currentPage);
			Position position2 = entity2.positionInPage(currentPage);

			if (position1 == null || position2 == null) {
				continue;
			}

			Point left1 = new Point(position1.x , (position1.y + position1.y + entity1.tmpHeight) / 2);
			Point top1 = new Point((position1.x + position1.x + entity1.tmpWidth)/2 , position1.y);
			Point right1 = new Point(position1.x + entity1.tmpWidth , (position1.y + position1.y + entity1.tmpHeight)/2);
			Point bottom1 = new Point((position1.x + position1.x + entity1.tmpWidth)/2 , position1.y + entity1.tmpHeight);

			Point left2 = new Point(position2.x , (position2.y + position2.y + entity2.tmpHeight) / 2);
			Point top2 = new Point((position2.x + position2.x + entity2.tmpWidth)/2 , position2.y);
			Point right2 = new Point(position2.x + entity2.tmpWidth , (position2.y + position2.y + entity2.tmpHeight)/2);
			Point bottom2 = new Point((position2.x + position2.x + entity2.tmpWidth)/2 , position2.y + entity2.tmpHeight);

			int minDist = (int)Math.min(left1.manhattanDistanceTo(right2),
				Math.min(top1.manhattanDistanceTo(bottom2),
					Math.min(right1.manhattanDistanceTo(left2),
						bottom1.manhattanDistanceTo(top2))));

			if (minDist == left1.manhattanDistanceTo(right2)) {
				grp.drawLine(left1.x + scrollX, left1.y + scrollY, 
					right2.x + scrollX, right2.y + scrollY);
				grp.drawLine(left1.x + scrollX, left1.y + scrollY, 
					right2.x + scrollX, right2.y + scrollY);
				grp.drawLine(left1.x + scrollX, left1.y + scrollY, 
					right2.x + scrollX, right2.y + scrollY);
			} else if (minDist == top1.manhattanDistanceTo(bottom2))  {
				grp.drawLine(top1.x + scrollX, top1.y + scrollY, 
					bottom2.x + scrollX, bottom2.y + scrollY);
				grp.drawLine(top1.x + scrollX, top1.y + scrollY, 
					bottom2.x + scrollX, bottom2.y + scrollY);
				grp.drawLine(top1.x + scrollX, top1.y + scrollY, 
					bottom2.x + scrollX, bottom2.y + scrollY);
			} else if (minDist == right1.manhattanDistanceTo(left2))  {
				grp.drawLine(right1.x + scrollX, right1.y + scrollY, 
					left2.x + scrollX, left2.y + scrollY);
				grp.drawLine(right1.x + scrollX, right1.y + scrollY, 
					left2.x + scrollX, left2.y + scrollY);
				grp.drawLine(right1.x + scrollX, right1.y + scrollY,
					left2.x + scrollX, left2.y + scrollY);
			} else {
				grp.drawLine(bottom1.x + scrollX, bottom1.y + scrollY, 
					top2.x + scrollX, top2.y + scrollY);
				grp.drawLine(bottom1.x + scrollX, bottom1.y + scrollY, 
					top2.x + scrollX, top2.y + scrollY);
				grp.drawLine(bottom1.x + scrollX, bottom1.y + scrollY, 
					left2.x + scrollX, left2.y + scrollY);
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
		chooser.setFileFilter(new FileNameExtensionFilter("ER Diagram", "a5er"));
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
			lERReleations = new ArrayList<Relation>();
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
			Relation relation = new Relation();
			int level = 0;

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
					relation = new Relation();
					continue;
				}

				final Pattern p6 = Pattern.compile("^Entity1=(.+)");
				Matcher m6 = p6.matcher(str);
				if (m6.find()) {
					relation.entity1 = m6.group(1);
					continue;
				}

				final Pattern p7 = Pattern.compile("^Entity2=(.+)");
				Matcher m7 = p7.matcher(str);
				if (m7.find()) {
					relation.entity2 = m7.group(1);
					continue;
				}

				final Pattern p8 = Pattern.compile("^\\[Relation\\]");
				Matcher m8 = p8.matcher(str);
				if (m8.find()) {
					lERReleations.add(0, relation);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					continue;
				}

				final Pattern p9 = Pattern.compile("^ViewMode=(\\d+)");
				Matcher m9 = p9.matcher(str);
				if (m9.find()) {
					int tmpLevel = Integer.parseInt(m9.group(1));
					switch (tmpLevel) {
						case 0:
						case 1:
						case 2:
						level = 0;
						break;
						case 3:
						case 4:
						case 6:
						level = 2;
						break;
						case 5:
						case 7:
						level = 1;
						break;
						default:
						level = 0;
						break;
					}
					continue;
				}
			}

			parent.cbPage.removeAllItems();
			for (String page : lPages) {
				parent.cbPage.addItem(page);
			}
			parent.cbLevel.setSelectedIndex(level);
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
			if (arg0.getButton() == MouseEvent.BUTTON2) {
				scrollX = originalX + (arg0.getX() - clickedX) * 2;
				scrollY = originalY + (arg0.getY() - clickedY) * 2;
			} else {
				scrollX = originalX + (arg0.getX() - clickedX);
				scrollY = originalY + (arg0.getY() - clickedY);
			}
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	public int showEntitiesList() {
		ArrayList<String> entitiesList = new ArrayList<String>();

		for (Entity item : lEREntities) {
			entitiesList.add(item.logicalName);
		}

		Collections.sort(entitiesList);
		String[] entities = (String[])(entitiesList.toArray(new String[0]));

		if (entities.length <= 0) {
			JOptionPane.showMessageDialog(parent,"No entities loaded.", 
				"Entities List", JOptionPane.INFORMATION_MESSAGE);
		}

		String result = (String)JOptionPane.showInputDialog(parent,
			null,
			"Entities List",
			JOptionPane.INFORMATION_MESSAGE,
			null,
			entities,
			entities[0]);
		
		if (result != null && result.length() > 0) {
			for (Entity item : lEREntities) {
				if (item.logicalName.equals(result)) {
					Position position = item.positions.get(0);
					parent.cbPage.setSelectedItem(position.page);
					scrollX = -position.x + 64;
					scrollY = -position.y + 64;
					repaint();
					break;
				}
			}
		}

		return 0;
	}

}