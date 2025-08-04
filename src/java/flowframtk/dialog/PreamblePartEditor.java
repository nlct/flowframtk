// File          : PreamblePartEditor.java
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
public class PreamblePartEditor extends JPanel
  implements TeXEditorListener,ActionListener
{
   public PreamblePartEditor(JDRFrame frame)
   {
      super(new BorderLayout());

      this.frame = frame;
      FlowframTk application = frame.getApplication();

      Box toolBar = Box.createHorizontalBox();

      SlidingToolBar sToolBar =
         new SlidingToolBar(getResources(), toolBar, SwingConstants.HORIZONTAL);

      setName(getResources().getMessage("texeditor.preamble"));

      add(sToolBar, BorderLayout.NORTH);

      document = new TeXEditorDocument(this,
         application.getSettings());
      textPane = new JTextPane(document);

      undoManager = new UndoManager();

      popupM = new JPopupMenu(getName());

      undoItem = createButtonItem("menu.edit", "undo", toolBar, popupM);
      undoItem.setEnabled(false);

      redoItem = createButtonItem("menu.edit", "redo", toolBar, popupM);
      redoItem.setEnabled(false);

      popupM.addSeparator();

      popupM.add(getResources().createAppMenuItem(
        "menu.texeditor.selectAllText", "selectAllText",
        null, this, getResources().getToolTipText("selectAllText")));

      cutItem = createButtonItem("menu.texeditor", "cutText", toolBar, popupM);

      copyItem = createButtonItem("menu.texeditor", "copyText", toolBar, popupM);

      createButtonItem("menu.texeditor", "pasteText", toolBar, popupM);

      popupM.addSeparator();

      findItem = createButtonItem("menu.texeditor.search", "find", toolBar, popupM);
      findAgainItem = createButtonItem("menu.texeditor.search", "find_again", 
        toolBar, popupM);

      findAgainItem.setEnabled(false);

      replaceItem = createButtonItem("menu.texeditor.search", "replace",
        toolBar, popupM);

      toolBar.add(getResources().createHelpDialogButton(application, "sec:preamble"));

      Font font = application.getTeXEditorFont();

      textPane.setFont(font);

      textPane.setEditable(true);

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

      add(new JScrollPane(textPane), BorderLayout.CENTER);
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

   // set text but don't change modified status
   public void setPreambleText(String text)
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

   public void updatePreambleText(String text)
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

   public void appendToPreamble(String text)
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

   public String getPreambleText()
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
      textPane.setFont(settings.getTeXEditorFont());
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

   private JTextPane textPane;

   private TeXEditorDocument document;

   private JDRButtonItem undoItem, redoItem, copyItem, cutItem;
   private JDRButtonItem findItem, findAgainItem, replaceItem;

   private JDRFrame frame;

   private FindDialog findDialog;

   private boolean modified = false;

   private JPopupMenu popupM;

   private UndoManager undoManager;
}
