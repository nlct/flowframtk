// File          : TextSelector.java
// Description   : Dialog for editing text areas
// Creation Date : 6th February 2006
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

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for editing text areas.
 * @author Nicola L C Talbot
 */

public class TextSelector extends JDialog
   implements ActionListener,SymbolSelectorListener
{
   public TextSelector(FlowframTk application)
   {
      super(application, application.getResources().getMessage("edittext.title"),
         true);
      application_ = application;

      init();
   }

   void init()
   {
      JDRResources resources = getResources();

      symbolSelector = new CharacterSelector(resources, this, this,
        application_.getUnicodeRanges());

      Box mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(0.0f);
      getContentPane().add(mainPanel);

      JComponent row = createRow();
      mainPanel.add(row);

      JLabel textLabel = resources.createAppLabel("edittext.canvastext");
      row.add(textLabel);

      textbox = new JTextField()
      {
         @Override
         protected void paintComponent(Graphics g)
         {
            Graphics2D g2 = (Graphics2D)g;

            RenderingHints hints = getRenderingHints();

            if (hints != null)
            {
               RenderingHints oldHints = g2.getRenderingHints();
               g2.setRenderingHints(hints);
               super.paintComponent(g);
               g2.setRenderingHints(oldHints);
            }
         }

         @Override
         public Dimension getMaximumSize()
         {
            Dimension maxSize = super.getMaximumSize();
            Dimension prefSize = getPreferredSize();

            maxSize.height = prefSize.height;

            return maxSize;
         }
      };

      textLabel.setLabelFor(textbox);

      row.add(resources.createLabelSpacer());

      row.add(textbox);

      textpopupMenu = new JPopupMenu();

      copyText = new JMenuItem(
          resources.getMessage("menu.edit.copy"),
          resources.getCodePoint("menu.edit.copy.mnemonic"));
      textpopupMenu.add(copyText);

      copyText.setAccelerator(resources.getAccelerator("menu.edit.copy",
          KeyStroke.getKeyStroke(KeyEvent.VK_C,
            InputEvent.CTRL_DOWN_MASK)));

      copyText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.copy();
            }
         });

      cutText = new JMenuItem(
          resources.getMessage("menu.edit.cut"),
          resources.getCodePoint("menu.edit.cut.mnemonic"));
      textpopupMenu.add(cutText);

      cutText.setAccelerator(resources.getAccelerator("menu.edit.cut",
         KeyStroke.getKeyStroke(KeyEvent.VK_X,
           InputEvent.CTRL_DOWN_MASK)));

      cutText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.cut();
            }
         });

      JMenuItem pasteText = new JMenuItem(
          resources.getMessage("menu.edit.paste"),
          resources.getCodePoint("menu.edit.paste.mnemonic"));
      textpopupMenu.add(pasteText);

      pasteText.setAccelerator(resources.getAccelerator("menu.edit.paste",
         KeyStroke.getKeyStroke(KeyEvent.VK_V,
           InputEvent.CTRL_DOWN_MASK)));

      pasteText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.paste();
            }
         });

      JMenuItem select_allText = new JMenuItem(
          resources.getMessage("menu.edit.select_all"),
          resources.getCodePoint("menu.edit.select_all.mnemonic"));
      textpopupMenu.add(select_allText);

      select_allText.setAccelerator(resources.getAccelerator("menu.edit.select_all",
         KeyStroke.getKeyStroke(KeyEvent.VK_A,
           InputEvent.CTRL_DOWN_MASK)));

      select_allText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.selectAll();
            }
         });

      KeyStroke insertKeyStroke = resources.getAccelerator(
         "menu.textarea.insert_symbol");

      JMenuItem insertSymbol = new JMenuItem(
         resources.getMessage("menu.textarea.insert_symbol"),
         resources.getCodePoint("menu.textarea.insert_symbol.mnemonic"));
      textpopupMenu.add(insertSymbol);

      insertSymbol.setAccelerator(insertKeyStroke);

      insertSymbol.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               showSymbolSelector();
            }
         });

      textbox.addMouseListener(new MouseAdapter()
        {
           public void mousePressed(MouseEvent evt)
           {
              checkForPopupTrigger(evt);
           }

           public void mouseReleased(MouseEvent evt)
           {
              checkForPopupTrigger(evt);
           }

           private void checkForPopupTrigger(MouseEvent evt)
           {
              if (evt.isPopupTrigger())
              {
                 showPopUp(evt.getX(), evt.getY());
              }
           }
        });

      textbox.registerKeyboardAction(this, "insert",
         insertKeyStroke,
         JComponent.WHEN_FOCUSED);

      textbox.registerKeyboardAction(this, "popup",
         resources.getAccelerator("action.popup"),
         JComponent.WHEN_FOCUSED);

      textbox.registerKeyboardAction(this, "popup",
         getResources().getAccelerator("action.context_menu"),
         JComponent.WHEN_FOCUSED);

      mainPanel.add(Box.createVerticalStrut(10));
      mainPanel.add(Box.createVerticalGlue());

      tabbedPane = new JTabbedPane();
      tabbedPane.setAlignmentX(0f);
      mainPanel.add(tabbedPane);

      JComponent latexTextPanel = new JPanel(new BorderLayout());
      addTab(latexTextPanel, "edittext.latextext");

      row = createRow();
      latexTextPanel.add(row, "North");

      row.add(new JLabel(
         resources.getMessage("edittext.latextext.textlabel")));

      row.add(resources.createLabelSpacer());

      ButtonGroup group = new ButtonGroup();

      same = resources.createAppRadioButton(
         "edittext.latextext", "same", group, true, this);

      row.add(same);

      row.add(resources.createButtonSpacer());

      different = resources.createAppRadioButton(
         "edittext.latextext", "different", group, false, this);

      row.add(different);

      row.add(resources.createAppJButton("edittext.latextext", "remap", this));

      JTextArea textArea = resources.createAppInfoArea(2,
        "edittext.latextext.info");
      textArea.setAlignmentX(0f);
      latexTextPanel.add(textArea, "South");

      latexbox = new LaTeXCodeBlockEditor(application_, "edittext.latextext.codeblock",
        false, LATEX_CODE_ROWS, false);

      latexTextPanel.add(latexbox, "Center");

      matrixPanel = new TransformationMatrixPanel(resources,
        "edittext.transformation");

      addTab(matrixPanel, "edittext.transformation");

      textPathPanel = new TextPathPanel(resources, latexbox.getFont());
      addTab(textPathPanel, "edittext.textpath");

      JPanel buttonPanel = new JPanel();

      resources.createOkayCancelHelpButtons(this, buttonPanel, this, "sec:edittext");

      getContentPane().add(buttonPanel, "South");

      pack();

      textPathPanel.updateInfoSize();

      setLocationRelativeTo(application_);
   }

   JComponent createRow()
   {
      JComponent row = Box.createHorizontalBox();
      row.setAlignmentX(0f);
      return row;
   }

   void addTab(JComponent comp, String tag)
   {
      JDRResources resources = getResources();
      tabbedPane.add(comp);
      int idx = tabbedPane.getTabCount()-1;
      tabbedPane.setMnemonicAt(idx, resources.getMnemonic(tag));
      tabbedPane.setTitleAt(idx, resources.getMessage(tag+".title"));
   }

   public void display()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      textObject = mainPanel.getCanvas().getSelectedTextualObject();

      if (textObject == null)
      {
         getResources().internalError(
            getResources().getMessage("internal_error.cant_find_selected_text"));
         return;
      }

      JDRTextual textual = textObject.getTextual();

      if (textual instanceof JDRTextPath)
      {
         tabbedPane.setEnabledAt(TEXTPATH_TAB, true);

         JDRStroke stroke = ((JDRTextPath)textual).getStroke();

         textPathPanel.apply((JDRTextPathStroke)stroke);

         pack();
      }
      else
      {
         tabbedPane.setEnabledAt(TEXTPATH_TAB, false);

         pack();
      }

      matrixPanel.setMatrix(textual.getTransformation(new double[6]));

      styNames.clear();
      String latexText = textual.getLaTeXText();

      textbox.setText(textual.getText());
      textbox.requestFocusInWindow();

      if (latexText == null || latexText.isEmpty()
       || textual.getText().equals(latexText))
      {
         same.setSelected(true);
         latexbox.setLaTeXCode(textual.getText());
         latexbox.setVisible(false);
      }
      else
      {
         different.setSelected(true);
         latexbox.setVisible(true);
         latexbox.setLaTeXCode(latexText);
      }

      Font textFont = textual.getFont();
      float fontSize = application_.getSettings().getTeXEditorFontSize();

      buttonFont = textFont.deriveFont(fontSize);

      textbox.setFont(buttonFont);

      setVisible(true);
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      latexbox.updateStyles(settings);
      textPathPanel.setDelimFont(latexbox.getFont());

      float fontSize = settings.getTeXEditorFontSize();

      buttonFont = textbox.getFont().deriveFont(fontSize);
      textbox.setFont(buttonFont);
   }

   public void setSymbolText(String text)
   {
      textbox.setText(text);
      textbox.requestFocusInWindow();
   }

   public Font getSymbolFont()
   {
      return textbox.getFont();
   }

   public String getSymbolText()
   {
      return textbox.getText();
   }

   public Font getSymbolButtonFont()
   {
      return buttonFont;
   }

   public int getSymbolCaretPosition()
   {
      return textbox.getCaretPosition();
   }

   public void setSymbolCaretPosition(int position)
   {
      textbox.setCaretPosition(position);
   }

   public void requestSymbolFocus()
   {
      textbox.requestFocusInWindow();
   }

   public RenderingHints getRenderingHints()
   {
      return application_.getRenderingHints();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      JDRCanvas canvas = mainPanel.getCanvas();

      double[] newmatrix = matrixPanel.getMatrix(null);

      if (same.isSelected())
      {
         canvas.setText(textObject, textbox.getText(), "",
            textPathPanel.getLeftDelim(), textPathPanel.getRightDelim(),
            null, newmatrix);
      }
      else
      {
         canvas.setText(textObject, textbox.getText(),
                           latexbox.getLaTeXCode(),
            textPathPanel.getLeftDelim(), textPathPanel.getRightDelim(),
            styNames, newmatrix);
      }

      setVisible(false);
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
      else if (action.equals("same"))
      {
         latexbox.updateLaTeXCode(textbox.getText());
         latexbox.setVisible(false);
         textbox.requestFocusInWindow();
      }
      else if (action.equals("different"))
      {
         latexbox.setVisible(true);
         latexbox.requestEditorFocus();
      }
      else if (action.equals("insert"))
      {
         showSymbolSelector();
      }
      else if (action.equals("popup"))
      {
         Point p = textbox.getCaret().getMagicCaretPosition();

         showPopUp(p.x, p.y);
      }
      else if (action.equals("remap"))
      {
         styNames.clear();
         String ltxText = latexbox.getLaTeXCode().trim();

         if (ltxText.startsWith("$") && ltxText.endsWith("$"))
         {
            ltxText = "$"+application_.applyMathModeMappings(
                textbox.getText(), styNames)+"$";
         }
         else
         {
            ltxText = application_.applyTextModeMappings(
                textbox.getText(), styNames);
         }

         latexbox.setVisible(true);
         latexbox.updateLaTeXCode(ltxText);
         different.setSelected(true);
         latexbox.requestEditorFocus();
      }
   }

   public void showSymbolSelector()
   {
      symbolSelector.display(
       latexbox.getLaTeXCode().startsWith("$") ? 
       application_.getMathModeMappings() :
       application_.getTextModeMappings());
   }

   public void showPopUp(int x, int y)
   {
      String selectedText = textbox.getSelectedText();

      copyText.setEnabled(selectedText != null);
      cutText.setEnabled(selectedText != null);

      textpopupMenu.show(textbox, x, y);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "TextSelector:"+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "text box has focus: "+textbox.hasFocus()+eol;
      str += "LaTeX box has focus: "+latexbox.hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]
             + " Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private JDRTextualObject textObject;

   private JTextField textbox;
   private LaTeXCodeBlockEditor latexbox;
   private JPopupMenu textpopupMenu;
   private JRadioButton same, different;
   private Font buttonFont=null;
   private JMenuItem copyText, cutText;
   private CharacterSelector symbolSelector;

   private JTabbedPane tabbedPane;

   private TextPathPanel textPathPanel;

   private TransformationMatrixPanel matrixPanel;

   static final int LATEX_TAB=0, MATRIX_TAB=1, TEXTPATH_TAB=2;

   public static final int LATEX_CODE_ROWS=10;

   private Vector<String> styNames = new Vector<String>();
}

