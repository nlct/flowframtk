// File          : ConvertToPolygon.java
// Description   : Dialog box for converting a shape to a polygon
// Date          : 2020-08-29
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for converting a shape to a polygon.
 * @author Nicola L C Talbot
 */
public class ConvertToPolygonDialog extends JDialog implements ActionListener
{
   public ConvertToPolygonDialog(FlowframTk application)
   {
      super(application, application.getResources().getString("polygon.title"), true);
      this.application = application;

      init();
   }

   private void init()
   {
      JDRResources resources = application.getResources();

      JComponent mainComp = new JPanel(new BorderLayout()); 
      getContentPane().add(mainComp, "Center");

      samplePanel = new PolygonSamplePanel(this);
      mainComp.add(new JScrollPane(samplePanel), "Center");

      JComponent flatnessPanel = new JPanel();
      mainComp.add(flatnessPanel, "South");

      JLabel flatnessLabel = resources.createAppLabel("polygon.flatness");
      flatnessPanel.add(flatnessLabel);

      flatnessField = new NumberSpinnerField(1.0, 0.0, 100.0, 1.0);
      flatnessLabel.setLabelFor(flatnessField);

      flatnessPanel.add(flatnessField);

      doTaskButton = resources.createDialogButton(
        "polygon", "dotask", this, null);
      flatnessPanel.add(doTaskButton);

      JComponent rightComp = Box.createVerticalBox();
      mainComp.add(rightComp, "East");

      infoArea = resources.createAppInfoArea(24);
      rightComp.add(new JScrollPane(infoArea));

      JComponent buttonPanel = new JPanel();
      getContentPane().add(buttonPanel, "South");

      okayButton = resources.createOkayButton(this);

      buttonPanel.add(okayButton); 
      buttonPanel.add(resources.createCancelButton(this));

      pack();
      setLocationRelativeTo(application);
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public RenderingHints getRenderingHints()
   {
      return canvas == null ? application.getRenderingHints() 
        : canvas.getRenderingHints();
   }

   public void display(JDRFrame frame)
   {
      canvas = frame.getCanvas();
      shape = canvas.getSelectedShape();
      polygon = null;

      if (shape.isPolygon())
      {
         JOptionPane.showMessageDialog(this, 
           getResources().getString("polygon.no_curves"));

         return;
      }

      okayButton.setEnabled(false);
      doTaskButton.setEnabled(true);
      infoArea.setText("");

      samplePanel.updateBounds(shape);

      setVisible(true);
   }

   public void cancel()
   {
      if (task != null)
      {
         JDRResources resources = getResources();

         if (resources.confirm(this, resources.getString("process.confirm.abort"))
             != JOptionPane.YES_OPTION)
         {
            return;
         }

         if (task != null)// may have finished while user was responding
         {
            task.cancel(true);
         }

         task = null;
      }

      shape.getCanvasGraphics().setComponent(canvas);
      setVisible(false);
   }

   public void okay()
   {
      shape.getCanvasGraphics().setComponent(canvas);
      canvas.convertToPolygon(shape, polygon);
      setVisible(false);
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
      else if (action.equals("dotask"))
      {
         doTask();
      }
   }

   private void doTask()
   {
      flatnessField.setEnabled(false);
      okayButton.setEnabled(false);
      doTaskButton.setEnabled(false);
      task = new PolygonTask(this);
      task.execute();
   }

   public void taskFailed(Exception e)
   {
      finishedTask(null);
      getResources().error(this, e);
   }

   private void finishedTask(JDRShape polygon)
   {
      this.polygon = polygon;

      if (polygon == null)
      {
         infoArea.setText("");
         okayButton.setEnabled(false);
      }
      else
      {
         okayButton.setEnabled(true);
      }

      doTaskButton.setEnabled(true);

      samplePanel.setPolygon(polygon);
      flatnessField.setEnabled(true);
      task = null;
   }

   public void finishedTask(JDRShape polygon, String infoText, int numComponents,
    double xorArea, double perimeterLength, double polyArea)
   {
      finishedTask(polygon);
      updateInfoArea(infoText, numComponents, xorArea, perimeterLength, polyArea);
   }

   public double getFlatness()
   {
      return flatnessField.getDouble();
   }

   public JDRShape getOriginalShape()
   {
      return shape;
   }

   public JDRShape getPolygon()
   {
      return polygon;
   }

   public void updateInfoArea(String infoText, int numComponents, 
     double xorArea, double perimeterLength, double polyArea)
   {
      if (infoText == null)
      {
         infoArea.setText("");
      }
      else
      {
         JDRResources resources = getResources();
         String unit = polygon.getCanvasGraphics().getStorageUnit().getLabel();

         infoArea.setText(String.format("%s%n%s%n%s%n%s%n%s",
          resources.getMessage("polygon.size", numComponents),
          resources.getMessage("polygon.length", perimeterLength, unit),
          resources.getMessage("polygon.area", polyArea, unit),
          resources.getMessage("polygon.xor_area", xorArea, unit),
          infoText));
      }

      infoArea.setCaretPosition(0);
   }

   private FlowframTk application;
   private JDRCanvas canvas;

   private JDRShape shape, polygon;

   private PolygonSamplePanel samplePanel;
   private NumberSpinnerField flatnessField;
   private JButton doTaskButton, okayButton;
   private JTextArea infoArea;

   private PolygonTask task=null;
}

class PolygonSamplePanel extends JPanel
{
   public PolygonSamplePanel(ConvertToPolygonDialog dialog)
   {
      super(null);

      this.dialog = dialog;

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension d = tk.getScreenSize();
      d.width = d.width/2;
      d.height = d.height/2;

      setSize(d);
      setPreferredSize(d);

      setBackground(Color.WHITE);
   }

