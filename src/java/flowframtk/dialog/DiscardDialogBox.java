// File          : DiscardDialogBox.java
// Description   : Dialog box used to confirm discarding an image
// Date          : 1st February 2006
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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box used to confirm discarding an image.
 */

public class DiscardDialogBox extends JDialog
   implements ActionListener
{
   public DiscardDialogBox(FlowframTk application)
   {
      super(application, 
         application.getResources().getString("discard.title"),true);

      application_ = application;

      setLocationRelativeTo(application);

      infoLabel = new JLabel("");

      getContentPane().add(infoLabel, "North");

      frameListComponent = Box.createVerticalBox();

      getContentPane().add(new JScrollPane(frameListComponent), "Center");

      JPanel p2 = new JPanel(new FlowLayout(FlowLayout.TRAILING));

      saveButton = getResources().createDialogButton(
        "discard.save_all", "save_all", this,
         null, getResources().getString("discard.save_all"));

      p2.add(saveButton);

      discardButton = getResources().createDialogButton("discard.discard_all", "discard_all", this,
         null, getResources().getString("discard.discard_all"));

      p2.add(discardButton);

      p2.add(getResources().createCancelButton(this));

      getContentPane().add(p2, "South");
   }

   public byte display(Vector<JDRFrame> frames)
   {
      return display(frames, false);
   }

   public byte display(Vector<JDRFrame> frames,
    boolean exitAfter)
   {
      this.exitAfter = exitAfter;

      response = CANCEL; // in case user presses close icon

      frameListComponent.removeAll();

      for (int i = 0; i < frames.size(); i++)
      {
         frameListComponent.add(
           new DiscardFrameItem(this, frames.get(i)));
      }

      boolean state = (frames.size() > 1);

      discardButton.setVisible(state);
      saveButton.setVisible(state);

      infoLabel.setText(
        frames.size() == 1 ?
         getResources().getString("discard.image_not_saved") :
         getResources().getStringWithValue("discard.images_not_saved", frames.size()));

      revalidate();
      pack();

      setVisible(true);

      return response;
   }

   public byte display(JDRFrame frame)
   {
      exitAfter = false;
      response = CANCEL; // in case user presses close icon

      frameListComponent.removeAll();

      frameListComponent.add(new DiscardFrameItem(this, frame));

      infoLabel.setText(getResources().getString("discard.image_not_saved"));

      discardButton.setVisible(false);
      saveButton.setVisible(false);

      revalidate();
      pack();

      setVisible(true);

      return response;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("save_all"))
      {
         int n = frameListComponent.getComponentCount()-1;

         if (exitAfter && n == -1)
         {
            System.exit(0);
         }

         for (int i = 0; i <= n; i++)
         {
            Component comp = frameListComponent.getComponent(i);

            if (comp instanceof DiscardFrameItem)
            {
               DiscardFrameItem item = (DiscardFrameItem)comp;

               if (!application_.saveImage(item.getFrame(), 
                   exitAfter && i==n))
               {
                  return;
               }
            }
         }

         response = SAVE;
         setVisible(false);
      }
      else if (action.equals("discard_all"))
      {
         for (int i = 0; i < frameListComponent.getComponentCount(); i++)
         {
            Component comp = frameListComponent.getComponent(i);

            if (comp instanceof DiscardFrameItem)
            {
               ((DiscardFrameItem)comp).getFrame().discard();
            }
         }

         response = DISCARD;
         setVisible(false);

         if (exitAfter)
         {
            System.exit(0);
         }
      }
      else if (action.equals("cancel"))
      {
         response = CANCEL;
         setVisible(false);
      }
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   protected void removeItem(DiscardFrameItem item)
   {
      frameListComponent.remove(item);

      if (frameListComponent.getComponentCount() == 0)
      {
         response = DISCARD;
         setVisible(false);

         if (exitAfter)
         {
            System.exit(0);
         }
      }
      else
      {
         revalidate();
      }
   }

   private FlowframTk application_;
   private JLabel infoLabel;
   private byte response = CANCEL;

   private boolean exitAfter = false;

   private JComponent frameListComponent;

   private AbstractButton discardButton, saveButton;

   public static final byte DISCARD=0, SAVE=1, CANCEL=2;
}

class DiscardFrameItem extends JPanel implements ActionListener
{
   public DiscardFrameItem(DiscardDialogBox discardDialog,
      JDRFrame frame)
   {
      super(new FlowLayout(FlowLayout.LEADING));
      setAlignmentX(0.0f);
      this.frame = frame;
      this.discardDialog = discardDialog;

      JDRResources resources = frame.getResources();

      add(resources.createDialogButton("discard.save", "save", this,
         null, resources.getString("discard.save")));
      add(resources.createDialogButton("discard.discard", "discard", this,
         null, resources.getString("discard.discard")));

      JTextField textField = new JTextField(frame.getFilename());
      textField.setEditable(false);
      textField.setOpaque(false);
      textField.setBorder(BorderFactory.createEmptyBorder());

      add(textField);

      Dimension dim = getPreferredSize();
      Dimension framePrefSize = frame.getPreferredSize();

      if (dim.width > framePrefSize.width)
      {
         dim.width = framePrefSize.width;
         setPreferredSize(dim);
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if ("save".equals(action))
      {
         if (frame.getApplication().saveImage(frame))
         {
            discardDialog.removeItem(this);
         }
      }
      else if ("discard".equals(action))
      {
         frame.discard();
         discardDialog.removeItem(this);
      }
   }

   public JDRFrame getFrame()
   {
      return frame;
   }

   private JDRFrame frame;
   private DiscardDialogBox discardDialog;
}
