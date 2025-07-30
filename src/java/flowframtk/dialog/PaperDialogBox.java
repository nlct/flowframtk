// File          : PaperDialogBox.java
// Description   : Dialog box to specify paper size
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box in which to specify paper size.
 * @author Nicola L C Talbot
 */
public class PaperDialogBox extends JDialog
   implements ActionListener
{
   public PaperDialogBox(FlowframTk application, JDRPaper paper)
   {
      super(application, application.getResources().getMessage("paper.title"),
            true);
      application_ = application;

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
      ButtonGroup group = new ButtonGroup();

      JPanel predefinedPanel = new JPanel(
         new FlowLayout(FlowLayout.LEFT));
      predefinedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.add(predefinedPanel);

      predefined = getResources().createAppRadioButton("paper", "predefined",
        group, false, this);

      predefinedPanel.add(predefined);

      predefinedBox = new JComboBox<String>(new String[]
      {
         getResources().getMessage("paper.a6"),
         getResources().getMessage("paper.a7"),
         getResources().getMessage("paper.a8"),
         getResources().getMessage("paper.a9"),
         getResources().getMessage("paper.a10"),
         getResources().getMessage("paper.b0"),
         getResources().getMessage("paper.b1"),
         getResources().getMessage("paper.b2"),
         getResources().getMessage("paper.b3"),
         getResources().getMessage("paper.b4"),
         getResources().getMessage("paper.b5"),
         getResources().getMessage("paper.b6"),
         getResources().getMessage("paper.b7"),
         getResources().getMessage("paper.b8"),
         getResources().getMessage("paper.b9"),
         getResources().getMessage("paper.b10"),
         getResources().getMessage("paper.c0"),
         getResources().getMessage("paper.c1"),
         getResources().getMessage("paper.c2"),
         getResources().getMessage("paper.c3"),
         getResources().getMessage("paper.c4"),
         getResources().getMessage("paper.c5"),
         getResources().getMessage("paper.c6"),
         getResources().getMessage("paper.c7"),
         getResources().getMessage("paper.c8"),
         getResources().getMessage("paper.c9"),
         getResources().getMessage("paper.c10")
      });
      predefinedPanel.add(predefinedBox);

      JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
      p1.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.add(p1);

      user = getResources().createAppRadioButton("paper", "user",
         group, false, this);
      p1.add(user);

      widthPanel = getResources().createNonNegativeLengthPanel("paper.width");
      widthPanel.getTextField().requestFocusInWindow();
      p1.add(widthPanel);

      heightPanel = getResources().createNonNegativeLengthPanel("paper.height");
      p1.add(heightPanel);

      getContentPane().add(panel, "Center");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(getRootPane(), this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpDialogButton(this, "sec:papermenu"));

      getContentPane().add(p2, "South");
      pack();
      setLocationRelativeTo(application_);

      setPaper(paper, application_.getDefaultCanvasGraphics().getStorageUnit());
   }

   public void initialise(boolean portrait)
   {
      mainPanel = application_.getCurrentFrame();
      portrait_ = portrait;

      CanvasGraphics cg = (mainPanel == null 
          ? application_.getDefaultCanvasGraphics()
          : mainPanel.getCanvasGraphics());

      setPaper(mainPanel == null ? cg.getPaper() : mainPanel.getPaper(),
        cg.getGrid().getMainUnit());
      setVisible(true);
   }

   public void setPaper(JDRPaper paper, JDRUnit unit)
   {
      widthPanel.setValue(unit.fromBp(paper.getWidth()), unit);
      heightPanel.setValue(unit.fromBp(paper.getHeight()), unit);

      portrait_ = paper.isPortrait();

      int id = portrait_ ? paper.getID()-JDRPaper.ID_A6
                         : paper.getID()-JDRPaper.ID_A6R;

      if (id >= 0)
      {
         predefined.setSelected(true);
         predefinedBox.setSelectedIndex(id);
         widthPanel.setEnabled(false);
         heightPanel.setEnabled(false);
         predefinedBox.setEnabled(true);
      }
      else
      {
         user.setSelected(true);
         widthPanel.setEnabled(true);
         heightPanel.setEnabled(true);
         predefinedBox.setEnabled(false);
      }
   }

   public JDRPaper getPaper()
   {
      if (predefined.isSelected())
      {
         int id = predefinedBox.getSelectedIndex();
         JDRPaper paper = JDRPaper.getPredefinedPaper(
            portrait_ ? id+JDRPaper.ID_A6 : id+JDRPaper.ID_A6R);

         if (paper != null)
         {
            return paper;
         }
      }

      return new JDRPaper(getResources().getMessageDictionary(), 
                          widthPanel.getValue(JDRUnit.bp),
                          heightPanel.getValue(JDRUnit.bp));
   }

   public void okay()
   {
      mainPanel.setPaperSize(getPaper());
      setVisible(false);
   }

   public void cancel()
   {
      JDRPaper oldPaper = mainPanel.getPaper();
      setVisible(false);
      application_.setPaperSize(oldPaper);
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         cancel();
      }
      else if (action.equals("predefined"))
      {
         widthPanel.setEnabled(false);
         heightPanel.setEnabled(false);
         predefinedBox.setEnabled(true);
      }
      else if (action.equals("user"))
      {
         widthPanel.setEnabled(true);
         heightPanel.setEnabled(true);
         predefinedBox.setEnabled(false);
      }
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "PaperDialogBox:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "widthPanel: "+widthPanel.info()+eol;
      str += "heightPanel: "+heightPanel.info()+eol;
      str += "predefined button has focus: "+predefined.hasFocus()+eol;
      str += "user button has focus: "+user.hasFocus()+eol;
      str += "predefined box has focus: "+predefinedBox.hasFocus()+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private NonNegativeLengthPanel widthPanel, heightPanel;
   private JRadioButton predefined, user;
   private JComboBox<String> predefinedBox;
   private static final int A6=0, A7=1, A8=2, A9=3, A10=4,
     B0=5, B1=6, B2=7, B3=8, B4=9, B5=10, C0=11, C1=12, C2=13,
     C3=14, C4=15, C5=16, C6=17, C7=18, C8=19, C9=20, C10=21;
   private boolean portrait_;

   private JDRFrame mainPanel = null;
}
