package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import Utils.BrowserControl;
import core.DataAccess;
import core.DataTuple;
import core.HomeEXConfig;
import exception.PasswordFormatException;
import static Utils.Log.log;
import static Utils.SwingConsole.run;


public class HomeExGUI extends JFrame {
	private static final int WIDTH = 1100;
	private static final int MINHEIGHT = 180;
	private static final int MAXHEIGHT = 880;
	private static final Dimension screenD = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int SCREENHEIGHT = (int) screenD.getHeight();
	private static final int SCREENWIDTH = (int) screenD.getWidth();
	private static List<String> nameList = new ArrayList<String>();
	
	String[] types = {"���","���.������","����������","�����������","������","����������","��������","�����. ������", "������"};
	
	JPanel
		currentPanel = new JPanel(),
		loadDataPanel = new JPanel(),
		readDataPanel = new JPanel(),
		selectorsPanel = new JPanel(new GridLayout(6, 3)),
		statusPanel = new JPanel();
	JLabel
		dateLabel = new JLabel("      ����      "),
		typeLabel = new JLabel("��� �������"),
		prodNameLabel = new JLabel("�������� �������"),
		prodNumbLabel = new JLabel("����������"),
		prodOneCostLabel = new JLabel("���� �� �������"),
		prodTotCostLabel = new JLabel("����� ����"),
		statusLabel = new JLabel();
	JTextField
		dateField = new JTextField(8),
		prodNameField = new JTextField(25),
		prodNameSelField = new JTextField(10),
		prodNumbField = new JTextField(3),
		prodOneCostField = new JTextField(5),
		prodTotCostField = new JTextField(5),
		prodCostStartField = new JTextField(5),
		prodCostEndField = new JTextField(5),
		dateStartField = new JTextField(),
		dateEndField = new JTextField();
	
	JComboBox
		typeCombo = new JComboBox(types),
		selTypeCombo = new JComboBox(types);
	JButton
		sendButton = new JButton("��������� ������"),
		getDataButton = new JButton("�������� ������"),
		dataToHTMLButton = new JButton("������� ������ � HTML");
	JToggleButton
		calcQuantButton = new JToggleButton("�������"),
		calcOneButton = new JToggleButton("�������");
	JCheckBox
		selDateButton = new JCheckBox("�� ����"),
		selTypeButton = new JCheckBox("�� ���� �������"),
		selNameButton = new JCheckBox("�� ����� �������"),
		selCostButton = new JCheckBox("�� ���������");
	JMenuItem
		loadDataItem = new JMenuItem("��������� ������"),
		readDataItem = new JMenuItem("������� ������"),
		dbToFileItem = new JMenuItem("����������� ������ � ����"),
		setMenuItem = new JMenuItem("��������� �������� ������");
	JMenu
		menu = new JMenu("������� ����"),
		menuSettings = new JMenu("���������");
	JMenuBar menuBar = new JMenuBar();
	
	KL keyListener = new KL(KL.FLOAT_TYPE);
	
	HomeEXConfig config;
	DataAccess da;
	DataGrid dGrid = new DataGrid();
	JScrollPane scrollDataPane = new JScrollPane();
	
	HomeExGUI(){
		config = loadConfigFromFile();
		da = new DataAccess(config, new MessagesListener());
		
		statusLabel.setText("���������� � ����� ������...");
		//���������� ���������� � �� � ��������� ������
		class BackWorker extends SwingWorker{

			@Override
			protected Object doInBackground() throws Exception {
				da.syncFileToDB();
				return null;
			}
			
		}
		new BackWorker().execute();
		nameList = da.getNamesList();
		
		
		setLayout(new BorderLayout());
		currentPanel.setLayout(new BoxLayout(currentPanel, BoxLayout.Y_AXIS));
		setResizable(false);

		sendButton.addActionListener(new SL());
		calcQuantButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				keyListener.quantityManualChanged();
				calcOneButton.setSelected(false);
//				prodTotCostField.disable();
			}
		});
		calcOneButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				keyListener.oneItemManualChanged();
				calcQuantButton.setSelected(false);
