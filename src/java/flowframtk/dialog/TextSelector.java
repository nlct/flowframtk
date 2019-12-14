// File          : TextSelector.java
// Description   : Dialog for editing text areas
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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
      super(application, application.getResources().getString("edittext.title"),
         true);
      application_ = application;

      symbolSelector = new CharacterSelector(getResources(), this, this,
        application_.getUnicodeRanges());

      Box mainPanel = Box.createVerticalBox();
      mainPanel.setAlignmentX(0.0f);
      getContentPane().add(mainPanel);

      JPanel p1 = new JPanel();
      p1.setAlignmentX(0.0f);
      p1.setLayout(new GridLayout(3,1));

      textbox = new JTextField()
      {
         public void paintComponent(Graphics g)
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
      };


      textpopupMenu = new JPopupMenu();

      copyText = new JMenuItem(
          getResources().getString("edit.copy"),
          getResources().getChar("edit.copy.mnemonic"));
      textpopupMenu.add(copyText);
      copyText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
         InputEvent.CTRL_MASK));
      copyText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.copy();
            }
         });

      cutText = new JMenuItem(
          getResources().getString("edit.cut"),
          getResources().getChar("edit.cut.mnemonic"));
      textpopupMenu.add(cutText);
      cutText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
         InputEvent.CTRL_MASK));
      cutText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.cut();
            }
         });

      JMenuItem pasteText = new JMenuItem(
          getResources().getString("edit.paste"),
          getResources().getChar("edit.paste.mnemonic"));
      textpopupMenu.add(pasteText);
      pasteText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
         InputEvent.CTRL_MASK));
      pasteText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.paste();
            }
         });

      JMenuItem select_allText = new JMenuItem(
          getResources().getString("edit.select_all"),
          getResources().getChar("edit.select_all.mnemonic"));
      textpopupMenu.add(select_allText);
      select_allText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
         InputEvent.CTRL_MASK));
      select_allText.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent evt)
            {
               textbox.selectAll();
            }
         });

      KeyStroke insertKeyStroke = getResources().getAccelerator(
         "text.insert_symbol");

      JMenuItem insertSymbol = new JMenuItem(
         getResources().getString("text.insert_symbol"),
         getResources().getChar("text.insert_symbol.mnemonic"));
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
         getResources().getAccelerator("accelerator.popup"),
         JComponent.WHEN_FOCUSED);

      textbox.registerKeyboardAction(this, "popup",
         getResources().getAccelerator("accelerator.alt_popup"),
         JComponent.WHEN_FOCUSED);

      p1.add(textbox);

      JPanel p3 = new JPanel();

      p3.add(new JLabel(
         getResources().getString("edittext.latexlabel")));

      ButtonGroup group = new ButtonGroup();

      same = getResources().createAppRadioButton(
         "edittext", "same", group, true, this);

      p3.add(same);

      different = getResources().createAppRadioButton(
         "edittext", "different", group, false, this);

      p3.add(different);

      p3.add(getResources().createAppJButton("edittext", "remap", this));

      p1.add(p3);

      latexbox = new JTextField();

      int fontSize = latexbox.getFont().getSize();

      Font ttFont = new Font("Monospaced", Font.PLAIN, fontSize);
      latexbox.setFont(ttFont);

      p1.add(latexbox);

      mainPanel.add(p1);

      mainPanel.add(Box.createVerticalStrut(10));

      textPathPanel = new TextPathPanel(getResources(), ttFont);
      mainPanel.add(textPathPanel);

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpButton("sec:edittext"));

      getContentPane().add(p2, "South");

      pack();

      textPathPanel.updateInfoSize();

      setLocationRelativeTo(application_);
   }

   public void display()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      JDRTextual text = mainPanel.getSelectedTextual();

      if (text == null)
      {
         getResources().internalError(
            getResources().getString("internal_error.cant_find_selected_text"));
         return;
      }

      if (text instanceof JDRTextPath)
      {
         textPathPanel.setVisible(true);

         JDRStroke stroke = ((JDRTextPath)text).getStroke();

         textPathPanel.apply((JDRTextPathStroke)stroke);

         pack();
      }
      else
      {
         textPathPanel.setVisible(false);

         pack();
      }

      styNames.clear();
      String latexText = text.getLaTeXText();

      textbox.setText(text.getText());
      textbox.requestFocusInWindow();
      latexbox.setText(latexText);

      if (latexText == null || latexText.isEmpty()
       || text.getText().equals(latexText))
      {
         same.setSelected(true);
         latexbox.setText(text.getText());
         latexbox.setEnabled(false);
      }
      else
      {
         different.setSelected(true);
         latexbox.setEnabled(true);
      }

      buttonFont = text.getFont().deriveFont(12);
      textbox.setFont(buttonFont);

      setVisible(true);
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

      if (same.isSelected())
      {
         mainPanel.setSelectedText(textbox.getText(),
            textPathPanel.getLeftDelim(), textPathPanel.getRightDelim());
      }
      else
      {
         mainPanel.setSelectedText(textbox.getText(),
                                   latexbox.getText(),
            textPathPanel.getLeftDelim(), textPathPanel.getRightDelim(),
            styNames);
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
         latexbox.setText(textbox.getText());
         latexbox.setEnabled(false);
         textbox.requestFocusInWindow();
      }
      else if (action.equals("different"))
      {
         latexbox.setEnabled(true);
         latexbox.requestFocusInWindow();
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
         String ltxText = latexbox.getText().trim();

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

         latexbox.setEnabled(true);
         latexbox.setText(ltxText);
         different.setSelected(true);
      }
   }

   public void showSymbolSelector()
   {
      symbolSelector.display(
       latexbox.getText().startsWith("$") ? 
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
   private JTextField textbox, latexbox;
   private JPopupMenu textpopupMenu;
   private JRadioButton same, different;
   private Font buttonFont=null;
   private JMenuItem copyText, cutText;
   private CharacterSelector symbolSelector;

   private TextPathPanel textPathPanel;

   private Vector<String> styNames = new Vector<String>();
}

class TextPathPanel extends JPanel
{
   public TextPathPanel(JDRResources resources, Font fieldFont)
   {
      super(null);

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      setAlignmentX(0.0f);

      setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),
        resources.getString("edittext.textpath")));

      Box box = Box.createHorizontalBox();
      box.setAlignmentX(0.0f);
      add(box);

      box.add(Box.createHorizontalGlue());

      JLabel leftDelimLabel = new JLabel(
         resources.getString("edittext.textpath.left_delim"));
      leftDelimLabel.setDisplayedMnemonic(
         resources.getChar("edittext.textpath.left_delim.mnemonic"));
      leftDelimLabel.setAlignmentX(0.0f);

      box.add(leftDelimLabel);

      leftDelimField = new DelimField(fieldFont);
      leftDelimLabel.setLabelFor(leftDelimField);
      leftDelimField.setAlignmentX(0.0f);
      box.add(leftDelimField);

      box.add(Box.createHorizontalStrut(20));

      JLabel rightDelimLabel = new JLabel(
         resources.getString("edittext.textpath.right_delim"));
      rightDelimLabel.setDisplayedMnemonic(
         resources.getChar("edittext.textpath.right_delim.mnemonic"));
      rightDelimLabel.setAlignmentX(0.0f);

      box.add(rightDelimLabel);

      rightDelimField = new DelimField(fieldFont);
      rightDelimLabel.setLabelFor(rightDelimField);
      rightDelimField.setAlignmentX(0.0f);
      box.add(rightDelimField);

      box.add(Box.createHorizontalGlue());

      add(Box.createVerticalStrut(10));

      textArea = new JTextArea(
         resources.getString("edittext.textpath.info"));
      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setAlignmentX(0.0f);

      add(textArea);
   }

   public void apply(JDRTextPathStroke stroke)
   {
      leftDelimField.setChar(stroke.getLeftDelim());
      rightDelimField.setChar(stroke.getRightDelim());
   }

   public char getLeftDelim()
   {
      return leftDelimField.getChar();
   }

   public char getRightDelim()
   {
      return rightDelimField.getChar();
   }

   public void updateInfoSize()
   {
      textArea.setMinimumSize(textArea.getPreferredSize());
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

   public char getChar()
   {
      String text = getText();

      return text.isEmpty() ? '\u0000' : text.charAt(0);
   }

   public void setChar(char delim)
   {
      setText(delim == '\u0000' ? "" : ""+delim);
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

      char delim = str.charAt(0);

      if (delim == '\\' || delim == '{' || delim == '}' || delim == '+'
        || getLength() > 0)
      {
         return;
      }

      super.insertString(offs, str, a);
   }
}
