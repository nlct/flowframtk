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
      super(application, application.getResources().getString("polygon.title"));
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
      okayButton.setEnabled(false);
      doTaskButton.setEnabled(true);
      infoArea.setText("");

      if (shape instanceof JDRCompoundShape)
      {
         shapeArea = new Area(
           ((JDRCompoundShape)shape).getUnderlyingShape().getGeneralPath());
      }
      else
      {
         shapeArea = new Area(shape.getGeneralPath());
      }

      samplePanel.updateShape(shapeArea);

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

      setVisible(false);
   }

   public void okay()
   {
      canvas.convertToPolygon(shape, samplePanel.getPolygon());
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

   private void finishedTask(Shape polygon)
   {
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

   public void finishedTask(Shape polygon, String infoText, int numComponents,
    double areaDiff)
   {
      finishedTask(polygon);
      updateInfoArea(infoText, numComponents, areaDiff);
   }

   public double getFlatness()
   {
      return flatnessField.getDouble();
   }

   public Area getShapeArea()
   {
      return shapeArea;
   }

   public void updateInfoArea(String infoText, int numComponents, double areaDiff)
   {
      if (infoText == null)
      {
         infoArea.setText("");
      }
      else
      {
         infoArea.setText(String.format("%s%n%s%n%s",
          getResources().getMessage("polygon.size", numComponents),
          getResources().getMessage("polygon.area_diff", areaDiff),
          infoText));
      }
   }

   private FlowframTk application;
   private JDRCanvas canvas;
   private JDRShape shape;
   private Area shapeArea;

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

   public void updateShape(Shape shape)
   {
      this.shape = shape;

      bounds = shape.getBounds2D();

      setSize(new Dimension(
         (int)Math.ceil(bounds.getWidth()), 
         (int)Math.ceil(bounds.getHeight())));

      polygon = null;
   }

   public void setPolygon(Shape polygon)
   {
      this.polygon = polygon;

      repaint();
   }

   public Shape getPolygon()
   {
      return polygon;
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

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

      double offsetX = bounds.getX()+0.5*(bounds.getWidth()-dim.getWidth());
      double offsetY = bounds.getY()+0.5*(bounds.getHeight()-dim.getHeight());

      g2.translate(-offsetX, -offsetY);

      if (shape != null)
      {
         g2.setPaint(Color.ORANGE);
         g2.draw(shape);
      }

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

   private Shape polygon;
   private Shape shape;
   private Rectangle2D bounds;
}

class PolygonTask extends SwingWorker<Path2D,Void>
{
   public PolygonTask(ConvertToPolygonDialog dialog)
   {
      super();
      this.dialog = dialog;
   }

   protected Path2D doInBackground() throws InterruptedException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      double flatness = dialog.getFlatness();
      Area shapeArea = dialog.getShapeArea();

      PathIterator pi = shapeArea.getPathIterator(null, flatness);

      Path2D polygon = new Path2D.Double(pi.getWindingRule());
      polygon.append(pi, false);

      polygonPathInfo = new StringBuilder();

      numComponents = JDRShape.svg(polygonPathInfo, polygon);

      Area area = new Area(polygon);
      area.exclusiveOr(shapeArea);

      areaDiff = JDRShape.computeArea(area);

      return polygon;
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         dialog.finishedTask(get(), 
           polygonPathInfo == null ? null : polygonPathInfo.toString(),
           numComponents, areaDiff);
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
      }
   }

   private ConvertToPolygonDialog dialog;
   private StringBuilder polygonPathInfo;
   private double areaDiff = 0.0;
   private int numComponents = 0;
}
