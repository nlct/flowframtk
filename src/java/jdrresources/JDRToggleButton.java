// File        : JDRButton.java
// Description : Toolbar buttons
// Date        : 5th June 2008
// Author      : Nicola L.C. Talbot
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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

/**
 * Toggle button.
 * @author Nicola L C Talbot
 */
public class JDRToggleButton extends JCheckBox
{
   public JDRToggleButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, ImageIcon icon_disabled,
                     ActionListener listener)
   {
      this(icon_up, icon_down, icon_rollover, icon_disabled, listener, null);
   }

   public JDRToggleButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover,
                     ActionListener listener)
   {
      this(icon_up, icon_down, icon_rollover, null, listener, null);
   }

   public JDRToggleButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, ImageIcon icon_disabled,
                     ActionListener listener, String info)
   {
      super();

      setText(null);
      setIcon(icon_up);
      setPressedIcon(icon_down);
      setSelectedIcon(icon_down);
      setRolloverIcon(icon_rollover);

      if (icon_disabled != null)
      {
         setDisabledIcon(icon_disabled);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRToggleButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover,
                     ActionListener listener, String info)
   {
      this(icon_up, icon_down, icon_rollover, null, listener, info);
   }

   public JDRToggleButton(String text, ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, ImageIcon icon_disabled,
                     ActionListener listener, String info)
   {
      super(text);

      setIcon(icon_up);

      setPressedIcon(icon_down);
      setSelectedIcon(icon_down);
      setRolloverIcon(icon_rollover);

      if (icon_disabled != null)
      {
         setDisabledIcon(icon_disabled);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRToggleButton(String text, ActionListener listener, String info)
   {
      super(text);

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

}
