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
public class LaTeXCodeEditor extends JPanel
{
   public LaTeXCodeEditor(JDRFrame frame)
   {
      super(new BorderLayout());

      tabbedPane = new JTabbedPane();
      add(tabbedPane, BorderLayout.CENTER);

      earlyPreamble = new LaTeXCodeBlockEditor(frame,
       frame.getResources().getMessage(
        "texeditor.latexcodeblock.earlypreamble"));

      tabbedPane.add(earlyPreamble);

      midPreamble = new LaTeXCodeBlockEditor(frame,
       frame.getResources().getMessage("texeditor.latexcodeblock.midpreamble"));

      tabbedPane.add(midPreamble);

      latePreamble = new LaTeXCodeBlockEditor(frame,
       frame.getResources().getMessage("texeditor.latexcodeblock.latepreamble"));

      tabbedPane.add(latePreamble);

      documentEnv = new LaTeXCodeBlockEditor(frame,
       frame.getResources().getMessage("texeditor.latexcodeblock.document"));

      tabbedPane.add(documentEnv);

      magicComments = new LaTeXCodeBlockEditor(frame,
       frame.getResources().getMessage("texeditor.latexcodeblock.magic_comments"));

      tabbedPane.add(magicComments);
   }

   public void showPopup(Component comp, int x, int y)
   {
      LaTeXCodeBlockEditor ed = 
        ((LaTeXCodeBlockEditor)tabbedPane.getSelectedComponent());

      if (ed != null)
      {
         ed.showPopup(comp, x, y);
      }
   }

   public boolean isEditing()
   {
      return earlyPreamble.isEditing()
          || midPreamble.isEditing()
          || latePreamble.isEditing()
          || documentEnv.isEditing()
          || magicComments.isEditing();
   }

   public void setLaTeXCode(String text,
     String midText, String endText, String docText, String magicText)
   {
      earlyPreamble.setLaTeXCode(text);
      midPreamble.setLaTeXCode(midText);
      latePreamble.setLaTeXCode(endText);
      documentEnv.setLaTeXCode(docText);
      magicComments.setLaTeXCode(magicText);
   }

   public void updateLaTeXCode(String text,
     String midText, String endText, String docText, String magicText)
   {
      earlyPreamble.updateLaTeXCode(text);
      midPreamble.updateLaTeXCode(midText);
      latePreamble.updateLaTeXCode(endText);
      documentEnv.updateLaTeXCode(docText);
      magicComments.updateLaTeXCode(magicText);
   }

   public void appendToLaTeXCode(String text)
   {
      earlyPreamble.appendToLaTeXCode(text);
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
      LaTeXCodeBlockEditor ed = 
        ((LaTeXCodeBlockEditor)tabbedPane.getSelectedComponent());

      if (ed != null)
      {
         ed.getTextPane().requestFocusInWindow();
      }
   }

   public String getEarlyPreambleText()
   {
      return earlyPreamble.getLaTeXCode();
   }

   public String getMidPreambleText()
   {
      return midPreamble.getLaTeXCode();
   }

   public String getEndPreambleText()
   {
      return latePreamble.getLaTeXCode();
   }

   public String getDocumentText()
   {
      return documentEnv.getLaTeXCode();
   }

   public String getMagicComments()
   {
      return magicComments.getLaTeXCode();
   }

   public boolean isModified()
   {
      return earlyPreamble.isModified()
          || midPreamble.isModified()
          || latePreamble.isModified()
          || documentEnv.isModified()
          || magicComments.isModified();
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      earlyPreamble.updateStyles(settings);
      midPreamble.updateStyles(settings);
      latePreamble.updateStyles(settings);
      documentEnv.updateStyles(settings);
      magicComments.updateStyles(settings);
   }

   private LaTeXCodeBlockEditor earlyPreamble, midPreamble, latePreamble,
    documentEnv, magicComments;

   private JTabbedPane tabbedPane;
}
