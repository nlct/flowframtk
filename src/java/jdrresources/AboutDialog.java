// File          : AboutDialog.java
// Purpose       : Provides an "About Dialog"
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

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

/**
 * Provides an "About Dialog".
 * @author Nicola L C Talbot
 */

public class AboutDialog extends JDialog implements ActionListener
{
   public AboutDialog(JDRResources resources, JFrame parent, 
      String appName, String version)
   {
      super(parent,
            resources.getMessage("about.title", appName));

      info = new JTextPane();
      info.setEditable(false);
      info.setOpaque(false);

      StyledDocument doc = info.getStyledDocument();
      addStylesToDocument(doc);

      try
      {
         doc.insertString(0,
            resources.getMessage("about.version", appName, version), 
            doc.getStyle("bold"));

         addInfo(String.format("%n%s %s", 
                   resources.getString("about.copyright"),
                   "2006 Nicola L.C. Talbot"));

         addInfo(String.format("%n%s", 
             resources.getMessage("about.disclaimer", appName)));

         addInfo("\nhttps://www.dickimaw-books.com/\n");

         addInfo(resources.getString("about.see_licence"));

         String translator=resources.getString("about.translator");

         if (translator.length() > 0 && !translator.equals("?unknown?"))
         {
            addInfo(String.format("%s%n", 
               resources.getMessage("about.translated_by", translator)));
            addInfo(translator);

            String url = resources.getString("about.translator_url");

            if (url.length() > 0 && !url.equals("?unknown?"))
            {
               addInfo(url);
            }

            String transInfo
               = resources.getString("about.translator_info");

            if (transInfo.length() > 0 && !transInfo.equals("?unknown?"))
            {
               addInfo(transInfo);
            }
         }
      }
      catch (BadLocationException e)
      {
         resources.internalError(null, e);
      }

      getContentPane().add(info, "Center");

      Box b2 = Box.createVerticalBox();
      b2.add(new JLabel(resources.getLargeAppIcon()));
      b2.add(Box.createGlue());

      getContentPane().add(b2, "West");

      JPanel p2 = new JPanel();

      JDRButton closeButton = resources.createCloseButton(this);

      p2.add(closeButton);

      info
       .getInputMap(JComponent.WHEN_FOCUSED)
       .put(resources.getAccelerator("label.close"), "close");

      getContentPane().add(p2, "South");

      getRootPane().setDefaultButton(closeButton);

      pack();

      setLocationRelativeTo(parent);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action.equals("close"))
      {
         setVisible(false);
      }
   }

   private void addStylesToDocument(StyledDocument doc)
   {
      Style def = StyleContext.getDefaultStyleContext()
                     .getStyle(StyleContext.DEFAULT_STYLE);

      Style regular = doc.addStyle("regular", def);

      Style s = doc.addStyle("bold", regular);
      StyleConstants.setBold(s, true);
   }

   private void addInfo(String text) throws BadLocationException
   {
      addInfo(text, "regular");
   }

   private void addInfo(String text, String style)
      throws BadLocationException
   {
      StyledDocument doc = info.getStyledDocument();

      doc.insertString(doc.getLength(), "\n"+text, doc.getStyle(style));
   }

   private JTextPane info;
}

