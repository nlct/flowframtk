// File          : CharacterSelector.java
// Description   : Dialog for selecting characters
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.beans.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.table.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for selecting characters.
 * @author Nicola L C Talbot
 */

public class CharacterSelector extends JDialog
   implements ActionListener,PropertyChangeListener,
              ListSelectionListener
{
   public CharacterSelector(FlowframTk gui, int[][] ranges)
   {
      super(gui, gui.getResources().getString("symbol.title"), true);

      this.resources = gui.getResources();

      this.symbolListener_ = gui;

      init(ranges);
   }

   public CharacterSelector(JDRResources resources, Dialog parent,
     SymbolSelectorListener symbolListener, int[][] ranges)
   {
      super(parent, resources.getString("symbol.title"), true);

      this.resources = resources;
      this.symbolListener_ = symbolListener;

      init(ranges);
   }

   private void init(int[][] ranges)
   {
      Box topPanel = Box.createHorizontalBox();

      JLabel label = new JLabel(resources.getString("symbol.text"));
      label.setDisplayedMnemonic(resources.getCodePoint("symbol.text.mnemonic"));

      topPanel.add(label);

      textfield = new JTextField()
      {
         public void paintComponent(Graphics g)
         {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHints(symbolListener_.getRenderingHints());
            super.paintComponent(g);
         }
      };

      label.setLabelFor(textfield);

      topPanel.add(textfield);

      getContentPane().add(topPanel, "North");

      unicodePanel = new UnicodePanel(symbolListener_, this);

      getContentPane().add(unicodePanel, "East");

      cardLayout = new CardLayout();

      cardPanel = new JPanel(cardLayout);

      blockPanels = new Vector<BlockPanel>();

      cardIndexTable = new JTable(
       new DefaultTableModel(
         new Object[] {resources.getString("unicode.blocks.title")},
         0)
      )
      {
          public boolean isCellEditable(int row, int col)
          {
             return false;
          }
      };

      cardIndexTable.setRowSorter(
         new TableRowSorter<TableModel>(cardIndexTable.getModel()));

      cardIndexTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      cardIndexTable.getSelectionModel().addListSelectionListener(this);

      JSplitPane splitPane = new JSplitPane(
         JSplitPane.HORIZONTAL_SPLIT, 
         new JScrollPane(cardIndexTable),
         cardPanel);

      getContentPane().add(splitPane, "Center");

      for (int i = 0; i < ranges.length; i++)
      {

         for (int codePoint = ranges[i][0]; codePoint <= ranges[i][1]; 
              codePoint++)
         {
            if (!Character.isISOControl(codePoint))
            {
               addCodePoint(codePoint);
            }
         }
      }

      JPanel bottomPanel = new JPanel(new BorderLayout());

      JPanel p2 = new JPanel();
      bottomPanel.add(p2, "Center");

      p2.add(resources.createOkayButton(this));
      p2.add(resources.createCancelButton(this));
      p2.add(resources.createHelpButton("mi:insertsymbol"));

      modeLabel = new JLabel();
      bottomPanel.add(modeLabel, "East");

      getContentPane().add(bottomPanel, "South");

      pack();

      int height = MAX_ROWS*cardIndexTable.getRowHeight();

      Dimension dim = cardIndexTable.getPreferredScrollableViewportSize();

      height = (int)Math.min(height, dim.height);

      dim.height = height;
      dim.width = (int)(0.8*dim.width);
      cardIndexTable.setPreferredScrollableViewportSize(dim);
      dim = splitPane.getPreferredSize();
      dim.height = height;
      splitPane.setPreferredSize(dim);
      
      pack();

      setLocationRelativeTo(getParent());

      addWindowListener(new WindowAdapter()
         {
            public void windowActivated(WindowEvent e)
            {
               textfield.requestFocusInWindow();
            }
         });

      KeyboardFocusManager focusManager =
         KeyboardFocusManager.getCurrentKeyboardFocusManager();
      focusManager.addPropertyChangeListener(this);

      cardIndexTable.getSelectionModel().setSelectionInterval(0, 0);
      splitPane.setDividerLocation(0.4);
   }

   private BlockPanel addBlock(Character.UnicodeBlock block)
   {
      BlockPanel panel = new BlockPanel(block, symbolListener_, this);

      String name = resources.getString("unicode."+block.toString());

      JScrollPane scrollPane = new JScrollPane(panel);
      scrollPane.setName(name);

      scrollPane.getVerticalScrollBar().setUnitIncrement(
         CharacterButton.HEIGHT);
      scrollPane.getHorizontalScrollBar().setUnitIncrement(
         CharacterButton.WIDTH);

      blockPanels.add(panel);
      cardLayout.addLayoutComponent(scrollPane, name);
      cardPanel.add(scrollPane);

      DefaultTableModel model = (DefaultTableModel)cardIndexTable.getModel();
      model.addRow(new Object[] {name});

      return panel;
   }

   private void addCodePoint(int codePoint)
   {
      Character.UnicodeBlock block = null;

      block = Character.UnicodeBlock.of(codePoint);

      if (block == null)
      {
         return;
      }

      for (BlockPanel panel : blockPanels)
      {
         if (panel.getBlock().equals(block))
         {
            panel.addButton(codePoint);
            return;
         }
      }

      BlockPanel panel = addBlock(block);
      panel.addButton(codePoint);
   }

   public void display(TeXMappings mapping)
   {
      this.texMapping = mapping;

      Font font = symbolListener_.getSymbolButtonFont();

      textfield.setFont(font);
      textfield.setText(symbolListener_.getSymbolText());
      textfield.setCaretPosition(
         symbolListener_.getSymbolCaretPosition());
      textfield.requestFocusInWindow();

      Dimension dim = textfield.getSize();

      modeLabel.setText(resources.getMessage(
        "symbol.mapping_mode", mapping.getModeName()));

      setVisible(true);
   }

   public void propertyChange(PropertyChangeEvent e)
   {
      String propertyName = e.getPropertyName();

      if (propertyName.equals("focusOwner")
        && e.getNewValue() instanceof CharacterButton)
      {
         CharacterButton button = (CharacterButton)e.getNewValue();

         unicodePanel.setHexString(button.getHexString());
      }
   }

   public void okay()
   {
      setVisible(false);
      symbolListener_.setSymbolText(textfield.getText());
      symbolListener_.setSymbolCaretPosition(
         textfield.getCaretPosition());
      symbolListener_.requestSymbolFocus();
   }

   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      String action = event.getActionCommand();

      if (action == null)
      {
         action = "";
      }

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
      else if (source instanceof CharacterButton)
      {
         CharacterButton charButton = (CharacterButton)source;

         unicodePanel.setHexString(charButton.getHexString());

         if (texMapping != null)
         {
            TeXLookup lookup = texMapping.get(charButton.getCodePoint());

            unicodePanel.setInfo(lookup == null ? "" : lookup.toString());
         }
         else
         {
            unicodePanel.setInfo("");
         }

         if ((event.getModifiers() & ActionEvent.SHIFT_MASK) == 0)
         {
            insert(charButton.getCodePoint());

            textfield.requestFocusInWindow();
         }
      }
   }

   public void valueChanged(ListSelectionEvent evt)
   {
      if (!evt.getValueIsAdjusting())
      {
         Object source = evt.getSource();

         int index = cardIndexTable.getSelectedRow();

         if (index == -1) return;

         cardLayout.show(cardPanel,
            cardIndexTable.getValueAt(index, 0).toString());
      }
   }

   public void insert(int codePoint)
   {
      int pos = textfield.getCaretPosition();

      StringBuilder builder = new StringBuilder();
      builder.appendCodePoint(codePoint);

      try
      {
         textfield.getDocument().insertString(pos, builder.toString(), null);
      }
      catch (BadLocationException e)
      {
         resources.debugMessage(e);
      }
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private SymbolSelectorListener symbolListener_;
   private JTextField textfield;

   private UnicodePanel unicodePanel;

   private Vector<BlockPanel> blockPanels;

   private JComponent cardPanel;

   private JTable cardIndexTable;

   private CardLayout cardLayout;

   private TeXMappings texMapping;

   private static final int MAX_ROWS = 20;

   private FlowframTk application_;

   private JDRResources resources;

   private JLabel modeLabel;
}

class BlockPanel extends JPanel
{
   public BlockPanel(Character.UnicodeBlock block, 
      SymbolSelectorListener symbolListener,
      ActionListener actionListener)
   {
      super(new GridBagLayout());

      gbc = new GridBagConstraints();

      gbc.gridx=0;
      gbc.gridy=0;
      gbc.gridwidth=1;
      gbc.gridheight=1;
      gbc.fill=GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.LINE_START;

      this.block = block;
      this.symbolListener = symbolListener;
      this.actionListener = actionListener;
   }

   public void addButton(int codePoint)
   {
      gbc.gridx++;

      if (gbc.gridx >= MAX_COLS)
      {
         gbc.gridx=0;
         gbc.gridy++;
      }

      CharacterButton button = new CharacterButton(codePoint, symbolListener);
      add(button, gbc);
      button.addActionListener(actionListener);
   }

   public Character.UnicodeBlock getBlock()
   {
      return block;
   }

   private Character.UnicodeBlock block;
   private SymbolSelectorListener symbolListener;
   private ActionListener actionListener;

   private GridBagConstraints gbc;

   private static final int MAX_COLS = 10;
}
