// File          : ArrowStyleDialog.java
// Description   : Dialog for setting marker styles
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting marker style. (Used by {@link LineStylePanel}.)
 * @see ArrowStyleSelector
 * @author Nicola L C Talbot
 */

public class ArrowStyleDialog extends JDialog
   implements ActionListener
{
   public ArrowStyleDialog(JDRSelector selector, int type)
   {
      super(selector,
            ArrowStylePanel.getTitle(selector.getResources(), type), true);
      selector_ = selector;

      oldMarker = new JDRMarker(getResources().getMessageSystem());

      setLayout(new BorderLayout());

      panel = new ArrowStylePanel(selector, type);
      panel.setBorder(BorderFactory.createLoweredBevelBorder());

      add(panel, BorderLayout.CENTER);

      JPanel buttonPanel = new JPanel(new BorderLayout());
      buttonPanel.setBorder(BorderFactory.createEtchedBorder());

      add(buttonPanel, BorderLayout.SOUTH);

      JPanel p1 = new JPanel();
      buttonPanel.add(p1, BorderLayout.CENTER);

      p1.add(getResources().createOkayButton(this));
      p1.add(getResources().createCancelButton(this));
      p1.add(getResources().createHelpButton("sec:markers"));

      defaultButton = getResources().createDefaultButton(this);
      JPanel p2 = new JPanel();
      p2.add(defaultButton);

      buttonPanel.add(p2, BorderLayout.EAST);

      pack();
      setLocationRelativeTo(selector);
   }

   public void init()
   {
      oldMarker = getMarker();

      setVisible(true);
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
      else if (action.equals("default"))
      {
         setDefaults();
      }
   }

   public void okay()
   {
      setVisible(false);
   }

   public void cancel()
   {
      setMarker(oldMarker);
      setVisible(false);
   }

   public String getDescription()
   {
      return panel.getDescription();
   }

   public void setMarker(JDRMarker marker)
   {
      panel.setMarker(marker);
   }

   public JDRMarker getMarker()
   {
      return panel.getMarker();
   }

   public void setDefaults()
   {
      panel.setDefaults();
      selector_.repaint();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "ArrowStyleDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "default has focus: "+defaultButton.hasFocus()+eol;
      str += "old marker: "+oldMarker+eol;

      return str+eol;
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;
   private ArrowStylePanel panel;

   private JButton defaultButton;

   private JDRMarker oldMarker;
}
