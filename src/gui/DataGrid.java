package gui;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import core.DataRow;
import core.DataTuple;

public class DataGrid extends JPanel{
	private final static String mTextNoColors = "Скрыть цвета цен",
			mTextColors = "Показать цвета цен";
	private String[] colHeaders = {"Дата", "Тип", "Наименование", "Количество", "Цена за единицу", "Общая цена"};
	private JPopupMenu pMenu = new JPopupMenu();
	private JMenuItem mItem = new JMenuItem(mTextColors),
			remItem = new JMenuItem("Удалить выбранные записи");

	private static boolean inColors = false;
	CostPainter cp;
	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	List<Long> selectedRows = new LinkedList<Long>();
	
	public DataGrid() {
		addMouseListener(new MenuShowMouseListener());
		mItem.addActionListener(new ColorSetActionListener());
		remItem.addActionListener(new RemoveActionListener());
		pMenu.add(mItem);
		pMenu.add(remItem);
		add(pMenu);
	}
	
	public void addRows(List<DataTuple> dta){
		cp = new CostPainter(dta);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		fillRows();
		
	}
	private void fillRows(){
		DataRow dr;
		removeAll();
		
		if(cp.getRowList().size() == 0) {
			add(new DataRow("Нет данных"));
			return;
		}
		dr = new DataRow(colHeaders);
		c.gridy = GridBagConstraints.RELATIVE;
		gbl.setConstraints(dr, c);
		add(dr);
		
		for(DataTuple dt:cp.getRowList()){
			dr = new DataRow(this, dt, cp.chooseColor(dt.prodTotCost));
			dr.addPropertyChangeListener("RowID", new IdChangeListener());
			c.gridy = GridBagConstraints.RELATIVE;
			gbl.setConstraints(dr, c);
			add(dr);
		}
		dr = new DataRow("", "", "Общее количество:", String.format("%.2f", cp.totalNumber), "Общая стоимость:", String.format("%.2f", cp.totalCost));
		c.gridy = GridBagConstraints.RELATIVE;
		gbl.setConstraints(dr, c);
		add(dr);
	}
	public String getHTML(){
		if(cp == null)
			return "";
		StringBuilder sb = new StringBuilder("<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=cp1251\">\n<TABLE BORDER>");
		
		
		Iterator<DataTuple> rowIt = cp.getRowList().iterator();
		Iterator<Color> colIt = cp.getColorList().iterator();
		
		//forming header
		sb.append("<TR BGCOLOR =\"#CCCCCC\">");
		for(String s:colHeaders)
			sb.append("<TD>").append(s).append("</TD>");
		sb.append("</TR>\n");
		
		while(rowIt.hasNext()){
			Color col = new Color(0xFFFFFF);
			if(colIt.hasNext())
				col = colIt.next();
			
			sb.append("<TR BGCOLOR=\"#"+Integer.toHexString(col.getRGB()&0xFFFFFF)+"\">").append(rowIt.next().toHTMLRowString()).append("</TR>").append("\n");
		}
		sb.append("<TR BGCOLOR=\"#CCCCCC\"><TD></TD><TD></TD><TD>Общее количество:</TD><TD>"+String.format("%.2f", cp.totalNumber)+"</TD><TD>Общая стоимость:</TD><TD>"+String.format("%.2f", cp.totalCost)+"</TD></TR>");
		sb.append("</TABLE></html>");
		
		
		
		
		return sb.toString();
	}
	private class MenuShowMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON3)
				pMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}
	private class ColorSetActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(inColors){
				mItem.setText(mTextColors);
				inColors = false;
			}
			else{
				mItem.setText(mTextNoColors);
				inColors = true;
			}
			fillRows();
			revalidate();
			repaint();
		}
	}
	class IdChangeListener implements PropertyChangeListener{

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		
			long actionValue =  (Long) evt.getOldValue();
			Long value = (Long) evt.getNewValue();
			if(actionValue == 1){
				selectedRows.add(value);
				System.out.println("Selected: "+value);
			}else if(actionValue == -1){
				selectedRows.remove(value);
				System.out.println("Unselected: "+value);
			}
			System.out.println(selectedRows);
		}
		
	}
	class RemoveActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			firePropertyChange("DeleteRows", null, selectedRows);
		}
		
	}
	
	static class CostPainter{
		//dedicated object which deal with statistic works and color choosing
		private final int topLevel = 120;
		private final Color TOPCOLOR = new Color(255,topLevel,topLevel);
		private final Color LOWCOLOR = Color.white;
		private List<DataTuple> dtList = null;
		private List<Color> clList = null;
		public float min, max, totalCost, totalNumber;
		public CostPainter(List<DataTuple> dta) {
			dtList = dta;
			clList = new ArrayList<Color>(dta.size());
			
			if(dtList.size() > 0)
				min = dtList.get(0).prodTotCost;
			for(DataTuple dt:dtList){
				if(min > dt.prodTotCost) min = dt.prodTotCost;
				if(max < dt.prodTotCost) max = dt.prodTotCost;
				totalNumber += dt.prodNum;
				totalCost += dt.prodTotCost;
			}
		}
		public Color chooseColor(float cost){
			if(!inColors)
				return Color.white;
			
			int colorLevel = (int) (255 - (cost - min)*(255 - topLevel)/(max - min));
			
			Color color = new Color(255, colorLevel, colorLevel);
			clList.add(color);
			
			return color;
		}
		public List<DataTuple> getRowList(){
			return dtList;
		}
		public List<Color> getColorList(){
			return clList;
		}

	}
}