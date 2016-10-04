/*
Copyright (c) 2002 Yankee Software.

This file is part of the JDO Learning Tools

The JDO Learning Tools is free software; you can use it, redistribute it,
and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

The JDO Learning Tools software is distributed in the hope that it
will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
the GNU General Public License for more details.

A copy of the GPL Version 2 is contained in LICENSE.TXT in this source
distribution.  If you cannot find LICENSE.TXT, write to the Free
Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA or visit www.fsf.org on the web.

Copyright law and the license agreement do not apply to your
understanding of the the concepts, principles, and practices embedded
in this code.  The purpose of the JDO Learning Tools to to help
advance the use and understanding of Java Data Objects, the standard
for transparent persistence for Java objects from the Java Community
Process.

Change History:

Please insert a brief record of any changes made.

Author            Date        Purpose
-----------------+----------+-----------------------------------
David Ezzio       09/01/02   Created
*/
package com.ysoft.jdo.book.rental.local.client.gui;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.common.swing.*;
import com.ysoft.jdo.book.rental.persistent.Week;


public class ReservationClient extends JFrame
   {
   static final String      APPLICATION_TITLE                = "The Maine Lighthouse Rental from Using and Understanding JDO";
   static ReservationClient rcReservationClient;
   JPanel                   rcMainPanel;
   JPanel                   rcCustomerPanel;
   JPanel                   rcControlPanel;
   JPanel                   rcViewPanel;
   JButton                  rcConfirmButton;
   JButton                  rcRefreshButton;
   JLabel                   rcCustomerLabel;
   JComboBox                rcCustomerComboBox;
   JScrollPane              rcScrollView;
   JTable                   rcViewTable;
   TableModel               rcViewTableModel;
   Container                rcContentPane;
   JRadioButtonMenuItem     rcViewAvailableButton;
   JRadioButtonMenuItem     rcViewCustomerButton;
   JRadioButtonMenuItem     rcViewCustomerAndAvailableButton;
   JMenu                    rcViewMenu;
   JMenuBar                 rcMenuBar;
   ButtonGroup              rcViewButtonGroup;
   JMenuItem                rcFileClearDBButton;
   JMenuItem                rcFilePopulateDBButton;
   JMenuItem                rcFileExitButton;
   JMenu                    rcFileMenu;
   JMenu                    rcHelpMenu;
   JMenuItem                rcHelpAboutButton;
   JMenuItem                rcHelpLighthousesButton;
   JMenuItem                rcConnectMenu;
   JRadioButtonMenuItem     rcConnectToDBButton;
   JRadioButtonMenuItem     rcDisconnectFromDBButton;
   ButtonGroup              rcConnectButtonGroup;
   ButtonControl            rcButtonControl;
   ReservationClientModel   rcReservationClientModel;
   SpinButton               rcClearCustomerNameButton;

   private ReservationClient()
      {
      // all initialization code is in rcInit to avoid construction problems
      }

   public static void main(String[] args)
      {
      // process the command line arguments
      handleArguments(args);

      // create main window
      rcReservationClient = new ReservationClient();

      // construct the window
      rcReservationClient.rcInit();

      // add the WindowListener
      rcReservationClient.addWindowListener(new ReservationClientWindowListener(
            rcReservationClient));

      // invoke layout manager and make visible
      rcReservationClient.pack();
      Utilities.centerOnParent(rcReservationClient);
      rcReservationClient.setVisible(true);

      // Give the GUI time to initialize
      Thread.yield();
      }

   private void rcInit()
      {
      setTitle(APPLICATION_TITLE);

      // our JDO based data model
      rcReservationClientModel = new ReservationClientModel();

      // create the listener for all of our command buttons
      final ReservationClientListener listener = new ReservationClientListener(this,
            rcReservationClientModel);

      // set up the menus
      rcFileMenu                = new JMenu("File");
      rcFileClearDBButton       = new JMenuItem("Clear database");
      rcFilePopulateDBButton    = new JMenuItem("Populate database");
      rcFileExitButton          = new JMenuItem("Exit");
      rcFileClearDBButton.addActionListener(listener);
      rcFilePopulateDBButton.addActionListener(listener);
      rcFileExitButton.addActionListener(listener);
      rcFileMenu.add(rcFileClearDBButton);
      rcFileMenu.add(rcFilePopulateDBButton);
      rcFileMenu.add(rcFileExitButton);

      rcConnectMenu           = new JMenu("Connect");
      rcConnectButtonGroup    = new ButtonGroup();
      rcConnectToDBButton     = new JRadioButtonMenuItem("Connect to datastore");
      rcDisconnectFromDBButton = new JRadioButtonMenuItem(
            "Disconnect from datastore");
      rcConnectToDBButton.addActionListener(listener);
      rcDisconnectFromDBButton.addActionListener(listener);
      rcConnectMenu.add(rcConnectToDBButton);
      rcConnectMenu.add(rcDisconnectFromDBButton);
      rcConnectButtonGroup.add(rcConnectToDBButton);
      rcConnectButtonGroup.add(rcDisconnectFromDBButton);

      rcViewMenu               = new JMenu("View");
      rcViewAvailableButton    = new JRadioButtonMenuItem("Available");
      rcViewCustomerButton    = new JRadioButtonMenuItem(
            "Customer's Reservations");
      rcViewCustomerAndAvailableButton = new JRadioButtonMenuItem("Both");
      rcViewAvailableButton.addActionListener(listener);
      rcViewCustomerButton.addActionListener(listener);
      rcViewCustomerAndAvailableButton.addActionListener(listener);
      rcViewMenu.add(rcViewAvailableButton);
      rcViewMenu.add(rcViewCustomerButton);
      rcViewMenu.add(rcViewCustomerAndAvailableButton);
      rcViewButtonGroup = new ButtonGroup();
      rcViewButtonGroup.add(rcViewAvailableButton);
      rcViewButtonGroup.add(rcViewCustomerButton);
      rcViewButtonGroup.add(rcViewCustomerAndAvailableButton);

      rcHelpMenu                 = new JMenu("Help");
      rcHelpAboutButton          = new JMenuItem("About");
      rcHelpLighthousesButton    = new JMenuItem("Lighthouses");
      rcHelpAboutButton.addActionListener(listener);
      rcHelpLighthousesButton.addActionListener(listener);
      rcHelpMenu.add(rcHelpAboutButton);
      rcHelpMenu.add(rcHelpLighthousesButton);

      rcMenuBar = new JMenuBar();
      rcMenuBar.add(rcFileMenu);
      rcMenuBar.add(rcConnectMenu);
      rcMenuBar.add(rcViewMenu);
      rcMenuBar.add(rcHelpMenu);
      setJMenuBar(rcMenuBar);

      // set up the customer name panel
      rcCustomerLabel = new JLabel("Customer's name:");
      class CustomerComboBoxModel extends DefaultComboBoxModel
         {
         CustomerComboBoxModel(String[] names)
            {
            super(names);
            }

         void replaceNames(String[] names)
            {
            //System.out.println("Asked to replace names to the customer combo box model");
            removeAllElements();

            if (names == null)
               {
               addElement("none");
               return;
               }

            for (int x = 0; x < names.length; x++)
               {
               addElement(names[x]);
               }
            }

         void removeNames()
            {
            replaceNames(null);
            }
         }

      final CustomerComboBoxModel ccBoxModel = new CustomerComboBoxModel(rcReservationClientModel.getCustomerNames());

      rcCustomerComboBox = new JComboBox(ccBoxModel);
      rcCustomerComboBox.addActionListener(listener);
      rcReservationClientModel.addModelChangeListener(new ModelChangeListener()
            {
            public void newData(ModelChangeEvent e)
               {
               String c = e.getComponentName();

               if (ReservationClientModel.CUSTOMER_NAME.equals(c))
                  {
                  //System.out.println("Got reservation model event: " + e.getComponentName());
                  String n = rcReservationClientModel.getCustomerName();
                  rcButtonControl.setCustomerName(rcReservationClientModel.isCustomerDefined());

                  //rcCustomerField.setText(n);
                  String name  = rcReservationClientModel.getCustomerName();
                  int    found = ccBoxModel.getIndexOf(name);

                  if (found < 0)
                     {
                     rcCustomerComboBox.addItem(name);
                     rcCustomerComboBox.setSelectedItem(name);
                     }
                  else
                     {
                     String select = (String) ccBoxModel.getElementAt(found);
                     rcCustomerComboBox.setSelectedItem(select);
                     }
                  }
               else if (ReservationClientModel.VIEW_CHANGED.equals(c))
                  {
                  //System.out.println("Model changed view event");
                  if (rcReservationClientModel.isViewCustomerAndAvailableRentals())
                     rcViewCustomerAndAvailableButton.setSelected(true);
                  else if (rcReservationClientModel.isViewAvailableRentals())
                     rcViewAvailableButton.setSelected(true);
                  else if (rcReservationClientModel.isViewCustomerRentals())
                     rcViewCustomerButton.setSelected(true);
                  }
               else if (ReservationClientModel.DISCONNECTED.equals(c))
                  {
                  //System.out.println("Model disconnected event");
                  ccBoxModel.removeNames();
                  rcButtonControl.setDisconnected();
                  }
               else if (ReservationClientModel.CUSTOMER_LIST.equals(c))
                  {
                  //System.out.println("Model connected event");
                  ccBoxModel.replaceNames(rcReservationClientModel.getCustomerNames());
                  }
               }
            });

      rcClearCustomerNameButton = new SpinButton("X");
      rcClearCustomerNameButton.addActionListener(listener);

      rcCustomerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      rcCustomerPanel.add(rcCustomerLabel);

      //rcCustomerPanel.add(rcCustomerField);
      rcCustomerPanel.add(rcCustomerComboBox);
      rcCustomerPanel.add(rcClearCustomerNameButton);

      // set up the control button panel
      rcConfirmButton    = new JButton("Confirm");
      rcRefreshButton    = new JButton("Refresh");
      rcConfirmButton.addActionListener(listener);
      rcRefreshButton.addActionListener(listener);

      rcControlPanel = new JPanel(new FlowLayout());
      rcControlPanel.add(rcConfirmButton);
      rcControlPanel.add(rcRefreshButton);

      // set up the rental panel
      rcViewPanel         = new JPanel(new BorderLayout());
      rcViewTableModel    = new ViewTableModel(rcReservationClientModel);
      rcViewTable         = new JTable(rcViewTableModel);
      rcViewTable.setDefaultRenderer(Boolean.class,
         new BooleanRenderer(rcReservationClientModel));
      rcScrollView = new JScrollPane(rcViewTable);
      rcViewPanel.add(rcScrollView, BorderLayout.CENTER);

      // we prefer 10 rows in the rental viewport
      int       row_height = rcViewTable.getRowHeight();
      JViewport jPort = rcScrollView.getViewport();
      Dimension d     = jPort.getPreferredSize();

      //System.out.println("JViewport original preferred size: " + d );
      d.height    = 10 * row_height;
      d.width     = (int) (1.3 * d.width);
      jPort.setPreferredSize(d);

      //System.out.println("JViewport new preferred size: " + d);
      // add a mouse listener so we can detect clicks
      // on the table header
      MouseListener ml = new MouseAdapter()
            {
            public void mouseClicked(MouseEvent e)
               {
               int vCol = rcViewTable.columnAtPoint(e.getPoint());
               int mCol = rcViewTable.convertColumnIndexToModel(vCol);

               //System.out.println("Mouse clicked on table header on vCol: " + vCol + ", mCol: " + mCol);
               int lighthouseIndex = ReservationClient.colToLighthouseIndex(mCol);
               listener.moreActions(lighthouseIndex);
               }
            };

      JTableHeader th = rcViewTable.getTableHeader();
      th.addMouseListener(ml);
      th.setReorderingAllowed(false);

      // add a customer header render so we can control header color
      TableCellRenderer tcr = new HeaderRenderer((DefaultTableCellRenderer) th.getDefaultRenderer(),
            rcViewTable);
      th.setDefaultRenderer(tcr);

      // set up the main panel
      rcMainPanel = new JPanel(new BorderLayout());
      rcMainPanel.add(rcCustomerPanel, BorderLayout.NORTH);
      rcMainPanel.add(rcViewPanel, BorderLayout.CENTER);
      rcMainPanel.add(rcControlPanel, BorderLayout.SOUTH);

      // set up the content pane
      rcContentPane = getContentPane();
      rcContentPane.setLayout(new BorderLayout());
      rcContentPane.add(rcMainPanel, BorderLayout.CENTER);

      // set up the button control
      rcButtonControl = new ButtonControl(this);
      rcButtonControl.initialize();
      }

   static int colToLighthouseIndex(int col)
      {
      // map 0 to 0, 1 to 0, 2 to 0, 3 to 1, 4 to 1, 5 to 2, etc.
      int retv = ((col + 1) / 2) - 1;

      if (retv < 0)
         retv = 0;

      return retv;
      }

   static char getLighthouseLetter(int column)
      {
      return (char) ('A' + colToLighthouseIndex(column));
      }

   private static void handleArguments(String[] args)
      {
      // no arguments or two arguments
      if ((args.length != 2) && (args.length != 0))
         tellSyntax();

      if (args.length == 2)
         {
         if (!args[0].equalsIgnoreCase("-date"))
            tellSyntax();

         DateFormat df = new SimpleDateFormat("MM-dd-yyyy");

         try
            {
            Date date = df.parse(args[1]);
            Week.setPopulationDate(date);
            }
         catch (ParseException e)
            {
            System.out.println("Incorrect date format");
            tellSyntax();
            }
         }
      }

   private static void tellSyntax()
      {
      System.out.println();
      System.out.println("Syntax:");
      System.out.println(
         "   java com.ysoft.jdo.book.rental.local.client.gui.ReservationClient [-date <mm-dd-yyyy>]");
      System.exit(1);
      }
   }


