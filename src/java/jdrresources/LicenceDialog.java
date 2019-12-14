// File          : LicenceDialog.java
// Purpose       : Provides a licence dialog box
// Date          : 4th June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdrresources;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Provides a licence dialog box.
 * @author Nicola L C Talbot
 */

public class LicenceDialog extends JDialog implements ActionListener
{
   /**
    * Creates a new licence dialog box with the given parent.
    * The licence information is obtained from the resources
    * dictionary.
    * @param parent the parent component
    */
   public LicenceDialog(JDRResources resources, JFrame parent)
   {
      super(parent, resources.getString("licence.title"));

      JTextArea textarea = new JTextArea(10, 40);
      JScrollPane scrollPane = new JScrollPane(textarea);

      textarea.setText(resources.getString("licence"));
      textarea.setEditable(false);

      getContentPane().add(scrollPane, "Center");

      JPanel p2 = new JPanel();

      JDRButton closeButton = resources.createCloseButton(this);

      textarea.registerKeyboardAction(this, "close", 
         KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
         JComponent.WHEN_FOCUSED);

      p2.add(closeButton);
      getContentPane().add(p2, "South");

      setSize(550, 500);
      setLocationRelativeTo(parent);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("close"))
      {
         setVisible(false);
      }
   }
}