   public void updateBounds(JDRShape shape)
   {
      updateBounds(shape, null);
   }

   public void updateBounds(JDRShape shape, JDRShape polygonShape)
   {
      CanvasGraphics cg = shape.getCanvasGraphics();
      cg.setComponent(this);

      bounds = shape.getComponentBBox();

      setSize(new Dimension(
        (int)Math.ceil(bounds.getWidth()), (int)Math.ceil(bounds.getHeight())));

      originalShape = shape.getComponentGeneralPath();
      setPolygon(polygonShape);

      repaint();
   }

   public void setPolygon(JDRShape polygonShape)
   {
      if (polygonShape == null)
      {
         polygon = null;
      }
      else
      {
         polygon = polygonShape.getComponentGeneralPath();
      }

      repaint();
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      if (originalShape == null)
      {
         return;
      }

      Graphics2D g2 = (Graphics2D)g;

      AffineTransform oldAf = g2.getTransform();
      Paint oldPaint = g2.getPaint();
      RenderingHints oldHints = g2.getRenderingHints();

      RenderingHints hints = dialog.getRenderingHints();

      if (hints != null)
      {
         g2.setRenderingHints(hints);
      }

      Dimension dim = getSize();

      double offsetX = bounds.getMinX()+0.5*(bounds.getWidth()-dim.getWidth());
      double offsetY = bounds.getMinY()+0.5*(bounds.getHeight()-dim.getHeight());

      g2.translate(-offsetX, -offsetY);

      g2.setPaint(JDRObject.draftColor);
      g2.draw(originalShape);

      if (polygon != null)
      {
         g2.setPaint(Color.BLACK);
         g2.draw(polygon);
      }

      g2.setTransform(oldAf);
      g2.setPaint(oldPaint);
      g2.setRenderingHints(oldHints);
   }

   private ConvertToPolygonDialog dialog;
   private Shape originalShape, polygon;
   private BBox bounds;
}

class PolygonTask extends SwingWorker<JDRShape,Void>
{
   public PolygonTask(ConvertToPolygonDialog dialog)
   {
      super();
      this.dialog = dialog;
   }

   protected JDRShape doInBackground() 
      throws InterruptedException,InvalidShapeException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      double flatness = dialog.getFlatness();

      JDRShape shape = dialog.getOriginalShape();

      JDRShape polygon = shape.toPolygon(flatness);

      polygonPathInfo = new StringBuilder();

      numComponents = polygon.svg(polygonPathInfo);

      try
      {
         JDRShape area = shape.exclusiveOr(polygon);
         xorArea = area.computeArea();// storage units
      }
      catch (EmptyPathException e)
      {
         xorArea = 0.0;
      }

      perimeterLength = polygon.computePerimeter();
      polyArea = polygon.computeArea();

      return polygon;
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         dialog.finishedTask(get(), 
           polygonPathInfo == null ? null : polygonPathInfo.toString(),
           numComponents, xorArea, perimeterLength, polyArea);
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
      }
   }

   private ConvertToPolygonDialog dialog;
   private StringBuilder polygonPathInfo;
   private double xorArea = 0.0, perimeterLength=0.0, polyArea=0.0;
   private int numComponents = 0;
}
