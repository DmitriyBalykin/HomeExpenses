package core;

import gui.DataGrid;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class DataRow extends JPanel{
	boolean selected = false;
	private long rowId;
	DataGrid dGrid = null;
	Color myColor;
	
	private DataRow(){
		setLayout(new GridLayout(1, 6));
		addMouseListener(new MouseClickListener());
	}
	public DataRow(DataGrid dg, DataTuple dt, Color clr){
		this();
		dGrid = dg;
		myColor = clr;
		setBackground(clr);
		
		rowId = dt.id;
		String date = Integer.toString(dt.date);
		date = date.substring(0, 4) + "-"+date.substring(4, 6) + "-" +date.substring(6, 8);
		add(new JLabel(date));
		add(new JLabel(dt.prodType));
		add(new JLabel(dt.prodName));
		add(new JLabel(Float.toString(dt.prodNum)));
		add(new JLabel(Float.toString(dt.prodCost)));
		add(new JLabel(Float.toString(dt.prodTotCost)));
	}
	public DataRow(String... header){
		this();
		for(String s:header)
			add(new JLabel(s));
	}
	private void setBackColor(){
		if(!selected)
		{
			selected = true;
			setBackground(new Color(201,218,235));
		}
		else{
			selected = false;
			setBackground(myColor);
		}
		repaint();
	}
	class MouseClickListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				setBackColor();
				firePropertyChange("RowID", new Long(selected ? 1 : -1), new Long(rowId));
			}
			else{
				if(dGrid != null)
					dGrid.dispatchEvent(e);	//dispatch rightClick to parent datagrid
			}
		}
	}

}