class ReservationClientWindowListener extends WindowAdapter
   {
   ReservationClient rc;

   public ReservationClientWindowListener(ReservationClient rc)
      {
      this.rc = rc;
      }

   /**
    * Invoked when the window is being closed by the UI windowing system.
    */
   public void windowClosing(WindowEvent e)
      {
      //System.out.println("Window closing");
      System.exit(0);
      }

   /**
    * Invoked when the window is being closed by dispose().
    */
   public void windowClosed(WindowEvent e)
      {
      //System.out.println("Window closed event");
      windowClosing(e);
      }
   }


class ViewTableModel extends AbstractTableModel implements ModelChangeListener
   {
   ReservationClientModel rcm;

   public ViewTableModel(ReservationClientModel rcm)
      {
      this.rcm = rcm;
      rcm.addModelChangeListener(this);
      }

   public int getColumnCount()
      {
      return (rcm.getNumLighthouses() * 2) + 1;
      }

   public int getRowCount()
      {
      return rcm.getNumRentalDates();
      }

   public Object getValueAt(int row, int col)
      {
      if (col == 0)
         return rcm.getRentalDate(row);
      else
         {
         int lighthouseIndex = ReservationClient.colToLighthouseIndex(col);

         if ((col % 2) == 0)
            return rcm.getPrice(row, lighthouseIndex);
         else
            return new Boolean(!rcm.isAvailable(row, lighthouseIndex));
         }
      }

   public void setValueAt(Object val, int row, int col)
      {
      try
         {
         if (col > 0)
            {
            if (!rcm.isCustomerDefined())
               {
               MessageHandler.reportError(ReservationClient.rcReservationClient,
                  "No customer");
               }
            else
               {
               int lighthouseIndex = ReservationClient.colToLighthouseIndex(col);

               if ((col % 2) == 1)
                  {
                  //System.out.println("Setting " + val + " at row: " + row + ", col: " + col +
                  //      ", lighthouse: " + lighthouseIndex);
                  rcm.setAvailable(row, lighthouseIndex,
                     !((Boolean) val).booleanValue());
                  }
               }
            }
         }
      catch (ClassCastException e)
         {
         MessageHandler.reportError(null, "Unexpected type for JTable column");
         }
      }

   public Class getColumnClass(int col)
      {
      if (col == 0)
         return Date.class;
      else if ((col % 2) == 1)
         return Boolean.class;
      else
         return BigDecimal.class;
      }

   public boolean isCellEditable(int row, int col)
      {
      boolean retv = false;

      if ((col > 0) && ((col % 2) == 1))
         {
         int lighthouseIndex = ReservationClient.colToLighthouseIndex(col);

         retv = rcm.isModifiable(row, lighthouseIndex);

         //System.out.println("cell at row:" + row + ", col:" + col + ", lighthouse:" + lighthouseIndex +
         //      " is modifiabled:" + retv);
         }

      return retv;
      }

   public String getColumnName(int col)
      {
      //System.out.println("getting column name for column: " + col);
      String s = null;

      if (col == 0)
         return "Week starting";
      else if ((col % 2) == 0)

         //return "Price (" + ReservationClient.getLighthouseLetter(col) + ")";
         return "Price";
      else
         {
         int lighthouseIndex = ReservationClient.colToLighthouseIndex(col);

         //return rcm.getLighthouseName(lighthouseIndex) + " (" + ReservationClient.getLighthouseLetter(col) + ")";
         return rcm.getLighthouseName(lighthouseIndex);
         }
      }

   public void newData(ModelChangeEvent e)
      {
      String c = e.getComponentName();

      if (ReservationClientModel.CONNECTED.equals(c))
         {
         //System.out.println("Got reservation model event: " + c);
         fireTableStructureChanged();
         }
      else if (ReservationClientModel.DATA_CHANGED.equals(c))
         {
         //System.out.println("Got reservation model event for: " + c);
         fireTableDataChanged();
         }
      }
   }


