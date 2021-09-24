package com.java.myapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
// ------------------------------------------------------------------------------
public class MyForm extends JFrame {
  
  private static  DefaultTableModel model;
  private static  JLabel            databaseLabel = new JLabel("Connected to:");
  private static  JLabel            dateLabel     = new JLabel(" on ");
  private static  JPanel            infoPanel     = new JPanel();
  private static  JScrollPane       scrollPane    = new JScrollPane();
  private static  JTable            table         = null;
  private static  JTextField        databaseID    = new JTextField(7);
  private static  JTextField        currentDate   = new JTextField(20);
  private static  String            userName      = null;
  private static  String            passWord      = null;
  private static  String            salesID       = null;

private static  String            ORAURL        = "jdbc:oracle:thin:@//<change to your RDS Oracle endpoint>:1521/ORACLEDB";

private static  String            POSTURL       = "jdbc:postgresql://<change to your Aurora PostgreSQL Cluster writer endpoint>:5432/AuroraDB";
  private static  String            DBURL         = ORAURL;

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        MyForm form = new MyForm();
        form.setVisible(true);
      }
    });
  }
  // ------------------------------------------------------------------------------
  public MyForm() {
            
    // Create Form Frame
    setTitle("DB Freedom SCT Java Program Conversion");
    setBounds(400, 300, 800, 400);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setLayout(null);
    
    // Table
    table = new JTable()
    {
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
          Component comp = super.prepareRenderer(renderer, row, column);
          comp.setBackground(row % 2 == 0 ? Color.yellow : Color.yellow.darker());
          return comp;
      }
    };

    // ScrollPane
    scrollPane.setBounds(50, 50, 700, 300);
    getContentPane().add(scrollPane);
    scrollPane.setViewportView(table);

    model = (DefaultTableModel)table.getModel();
    
    model.addColumn("Document ID");
    model.addColumn("Create Date");
    model.addColumn("Description");

    // DB Info
    infoPanel.setBounds(0, 10, 800, 35);
    getContentPane().add(infoPanel);
    infoPanel.add(databaseLabel);
    infoPanel.add(databaseID);
    infoPanel.add(dateLabel);
    infoPanel.add(currentDate);
    databaseID.setEditable(false);
    currentDate.setEditable(false);
    databaseID.setBackground(Color.lightGray);
    currentDate.setBackground(Color.lightGray);
    infoPanel.setVisible(true);

    // When Frame Loaded
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        LoginDialog();
      }
    });
  }
  // ------------------------------------------------------------------------------
  public static Boolean getData() {
    
    Boolean           status  = false;
    Connection        connect = null;
    PreparedStatement pre     = null;
    ResultSet         rec     = null;
    String            sql     = null;
    int               row     = 0;

    try {
      Class.forName("oracle.jdbc.OracleDriver");
      Class.forName("org.postgresql.Driver");

      connect = DriverManager.getConnection(DBURL, userName, passWord);
      
      sql = "select "                                                           +
                  "sys_context('userenv','db_name')             name,"          +
                  "to_char(sysdate, 'Day DDth Mon hh24:mi:ss')  current_date "  +
            "from dual";
      
      pre = connect.prepareStatement(sql);
      rec = pre.executeQuery();
      if(rec.next()) {
        databaseID.setText(rec.getString("name"));
        currentDate.setText(rec.getString("current_date"));
      }
      
      sql = "SELECT "                                               +
                  "a.document_id,"                                  +
                  "a.created,"                                      +
                  "NVL(a.description,a.documentname) description "  +
            "FROM "                                                 +
                  "dms_sample.document  a,"                                    +
                  "dms_sample.webuserx  b "                                    +
            "WHERE "                                                +
                  "a.createdby = b.webuser_id "                     +
            "AND   b.username  = ?";
      
      pre = connect.prepareStatement(sql);
      pre.setString(1, salesID);
      rec = pre.executeQuery();
      
      if(rec.next()) {
        model.addRow(new Object[0]);
        model.setValueAt(rec.getInt("document_id"),     row, 0);
        model.setValueAt(rec.getString("created"),      row, 1);
        model.setValueAt(rec.getString("description"),  row, 2);
        row++;
        while(rec.next()){
          model.addRow(new Object[0]);
          model.setValueAt(rec.getInt("document_id"),     row, 0);
          model.setValueAt(rec.getString("created"),      row, 1);
          model.setValueAt(rec.getString("description"),  row, 2);
          row++;
        }
        status = true;
      } else {
         JOptionPane.showMessageDialog(null, "Incorrect Username/Password");
      }
             
    } catch (Exception e) {
      // TODO Auto-generated catch block
      JOptionPane.showMessageDialog(null, e.getMessage());
      e.printStackTrace();
    }
    
    try {
      if(pre != null) {
        pre.close();
        connect.close();
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    
    return status;
  }
  // ------------------------------------------------------------------------------
  public static void LoginDialog() {

    JLabel          title     = new JLabel("Enter Query Details");
    JTextField      salesid   = new JTextField("MRevitt");
    JTextField      username  = new JTextField("dbmaster");
    JPasswordField  password  = new JPasswordField("dbmaster123");
    
    final JComponent[] inputs = new JComponent[] {
        title,
        new JLabel("Sales ID"),
        salesid,
        new JLabel("Username"),
        username,
        new JLabel("Password"),
        password
    };
    JOptionPane.showMessageDialog(null, inputs, "Login", JOptionPane.PLAIN_MESSAGE);
    
    salesID  = salesid.getText();
    userName = username.getText();
    passWord =  new String(password.getPassword());
    
    // Check Login
    if(!getData())
    {
      LoginDialog();
    }
  }
}
