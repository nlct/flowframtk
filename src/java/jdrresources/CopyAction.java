// File          : CopyAction.java
// Purpose       : Action event for copying the entire contents of a 
//                 text area
// Date          : 4th June 2008
// Last Modified : 4th June 2008
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

import java.io.*;
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.filter.*;

/**
 * Action event for copying the entire contents from a text area.
 * @author Nicola L C Talbot
 */

public class CopyAction extends AbstractAction
{
   /**
    * Creates a new instance, associating this action with the 
    * given text area.
    * @param textArea the text area that can be copied
    */
   public CopyAction(JTextArea textArea)
   {
      textarea_ = textArea;
   }

   /**
    * Selects all the contents of the text area and copies to the
    * clipboard.
    * @param e the event that triggers this action
    */
   public void actionPerformed(ActionEvent e)
   {
      textarea_.selectAll();
      textarea_.copy();
   }

   private JTextArea textarea_;
}