class ReservationClientListener implements ActionListener
   {
   private ReservationClient      rc;
   private ReservationClientModel rcm;

   public ReservationClientListener(ReservationClient rc,
      ReservationClientModel rcm)
      {
      this.rc     = rc;
      this.rcm    = rcm;
      }

   public void actionPerformed(ActionEvent e)
      {
      Object source = e.getSource();

      if (source instanceof AbstractButton)
         {
         AbstractButton button = (AbstractButton) e.getSource();

         if (button == rc.rcFileExitButton)
            {
            //System.out.println("asked to exit application");
            rc.dispose();
            }
         else if (button == rc.rcFileClearDBButton)
            {
            //System.out.println("asked to clear db");
            rcm.cleanDatastore();
            rc.rcButtonControl.setDBPopulated(rcm.isPopulatedDatastore());
            }
         else if (button == rc.rcFilePopulateDBButton)
            {
            //System.out.println("asked to populate db");
            rcm.populateDatastore();
            rc.rcButtonControl.setDBPopulated(rcm.isPopulatedDatastore());
            }
         else if (button == rc.rcHelpAboutButton)
            {
            doAboutDialog(Copyright.get());
            }
         else if (button == rc.rcHelpLighthousesButton)
            {
            //System.out.println("asked to show help on lighthouses");
            doAboutDialog("Lighthouse Getaway Images",
               "Copyright (c) 1994-2002 William A. Britten\n" +
               "Reproduced with permission\n" +
               "Visit www.lighthousegetaway.com");
            }
         else if (button == rc.rcViewAvailableButton)
            {
            if (!rcm.isViewAvailableRentals())
               {
               //System.out.println("asked to show available reservations");
               rcm.viewAvailableRentals();
               }
            }
         else if (button == rc.rcViewCustomerButton)
            {
            if (!rcm.isViewCustomerRentals())
               {
               //System.out.println("asked to show customers reservations");
               rcm.viewCustomerRentals();
               }
            }
         else if (button == rc.rcViewCustomerAndAvailableButton)
            {
            if (!rcm.isViewCustomerAndAvailableRentals())
               {
               //System.out.println("asked to show both");
               rcm.viewCustomerAndAvailableRentals();
               }
            }
         else if (button == rc.rcConfirmButton)
            {
            //System.out.println("asked to confirm");
            rcm.confirm();
            }
         else if (button == rc.rcRefreshButton)
            {
            //System.out.println("asked to refresh");
            rcm.refresh();
            }
         else if (button == rc.rcConnectToDBButton)
            {
            if (!rcm.isConnected())
               {
               //System.out.println("connecting to db");
               rcm.connect();

               if (rcm.isConnected())
                  rc.rcButtonControl.setConnected(rcm.isPopulatedDatastore());
               else
                  rc.rcButtonControl.setDisconnected();
               }
            }
         else if (button == rc.rcDisconnectFromDBButton)
            {
            if (rcm.isConnected())
               {
               //System.out.println("disconnecting from DB");
               rcm.disconnect();
               rc.rcButtonControl.setDisconnected();
               }
            }
         else if (button == rc.rcClearCustomerNameButton)
            {
            //System.out.println("asked to clear customer name");
            rcm.setCustomerName(null);
            }
         else
            {
            System.out.println("unknown button pressed");
            }
         }
      else if (source instanceof JComboBox)
         {
         JComboBox comboBox = (JComboBox) source;

         if (comboBox == rc.rcCustomerComboBox)
            {
            rcm.setCustomerName((String) comboBox.getSelectedItem());
            }
         else
            {
            System.out.println("action received for unknown JComboBox");
            }
         }
      else
         {
         System.out.println("got action for unknown component");
         }
      }

   public void moreActions(int id)
      {
      String name      = rcm.getLighthouseName(id);
      String desc      = rcm.getLighthouseDescription(id);
      String imageFile = rcm.getLighthouseImageName(id);
      System.out.println("asked to view lighthouse: " + name);

      doInfoDialog(name, desc, imageFile);
      }

   private void doInfoDialog(String lighthouseName, String lighthouseDesc,
      String lighthouseImageName)
      {
      if ((lighthouseName == null) || (lighthouseDesc == null) ||
               (lighthouseImageName == null))
         {
         MessageHandler.reportError(rc.rcReservationClient,
            "Unable to display additional information");
         return;
         }

      LighthouseInfoDialog dialog = new LighthouseInfoDialog(rc,
            lighthouseName, lighthouseDesc, lighthouseImageName);

      try
         {
         dialog.init();
         dialog.show();
         }
      catch (DialogException e)
         {
         MessageHandler.reportException(rc.rcReservationClient, e);
         }
      }

   private void doAboutDialog(String heading, String text)
      {
      AboutDialog dialog = new AboutDialog(rc, heading, text);
      dialog.init();
      dialog.show();
      }

   private void doAboutDialog(String[] txtArray)
      {
      StringBuffer tBuf    = new StringBuffer();
      String       heading = txtArray[0];

      for (int x = 1; x < txtArray.length; x++)
         {
         if (x > 1)
            tBuf.append("\n");

         tBuf.append(txtArray[x]);
         }

      doAboutDialog(heading, tBuf.toString());
      }
   }


