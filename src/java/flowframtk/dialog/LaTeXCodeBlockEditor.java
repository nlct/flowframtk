/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
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

import java.util.regex.*;
import java.text.BreakIterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

/**
 * Component for editing a block of (La)TeX code.
 */
public class LaTeXCodeBlockEditor extends JPanel
  implements TeXEditorListener,ActionListener
{
   public LaTeXCodeBlockEditor(JDRFrame frame, String id)
   {
      super(new BorderLayout());

      this.frame = frame;
      FlowframTk application = frame.getApplication();
      JDRResources resources = getResources();

      String name = resources.getMessage(id);
      String tooltip = resources.getToolTipText(id);
      displayedMnemonic = resources.getMnemonic(id);

      setName(name);

      if (tooltip != null)
      {
         setToolTipText(tooltip);
      }

      Box toolBar = Box.createHorizontalBox();

      SlidingToolBar sToolBar =
         new SlidingToolBar(resources, toolBar, SwingConstants.HORIZONTAL);

      add(sToolBar, BorderLayout.NORTH);

      document = new TeXEditorDocument(this,
         application.getSettings());

      textPane = new LaTeXEditorPane(document, resources.getMessage("lang.widest_char"));

      undoManager = new UndoManager();

      popupM = new JPopupMenu(getName());

      undoItem = createButtonItem("menu.texeditor", "undo", toolBar, popupM);
      undoItem.setEnabled(false);

      redoItem = createButtonItem("menu.texeditor", "redo", toolBar, popupM);
      redoItem.setEnabled(false);

      popupM.addSeparator();

      popupM.add(resources.createAppMenuItem(
        "menu.texeditor.selectAllText", "selectAllText",
        null, this, getResources().getToolTipText("selectAllText")));

      cutItem = createButtonItem("menu.texeditor", "cutText", toolBar, popupM);

      copyItem = createButtonItem("menu.texeditor", "copyText", toolBar, popupM);

      createButtonItem("menu.texeditor", "pasteText", toolBar, popupM);

      popupM.addSeparator();

      findItem = createButtonItem("menu.texeditor", "find", toolBar, popupM);
      findAgainItem = createButtonItem("menu.texeditor", "find_again", 
        toolBar, popupM);

      findAgainItem.setEnabled(false);

      replaceItem = createButtonItem("menu.texeditor", "replace",
        toolBar, popupM);

      popupM.addSeparator();

      createButtonItem("menu.texeditor", "settings",
        toolBar, popupM);

      try
      {
         toolBar.add(resources.createHelpDialogButton(application, "sec:preamble"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      modified = false;

      document.addDocumentListener(new DocumentListener()
      {
         public void changedUpdate(DocumentEvent e)
         {
            markAsModified();
         }

         public void insertUpdate(DocumentEvent e)
         {
            markAsModified();
         }

         public void removeUpdate(DocumentEvent e)
         {
            markAsModified();
         }
      });

      textPane.addCaretListener(new CaretListener()
      {
         public void caretUpdate(CaretEvent evt)
         {
            updateEditButtons();
         }
      });

      findDialog = new FindDialog(application, textPane);

      MouseListener popupListener = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            showPopup(e);
         }

         public void mouseReleased(MouseEvent e)
         {
            showPopup(e);
         }
      };

      textPane.addMouseListener(popupListener);
      toolBar.addMouseListener(popupListener);

      KeyStroke popupKeyStroke = resources.getAccelerator("action.popup");

      Action popupAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent evt)
            {
               Point p = textPane.getCaret().getMagicCaretPosition();

               if (p == null)
               {
                  showPopup(textPane, 0, 0);
               }
               else
               {
                  showPopup(textPane, p.x, p.y);
               }
            }
         };

      if (popupKeyStroke != null)
      {
         textPane.getInputMap(JComponent.WHEN_FOCUSED)
            .put(popupKeyStroke, "show-popup");
         textPane.getActionMap().put("show-popup", popupAction);
      }

      KeyStroke contextMenuKeyStroke = resources.getAccelerator("action.context_menu");

      if (contextMenuKeyStroke != null)
      {
         textPane.getInputMap(JComponent.WHEN_FOCUSED)
            .put(contextMenuKeyStroke, "show-popup");

         if (popupKeyStroke == null)
         {
            textPane.getActionMap().put("show-popup", popupAction);
         }
      }

      JComponent mainComp = new JPanel(new BorderLayout());
      JLabel label = resources.createAppLabel("texeditor.latexcodeblock.code");
      mainComp.add(label, BorderLayout.NORTH);

      label.setLabelFor(textPane);

      mainComp.add(new JScrollPane(textPane), BorderLayout.CENTER);

      add(mainComp, BorderLayout.CENTER);
   }

   public void showPopup(MouseEvent e)
   {
      if (e.isPopupTrigger())
      {
         popupM.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   public void showPopup(Component comp, int x, int y)
   {
      popupM.show(comp, x, y);
   }

   private JDRButtonItem createButtonItem(String parentId, final String action,
     JComponent comp, JComponent menu)
   {
      String menuId = (parentId == null ? action : parentId+"."+action);

      KeyStroke keyStroke = getResources().getAccelerator(menuId);

      JDRButtonItem button = new JDRButtonItem(getResources(), menuId, action,
        null, this, getResources().getToolTipText(parentId),
        comp, menu);

      if (keyStroke != null)
      {
         textPane.getInputMap(JComponent.WHEN_FOCUSED)
            .put(keyStroke, action);
         textPane.getActionMap().put(action, new AbstractAction()
         {
            public void actionPerformed(ActionEvent evt)
            {
               doAction(action);
            }
         });
      }

      return button;
   }

   public boolean isEditing()
   {
      return textPane.hasFocus();
   }

   private void updateEditButtons()
   {
      boolean enable = canCutOrCopy();

      copyItem.setEnabled(enable);
      cutItem.setEnabled(enable);

      findAgainItem.setEnabled(canFindAgain());
   }

   public void replace(int startIdx, int endIdx, String replacement)
     throws BadLocationException
   {
      document.replace(startIdx, endIdx-startIdx, replacement);
   }

   /**
    * Sets the JTextPane's content but doesn't change the modified status.
    */
   public void setLaTeXCode(String text)
   {
      boolean isMod = isModified();
      try
      {
         document.setText(text == null ? "" : text);
      }
      catch (BadLocationException e)
      {
         e.printStackTrace();
      }

      revalidate();
      modified = isMod;
      undoManager.discardAllEdits();
      updateEditButtons();
      undoItem.setEnabled(false);
      redoItem.setEnabled(false);
   }

   public void updateLaTeXCode(String text)
   {
      int pos = textPane.getCaretPosition();
      textPane.selectAll();
      textPane.replaceSelection(text);

      if (text.isEmpty())
      {
         pos = 0;
      }
      else if (pos > text.length())
      {
         pos = text.length()-1;
      }

      textPane.setCaretPosition(pos);

      updateEditButtons();
      revalidate();
   }

   public void appendToLaTeXCode(boolean insertPar, String text)
   {
      if (insertPar && !getLaTeXCode().isEmpty())
      {
         text = String.format("%n%n%s", text);
      }

      appendToLaTeXCode(text);
   }

   public void appendToLaTeXCode(String text)
   {
      try
      {
         document.append(text);
      }
      catch (BadLocationException e)
      {
         getResources().error(this, e);
      }

      updateEditButtons();
      revalidate();
   }

   public String getLaTeXCode()
   {
      return textPane.getText();
   }

   public void markAsModified()
   {
      modified = true;
      frame.markAsModified();
   }

   public boolean isModified()
   {
      return modified;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      doAction(action);
   }

   public void doAction(String action)
   {
      if (action.equals("undo"))
      {
         try
         {
            undoManager.undo();
            undoItem.setEnabled(undoManager.canUndo());
         }
         catch (CannotUndoException e)
         {
            undoItem.setEnabled(false);
            getResources().debugMessage(e);
         }

         redoItem.setEnabled(undoManager.canRedo());
         revalidate();
      }
      else if (action.equals("redo"))
      {
         try
         {
            undoManager.redo();
            redoItem.setEnabled(undoManager.canRedo());
         }
         catch (CannotRedoException e)
         {
            getResources().debugMessage(e);
            redoItem.setEnabled(false);
         }

         undoItem.setEnabled(undoManager.canUndo());
         revalidate();
      }
      else if (action.equals("selectAllText"))
      {
         selectAllText();
      }
      else if (action.equals("copyText"))
      {
         copyText();
      }
      else if (action.equals("cutText"))
      {
         cutText();
      }
      else if (action.equals("pasteText"))
      {
         pasteText();
      }
      else if (action.equals("find"))
      {
         find();
      }
      else if (action.equals("find_again"))
      {
         findAgain();
      }
      else if (action.equals("replace"))
      {
         replace();
      }
      else if (action.equals("settings"))
      {
         frame.getApplication().displayTeXEditorUIDialog();
      }
   }

   public void selectAllText()
   {
      textPane.requestFocusInWindow();
      textPane.selectAll();
   }

   public void deselectText()
   {
      int end = textPane.getSelectionEnd();
      textPane.setSelectionStart(end);
      textPane.setSelectionEnd(end);
   }

   public void copyText()
   {
      textPane.requestFocusInWindow();
      textPane.copy();
   }

   public void cutText()
   {
      textPane.requestFocusInWindow();
      textPane.cut();
   }

   public void pasteText()
   {
      textPane.requestFocusInWindow();
      textPane.paste();
   }

   public void find()
   {
      String selectedText = textPane.getSelectedText();

      if (selectedText != null)
      {
         findDialog.setSearchText(selectedText);
      }

      findDialog.display(false);
   }

   public void findAgain()
   {
      findDialog.find();
   }

   public void replace()
   {
      String selectedText = textPane.getSelectedText();

      if (selectedText != null)
      {
         findDialog.setSearchText(selectedText);
      }

      findDialog.display(true);
   }

   public boolean canCutOrCopy()
   {
      String selected = (textPane == null ? null : textPane.getSelectedText());

      if (selected == null || selected.isEmpty())
      {
         return false;
      }

      return true;
   }

   public boolean canFindAgain()
   {
      return (findDialog == null ? false :
        !findDialog.getSearchText().isEmpty());
   }

   public void addEdit(UndoableEdit edit)
   {
      undoManager.addEdit(edit);

      try
      {
         undoItem.setEnabled(undoManager.canUndo());
      }
      catch (CannotUndoException e)
      {
         undoItem.setEnabled(false);
      }
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      document.updateStyles(settings);
      textPane.updateStyles(settings);
   }

   public Dimension getTextPaneSize()
   {
      return textPane.getSize();
   }

   protected JTextPane getTextPane()
   {
      return textPane;
   }

   public JDRResources getResources()
   {
      return frame.getResources();
   }

   public int getMnemonic()
   {
      return displayedMnemonic;
   }

   private LaTeXEditorPane textPane;

   private TeXEditorDocument document;

   private JDRButtonItem undoItem, redoItem, copyItem, cutItem;
   private JDRButtonItem findItem, findAgainItem, replaceItem;

   private JDRFrame frame;

   private FindDialog findDialog;

   private boolean modified = false;

   private JPopupMenu popupM;

   private UndoManager undoManager;

   private int displayedMnemonic = -1;
}

class LaTeXEditorPane extends JTextPane
{
   LaTeXEditorPane(TeXEditorDocument document, String widestChar)
   {
      super();
      this.widestChar = widestChar;
      setEditable(true);
      setEditorKit(new WrapEditorKit());
      setDocument(document);

      updateStyles(document.getSettings());
   }

   @Override
   public void setFont(Font font)
   {
      super.setFont(font);
      updateSize();
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      this.prefMaxColumns = settings.getCodeBlockEditorMaxColumns();
      setFont(settings.getTeXEditorFont());
   }

   public void setMaxColumns(int prefMaxColumns)
   {
      this.prefMaxColumns = prefMaxColumns;
      updateSize();
   }

   protected void updateSize()
   {
      FontMetrics fm = getFontMetrics(getFont());

      widestCharWidth = fm.stringWidth(widestChar==null?"M":widestChar);

      maxWidth = widestCharWidth * (prefMaxColumns+1);

      Insets insets = getInsets();

      Dimension dim = getPreferredSize();
      dim.width = maxWidth + insets.left + insets.right;
      setPreferredSize(dim);
   }

   public boolean getScrollableTracksViewportWidth() { return false; }

   private class WrapEditorKit extends StyledEditorKit
   {
      private final ViewFactory defaultFactory = new WrapColumnFactory();

      @Override
      public ViewFactory getViewFactory()
      {
         return defaultFactory;
      }
   }

   private class WrapColumnFactory implements ViewFactory
   {
      @Override
      public View create(final Element element)
      {
         final String name = element.getName();

         if (name != null)
         {
            switch (name)
            {
               case AbstractDocument.ContentElementName:
                  return new WrapLabelView(element);
               case AbstractDocument.ParagraphElementName:
                  return new ParagraphView(element);
               case AbstractDocument.SectionElementName:
                  return new BoxView(element, View.Y_AXIS);
               case StyleConstants.ComponentElementName:
                  return new ComponentView(element);
               case StyleConstants.IconElementName:
                  return new IconView(element);
            }
         }

         // Default to text display.
         return new WrapLabelView(element);
      }
   }

   // Adapted from GlyphView
   private class WrapLabelView extends LabelView
   {
      public WrapLabelView(Element element)
      {
         super(element);
      }

      @Override
      public float getMinimumSpan(int axis)
      {
         if (axis == View.X_AXIS)
         {
            if (minimumSpan < 0)
            {
               minimumSpan = 0;

               int p0 = getStartOffset();
               int p1 = getEndOffset();

               while (p1 > p0)
               {
                  int breakSpot = getBreakSpot(p0, p1);

                  if (breakSpot == BreakIterator.DONE)
                  {
                     // the rest of the view is non-breakable
                     breakSpot = p0;
                  }

                  minimumSpan = Math.max(minimumSpan,
                                getPartialSpan(breakSpot, p1));

                  // Note: getBreakSpot returns the *last* breakspot
                  p1 = breakSpot - 1;
               }
            }

            return minimumSpan;
         }

         return super.getMinimumSpan(axis);
      }

      @Override
      public int getBreakWeight(int axis, float pos, float len)
      {
         if (axis == View.X_AXIS)
         {
            // adapted from GlyphView.getBreakWeight()
            checkPainter();
            int p0 = getStartOffset();
            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);

            if (p1 == p0)
            {
               return View.BadBreakWeight;
            }

            int breakSpot = getBreakSpot(p0, p1);

            if (breakSpot == BreakIterator.DONE)
            {
               return View.GoodBreakWeight;
            }
            else
            {
               return View.ExcellentBreakWeight;
            }
         }

         return super.getBreakWeight(axis, pos, len);
      }

      @Override
      public View breakView(int axis, int p0, float pos, float len)
      {
         if (axis == View.X_AXIS)
         {
            checkPainter();
            int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);

            int breakSpot = getBreakSpot(p0, p1);

            if (breakSpot != BreakIterator.DONE)
            {
               p1 = breakSpot;
            }

            if (p0 == getStartOffset() && p1 == getEndOffset())
            {
               return this;
            }

            GlyphView v = (GlyphView) createFragment(p0, p1);

            //v.x = (int)pos;
            v.getTabbedSpan(pos, v.getTabExpander());

            return v;
         }

         return this;
      }

      @Override
      public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f)
      {
         breakSpots = null;
         minimumSpan = -1;
         super.insertUpdate(e, a, f);
      }

      @Override
      public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f)
      {
         breakSpots = null;
         minimumSpan = -1;
         super.removeUpdate(e, a, f);
      }

      public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f)
      {
         minimumSpan = -1;
         breakSpots = null;
         super.changedUpdate(e, a, f);
      }

      private int getBreakSpot(int p0, int p1)
      {
         if (breakSpots == null)
         {
            int start = getStartOffset();
            int end = getEndOffset();
            int[] bs = new int[end + 1 - start];
            int idx = 0;

            Element parent = getElement().getParentElement();
            int pstart = (parent == null ? start : parent.getStartOffset());
            int pend = (parent == null ? end : parent.getEndOffset());

            Segment s = getText(pstart, pend);

            BreakIterator breaker = BreakIterator.getCharacterInstance();
            breaker.setText(s);

            int startFrom = end + (pend > end ? 1 : 0);

            int prev = pend;
            boolean found = false;

            for (;;)
            {
               startFrom = breaker.preceding(s.offset + (startFrom - pstart))
                         + (pstart - s.offset);

               if (startFrom <= start)
               {
                  break;
               }

               String substr = getText(startFrom, prev).toString();

               if (isWordSeparator(substr))
               {
                  bs[idx++] = prev;

                  if (startFrom - pstart <= prefMaxColumns)
                  {
                     found = true;
                  }
               }

               prev = startFrom;
            }

            if (!found)
            {
               if (idx > 0)
               {
                  startFrom = bs[idx-1];
               }
               else
               {
                  startFrom = end + (pend > end ? 1 : 0);
               }

               idx = 0;

               for (;;)
               {
                  startFrom = breaker.preceding(s.offset + (startFrom - pstart))
                         + (pstart - s.offset);

                  if (startFrom <= start)
                  {
                     break;
                  }

                  bs[idx++] = startFrom;
               }
            }

            breakSpots = new int[idx];
            System.arraycopy(bs, 0, breakSpots, 0, idx);
         }

         int breakSpot = BreakIterator.DONE;

         for (int i = 0; i < breakSpots.length; i++)
         {
            int bsp = breakSpots[i];

            if (bsp <= p1)
            {
               if (bsp > p0)
               {
                  breakSpot = bsp;
               }

               break;
            }
         }

         return breakSpot;
      }

      boolean isWordSeparator(String str)
      {
         if (str.isEmpty()) return false;

         for (int i = 0; i < str.length(); )
         {
            int cp = str.codePointAt(i);
            i += Character.charCount(cp);

            if (!Character.isWhitespace(cp))
            {
               return false;
            }
         }

         return true;
      }

      private int[] breakSpots = null;
      private float minimumSpan = -1;
   }

   int prefMaxColumns, maxWidth, widestCharWidth;
   String widestChar;
}