class TextPathPanel extends JPanel
{
   public TextPathPanel(JDRResources resources, Font fieldFont)
   {
      super(null);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      setAlignmentX(0.0f);

      Box box = Box.createHorizontalBox();
      box.setAlignmentX(0.0f);
      add(box);

      JLabel leftDelimLabel = resources.createAppLabel(
         "edittext.textpath.left_delim");

      box.add(leftDelimLabel);

      box.add(resources.createLabelSpacer());

      leftDelimField = new DelimField(fieldFont);
      leftDelimLabel.setLabelFor(leftDelimField);
      leftDelimField.setAlignmentX(0.0f);
      box.add(leftDelimField);

      box.add(resources.createButtonSpacer());

      JLabel rightDelimLabel = resources.createAppLabel(
         "edittext.textpath.right_delim");

      box.add(rightDelimLabel);

      box.add(resources.createLabelSpacer());

      rightDelimField = new DelimField(fieldFont);
      rightDelimLabel.setLabelFor(rightDelimField);
      rightDelimField.setAlignmentX(0.0f);
      box.add(rightDelimField);

      box.add(Box.createHorizontalGlue());

      add(Box.createVerticalStrut(10));

      textArea = resources.createAppInfoArea("edittext.textpath.info");
      textArea.setAlignmentX(0.0f);

      add(textArea);
   }