class HeaderRenderer implements TableCellRenderer
   {
   DefaultTableCellRenderer dtcr;
   JTable                   table;

   public HeaderRenderer(DefaultTableCellRenderer tcr, JTable table)
      {
      dtcr          = tcr;
      this.table    = table;
      }

   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
      {
      Component c = dtcr.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);

      int       mCol = table.convertColumnIndexToModel(column);

      if ((row == -1) && (mCol > 0))
         c.setBackground(new Color(170, 210, 255));

      return c;
      }
   }


class ButtonControl
   {
   ReservationClient client;
   boolean           customerNameAvailable;
   boolean           connected;

   public ButtonControl(ReservationClient rc)
      {
      client = rc;
      }

   public void setDisconnected()
      {
      setConnected(false, false);
      }

   public void setConnected(boolean populateFlag)
      {
      setConnected(true, populateFlag);
      }

   private void setConnected(boolean connectFlag, boolean populatedFlag)
      {
      connected = connectFlag;

      setDBPopulated(populatedFlag);
      client.rcViewAvailableButton.setEnabled(connectFlag);
      client.rcClearCustomerNameButton.setEnabled(connectFlag);

      //client.rcCustomerField.setEditable(connectFlag);
      //client.rcCustomerField.setEnabled(connectFlag);
      client.rcCustomerComboBox.setEditable(connectFlag);
      client.rcCustomerComboBox.setEnabled(connectFlag);
      client.rcConfirmButton.setEnabled(connectFlag);
      client.rcRefreshButton.setEnabled(connectFlag);

      // clean up button state if connection failed
      if (client.rcConnectToDBButton.isSelected() && !connectFlag)
         client.rcDisconnectFromDBButton.setSelected(true);

      setCustomerName();
      }

   public void setCustomerName(boolean flag)
      {
      customerNameAvailable = flag;

      // if clearing the customer and viewing something other than available,
      // change to available
      if (!flag &&
               (client.rcViewCustomerButton.isSelected() ||
               client.rcViewCustomerAndAvailableButton.isSelected()))
         client.rcViewAvailableButton.setSelected(true);

      setCustomerName();
      }

   public void initialize()
      {
      client.rcDisconnectFromDBButton.setSelected(true);

      //client.rcViewAvailableButton.setSelected(true);
      setConnected(false, false);
      }

   public void setDBPopulated(boolean flag)
      {
      //System.out.println("setDBPopulated: " + flag + ", connected: " + connected);
      if (connected)
         {
         client.rcFileClearDBButton.setEnabled(flag);
         client.rcFilePopulateDBButton.setEnabled(!flag);
         }
      else
         {
         client.rcFileClearDBButton.setEnabled(false);
         client.rcFilePopulateDBButton.setEnabled(false);
         }
      }

   private void setCustomerName()
      {
      client.rcViewCustomerAndAvailableButton.setEnabled(customerNameAvailable &&
               connected);
      client.rcViewCustomerButton.setEnabled(customerNameAvailable &&
               connected);
      }
   }


class BooleanRenderer extends JCheckBox implements TableCellRenderer
   {
   private ReservationClientModel rcm;

   public BooleanRenderer(ReservationClientModel rcm)
      {
      super();
      setHorizontalAlignment(JLabel.CENTER);
      this.rcm = rcm;
      }

   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
      {
      if (isSelected)
         {
         setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
         }
      else
         {
         setForeground(table.getForeground());
         setBackground(table.getBackground());
         }

      setSelected(((value != null) && ((Boolean) value).booleanValue()));
      setEnabled(rcm.isModifiable(row,
            ReservationClient.colToLighthouseIndex(column)));
      return this;
      }
   }
