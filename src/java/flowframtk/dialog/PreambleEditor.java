// File          : PreambleEditor.java
// Description   : Component for typing TeX code
// Creation Date : 2014-09-16
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

package com.dickimawbooks.flowframtk.dialog;

import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

/**
 * Component for editing (La)TeX code.
 */
public class PreambleEditor extends JPanel
{
   public PreambleEditor(JDRFrame frame)
   {
      super(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(
        frame.getResources().getString("texeditor.preamble")));

      tabbedPane = new JTabbedPane();
      add(tabbedPane, BorderLayout.CENTER);

      earlyPreamble = new PreamblePartEditor(frame);
      tabbedPane.add(frame.getResources().getString("texeditor.preamble.early"),
         earlyPreamble);

      midPreamble = new PreamblePartEditor(frame);
      tabbedPane.add(frame.getResources().getString("texeditor.preamble.mid"),
         midPreamble);

      latePreamble = new PreamblePartEditor(frame);
      tabbedPane.add(frame.getResources().getString("texeditor.preamble.late"),
         latePreamble);

   }

   public void showPopup(Component comp, int x, int y)
   {
      PreamblePartEditor ed = 
        ((PreamblePartEditor)tabbedPane.getSelectedComponent());

      if (ed != null)
      {
         ed.showPopup(comp, x, y);
      }
   }

   public boolean isEditing()
   {
      return earlyPreamble.isEditing()
          || midPreamble.isEditing()
          || latePreamble.isEditing();
   }

   public void setPreambleText(String text,
     String midText, String endText)
   {
      earlyPreamble.setPreambleText(text);
      midPreamble.setPreambleText(midText);
      latePreamble.setPreambleText(endText);
   }

   public void updatePreambleText(String text,
     String midText, String endText)
   {
      earlyPreamble.updatePreambleText(text);
      midPreamble.updatePreambleText(midText);
      latePreamble.updatePreambleText(endText);
   }

   public void appendToPreamble(String text)
   {
      earlyPreamble.appendToPreamble(text);
      tabbedPane.setSelectedIndex(0);
   }

   public void earlyReplace(int startIdx, int endIdx,
      String replacement)
   throws BadLocationException
   {
      earlyPreamble.replace(startIdx, endIdx, replacement);
   }

   public void display()
   {
      setVisible(true);
      PreamblePartEditor ed = 
        ((PreamblePartEditor)tabbedPane.getSelectedComponent());

      if (ed != null)
      {
         ed.getTextPane().requestFocusInWindow();
      }
   }

   public String getPreambleText()
   {
      return earlyPreamble.getPreambleText();
   }

   public String getMidPreambleText()
   {
      return midPreamble.getPreambleText();
   }

   public String getEndPreambleText()
   {
      return latePreamble.getPreambleText();
   }

   public boolean isModified()
   {
      return earlyPreamble.isModified()
          || midPreamble.isModified()
          || latePreamble.isModified();
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      earlyPreamble.updateStyles(settings);
      midPreamble.updateStyles(settings);
      latePreamble.updateStyles(settings);
   }

   private PreamblePartEditor earlyPreamble, midPreamble, latePreamble;

   private JTabbedPane tabbedPane;
}
