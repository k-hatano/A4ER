import java.util.*;

public class Entity {
	public String physicalName;
	public String logicalName;
	public ArrayList<Field> fields;
	public int left;
	public int top;
	public String page;

	public int physicalNameWidth;
	public int logicalNameWidth;

	public Entity() {
		super();
		fields = new ArrayList<Field>();
	}
}