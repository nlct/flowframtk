// File          : TeXEditorDialog.java
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

/**
 * Dialog box for editing (La)TeX code.
 */
public class TeXEditorDialog extends JDialog
  implements ActionListener
{
   public TeXEditorDialog(FlowframTk application)
   {
      super(application,
        application.getResources().getMessage("texeditor.frame_contents"),
        true);

      this.application = application;

      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            cancel();
         }
      });

      JMenuBar mbar = new JMenuBar();
      setJMenuBar(mbar);

      JMenu editM = getResources().createAppMenu("edit");
      mbar.add(editM);

      JMenu searchM = getResources().createAppMenu("texeditor.search");
      mbar.add(searchM);

      texEditorPanel = new TeXEditorPanel(application, this,
        editM, searchM);

      getContentPane().add(texEditorPanel, BorderLayout.CENTER);

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(getResources().createOkayButton(this));
      buttonPanel.add(getResources().createCancelButton(this));

      texEditorPanel.add(buttonPanel, BorderLayout.SOUTH);

      pack();

      setLocationRelativeTo(application);
   }

   public String display(String text)
   {
      texEditorPanel.initialise(text);

      setVisible(true);

      return texEditorPanel.isModified() ? 
        texEditorPanel.getText() : null;
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      }
      else if (action.equals("cancel"))
      {
         cancel();
      }
   }

   public void okay()
   {
      setVisible(false);
   }

   public void cancel()
   {
      if (texEditorPanel.isModified())
      {
         if (getResources().confirm(this, 
            getResources().getMessage("texeditor.discard_edit_query"),
            getResources().getMessage("texeditor.confirm_discard"))
            != JOptionPane.YES_OPTION)
         {
            return;
         }

         texEditorPanel.setModified(false);
      }

      setVisible(false);
   }

   public void updateStyles(FlowframTkSettings appSettings)
   {
      texEditorPanel.updateStyles(appSettings);
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private FlowframTk application;

   private TeXEditorPanel texEditorPanel;
}
