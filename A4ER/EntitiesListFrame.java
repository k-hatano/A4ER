import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

class EntitiesListFrame extends JFrame implements ActionListener {
	A4ER parent;
	JTable tEntitiesList;
	DefaultTableModel dtmTableModel;
	JScrollPane spScrollPane;
	Button bSelect;

	String[][] data = {{"", "", ""}};
	String[] columns = {"Logical Name", "Physical Name", "Tag"};

	EntitiesListFrame(A4ER a4er) {
		super();
		parent = a4er;
    	setLayout(new BorderLayout());

		setSize(640, 480);
		setLocation(32, 128);
		setTitle("Entities List");

		bSelect = new Button("Select");
		add(bSelect, BorderLayout.SOUTH);
		bSelect.addActionListener(this);

		dtmTableModel = new DefaultTableModel(data, columns);
		tEntitiesList = new JTable(dtmTableModel);
		add(tEntitiesList.getTableHeader(), BorderLayout.NORTH);
    	spScrollPane = new JScrollPane(tEntitiesList);
		add(spScrollPane, BorderLayout.CENTER);
	}

	public void receiveEntities(ArrayList<Entity> entities) {
		ArrayList<String[]> newDataList = new ArrayList<String[]>();
		int i;

		int rows = dtmTableModel.getRowCount(); 
		for (i = rows - 1; i >=0; i--)
		{
		   dtmTableModel.removeRow(i); 
		}

		i = 0;
		for (Entity entity : entities) {
			String[] datum = {entity.logicalName, entity.physicalName, entity.tag};
			newDataList.add(datum);
			i++;
		}
		Collections.sort(newDataList, (String[] a, String b[]) -> {
			return a[1].compareTo(b[1]);
		});

		for (String[] datum : newDataList) {
			dtmTableModel.addRow(datum);
		}

		data = newDataList.toArray(new String[0][0]);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == bSelect) {
			int selectedIndex = tEntitiesList.getSelectedRow();
			if (selectedIndex >= 0) {
				parent.a4erCanvas.showEntity(data[selectedIndex][1]);
			}
		}
	}
}