   public void apply(JDRTextPathStroke stroke)
   {
      leftDelimField.setCodePoint(stroke.getLeftDelim());
      rightDelimField.setCodePoint(stroke.getRightDelim());
   }

   public int getLeftDelim()
   {
      return leftDelimField.getCodePoint();
   }

   public int getRightDelim()
   {
      return rightDelimField.getCodePoint();
   }

   public void updateInfoSize()
   {
      textArea.setMinimumSize(textArea.getPreferredSize());
   }

   public void setDelimFont(Font font)
   {
      leftDelimField.setFont(font);
      rightDelimField.setFont(font);
   }

   private DelimField leftDelimField, rightDelimField;

   private JTextArea textArea;
}

class DelimField extends JTextField
{
   public DelimField(Font fieldFont)
   {
      super(new DelimFieldDocument(), "|", 1);
      setFont(fieldFont);

      setMaximumSize(getPreferredSize());
   }

   @Deprecated
   public char getChar()
   {
      String text = getText();

      return text.isEmpty() ? '\u0000' : text.charAt(0);
   }

   @Deprecated
   public void setChar(char delim)
   {
      setText(delim == '\u0000' ? "" : ""+delim);
   }

   public int getCodePoint()
   {
      String text = getText();

      return text.isEmpty() ? 0 : text.codePointAt(0);
   }

   public void setCodePoint(int delim)
   {
      if (delim == 0)
      {
         setText("");
      }
      else if (Character.charCount(delim) == 1)
      {
         setText(String.format("%c", (char)delim));
      }
      else
      {
         setText(new String(Character.toChars(delim)));
      }
   }
}

class DelimFieldDocument extends PlainDocument
{
   public DelimFieldDocument()
   {
      super();
   }

   public void insertString(int offs, String str, AttributeSet a)
      throws BadLocationException
   {
      if (str == null || str.isEmpty() || str.length() > 1) return;

      int delim = str.codePointAt(0);

      if (delim == '\\' || delim == '{' || delim == '}' || delim == '+'
        || getLength() >= Character.charCount(delim))
      {
         return;
      }

      super.insertString(offs, str, a);
   }
}
