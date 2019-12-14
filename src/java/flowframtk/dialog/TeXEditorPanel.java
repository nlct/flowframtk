// File          : TeXEditorPanel.java
// Description   : Component for typing TeX code
// Creation Date : 2014-05-15
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
public class TeXEditorPanel extends JPanel
  implements ActionListener,TeXEditorListener
{
   public TeXEditorPanel(FlowframTk application,
     JDialog parent, JComponent editM, JComponent searchM)
   {
      super(new BorderLayout());

      this.application = application;

      toolBar = Box.createHorizontalBox();

      SlidingToolBar sToolBar = 
         new SlidingToolBar(getResources(), toolBar, SwingConstants.HORIZONTAL);

      add(sToolBar, BorderLayout.NORTH);

      JPanel mainPanel = new JPanel(new BorderLayout());

      add(mainPanel, BorderLayout.CENTER);

      undoManager = new UndoManager();

      undoItem = createButtonItem("edit", "undo", toolBar, editM);

      redoItem = createButtonItem("edit", "redo", toolBar, editM);

      if (editM instanceof JMenu)
      {
         ((JMenu)editM).addSeparator();
      }
      else if (editM instanceof JPopupMenu)
      {
         ((JPopupMenu)editM).addSeparator();
      }

      editM.add(getResources().createAppMenuItem("texeditor", "selectText", this));

      cutItem = createButtonItem("texeditor", "cutText", toolBar, editM);

      copyItem = createButtonItem("texeditor", "copyText", toolBar, editM);

      createButtonItem("texeditor", "pasteText", toolBar, editM);

      if (searchM == editM)
      {
         if (editM instanceof JMenu)
         {
            ((JMenu)editM).addSeparator();
         }
         else if (editM instanceof JPopupMenu)
         {
            ((JPopupMenu)editM).addSeparator();
         }
      }

      findItem = createButtonItem("texeditor.search", "find", toolBar, searchM);
      findAgainItem = createButtonItem("texeditor.search", "find_again",
        toolBar, searchM);

      replaceItem = createButtonItem("texeditor.search", "replace",
        toolBar, searchM);

      document = new TeXEditorDocument(this,
         application.getSettings());
      textPane = new JTextPane(document);

      Font font = application.getTeXEditorFont();

      textPane.setFont(font);

      FontMetrics fm = getFontMetrics(font);

      textPane.setPreferredSize(new Dimension
         (application.getTeXEditorWidth()*fm.getMaxAdvance(),
          application.getTeXEditorHeight()*fm.getHeight()));

      textPane.setEditable(true);

      document.addDocumentListener(new DocumentListener()
      {
         public void changedUpdate(DocumentEvent e)
         {
            modified = true;
         }

         public void insertUpdate(DocumentEvent e)
         {
            modified = true;
         }

         public void removeUpdate(DocumentEvent e)
         {
            modified = true;
         }
      });

      textPane.addCaretListener(new CaretListener()
      {
         public void caretUpdate(CaretEvent evt)
         {
            updateEditButtons();
         }
      });

      updateEditButtons();

      findDialog = new FindDialog(getResources(), parent, textPane);

      textPane.setMinimumSize(new Dimension(0,0));
      mainPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
   }

   private JDRButtonItem createButtonItem(String parentId, String action,
     JComponent comp, JComponent menu)
   {
      return getResources().createButtonItem(parentId, action, this, comp, menu);
   }

   public void initialise(String text)
   {
      textPane.setFont(application.getTeXEditorFont());

      try
      {
         document.setText(text);
      }
      catch (BadLocationException e)
      {
         getResources().error(this, e);
      }

      undoManager.discardAllEdits();
      undoItem.setEnabled(false);
      redoItem.setEnabled(false);

      revalidate();

      modified = false;
      textPane.requestFocusInWindow();
      textPane.setCaretPosition(0);
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("undo"))
      {
         try
         {
            undoManager.undo();
         }
         catch (CannotUndoException e)
         {
            getResources().debugMessage(e);
         }

         undoItem.setEnabled(undoManager.canUndo());
         redoItem.setEnabled(undoManager.canRedo());
      }
      else if (action.equals("redo"))
      {
         try
         {
            undoManager.redo();
         }
         catch (CannotRedoException e)
         {
            getResources().debugMessage(e);
         }

         undoItem.setEnabled(undoManager.canUndo());
         redoItem.setEnabled(undoManager.canRedo());
      }
      else if (action.equals("selectText"))
      {
         textPane.requestFocusInWindow();
         textPane.selectAll();
      }
      else if (action.equals("copyText"))
      {
         textPane.requestFocusInWindow();
         textPane.copy();
      }
      else if (action.equals("cutText"))
      {
         textPane.requestFocusInWindow();
         textPane.cut();
      }
      else if (action.equals("pasteText"))
      {
         textPane.requestFocusInWindow();
         textPane.paste();
      }
      else if (action.equals("find"))
      {
         String selectedText = textPane.getSelectedText();

         if (selectedText != null)
         {
            findDialog.setSearchText(selectedText);
         }

         findDialog.display(false);
      }
      else if (action.equals("find_again"))
      {
         findDialog.find();
      }
      else if (action.equals("replace"))
      {
         String selectedText = textPane.getSelectedText();

         if (selectedText != null)
         {
            findDialog.setSearchText(selectedText);
         }

         findDialog.display(true);
      }
   }

   private void updateEditButtons()
   {
      String selected = (textPane == null ? null : textPane.getSelectedText());

      if (selected == null || selected.isEmpty())
      {
         copyItem.setEnabled(false);
         cutItem.setEnabled(false);
      }
      else
      {
         copyItem.setEnabled(true);
         cutItem.setEnabled(true);
      }

      findAgainItem.setEnabled(findDialog == null ? false :
        !findDialog.getSearchText().isEmpty());
   }

   public void addEdit(UndoableEdit edit)
   {
      undoManager.addEdit(edit);

      undoItem.setEnabled(true);
   }

   public void updateStyles(FlowframTkSettings settings)
   {
      document.updateStyles(settings);
      textPane.setFont(settings.getTeXEditorFont());
   }

   public boolean isModified()
   {
      return modified;
   }

   public void setModified(boolean isMod)
   {
      modified = isMod;
   }

   public String getText()
   {
      return textPane.getText();
   }

   public JTextPane getTextPane()
   {
      return textPane;
   }

   public JComponent getToolBar()
   {
      return toolBar;
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private JTextPane textPane;

   private TeXEditorDocument document;

   private JDRButtonItem undoItem, redoItem, copyItem, cutItem;
   private JDRButtonItem findItem, findAgainItem, replaceItem;

   private FlowframTk application;

   private boolean modified;

   private FindDialog findDialog;

   private UndoManager undoManager;

   private JComponent toolBar;
}
