import java.util.*;

public class Entity {
	public String physicalName;
	public String logicalName;
	public ArrayList<Position> positions;

	public int physicalNameWidth;
	public int logicalNameWidth;
	public int typeWidth;

	public Entity() {
		super();
		positions = new ArrayList<Position>();
	}

	public Position positionInPage(String page){
		for (Position position : positions) {
			if (position.page.equals(page)) {
				return position;
			}
		}
		return null;
	}
}