//				prodTotCostField.disable();
			}
		});
		getDataButton.addActionListener(new GL());
		prodNumbField.addKeyListener(keyListener);
		prodOneCostField.addKeyListener(keyListener);
		prodTotCostField.addKeyListener(keyListener);
		
		loadDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadDataPanel.setVisible(true);
				readDataPanel.setVisible(false);
				menu.remove(loadDataItem);
				menu.add(readDataItem);
				setBounds((SCREENWIDTH - WIDTH)/2, (SCREENHEIGHT - MINHEIGHT)/2, WIDTH, MINHEIGHT);
			}
		});
		readDataItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				loadDataPanel.setVisible(false);
				readDataPanel.setVisible(true);
				menu.remove(readDataItem);
				menu.add(loadDataItem);
				setBounds((SCREENWIDTH - WIDTH)/2, (SCREENHEIGHT - MAXHEIGHT)/2, WIDTH, MAXHEIGHT);
			}
		});
		dbToFileItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				statusLabel.setText("� ��������...");
				da.dbToFile();
				statusLabel.setText("������");
			}
		});
		prodNameField.addKeyListener(new NameFiller());
		prodNameSelField.addKeyListener(new NameFiller());
		setMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new DBConfigDialog(HomeExGUI.this, config).setVisible(true);
			}
		});
		
		confLoadPanel();
		confReadPanel();
		menu.add(readDataItem);
		menuSettings.add(setMenuItem);
		
		menuBar.add(menu);
		menuBar.add(menuSettings);
		setJMenuBar(menuBar);
		
		Calendar cal = Calendar.getInstance();
		String year = Integer.toString(cal.get(cal.YEAR));
		String month = Integer.toString(cal.get(cal.MONTH) + 1);
		if(month.length() < 2) month = "0"+month;
		String day = Integer.toString(cal.get(cal.DAY_OF_MONTH));
		if(day.length() < 2) day = "0"+day;
		dateField.setText(year+"-"+month+"-"+day);

		currentPanel.add(loadDataPanel);
		currentPanel.add(readDataPanel);
		statusPanel.add(statusLabel);
		statusPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		add(currentPanel, BorderLayout.CENTER);
		add(statusPanel, BorderLayout.SOUTH);
		

	}
	private void confLoadPanel(){
		loadDataPanel.setMinimumSize(new Dimension(1000, 100));
		loadDataPanel.setMaximumSize(new Dimension(1000, 100));
		loadDataPanel.setSize(new Dimension(1000, 100));
		
		GridBagLayout gbl = new GridBagLayout();
		loadDataPanel.setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		//Row 1: headers
		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(5,10,5,10);
		gbl.setConstraints(dateLabel, c);
		loadDataPanel.add(dateLabel);
		c.gridx = 2;
		c.gridy = 1;
		gbl.setConstraints(typeLabel, c);
		loadDataPanel.add(typeLabel);
		c.gridx = 3;
		c.gridy = 1;
		gbl.setConstraints(prodNameLabel, c);
		loadDataPanel.add(prodNameLabel);
		c.gridx = 4;
		c.gridy = 1;
		gbl.setConstraints(prodNumbLabel, c);
		loadDataPanel.add(prodNumbLabel);
		c.gridx = 5;
		c.gridy = 1;
		gbl.setConstraints(prodOneCostLabel, c);
		loadDataPanel.add(prodOneCostLabel);
		c.gridx = 6;
		c.gridy = 1;
		gbl.setConstraints(prodTotCostLabel, c);
		loadDataPanel.add(prodTotCostLabel);
		//Row two: fields
		c.gridx = 1;
		c.gridy = 2;
		gbl.setConstraints(dateField, c);
		loadDataPanel.add(dateField);
		c.gridx = 2;
		c.gridy = 2;
		gbl.setConstraints(typeCombo, c);
		loadDataPanel.add(typeCombo);
		c.gridx = 3;
		c.gridy = 2;
		gbl.setConstraints(prodNameField, c);
		loadDataPanel.add(prodNameField);
		c.gridx = 4;
		c.gridy = 2;
		gbl.setConstraints(prodNumbField, c);
		loadDataPanel.add(prodNumbField);
		c.gridx = 5;
		c.gridy = 2;
		gbl.setConstraints(prodOneCostField, c);
		loadDataPanel.add(prodOneCostField);
		c.gridx = 6;
		c.gridy = 2;
		gbl.setConstraints(prodTotCostField, c);
		loadDataPanel.add(prodTotCostField);
		c.gridx = 7;
		c.gridy = 2;
		gbl.setConstraints(sendButton, c);
		loadDataPanel.add(sendButton);
		//Row 3: begin: control buttons
		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 3;
		gbl.setConstraints(calcQuantButton, c);
		loadDataPanel.add(calcQuantButton);
		c.gridx = 5;
		c.gridy = 3;
		gbl.setConstraints(calcOneButton, c);
		loadDataPanel.add(calcOneButton);
	}
	private void confReadPanel(){
		
		readDataPanel.setLayout(new BoxLayout(readDataPanel, BoxLayout.PAGE_AXIS));
		selectorsPanel.setMinimumSize(new Dimension(1000, 200));
		selectorsPanel.setMaximumSize(new Dimension(1000, 200));
		selectorsPanel.setSize(new Dimension(1000, 200));
		
		//���� �� ����
		selectorsPanel.add(selDateButton);
		selectorsPanel.add(dateStartField);
		selectorsPanel.add(dateEndField);
		//����� �� �����
		selectorsPanel.add(selNameButton);
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(prodNameSelField);
		//����� �� ����
		selectorsPanel.add(selTypeButton);
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(selTypeCombo);
		//����� �� ����
		selectorsPanel.add(selCostButton);
		selectorsPanel.add(prodCostStartField);
		selectorsPanel.add(prodCostEndField);
		//������ ������� ������
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(getDataButton);
		//
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(dataToHTMLButton);
		
		dataToHTMLButton.addActionListener(new DataToHTMLAction());
		//export to HTML and opening it in a browser implemented only for Windows OS
/*		if(!System.getProperty("os.name").startsWith("WIN_ID"))	
			dataToHTMLButton.setEnabled(false);*/
		selectorsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "������� ������:"));
		readDataPanel.add(selectorsPanel);

		
		scrollDataPane = new JScrollPane(dGrid);
		scrollDataPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "���������� ������:"));
		readDataPanel.add(scrollDataPane);
		dGrid.addPropertyChangeListener("DeleteRows", new DeleteRowsListener());
		dGrid.setVisible(false);
		readDataPanel.setVisible(false);
		
		
		
		dateStartField.addFocusListener(new DateListener());
		dateEndField.addFocusListener(new DateListener());
		prodCostStartField.addFocusListener(new CostListener());
		prodCostEndField.addFocusListener(new CostListener());
		prodNameSelField.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e){
				selNameButton.setSelected(true);
			}
		});
		selTypeCombo.addFocusListener(new FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e){
				selTypeButton.setSelected(true);
			}
		});
	}
	private HomeEXConfig loadConfigFromFile(){
		DataInputStream input = null;
		HomeEXConfig conf = null;
		try {
			conf = new HomeEXConfig();
			input = new DataInputStream(new BufferedInputStream(new FileInputStream(DataAccess.confFile)));
			conf = new HomeEXConfig(input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF(), false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			statusLabel.setText("���� ������������ �� ������");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			statusLabel.setText("������ ��� ������ ����� ������������");
		} catch (PasswordFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(input != null)
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return conf;
	}
	private void saveConfigToFile(HomeEXConfig conf){
		DataOutputStream output = null;
		try {
			output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(DataAccess.confFile)));
			output.writeUTF(conf.hostAddr.isEmpty() ? "localhost" : conf.hostAddr);
			output.writeUTF(conf.hostPort.isEmpty() ? "3306":conf.hostPort);
			output.writeUTF(conf.userName);
			output.writeUTF(conf.userPass);
			output.writeUTF(conf.dbName.isEmpty() ? "homeex" : conf.dbName);
			output.writeUTF(conf.tableName.isEmpty() ? "EX" : conf.tableName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(output != null)
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	private class DeleteRowsListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			da.deleteRows((List<Long>)evt.getNewValue());
		}
	}
	class DataToHTMLAction implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
		    File file = new File("~tempOut.html");
		    String url = "file://"+file.getAbsolutePath();
	    	
	    	
			BufferedWriter buff = null;
			try {
				buff = new BufferedWriter(new FileWriter(file));
				buff.append(dGrid.getHTML());
				buff.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if(buff != null)
					try {
						buff.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
			
			BrowserControl.displayURL(url);
			
			file.deleteOnExit();
			
		}
		
	}
	class SL implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean success = da.addDataTuple(new DataTuple(
							0,
							new Integer(dateField.getText().replaceAll("-", "")),
							typeCombo.getSelectedItem().toString(),
							prodNameField.getText().replaceAll(",", "."),
							new Float(prodNumbField.getText()),
							new Float(prodOneCostField.getText()),
							new Float(prodTotCostField.getText())));
			
			if(success)
				statusLabel.setText("������ \""+prodNameField.getText()+"\" ���������");
			else
				statusLabel.setText("������ ��� ���������� ������");
			
			calcOneButton.setSelected(false);
			calcQuantButton.setSelected(false);
			keyListener.clearManualCalc();
		}
		
	}
	class GL implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			statusLabel.setText("� ��������...");
			
			if(!dGrid.isVisible()) dGrid.setVisible(true);
			
			Map<String, String> qMap = new HashMap<String, String>();

			if(selDateButton.isSelected())
				qMap.put("DATE", dateStartField.getText().replaceAll("-", "")+"--"+dateEndField.getText().replaceAll("-", ""));
			if(selTypeButton.isSelected())
				qMap.put("TYPE", selTypeCombo.getSelectedItem().toString());
			if(selNameButton.isSelected()){
				String s = prodNameSelField.getText();
				qMap.put("NAME", s.contains("*") ? s.replaceAll("\\*", "%") : s);	//% uses in SQL stmt for searcch with mask
			}
			if(selCostButton.isSelected())
				qMap.put("TOTCOST", prodCostStartField.getText()+"--"+prodCostEndField.getText());

			List<DataTuple> dataList =  da.getDataSet(qMap);

			dGrid.addRows(dataList);
			dGrid.revalidate();
			dGrid.repaint();
			
			statusLabel.setText("������");
		}
		
	}
	class KL implements KeyListener{
		String type;
		
		private boolean calcQuantityManual = false;
		private boolean calcOneItemCostManual = false;
		
		public static final String FLOAT_TYPE = "float";
		protected boolean actionPerformed = false;
		char[] digits = "0123456789".toCharArray();
		
		Float quant;
		Float one;
		Float total;
		
		protected boolean notDigit = false;
		
		KL(String t){
			type = t;
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			log("pressed:" + e.getKeyCode());
			if(e.getKeyChar() == ','){
				e.setKeyChar('.');
			}
			checkForConsume(e);
		}
		public void quantityManualChanged() {
			calcQuantityManual = !calcQuantityManual;
			calcOneItemCostManual = false;
		}

		public void oneItemManualChanged() {
			calcOneItemCostManual = !calcOneItemCostManual;
			calcQuantityManual = false;
		}
		
		public void clearManualCalc(){
			calcOneItemCostManual = false;
			calcQuantityManual = false;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			updateFieldsValues();
			boolean allFilled = (one != null) && (quant != null) && (total != null);
			log("is calc quantity manual: "+calcQuantityManual);
			log("is calc one manual: "+calcOneItemCostManual);
			
			//three cases when one of the fields is empty with Manual/Automatic target field detection
			if((one == null || calcOneItemCostManual && allFilled) && quant != null && total != null){
				prodOneCostField.setText(formatValue(total/quant).replace(',', '.'));
				log("set product item cost");
				
			} else if((quant == null || calcQuantityManual && allFilled) && one != null && total != null){
				prodNumbField.setText(formatValue(total/one));
				log("set product items quantity");
				
			} else if(quant != null && one != null){
				prodTotCostField.setText(formatValue(quant*one));
				log("set product total items cost");
			}
		}
		protected boolean checkForConsume(KeyEvent e){
			if(!isDigit(e.getKeyChar())){
				e.consume();
				notDigit = true;
				log("consume");
				return true;
			}
			return false;
		}
		private void updateFieldsValues(){
			quant = toFloat(prodNumbField.getText());
			one = toFloat(prodOneCostField.getText());
			total = toFloat(prodTotCostField.getText());
		}
		private boolean isDigit(char c){
			if(type == FLOAT_TYPE && c == '.'){
				return true;
			}
			for(char dc:digits){
				if(dc == c) return true;
			}
			return false;
		}
		private Float toFloat(String s){
			if(s.isEmpty()){
				return null;
			}
			else{
				return Float.valueOf(s);
			}
		}
		protected String formatValue(Float f){
			return String.format("%.2f", f).replace(',', '.');
		}

		@Override
		public void keyPressed(KeyEvent e) {}
	}
	
	class DateListener extends FocusAdapter{
		@Override
		public void focusLost(FocusEvent e){
			if(dateStartField.getText().isEmpty())
				dateStartField.setText(dateEndField.getText());
			if(dateEndField.getText().isEmpty())
				dateEndField.setText(dateStartField.getText());
			
			if(dateStartField.getText().isEmpty() || dateEndField.getText().isEmpty()) return;
			Integer startDate = new Integer(dateStartField.getText().replaceAll("-", ""));
			Integer endDate = new Integer(dateEndField.getText().replaceAll("-", ""));

			if(startDate > endDate)
			{
				String endToStart = dateEndField.getText();
				dateEndField.setText(dateStartField.getText());
				dateStartField.setText(endToStart);
			}
		}
		@Override
		public void focusGained(FocusEvent e){
			selDateButton.setSelected(true);
		}
	}
	class CostListener extends FocusAdapter{
		@Override
		public void focusLost(FocusEvent e){
			if(prodCostStartField.getText().isEmpty())
				prodCostStartField.setText("0");
			if(prodCostEndField.getText().isEmpty())
				prodCostEndField.setText(prodCostStartField.getText());
			
			if(prodCostStartField.getText().isEmpty() || prodCostEndField.getText().isEmpty()) return;
			Integer startCost = new Integer(prodCostStartField.getText());
			Integer endCost = new Integer(prodCostEndField.getText());
			
			if(startCost > endCost)
			{
				String endToStart = prodCostEndField.getText();
				prodCostEndField.setText(prodCostStartField.getText());
				prodCostStartField.setText(endToStart);
			}
		}
		@Override
		public void focusGained(FocusEvent e){
			selCostButton.setSelected(true);
		}
	}
	class NameFiller implements KeyListener{
		@Override
		public void keyTyped(KeyEvent e) {
			
		}
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar() == ',')
				e.setKeyChar('.');
		}
		@Override
		public void keyReleased(KeyEvent e) {
			//do nothing if text deleted
			if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
					e.getKeyCode() == KeyEvent.VK_DELETE ||
					e.getKeyCode() == KeyEvent.VK_SHIFT || 
					e.getKeyCode() == KeyEvent.VK_CONTROL)
				return;

			JTextField field = (JTextField) e.getSource();
			
			if(field.getText().isEmpty()) return;
			
			String s = field.getText().toLowerCase();
			
			for(String fs:nameList){
				if(fs.length() <= s.length()) continue;
				
				if(fs.substring(0, s.length()).toLowerCase().equals(s)){
					//set unselected entered part pf name and selected predicted
					field.setText(fs);
					field.select(s.length(), fs.length());
					return;
				}
			}
		}
	}
	class MessagesListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals("TextReport")){
				statusLabel.setText((String) evt.getNewValue());
			}
		}
	}
	class DBConfigDialog extends JDialog{
		JLabel 	hostAddrLabel = new JLabel("������� �����:"),
				hostPortLabel = new JLabel("����:"),
				dbNameLabel = new JLabel("��� ���� ������:"),
				tableNameLabel = new JLabel("��� �������:"),
				userNameLabel = new JLabel("��� ������������:"),
				userPassLabel = new JLabel("������:");
		
		JTextField hostAddrField = new JTextField("127.0.0.1"),
				hostPortField = new JTextField("3306"),
				dbNameField = new JTextField("homeex"),
				tableNameField = new JTextField("EX"),
				userNameField = new JTextField(""),
				userPassField = new JPasswordField("");
		
		JButton saveButton = new JButton("���������"),
				cancelButton = new JButton("������"),
				fileNameButton = new JButton("������� ����");
		
		String pass = "";
		
		public DBConfigDialog(JFrame parent, HomeEXConfig conf){
			super(parent, "Database configuration", true);
			
			setLayout(new GridLayout(7,2));
			
			setBounds(parent.getLocation().x + parent.getWidth()/2, parent.getLocation().y + parent.getHeight()/2, 400, 200);
			
			saveButton.addActionListener(new SaveActionListener());
			cancelButton.addActionListener(new CancelActionListener());
			userPassField.setToolTipText("������ �� ������� 50 ��������");
			
			hostAddrField.setText(conf.hostAddr);
			hostPortField.setText(conf.hostPort);
			dbNameField.setText(conf.dbName);
			tableNameField.setText(conf.tableName);
			userNameField.setText(conf.userName);
			pass = conf.getUserPass();
			
			add(hostAddrLabel);
			add(hostAddrField);
			add(hostPortLabel);
			add(hostPortField);
			add(dbNameLabel);
			add(dbNameField);
			add(tableNameLabel);
			add(tableNameField);
			add(userNameLabel);
			add(userNameField);
			add(userPassLabel);
			add(userPassField);
			add(saveButton);
			add(cancelButton);
		}
		
		class SaveActionListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					config = new HomeEXConfig(
							hostAddrField.getText(),
							hostPortField.getText(),
							userNameField.getText(),
							userPassField.getText().isEmpty() ? pass : userPassField.getText(),	//password not show even as stars, it saves internally
							dbNameField.getText(),
							tableNameField.getText(),
							true);
				} catch (PasswordFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					statusLabel.setText("������ ��� ���������� ������������");
					return;
				}
				da = new DataAccess(config);
				saveConfigToFile(config);
				dispose();
			}
			
		}
		
		class CancelActionListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		}
	}
	
	public static void main(String[] args){
		run(new HomeExGUI(), WIDTH, MINHEIGHT);
	}
}