// File          : TeXEditorDocument.java
// Description   : Dialog box for typing TeX code
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

public class TeXEditorDocument extends DefaultStyledDocument
{
   public TeXEditorDocument(TeXEditorListener editorListener, FlowframTkSettings settings)
   {
      super();

      this.listener = editorListener;
      this.highlightOn = settings.isSyntaxHighlightingOn();

      StyleContext context = StyleContext.getDefaultStyleContext();
      attrPlain = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, Color.BLACK);
      attrControlSequence = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, settings.getControlSequenceHighlight());

      attrComment = new SimpleAttributeSet();
      StyleConstants.setItalic(attrComment, true);
      StyleConstants.setForeground(attrComment, 
         settings.getCommentHighlight());

      addUndoableEditListener(
       new UndoableEditListener()
       {
          public void undoableEditHappened(UndoableEditEvent event)
          {
             UndoableEdit edit = event.getEdit();

             if (edit instanceof AbstractDocument.DefaultDocumentEvent)
             {
                AbstractDocument.DefaultDocumentEvent docEvent =
                  ((AbstractDocument.DefaultDocumentEvent)edit);

                if (docEvent.getType() == DocumentEvent.EventType.CHANGE)
                {
                   listener.addEdit(new NonSignificantUndoableEdit(edit));
                }
                else
                {
                   listener.addEdit(edit);
                }
             }
             else
             {
                listener.addEdit(edit);
             }
          }
       });

   }

   public void updateStyles(FlowframTkSettings settings)
   {
      StyleContext context = StyleContext.getDefaultStyleContext();
      attrPlain = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, Color.BLACK);

      attrControlSequence = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, settings.getControlSequenceHighlight());

      attrComment = new SimpleAttributeSet();
      StyleConstants.setItalic(attrComment, true);
      StyleConstants.setForeground(attrComment, 
         settings.getCommentHighlight());
   }

   public void remove(int offset, int length)
     throws BadLocationException
   {
      super.remove(offset, length);

      updateHighlight();
   }

   public void insertString(int offset, String str, AttributeSet at)
     throws BadLocationException
   {
      super.insertString(offset, str, at);

      updateHighlight();
   }

   public void append(String text)
    throws BadLocationException
   {
      int offset = getLength();

      super.insertString(offset, text, attrPlain);

      if (!highlightOn) return;

      Matcher matcher = PATTERN_CS.matcher(text);

      while (matcher.find())
      {
         int newOffset = offset + matcher.start();
         int len = matcher.end() - matcher.start();

         String group = matcher.group();

         if (group.startsWith("%"))
         {
            setCharacterAttributes(newOffset, len, attrComment, true);
         }
         else
         {
            setCharacterAttributes(newOffset, len, attrControlSequence, false);
         }
      }
   }

   private void updateHighlight()
   throws BadLocationException
   {
      if (!highlightOn) return;

      String text = getText(0, getLength());

      setCharacterAttributes(0, getLength(), 
        attrPlain, true);

      Matcher matcher = PATTERN_CS.matcher(text);

      while (matcher.find())
      {
         int newOffset = matcher.start();
         int len = matcher.end() - newOffset;

         String group = matcher.group();

         if (group.startsWith("%"))
         {
            setCharacterAttributes(newOffset, len, attrComment, true);
         }
         else
         {
            setCharacterAttributes(newOffset, len, attrControlSequence, false);
         }
      }
   }

   public void replace(int offset, int length, String text,
      AttributeSet attrs)
   throws BadLocationException
   {
      super.replace(offset, length, text, attrs);

      updateHighlight();
   }

   public void replace(int offset, int length, String text)
     throws BadLocationException
   {
      replace(offset, length, text, attrPlain);
   }

   // This method is for initialising so bypass the undo/redo
   // stuff
   public void setText(String text)
     throws BadLocationException
   {
      super.remove(0, getLength());
      super.insertString(0, text, null);
      updateHighlight();
   }

   class NonSignificantUndoableEdit extends AbstractUndoableEdit
   {
      private UndoableEdit original;

      public NonSignificantUndoableEdit(UndoableEdit original)
      {
         super();
         this.original = original;
      }

      public boolean isSignificant()
      {
         return false;
      }

      public void undo()
      {
         original.undo();
      }

      public void redo()
      {
         original.redo();
      }

      public boolean canRedo() {return original.canRedo();}
      public boolean canUndo() {return original.canUndo();}

      public String getPresentationName()
      {
         return original.getPresentationName();
      }
   }

   private TeXEditorListener listener;

   private boolean highlightOn;

   private AttributeSet attrPlain;
   private AttributeSet attrControlSequence;
   private SimpleAttributeSet attrComment;

   private static final Pattern PATTERN_CS = Pattern.compile(
      "((?:\\\\[^a-zA-Z]{1})|(?:\\\\[a-zA-Z]+)|(?:[#~\\{\\}\\^\\$_])|(?:%.*))");
}
