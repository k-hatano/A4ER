public class Position {
	public String page;
	public int x;
	public int y;

	public Position() {
		super();
	}

	public Position(Position p) {
		super();
		this.page = p.page;
		this.x = p.x;
		this.y = p.y;
	}
}