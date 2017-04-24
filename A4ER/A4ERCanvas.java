import java.awt.*;

public class A4ERCanvas extends Canvas {

	public void paint(final Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();

		Image img = createImage(w,h);
		Graphics2D grp = (Graphics2D)(img.getGraphics());

		grp.setColor(Color.white);
		grp.fillRect(0, 0, w, h);

		g.drawImage(img, 0, 0, this);
	}

}