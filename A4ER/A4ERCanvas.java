import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.awt.datatransfer.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	A4ER parent;
	ArrayList<Entity> lEREntities = new ArrayList<Entity>();
	HashMap<String, ArrayList<Field>> lERFields = new HashMap<String, ArrayList<Field>>();
	ArrayList<Relation> lERReleations = new ArrayList<Relation>();
	ArrayList<String> lPages = new ArrayList<String>();
	ArrayList<Comment> lComments = new ArrayList<Comment>();

	Image imgForCopying = null;

	int originalX, originalY;
	int scrollX, scrollY;
	int clickedX, clickedY;
	boolean dragging = false;

	public int maxWidth = 0;
	public int maxHeight = 0;

	float xRate = 1.4f;
	float yRate = 1.4f;

	long lastClickedTime = 0;
	int selectedIndex = 0;

	File lastFile = null;

	String searchingString = null;
	int foundX = -1;
	int foundY = -1;
	boolean showSearchResultFlag = false;
	boolean drawForCopying = false;

	public A4ERCanvas(A4ER a4er) {
		parent = a4er;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}

	public void paint(final Graphics g){
		int oldMaxWidth = maxWidth;
		int oldMaxHeight = maxHeight;

		int w = this.getWidth();
		int h = this.getHeight();
		int minX = w;
		int minY = h;
		if (drawForCopying) {
			w = maxWidth;
			h = maxHeight;
		} else {
			maxWidth = 0;
			maxHeight = 0;
		}

		foundX = -1;
		foundY = -1;

		int level = parent.cbLevel.getSelectedIndex();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		if (parent.miAntialiasing.getState()) {
			grp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            	            RenderingHints.VALUE_ANTIALIAS_ON);
		}

		String currentPage = (String) parent.cbPage.getSelectedItem();
		if (currentPage != null) {
			currentPage = currentPage.toUpperCase();
		}
		FontMetrics metrics = grp.getFontMetrics();

		grp.setColor(new Color(240, 240, 240));
		grp.fillRect(0, 0, w, h);

		grp.setColor(new Color(255, 255, 255));
		grp.fillRect(scrollX, scrollY, oldMaxWidth, oldMaxHeight); // 雑！

		if (parent.miGrid.getState()) {
			int gridX = scrollX % 64;
			grp.setColor(new Color(224, 224, 224));
			while (gridX < w) {
				grp.drawLine(gridX, 0, gridX, h);
				gridX += 64;
			}

			int gridY = scrollY % 64;
			grp.setColor(new Color(224, 224, 224));
			while (gridY < h) {
				grp.drawLine(0, gridY, w, gridY);
				gridY += 64;
			}
		}

		grp.setColor(Color.black);
		for (Entity entity : lEREntities) {
			Position position = entity.positionInPage(currentPage);
			if (position == null) {
				continue;
			}

			if (searchingString != null && entity.logicalName.indexOf(searchingString) >= 0) {
				grp.setColor(Color.red);
				if (foundX == -1 && foundY == -1) {
					foundX = (int)(position.x / xRate + scrollX);
					foundY = (int)(position.y / yRate - 2 + scrollY);
				}
			} else {
				grp.setColor(Color.black);
			}
			grp.drawString(entity.logicalName, 
				(int)(position.x / xRate + scrollX),
				(int)(position.y / yRate - 2 + scrollY));

			if (level == 3 || level == 4) {
				int tableLogicalNameWidth = metrics.getStringBounds(entity.logicalName, grp).getBounds().width;

				if (searchingString != null && entity.physicalName.indexOf(searchingString) >= 0) {
					grp.setColor(Color.red);
					if (foundX == -1 && foundY == -1) {
						foundX = (int)(position.x / xRate + tableLogicalNameWidth + 8 + scrollX);
						foundY = (int)(position.y / yRate - 2 + scrollY);
					}
				} else {
					grp.setColor(new Color(128, 0, 128));
				}
				grp.drawString(entity.physicalName, 
					(int)(position.x / xRate + tableLogicalNameWidth + 8 + scrollX),
					(int)(position.y / yRate - 2 + scrollY));
			}

			int physicalNameWidth = 0;
			entity.physicalNameWidth = 8;
			for (Field field : lERFields.get(entity.physicalName)) {
				if (level == 0 && (field.key == null || field.key.length() == 0)) {
					continue;
				}
				physicalNameWidth = metrics.getStringBounds(field.physicalName, grp).getBounds().width;
				if (entity.physicalNameWidth < physicalNameWidth) {
					entity.physicalNameWidth = physicalNameWidth;
				}
			}

			int logicalNameWidth = 0;
			entity.logicalNameWidth = 8;
			for (Field field : lERFields.get(entity.physicalName)) {
				if (level == 0 && (field.key == null || field.key.length() == 0)) {
					continue;
				}
				logicalNameWidth = metrics.getStringBounds(field.logicalName, grp).getBounds().width;
				if (entity.logicalNameWidth < logicalNameWidth) {
					entity.logicalNameWidth = logicalNameWidth;
				}
			}

			int typeWidth = 0;
			entity.typeWidth = 8;
			for (Field field : lERFields.get(entity.physicalName)) {
				if (level == 0 && (field.key == null || field.key.length() == 0)) {
					continue;
				}
				typeWidth = metrics.getStringBounds(field.type, grp).getBounds().width;
				if (entity.typeWidth < typeWidth) {
					entity.typeWidth = typeWidth;
				}
			}

			int left = (int)(position.x / xRate + scrollX - 6);
			int top = (int)(position.y / yRate + scrollY);
			int width = entity.logicalNameWidth + 12;
			int height = lERFields.get(entity.physicalName).size() * 16 + 2;
			if (level == 3) {
				width += entity.physicalNameWidth;
			} else if (level == 2) {
				width += entity.typeWidth;
			} else if (level == 4) {
				width += entity.physicalNameWidth + entity.typeWidth;
			} else if (level == 0) {
				height = entity.keys * 16 + 2;
			}
			entity.tmpLeft = left;
			entity.tmpTop = top;
			entity.tmpWidth = width;
			entity.tmpHeight = height;

			grp.setColor(Color.white);
			grp.fillRect(left, top, width, height);
			grp.setColor(Color.black);
			grp.drawRect(left, top, width, height);

			if (minX > position.x) {
				minX = position.x;
			}

			if (minY > position.y) {
				minY = position.y;
			}

			if (maxWidth < left + width - scrollX) {
				maxWidth = left + width - scrollX;
			}

			if (maxHeight < top + height - scrollY) {
				maxHeight = top + height - scrollY;
			}
		}

		for (String physicalName : lERFields.keySet()) {
			Entity entity = Entity.entityNamed(lEREntities, physicalName);
			if (entity == null) {
				continue;
			}
			Position position = entity.positionInPage(currentPage);
			if (position == null) {
				continue;
			}

			ArrayList<Field> fields = lERFields.get(physicalName);
			int x = (int)(position.x / xRate + scrollX);
			int y = (int)(position.y / yRate + scrollY);
			int keys = 0;
			for (Field field : fields) {
				// キー値
				if (field.key == null || field.key.length() == 0) {
					continue;
				}
				y+=16;
				keys++;

				if (searchingString != null && field.logicalName.indexOf(searchingString) >= 0) {
					grp.setColor(Color.red);
					if (foundX == -1 && foundY == -1) {
						foundX = (int)(position.x / xRate + scrollX);
						foundY = (int)(position.y / yRate + scrollY);
					}
				} else {
					grp.setColor(Color.black);
				}
				if (((!dragging || !parent.miUseDraftMode.getState()) || !parent.miUseDraftMode.getState())) {
					grp.drawString(field.logicalName, x, y);
				}
				if ((level == 3 || level == 4) && ((!dragging || !parent.miUseDraftMode.getState()) || !parent.miUseDraftMode.getState())) {
					if (searchingString != null && field.physicalName.indexOf(searchingString) >= 0) {
						if (foundX == -1 && foundY == -1) {
							foundX = (int)(position.x / xRate + scrollX);
							foundY = (int)(position.y / yRate + scrollY);
						}
						grp.setColor(Color.red);
					} else {
						grp.setColor(new Color(128, 0, 128));
					}
					grp.drawString(field.physicalName, x + entity.logicalNameWidth + 4, y);
				}
				grp.setColor(new Color(128, 64, 0));
				if (level == 2 && ((!dragging || !parent.miUseDraftMode.getState()) || !parent.miUseDraftMode.getState())) {
					grp.drawString(field.type, x + entity.logicalNameWidth + 4, y);
				}
				if (level == 4 && ((!dragging || !parent.miUseDraftMode.getState()) || !parent.miUseDraftMode.getState())) {
					grp.drawString(field.type, x + entity.logicalNameWidth + entity.physicalNameWidth + 4, y);
				}

				grp.setColor(Color.black);
				if ((field.notNull != null && field.notNull.length() > 0) && (!dragging || !parent.miUseDraftMode.getState())) {
					grp.drawRect(x - 4, y - 7, 2, 4);
				}
			}

			if (level > 0) {
				for (Field field : fields) {
				// 非キー値
					if (field.key != null && field.key.length() > 0) {
						continue;
					}
					y+=16;

					if (searchingString != null && field.logicalName.indexOf(searchingString) >= 0) {
						grp.setColor(Color.red);
						if (foundX == -1 && foundY == -1) {
							foundX = (int)(position.x / xRate + scrollX);
							foundY = (int)(position.y / yRate + scrollY);
						}
					} else {
						grp.setColor(Color.black);
					}
					if ((!dragging || !parent.miUseDraftMode.getState())) {
						grp.drawString(field.logicalName, x, y);
					}
					if ((level == 3 || level == 4) && (!dragging || !parent.miUseDraftMode.getState())) {
						if (searchingString != null && field.physicalName.indexOf(searchingString) >= 0) {
							if (foundX == -1 && foundY == -1) {
								foundX = (int)(position.x / xRate + scrollX);
								foundY = (int)(position.y / yRate + scrollY);
							}
							grp.setColor(Color.red);
						} else {
							grp.setColor(new Color(128, 0, 128));
						}
						grp.drawString(field.physicalName, x + entity.logicalNameWidth + 4, y);
					}
					grp.setColor(new Color(128, 64, 0));
					if (level == 2 && (!dragging || !parent.miUseDraftMode.getState())) {
						grp.drawString(field.type, x + entity.logicalNameWidth + 4, y);
					}
					if (level == 4 && (!dragging || !parent.miUseDraftMode.getState())) {
						grp.drawString(field.type, x + entity.logicalNameWidth + entity.physicalNameWidth + 4, y);
					}

					grp.setColor(Color.black);
					if ((field.notNull != null && field.notNull.length() > 0) && (!dragging || !parent.miUseDraftMode.getState())) {
						grp.drawRect(x - 4, y - 7, 2, 4);
					}
				}
			}
			if (keys > 0 && level > 0) {
				grp.setColor(Color.black);
				grp.drawLine(entity.tmpLeft + 2, entity.tmpTop + 16 * keys + 2, entity.tmpLeft + entity.tmpWidth - 3, entity.tmpTop + 16 * keys + 2);
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

			Point left1 = new Point(entity1.tmpLeft , 
				(entity1.tmpTop + entity1.tmpTop + entity1.tmpHeight) / 2);
			Point top1 = new Point((entity1.tmpLeft  + entity1.tmpLeft + entity1.tmpWidth) / 2 ,
				entity1.tmpTop);
			Point right1 = new Point(entity1.tmpLeft + entity1.tmpWidth ,
				(entity1.tmpTop + entity1.tmpTop + entity1.tmpHeight) / 2);
			Point bottom1 = new Point((entity1.tmpLeft + entity1.tmpLeft + entity1.tmpWidth) / 2 , 
				entity1.tmpTop + entity1.tmpHeight);

			Point left2 = new Point(entity2.tmpLeft ,
				(entity2.tmpTop + entity2.tmpTop + entity2.tmpHeight) / 2);
			Point top2 = new Point((entity2.tmpLeft + entity2.tmpLeft + entity2.tmpWidth) / 2 ,
				entity2.tmpTop);
			Point right2 = new Point(entity2.tmpLeft+ entity2.tmpWidth ,
				(entity2.tmpTop + entity2.tmpTop + entity2.tmpHeight) / 2);
			Point bottom2 = new Point((entity2.tmpLeft + entity2.tmpLeft + entity2.tmpWidth) / 2 ,
				 entity2.tmpTop + entity2.tmpHeight);

			int minDist = (int)Math.min(left1.manhattanDistanceTo(right2, Point.SITUATION_LEFT_TO_RIGHT),
				Math.min(top1.manhattanDistanceTo(bottom2, Point.SITUATION_TOP_TO_BOTTOM),
					Math.min(right1.manhattanDistanceTo(left2, Point.SITUATION_RIGHT_TO_LEFT),
						bottom1.manhattanDistanceTo(top2, Point.SITUATION_BOTTOM_TO_TOP))));

			int rab1 = relation.bar1;
			int rab2 = relation.bar2;
			int rab3 = relation.bar3;

			int bar1 = 1000 - rab1;
			int bar2 = 1000 - rab2;
			int bar3 = 1000 - rab3;

			int situation = Point.SITUATION_NONE;

			grp.setColor(Color.black);

			if (entity1.physicalName.equals(entity2.physicalName)) {
				grp.drawLine(right1.x, (top1.y * bar1 + bottom1.y * rab1) / 1000, 
					right1.x + 16, (top1.y * bar1 + bottom1.y * rab1) / 1000);
				grp.drawLine(right1.x + 16, (top1.y * bar1 + bottom1.y * rab1) / 1000, 
					right1.x + 16, (top1.y * bar3 + bottom1.y * rab3) / 1000);
				grp.drawLine(right2.x + 16, (top2.y * bar3 + bottom2.y * rab3) / 1000, 
					right2.x, (top2.y * bar3 + bottom2.y * rab3) / 1000);
				situation = Point.SITUATION_SELF_TO_SELF;
			} else if (minDist == left1.manhattanDistanceTo(right2, Point.SITUATION_LEFT_TO_RIGHT)) {
				left1 = new Point(entity1.tmpLeft , 
					(entity1.tmpTop * bar1 + (entity1.tmpTop + entity1.tmpHeight) * rab1) / 1000);
				right2 = new Point(entity2.tmpLeft + entity2.tmpWidth ,
					(entity2.tmpTop * bar3 + (entity2.tmpTop + entity2.tmpHeight) * rab3) / 1000);

				grp.drawLine(left1.x, left1.y, 
					(left1.x * bar2 + right2.x * rab2) / 1000, left1.y);
				grp.drawLine((left1.x * bar2 + right2.x * rab2) / 1000, left1.y, 
					(left1.x * bar2 + right2.x * rab2) / 1000, right2.y);
				grp.drawLine((left1.x * bar2 + right2.x * rab2) / 1000, right2.y, 
					right2.x, right2.y);
				situation = Point.SITUATION_LEFT_TO_RIGHT;
			} else if (minDist == top1.manhattanDistanceTo(bottom2, Point.SITUATION_TOP_TO_BOTTOM))  {
				top1 = new Point((entity1.tmpLeft * bar1 + (entity1.tmpLeft + entity1.tmpWidth) * rab1) / 1000 ,
					entity1.tmpTop);
				bottom2 = new Point((entity2.tmpLeft * bar3 + (entity2.tmpLeft + entity2.tmpWidth) * rab3) / 1000 ,
					entity2.tmpTop + entity2.tmpHeight);

				grp.drawLine(top1.x, top1.y, 
					top1.x, (top1.y * bar2 + bottom2.y * rab2) / 1000);
				grp.drawLine(top1.x, (top1.y * bar2 + bottom2.y * rab2) / 1000, 
					bottom2.x, (top1.y * bar2 + bottom2.y * rab2) / 1000);
				grp.drawLine(bottom2.x, (top1.y * bar2 + bottom2.y * rab2) / 1000, 
					bottom2.x, bottom2.y);
				situation = Point.SITUATION_TOP_TO_BOTTOM;
			} else if (minDist == right1.manhattanDistanceTo(left2, Point.SITUATION_RIGHT_TO_LEFT))  {
				right1 = new Point(entity1.tmpLeft + entity1.tmpWidth ,
					(entity1.tmpTop * bar1 + (entity1.tmpTop + entity1.tmpHeight) * rab1) / 1000);
				left2 = new Point(entity2.tmpLeft ,
					(entity2.tmpTop * bar3 + (entity2.tmpTop + entity2.tmpHeight) * rab3) / 1000);

				grp.drawLine(right1.x, right1.y, 
					(right1.x * bar2 + left2.x * rab2) / 1000, right1.y);
				grp.drawLine((right1.x * bar2 + left2.x * rab2) / 1000, right1.y, 
					(right1.x * bar2 +left2.x * rab2) / 1000, left2.y);
				grp.drawLine((right1.x * bar2 + left2.x * rab2) / 1000, left2.y,
					left2.x, left2.y);
				situation = Point.SITUATION_RIGHT_TO_LEFT;
			} else {
				bottom1 = new Point((entity1.tmpLeft * bar1 + (entity1.tmpLeft + entity1.tmpWidth) * rab1) / 1000 , 
					entity1.tmpTop + entity1.tmpHeight);
				top2 = new Point((entity2.tmpLeft * bar3 + (entity2.tmpLeft + entity2.tmpWidth) * rab3) / 1000 ,
					entity2.tmpTop);

				grp.drawLine(bottom1.x, bottom1.y, 
					bottom1.x, (bottom1.y * bar2 + top2.y * rab2) / 1000);
				grp.drawLine(bottom1.x, (bottom1.y * bar2 + top2.y * rab2) / 1000, 
					top2.x, (bottom1.y * bar2 + top2.y * rab2) / 1000);
				grp.drawLine(top2.x, (bottom1.y * bar2 + top2.y * rab2) / 1000, 
					top2.x, top2.y);
				situation = Point.SITUATION_BOTTOM_TO_TOP;
			}

			String relationType1 = "";
			if (relation.relationType1 == 1) {
				relationType1 = "0...1";
			} else if (relation.relationType1 == 2) {
				relationType1 = "1";
			} else if (relation.relationType1 == 3) {
				relationType1 = "0...N";
			} else if (relation.relationType1 == 4) {
				relationType1 = "1...N";
			} else if (relation.relationType1 == 5) {
				relationType1 = "N";
			}

			if ((!dragging || !parent.miUseDraftMode.getState())) {
				if (situation == Point.SITUATION_LEFT_TO_RIGHT) {
					grp.drawString(relationType1, left1.x - 4 - metrics.getStringBounds(relationType1, grp).getBounds().width, 
						left1.y - 4);
				} else if (situation == Point.SITUATION_TOP_TO_BOTTOM) {
					grp.drawString(relationType1, top1.x + 4, 
						top1.y - 16);
				} else if (situation == Point.SITUATION_RIGHT_TO_LEFT) {
					grp.drawString(relationType1, right1.x + 4, 
						right1.y - 4);
				} else if (situation == Point.SITUATION_BOTTOM_TO_TOP) {
					grp.drawString(relationType1, bottom1.x + 4, 
						bottom1.y + 16);
				} else {
					grp.drawString(relationType1, right1.x + 4, 
						(top1.y * bar1 + bottom1.y * rab1) / 1000 - 4);
				}
			}

			String relationType2 = "";
			if (relation.relationType2 == 1) {
				relationType2 = "0...1";
			} else if (relation.relationType2 == 2) {
				relationType2 = "1";
			} else if (relation.relationType2 == 3) {
				relationType2 = "0...N";
			} else if (relation.relationType2 == 4) {
				relationType2 = "1...N";
			} else if (relation.relationType2 == 5) {
				relationType2 = "N";
			}

			if ((!dragging || !parent.miUseDraftMode.getState())) {
				if (situation == Point.SITUATION_LEFT_TO_RIGHT) {
					grp.drawString(relationType2, right2.x + 4, 
						right2.y - 4);
				} else if (situation == Point.SITUATION_TOP_TO_BOTTOM) {
					grp.drawString(relationType2, bottom2.x + 4, 
						bottom2.y + 16);
				} else if (situation == Point.SITUATION_RIGHT_TO_LEFT) {
					grp.drawString(relationType2, left2.x - 4 - metrics.getStringBounds(relationType2, grp).getBounds().width, 
						left2.y - 4);
				} else if (situation == Point.SITUATION_BOTTOM_TO_TOP) {
					grp.drawString(relationType2, top2.x + 4, 
						top2.y - 16);
				} else {
					grp.drawString(relationType2, right2.x + 4, 
						(top2.y * bar3 + bottom2.y * rab3) / 1000 - 4);
				}
			}
		}

		for (Comment comment : lComments) {
			if (currentPage.equals(comment.page)) {
				int left = (int)(comment.left / xRate + scrollX);
				int top = (int)(comment.top / yRate + scrollY);
				int width = (int)(comment.width / xRate);
				int height = (int)(comment.height / yRate);
				grp.setColor(Color.white);
				grp.fillRect(left, top, width, height);
				grp.setColor(Color.black);
				grp.drawRect(left, top, width, height);
				if (searchingString != null && comment.comment.indexOf(searchingString) >= 0) {
					grp.setColor(Color.red);
					if (foundX == -1 && foundY == -1) {
						foundX = (int)(left + 8);
						foundY = (int)(top + 16);
					}
				} else {
					grp.setColor(Color.black);
				}
				grp.drawString(comment.comment, left + 8, top + 16);

				if (minX > comment.left) {
					minX = comment.left;
				}

				if (minY > comment.top) {
					minY = comment.top;
				}
			}
		}

		maxWidth += minX;
		maxHeight += minY;

		if (parent.miGrid.getState()) {
			grp.setColor(new Color(128, 128, 128));
			grp.drawRect(scrollX, scrollY, maxWidth, maxHeight);
		}

		if (dragging) {
			grp.setColor(Color.red);
			grp.drawRect(scrollX, scrollY, maxWidth, maxHeight);

			grp.drawString("" + (-scrollX) + "," + (-scrollY), 2, 16);
		}

		g.drawImage(img, 0, 0, this);

		if (showSearchResultFlag && foundX != -1 && foundY != -1) {
			scrollX = - foundX + scrollX + 64;
			scrollY = - foundY + scrollY + 64;
			showSearchResultFlag = false;
			repaint(); // あんまりよくない
		}

		if (maxWidth != oldMaxWidth || maxHeight != oldMaxHeight) {
			repaint(); // あんまりよくない！
		}

		if (drawForCopying) {
			this.imgForCopying = img;
		}
		drawForCopying = false;
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
		A4ERImport.importA5ER(path, this);
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (System.currentTimeMillis() - lastClickedTime < 1000) {
			if (scrollX > 0) {
				scrollX = 0;
			} else if (scrollX < -(maxWidth - this.getWidth())) {
				scrollX = -(int)(maxWidth - this.getWidth());
			}
			if (scrollY > 0) {
				scrollY = 0;
			} else if (scrollY < -(maxHeight - this.getHeight())) {
				scrollY = -(int)(maxHeight - this.getHeight());
			}
			repaint();
		}
		lastClickedTime = System.currentTimeMillis();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			this.parent.mPopupMenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			return;
		}
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
			if (arg0.getButton() == MouseEvent.BUTTON2 || arg0.isAltDown()) {
				scrollX = originalX + (arg0.getX() - clickedX) * 2;
				scrollY = originalY + (arg0.getY() - clickedY) * 2;
			} else {
				scrollX = originalX + (arg0.getX() - clickedX);
				scrollY = originalY + (arg0.getY() - clickedY);
			}
			if (arg0.isShiftDown()) {
				if (Math.abs(arg0.getX() - clickedX) > Math.abs(arg0.getY() - clickedY)) {
					scrollY = originalY;
				} else {
					scrollX = originalX;
				}
			}
			if (arg0.isMetaDown() || arg0.isControlDown()) {
				if (scrollX > 0) {
					scrollX = 0;
				} else if (scrollX < -(maxWidth - this.getWidth())) {
					scrollX = -(int)(maxWidth - this.getWidth());
				}
				if (scrollY > 0) {
					scrollY = 0;
				} else if (scrollY < -(maxHeight - this.getHeight())) {
					scrollY = -(int)(maxHeight - this.getHeight());
				}
			}
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (arg0.isShiftDown()) {
			scrollX += arg0.getWheelRotation();
		} else {
			scrollY += arg0.getWheelRotation();
		}
		selectedIndex = 0;
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent arg0){
		int keyCode = arg0.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_UP:
			if (arg0.isAltDown()) {
				scrollY += 32;
			} else if (arg0.isMetaDown() || arg0.isControlDown()) {
				scrollY = 0;
			} else {
				scrollY += 8;
			}
			repaint();
			break;
			case KeyEvent.VK_DOWN:
			if (arg0.isAltDown()) {
				scrollY -= 32;
			} else if (arg0.isMetaDown() || arg0.isControlDown()) {
				scrollY = -(int)(maxHeight - this.getHeight());
			} else {
				scrollY -= 8;
			}
			repaint();
			break;
			case KeyEvent.VK_LEFT:
			if (arg0.isAltDown()) {
				scrollX += 32;
			} else if (arg0.isMetaDown() || arg0.isControlDown()) {
				scrollX = 0;
			} else {
				scrollX += 8;
			}
			repaint();
			break;
			case KeyEvent.VK_RIGHT:
			if (arg0.isAltDown()) {
				scrollX -= 32;
			} else if (arg0.isMetaDown() || arg0.isControlDown()) {
				scrollX = -(int)(maxWidth - this.getWidth());
			} else {
				scrollX -= 8;
			}
			repaint();
			break;
			case KeyEvent.VK_BACK_SPACE :
			case KeyEvent.VK_HOME :
			scrollX = 0;
			scrollY = 0;
			repaint();
			break;
			case KeyEvent.VK_END :
			scrollX = -(int)(maxWidth - this.getWidth());
			scrollY = -(int)(maxHeight - this.getHeight());
			repaint();
			break;
			default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0){

	}

	@Override
	public void keyTyped(KeyEvent arg0){
		
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
			entities[selectedIndex]);
		
		if (result != null && result.length() > 0) {
			for (Entity entity : lEREntities) {
				if (entity.logicalName.equals(result)) {
					Position position = entity.positions.get(0);
					parent.cbPage.setSelectedItem(position.page);
					scrollX = (int)(-position.x / xRate + 64);
					scrollY = (int)(-position.y / yRate + 64);
					this.selectedIndex = entitiesList.indexOf(result);
					repaint();
					break;
				}
			}
		}

		return 0;
	}


	public int searchStringAndShowItInRed() {
		if (searchingString == null) {
			searchingString = "";
		}
		searchingString = JOptionPane.showInputDialog("Input string to search...", searchingString);
		if (searchingString != null && searchingString.equals("")) {
			searchingString = null;
		}
		showSearchResultFlag = true;
		repaint();

		return 0;
	}

	public int cancelSearching() {
		searchingString = null;
		repaint();

		return 0;
	}

	public void copyPageAsImage() {
		drawForCopying = true;
		int originalScrollX = scrollX;
		int originalScrollY = scrollY;
		scrollX = 0;
		scrollY = 0;
		repaint();

		Thread copyThread = new Thread(() -> {
			while (drawForCopying) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {

				}
			}

			Toolkit kit = Toolkit.getDefaultToolkit();
			Clipboard clip = kit.getSystemClipboard();

			ImageSelection imageSelection = new ImageSelection(imgForCopying);
			clip.setContents(imageSelection, imageSelection);

			scrollX = originalScrollX;
			scrollY = originalScrollY;
			repaint();

			JOptionPane.showMessageDialog(parent,"Copied to clipboard.", 
				"Entities List", JOptionPane.INFORMATION_MESSAGE);
		});
		copyThread.start();
	}

	public class ImageSelection implements Transferable, ClipboardOwner {

		protected Image data;

		public ImageSelection(Image image) {
			this.data = image;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (DataFlavor.imageFlavor.equals(flavor)) {
				return data;
			}
			throw new UnsupportedFlavorException(flavor);
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			this.data = null;
		}
	}

}