package Utils;
//: net/mindview/util/SwingConsole.java
// Tool for running Swing demos from the
// console, both applets and JFrames.

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class SwingConsole {
  public static void run(final JFrame f, final int width, final int height) {
    SwingUtilities.invokeLater(new Runnable() {
    	
      public void run() {
        f.setTitle(f.getClass().getSimpleName());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
        f.setVisible(true);
      }
    });
  }
  public static void run(final JFrame f, final int width, final int height, String lookAndFeel) {
	  setLookAndFeel(lookAndFeel);
	   run(f, width, height);
	  }
  public static void  run(final JFrame f) {
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
	    	int width = f.getWidth(),
	    		height = f.getHeight();
	        f.setTitle(f.getClass().getSimpleName());
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        f.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
	        f.setVisible(true);
	      }
	    });
	  }
  public static void run(final JFrame f, String lookAndFeel) {
	  	setLookAndFeel(lookAndFeel);
	   run(f);
  }
	  
  private static void setLookAndFeel(String s){
	  if(s.equals("cross")) {
	      try {
	        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	        System.out.println("cross");
	        return;
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    } else if(s.equals("system")) {
	      try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        System.out.println("system");
	        return;
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    } else if(s.equals("motif")) {
	      try {
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
	        System.out.println("motif");
	        return;
	      } catch(Exception e) {
	        e.printStackTrace();
	      }
	    } else {
		  try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			System.out.println("nimbus");
			return;
		  } catch(Exception e) {
		    e.printStackTrace();
		  }
	    } 
  }
  
//Создание строки заголовка из имени класса:
 public static String title(Object o) {
   String t = o.getClass().toString();
   // Удаление слова "class":
   if(t.indexOf("class") != -1)
     t = t.substring(6);
   return t;
 }
 public static void setupClosing(JFrame frame) {
   // Решение JDK 1.2 - это
   // анонимный внутренний класс:
   frame.addWindowListener(new WindowAdapter() {
     public void windowClosing(WindowEvent e) {
       System.exit(0);
     }
   });
   // улучшенное решение в JDK 1.3:
   // frame.setDefaultCloseOperation(
   //     EXIT_ON_CLOSE);
 }

 public static void run(JApplet applet, int width, int height) {
   JFrame frame = new JFrame(title(applet));
   setupClosing(frame);
   frame.getContentPane().add(applet);
   frame.setSize(width, height);
   applet.init();
   applet.start();
   frame.setVisible(true);
 }
 public static void run(JPanel panel, int width, int height) {
   JFrame frame = new JFrame(title(panel));
   setupClosing(frame);
   frame.getContentPane().add(panel);
   frame.setSize(width, height);
   frame.setVisible(true);
 }
}
