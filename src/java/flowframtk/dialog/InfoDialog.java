// File          : InfoDialog.java
// Description   : Dialog for displaying status information
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

import java.io.IOException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class InfoDialog extends JDialog
   implements ActionListener
{
   public InfoDialog(FlowframTk application, String helpSectionId)
   {
      super(application,
         application.getResources().getMessage("info.title"), false);
      application_ = application;
 
      helpId = helpSectionId;

      textArea = new JTextArea(10,20);
      textArea.setEditable(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      getContentPane().add(new JScrollPane(textArea), "Center");

      // Button panel

      JPanel p = new JPanel();
      getContentPane().add(p, "South");

      JDRResources resources = getResources();

      p.add(resources.createCloseButton(getRootPane(), this));

      helpButton = resources.createDialogButton("button.help", "help", this,
       resources.getAccelerator("button.help"));

      p.add(helpButton);

      pack();
      setLocationRelativeTo(application_);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("close"))
      {
         setVisible(false);
      }
      else if (action.equals("help"))
      {
         setVisible(false);

         try
         {
            if (targetRef != null)
            {
               getResources().getHelpLib().openHelp(targetRef);
            }
            else if (navNode != null)
            {
               getResources().getHelpLib().openHelp(navNode);
            }
            else if (helpId != null)
            {
               getResources().getHelpLib().openHelpForId(helpId);
            }
         }
         catch (Exception e)
         {
            getResources().error(application_, e);
         }
      }
   }

   public void display(String text)
   {
      display(text, null, null, null);
   }

   public void display(String text, TargetRef targetRef,
      NavigationNode navNode, String helpId)
   {
      update(text, targetRef, navNode, helpId);

      setVisible(true);
   }

   public void update(String text, TargetRef targetRef,
      NavigationNode navNode, String helpId)
   {
      this.helpId = helpId;
      this.navNode = navNode;
      this.targetRef = targetRef;

      textArea.setText(text);

      if (helpId == null && targetRef == null && navNode == null)
      {
         helpButton.setEnabled(false);
         helpButton.setVisible(false);
      }
      else
      {
         helpButton.setEnabled(true);
         helpButton.setVisible(true);
      }

   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;

   private JTextArea textArea;

   private AbstractButton helpButton;
   private String helpId;
   private NavigationNode navNode = null;
   private TargetRef targetRef = null;
}
