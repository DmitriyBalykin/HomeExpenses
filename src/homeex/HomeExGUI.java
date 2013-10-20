package homeex;
import static Utils.SwingConsole.run;

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
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import Utils.BrowserControl;


public class HomeExGUI extends JFrame {
	private static final int WIDTH = 1100;
	private static final int MINHEIGHT = 150;
	private static final int MAXHEIGHT = 880;
	private static final Dimension screenD = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int SCREENHEIGHT = (int) screenD.getHeight();
	private static final int SCREENWIDTH = (int) screenD.getWidth();
	private static List<String> nameList = new ArrayList<String>();
	String[] types = {"Еда","Хоз.Товары", "Электроника","Одежда","Снаряжение","Медицина","Кварт. услуги", "Прочее"};
	
	JPanel currentPanel = new JPanel(),
			loadDataPanel = new JPanel(),
			readDataPanel = new JPanel(),
			selectorsPanel = new JPanel(new GridLayout(6, 3)),
			statusPanel = new JPanel();
	JLabel dateLabel = new JLabel("      Дата      "),
			typeLabel = new JLabel("Тип покупки"),
			prodNameLabel = new JLabel("Название покупки"),
			prodNumbLabel = new JLabel("Количество"),
			prodOneCostLabel = new JLabel("Цена за единицу"),
			prodTotCostLabel = new JLabel("Общая цена"),
			statusLabel = new JLabel();
	JTextField dateField = new JTextField(8),
			
			prodNameField = new JTextField(25),
			prodNameSelField = new JTextField(10),
			prodNumbField = new JTextField(3),
			prodOneCostField = new JTextField(5),
			prodTotCostField = new JTextField(5),
			prodCostStartField = new JTextField(5),
			prodCostEndField = new JTextField(5),
			dateStartField = new JTextField(),
			dateEndField = new JTextField();

	
	JComboBox typeCombo = new JComboBox(types),
				selTypeCombo = new JComboBox(types);
	JButton sendButton = new JButton("Сохранить данные"),
			getDataButton = new JButton("Получить данные"),
			dataToHTMLButton = new JButton("Открыть данные в HTML");
	JCheckBox selDateButton = new JCheckBox("по дате"),
			selTypeButton = new JCheckBox("по типу покупки"),
			selNameButton = new JCheckBox("по имени покупки"),
			selCostButton = new JCheckBox("по стоимости");
	JMenuItem loadDataItem = new JMenuItem("Загрузить данные"),
			readDataItem = new JMenuItem("Считать данные"),
			dbToFileItem = new JMenuItem("Скопировать данные в файл"),
			setMenuItem = new JMenuItem("Настройки хранения данных");
	JMenu menu = new JMenu("Главное меню"),
			menuSettings = new JMenu("Настройки");
	JMenuBar menuBar = new JMenuBar();
	
	HomeEXConfig config;
	DataAccess da;
	DataGrid dGrid = new DataGrid();
	JScrollPane scrollDataPane = new JScrollPane();
	
