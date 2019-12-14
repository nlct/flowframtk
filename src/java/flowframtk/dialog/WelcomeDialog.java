// File          : WelcomeDialog.java
// Description   : Welcome frame
// Creation Date : 2015-10-18
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

public class WelcomeDialog extends JDialog
  implements ActionListener
{
   public WelcomeDialog(FlowframTkInvoker invoker)
   {
      super(invoker.getGUI(), 
        invoker.getResources().getString("welcome.title"), true);

      JDRResources resources = invoker.getResources();

      setIconImage(resources.getSmallAppIcon().getImage());

      JEditorPane editorPane = new JEditorPane("text/html",
        resources.getStringWithValues("welcome.text",
        new String[] {invoker.getName(), invoker.getVersion(),
         resources.getString("config.title"),
         resources.getString("settings.label"),
         resources.getString("settings.config"),
         resources.getString("configui.title"),
         resources.getString("settings.label"),
         resources.getString("settings.configui")}));

      Font font = editorPane.getFont();
      FontMetrics fm = getFontMetrics(font);
      editorPane.setPreferredSize(new Dimension(
        30*fm.getMaxAdvance(),
        20*fm.getHeight()
      ));

      editorPane.setEditable(false);
      JPanel buttonPanel = new JPanel();
      AbstractButton closeButton = resources.createCloseButton(this);
      buttonPanel.add(closeButton);

      JScrollPane sp = new JScrollPane(editorPane);

      editorPane.setCaretPosition(0);

      getContentPane().add(sp, "Center");
      getContentPane().add(buttonPanel, "South");

      pack();
      setLocationRelativeTo(invoker.getGUI());
      sp.getVerticalScrollBar().setValue(0);

      closeButton.requestFocusInWindow();
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null)
      {
         return;
      }

      if (action.equals("close"))
      {
         setVisible(false);
      }
   }
}
