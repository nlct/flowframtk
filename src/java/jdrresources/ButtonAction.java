// File          : ButtonAction.java
// Purpose       : Action event for buttons
// Date          : 2015-09-24
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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;

public class ButtonAction extends AbstractAction
{
   public ButtonAction(AbstractButton button,
     ActionListener buttonListener)
   {
      super(button.getActionCommand());

      putValue(ACTION_COMMAND_KEY, button.getActionCommand());

      this.listener = buttonListener;
   }

   public void actionPerformed(ActionEvent evt)
   {
      listener.actionPerformed(evt);
   }

   private ActionListener listener;
}