	HomeExGUI(){
		config = loadConfigFromFile();
		da = new DataAccess(config, new MessagesListener());
		
		statusLabel.setText("Соединение с базой данных...");
		//выполнение соединения с БД в отдельном потоке
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
		getDataButton.addActionListener(new GL());
		prodNumbField.addKeyListener(new KL("float"));
		prodOneCostField.addKeyListener(new KL("float"));
		prodTotCostField.addKeyListener(new TotalCostListener("float"));
		
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
				statusLabel.setText("В процессе...");
				da.dbToFile();
				statusLabel.setText("Готово");
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

	}
	private void confReadPanel(){
		
		readDataPanel.setLayout(new BoxLayout(readDataPanel, BoxLayout.PAGE_AXIS));
		selectorsPanel.setMinimumSize(new Dimension(1000, 200));
		selectorsPanel.setMaximumSize(new Dimension(1000, 200));
		selectorsPanel.setSize(new Dimension(1000, 200));
		
		//вібор по дате
		selectorsPanel.add(selDateButton);
		selectorsPanel.add(dateStartField);
		selectorsPanel.add(dateEndField);
		//выбор по имени
		selectorsPanel.add(selNameButton);
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(prodNameSelField);
		//выбор по типу
		selectorsPanel.add(selTypeButton);
		selectorsPanel.add(new JLabel());
		selectorsPanel.add(selTypeCombo);
		//выбор по цене
		selectorsPanel.add(selCostButton);
		selectorsPanel.add(prodCostStartField);
		selectorsPanel.add(prodCostEndField);
		//кнопка запроса данных
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
		selectorsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Выбрать данные:"));
		readDataPanel.add(selectorsPanel);

		
		scrollDataPane = new JScrollPane(dGrid);
		scrollDataPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Полученные данные:"));
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
			statusLabel.setText("Файл конфигурации не найден");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			statusLabel.setText("Ошибка при чтении файла конфигурации");
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
				statusLabel.setText("Запись \""+prodNameField.getText()+"\" сохранена");
			else
				statusLabel.setText("Ошибка при сохранении записи");
		}
		
	}
	class GL implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			statusLabel.setText("В процессе...");
			
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
			
			statusLabel.setText("Готово");
		}
		
	}
	class KL implements KeyListener{
		String type;
		char[] digits = "0123456789".toCharArray();
		
		KL(String t){
			type = t;
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			
			if(e.getKeyChar() == ','){
				e.setKeyChar('.');
			}
			if(!isDigit(e.getKeyChar()))
				e.consume();
		}
		@Override
		public void keyPressed(KeyEvent e) {
		}
		@Override
		public void keyReleased(KeyEvent e) {
			String s1 = prodNumbField.getText();
			String s2 = prodOneCostField.getText();
			if(!s1.equals("") && !s2.equals("")){
				Float numb = new Float(s1);
				Float onecost = new Float(s2);
				prodTotCostField.setText(String.format("%.2f", numb*onecost).replace(',', '.'));
			}
		}
		private boolean isDigit(char c){
			if(type == "float" && c == '.')	return true;
			for(char dc:digits)
				if(dc == c) return true;
			return false;
		}
	}
	class TotalCostListener extends KL{
		TotalCostListener(String t){
			super(t);
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			
			String s1 = prodTotCostField.getText();
			String s2 = prodOneCostField.getText();
			if(!s1.isEmpty() || !s2.isEmpty()){
				Float totCost = new Float(s1);
				Float oneCost = new Float(s2);
				prodNumbField.setText(String.format("%.2f", totCost/oneCost).replace(',', '.'));
			}
		}
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
		JLabel 	hostAddrLabel = new JLabel("Сетевой адрес:"),
				hostPortLabel = new JLabel("Порт:"),
				dbNameLabel = new JLabel("Имя базы данных:"),
				tableNameLabel = new JLabel("Имя таблицы:"),
				userNameLabel = new JLabel("Имя пользователя:"),
				userPassLabel = new JLabel("Пароль:");
		
		JTextField hostAddrField = new JTextField("127.0.0.1"),
				hostPortField = new JTextField("3306"),
				dbNameField = new JTextField("homeex"),
				tableNameField = new JTextField("EX"),
				userNameField = new JTextField(""),
				userPassField = new JPasswordField("");
		
		JButton saveButton = new JButton("Сохранить"),
				cancelButton = new JButton("Отмена"),
				fileNameButton = new JButton("Выбрать файл");
		
		String pass = "";
		
		public DBConfigDialog(JFrame parent, HomeEXConfig conf){
			super(parent, "Database configuration", true);
			
			setLayout(new GridLayout(7,2));
			
			setBounds(parent.getLocation().x + parent.getWidth()/2, parent.getLocation().y + parent.getHeight()/2, 400, 200);
			
			saveButton.addActionListener(new SaveActionListener());
			cancelButton.addActionListener(new CancelActionListener());
			userPassField.setToolTipText("Пароль не длиннее 50 символов");
			
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
					statusLabel.setText("Ошибка при сохранении конфигурации");
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



