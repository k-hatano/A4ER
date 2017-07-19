import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;

public class A4ERImport {

	public static boolean importA5ER(String path, A4ERCanvas canvas) {
		File file = new File(path);
		canvas.parent.setTitle(file.getName());
		try {
			canvas.lEREntities = new ArrayList<Entity>();
			canvas.lERFields = new HashMap<String, ArrayList<Field>>();
			canvas.lERReleations = new ArrayList<Relation>();
			canvas.lPages = new ArrayList<String>();

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
					String page = m1.group(1).toUpperCase();

					Position position = new Position();
					position.x = Integer.parseInt(m1.group(2));
					position.y = Integer.parseInt(m1.group(3));
					position.page = page;
					entity.positions.add(position);

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
					canvas.lEREntities.add(0, entity);
					canvas.lERFields.put(entity.physicalName ,fields);
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
					canvas.lERReleations.add(0, relation);
					entity = new Entity();
					fields = new ArrayList<Field>();
					field = new Field();
					relation = new Relation();
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

				final Pattern p10 = Pattern.compile("^PageInfo=\"([^\"]*)\"");
				Matcher m10 = p10.matcher(str);
				if (m10.find()) {
					canvas.lPages.add(0, m10.group(1).toUpperCase());
					continue;
				}

				final Pattern p11 = Pattern.compile("^Bar1=(\\d+)");
				Matcher m11 = p11.matcher(str);
				if (m11.find()) {
					relation.bar1 = Integer.parseInt(m11.group(1));
					continue;
				}

				final Pattern p12 = Pattern.compile("^Bar2=(\\d+)");
				Matcher m12 = p12.matcher(str);
				if (m12.find()) {
					relation.bar2 = Integer.parseInt(m12.group(1));
					continue;
				}

				final Pattern p13 = Pattern.compile("^Bar3=(\\d+)");
				Matcher m13 = p13.matcher(str);
				if (m13.find()) {
					relation.bar3 = Integer.parseInt(m13.group(1));
					continue;
				}

				final Pattern p14 = Pattern.compile("^RelationType1=(\\d+)");
				Matcher m14 = p14.matcher(str);
				if (m14.find()) {
					relation.relationType1 = Integer.parseInt(m14.group(1));
					continue;
				}

				final Pattern p15 = Pattern.compile("^RelationType2=(\\d+)");
				Matcher m15 = p15.matcher(str);
				if (m15.find()) {
					relation.relationType2 = Integer.parseInt(m15.group(1));
					continue;
				}
				
			}

			canvas.parent.cbPage.removeAllItems();
			for (String page : canvas.lPages) {
				canvas.parent.cbPage.addItem(page);
			}
			canvas.parent.cbLevel.setSelectedIndex(level);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}