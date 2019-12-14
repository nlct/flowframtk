// File          : JDRSelector.java
// Description   : Dialog for setting path and/or text styles
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting marker style.
 * @see ArrowStyleSelector
 * @author Nicola L C Talbot
 */

public class JDRSelector extends JDialog 
   implements ActionListener
{
   public JDRSelector(FlowframTk application,
                      String title, 
                      boolean showPaths, boolean showText,
                      boolean showAnchor)
   {
      super(application, true);
      application_ = application;
      setTitle(title);

      mainPanel = new JPanel();

      if (showPaths)
      {
         samplePathPanel = new SamplePathPanel(this, showText);

         Component comp;

         if (showText)
         {
            mainSplit = new JSplitPane(
               JSplitPane.HORIZONTAL_SPLIT,
               mainPanel,
               new JScrollPane(getSamplePathComp()));

            mainSplit.setResizeWeight(0.7);

            sampleTextPanel = new SampleTextPanel(this, showAnchor);

            JSplitPane split2 = new JSplitPane(
               JSplitPane.VERTICAL_SPLIT,
               new JScrollPane(getSampleTextComp()),
               mainSplit);

            split2.setResizeWeight(0.2);

            comp = split2;

            setSize(new Dimension(770, 820));
            setPreferredSize(new Dimension(770, 820));
         }
         else
         {
            mainSplit = new JSplitPane(
               JSplitPane.VERTICAL_SPLIT,
               new JScrollPane(getSamplePathComp()),
               mainPanel);

            mainSplit.setResizeWeight(0.5);

            comp = mainSplit;

            setSize(new Dimension(800, 700));
            setPreferredSize(new Dimension(800, 700));
         }

         add(comp);
      }
      else if (showText)
      {
         sampleTextPanel = new SampleTextPanel(this, showAnchor);

         //getSampleTextComp().setPreferredSize(new Dimension(600,200));

         mainSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(getSampleTextComp()),
            mainPanel);

         mainSplit.setResizeWeight(0.5);

         add(mainSplit);

         setSize(new Dimension(770, 500));
         setPreferredSize(new Dimension(770, 500));
      }

      // OK/Cancel etc panel

      actionPanel = new JPanel(new BorderLayout());
      add(actionPanel, "South");

      actionPanel.setBorder(BorderFactory.createEtchedBorder());

      JPanel panel = new JPanel();
      actionPanel.add(panel, "Center");

      panel.add(getResources().createOkayButton(this));
      panel.add(getResources().createCancelButton(this));

      help = getResources().createDialogButton("label.help", "help", null, null);
      panel.add(help);

      defaults = getResources().createDefaultButton(this);
      JPanel p2 = new JPanel();
      p2.add(defaults);

      actionPanel.add(p2, "East");

      //pack();

      setLocationRelativeTo(application_);
   }

   public JDRSelector(FlowframTk application,
                      String title, 
                      boolean showPaths)
   {
      this(application, title, showPaths, false, false);
   }

   public JDRSelector(FlowframTk application,
                      String title, 
                      boolean showPaths, boolean showText)
   {
      this(application, title, showPaths, showText, false);
   }

   public JDRSelector(FlowframTk application,
                      String title)
   {
      this(application, title, true, true, true);
   }

   public void addToMain(JComponent comp)
   {
      mainPanel.add(comp);
   }

   public void setToMain(JComponent comp)
   {
      if (mainSplit.getTopComponent() == mainPanel)
      {
         mainSplit.setTopComponent(comp);
      }
      else
      {
         mainSplit.setBottomComponent(comp);
      }

      mainPanel = comp;
   }

   public void setMain(JComponent comp)
   {
   }

   public void repaintSample()
   {
      if (samplePathPanel != null)
      {
         samplePathPanel.updateSamples();
         samplePathPanel.repaint();
      }

      if (sampleTextPanel != null)
      {
         sampleTextPanel.updateSamples();
         sampleTextPanel.repaint();
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("cancel"))
      {
         cancel();
      }
      else if (action.equals("default"))
      {
         setDefaults();
      }
      else if (action.equals("okay"))
      {
         okay();
      }
   }

   public void initialise()
   {
      if (samplePathPanel != null)
      {
         samplePathPanel.updateSamples();
      }

      if (sampleTextPanel != null)
      {
         sampleTextPanel.updateSamples();
      }
 
      setVisible(true);
   }

   public void okay()
   {
      setVisible(false);
   }

   public void cancel()
   {
      setVisible(false);
   }

   public void setDefaults()
   {
   }

   public int getFontSeries()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return 0;
      }

      return frame.getSelectedFontSeries();
   }

   public int getFontShape()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return 0;
      }

      return frame.getSelectedFontShape();
   }

   public JDRPaint getLinePaint()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return application_.getCurrentLinePaint();
      }

      JDRPaint paint = frame.getSelectedLinePaint();

      if (paint == null)
      {
         return application_.getCurrentLinePaint();
      }

      return paint;
   }

   public JDRPaint getTextPaint()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return application_.getCurrentTextPaint();
      }

      JDRPaint paint = frame.getSelectedTextPaint();

      if (paint == null)
      {
         return application_.getCurrentTextPaint();
      }

      return paint;
   }

   public JDRPaint getFillPaint()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return application_.getCurrentFillPaint();
      }

      JDRPaint paint = frame.getSelectedFillPaint();

      if (paint == null)
      {
         return application_.getCurrentFillPaint();
      }

      return paint;
   }

   public String getFontName()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return "Serif";
      }

      return frame.getSelectedFontName();
   }

   public JDRLength getFontSize()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return new JDRLength(application_.getMessageSystem(), 10, JDRUnit.pt);
      }

      return frame.getSelectedFontSize();
   }

   public int getHalign()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return 0;
      }

      return frame.getSelectedHalign();
   }

   public int getValign()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return 0;
      }

      return frame.getSelectedValign();
   }

   public JDRBasicStroke getStroke()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame == null)
      {
         return application_.getCurrentStroke();
      }

      JDRBasicStroke stroke = frame.getSelectedStroke();

      if (stroke == null)
      {
         return application_.getCurrentStroke();
      }

      return stroke;
   }

   public String getSampleText()
   {
      return getResources().getString("font.sample");
   }

   public RenderingHints getRenderingHints()
   {
      return application_.getRenderingHints();
   }

   public FlowframTk getApplication()
   {
      return application_;
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str="";

      str += "is visible: "+isVisible()+eol;
      str += "has focus: "+hasFocus()+eol;
      str += "defaults has focus: "+defaults.hasFocus()+eol;
      str += "help has focus: "+help.hasFocus()+eol;

      ActionMap actionMap = getRootPane().getActionMap();
      str += "action map: "+eol;

      Object[] allKeys = actionMap.allKeys();

      for (int i = 0; i < allKeys.length; i++)
      {
         str += "Key: "+allKeys[i]+" Action: "+actionMap.get(allKeys[i])+eol;
      }

      return str+eol;
   }

   public SamplePanel getSamplePathPanel()
   {
      return samplePathPanel;
   }

   public SamplePanel getSampleTextPanel()
   {
      return sampleTextPanel;
   }

   public JComponent getSamplePathComp()
   {
      return (JComponent)samplePathPanel;
   }

   public JComponent getSampleTextComp()
   {
      return (JComponent)sampleTextPanel;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (frame != null) return frame.getCanvasGraphics();

      return application_.getDefaultCanvasGraphics();
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   public FlowframTk application_;
   private SamplePanel samplePathPanel, sampleTextPanel;
   public JButton defaults, help;
   protected JPanel actionPanel;

   protected JComponent mainPanel;

   protected JSplitPane mainSplit;
}
