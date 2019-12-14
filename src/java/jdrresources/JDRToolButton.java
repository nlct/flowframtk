// File        : JDRToolButton.java
// Description : Tool radio style button
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
 * Tool radio style button.
 * @author Nicola L C Talbot
 */
public class JDRToolButton extends JRadioButton
{
   public JDRToolButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, 
                     ActionListener listener, ButtonGroup g)
   {
      this(icon_up, icon_down, icon_rollover, listener, g, false, null);
   }

   public JDRToolButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, 
                     ActionListener listener, ButtonGroup g,
                     boolean selected)
   {
      this(icon_up, icon_down, icon_rollover, listener, g, selected, null);
   }

   public JDRToolButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, 
                     ActionListener listener, ButtonGroup g, 
                     boolean selected, String info)
   {
      this(icon_up, icon_down, icon_rollover, null, listener,
           g, selected, info);
   }

   public JDRToolButton(ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, ImageIcon icon_disabled,
                     ActionListener listener, ButtonGroup g, 
                     boolean selected, String info)
   {
      super();
      setIcon(icon_up);
      setSelected(selected);
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

      if (g != null)
      {
         g.add(this);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRToolButton(String text, ImageIcon icon_up, ImageIcon icon_down, 
                     ImageIcon icon_rollover, 
                     ImageIcon icon_disabled, 
                     ActionListener listener, ButtonGroup g,
                     boolean selected, String info)
   {
      super(text);
      setIcon(icon_up);
      setSelected(selected);
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

      if (g != null)
      {
         g.add(this);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRToolButton(String text,
                     ActionListener listener, ButtonGroup g,
                     boolean selected, String info)
   {
      super(text, selected);

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (g != null)
      {
         g.add(this);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

}
