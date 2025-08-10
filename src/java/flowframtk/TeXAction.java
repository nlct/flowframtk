// File          : TeXAction.java
// Description   : TeX action (shapepar/parshape)
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
package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

/**
 * Action associated with computing shapepar/parshape parameters.
 * @author Nicola L C Talbot
 */
public class TeXAction extends FlowframTkAction
  implements FlowframTkActionListener
{
   public TeXAction(FlowframTk application, byte act)
   {
      super(application, act == PARSHAPE ? "parshape" : "shapepar", null, 
         TOOL_FLAG_SELECT, EDIT_FLAG_NONE,
         CONSTRUCTION_FLAG_NONE, SELECT_FLAG_PATH,
         SELECTION_SINGLE_SHAPE_NO_GROUP);

      setListener(this);
      application.addAppAction(this);

      actionId = act;

      String frameTitle = "";

      String helpID = null;

      switch (actionId)
      {
         case PARSHAPE :
            putValue(Action.NAME, getResources().getMessage("menu.tex.parshape"));
            frameTitle = getResources().getMessage("parshape.title");
            helpID = "sec:parshape";
         break;
         case SHAPEPAR :
            putValue(Action.NAME, getResources().getMessage("menu.tex.shapepar"));
            frameTitle = getResources().getMessage("shapepar.title");
            helpID = "sec:shapepar";
         break;
      }

      dbox = new JDialog(application, frameTitle, true);
      dbox.setSize(250,120);
      dbox.setLocationRelativeTo(application);

      JPanel options = new JPanel();
      options.setLayout(new GridLayout(2,1));
      ButtonGroup group = new ButtonGroup();

      pathButton = getResources().createAppRadioButton(
         "parshape", "use_path", group, true, null);
      options.add(pathButton);

      outlineButton = getResources().createAppRadioButton(
         "parshape", "use_outline", group, false, null);
      options.add(outlineButton);

      dbox.getContentPane().add(options, "Center");

      JPanel panel = new JPanel();

      ActionListener buttonAction = new TeXActionButtonListener(this);

      panel.add(getResources().createOkayButton(dbox.getRootPane(), buttonAction));
      panel.add(getResources().createCancelButton(buttonAction));

      try
      {
         panel.add(getResources().createHelpDialogButton(application, helpID));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      dbox.getContentPane().add(panel, "South");
   }

   public void doAction(FlowframTkAction action, ActionEvent evt)
   {
      dbox.setVisible(true);
   }

   public void okay()
   {
      boolean outline = outlineButton.isSelected();
      JDRFrame target = getFrame();

      switch (actionId)
      {
         case PARSHAPE :
            target.parshape(outline);
         break;
         case SHAPEPAR :
            target.shapepar(outline);
         break;
      }

      dbox.setVisible(false);
   }

   public void hideDialog()
   {
      dbox.setVisible(false);
   }

   public JDialog getDialog()
   {
      return dbox;
   }

   private byte actionId;
   public static final byte PARSHAPE=0, SHAPEPAR=1;
   private JRadioButton outlineButton, pathButton;
   private JDialog dbox;
}

class TeXActionButtonListener implements ActionListener
{
   private TeXAction texAction;

   public TeXActionButtonListener(TeXAction texAction)
   {
      this.texAction = texAction;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String actionCommand = evt.getActionCommand();

      if (actionCommand == null) return;

      if (actionCommand.equals("okay"))
      {
         texAction.okay();
      }
      else if (actionCommand.equals("cancel"))
      {
         texAction.hideDialog();
      }
   }

}
