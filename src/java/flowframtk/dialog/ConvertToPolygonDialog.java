// File          : ConvertToPolygon.java
// Description   : Dialog box for converting a shape to a polygon
// Date          : 2020-08-29
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020-2025 Nicola L.C. Talbot

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

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for converting a shape to a polygon.
 * @author Nicola L C Talbot
 */
public class ConvertToPolygonDialog extends JDialog
 implements ActionListener, JDRApp
{
   public ConvertToPolygonDialog(FlowframTk application)
   {
      super(application, application.getResources().getMessage("polygon.title"), true);
      this.application = application;

      init();
   }

   private void init()
   {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
       {
          public void windowClosing(WindowEvent evt)
          {
             cancel();
          }
       });

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

      JComponent bottomPanel = new JPanel(new BorderLayout());
      getContentPane().add(bottomPanel, "South");

      JComponent buttonPanel = new JPanel();
      bottomPanel.add(buttonPanel, "Center");

      okayButton = resources.createOkayButton(getRootPane(), this);

      buttonPanel.add(okayButton); 
      buttonPanel.add(resources.createCancelButton(this));

      try
      {
         buttonPanel.add(resources.createHelpDialogButton(this, "sec:converttopolygon"));
      }
      catch (HelpSetNotInitialisedException e)
      {
         getResources().internalError(null, e);
      }

      zoomComp = new ZoomComponent(this);
      bottomPanel.add(zoomComp, "West");

      resetButton = resources.createDialogButton(
        "polygon", "reload", this, null);
      bottomPanel.add(resetButton, "East");

      pack();
      setLocationRelativeTo(application);
   }

   @Override
   public JDRResources getResources()
   {
      return application.getResources();
   }

   @Override
   public double getCurrentMagnification()
   {
      CanvasGraphics cg = getCanvasGraphics();
   
      return cg == null ? 1.0 : cg.getMagnification();
   }

   @Override
   public void setCurrentMagnification(double factor)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg != null && factor > 0.0)
      {
         cg.setMagnification(factor);
         samplePanel.updateBounds(getOriginalShape(), getPolygon());
      }
   }

   @Override
   public double zoomAction(ZoomValue zoomValue)
   {
      updateCurrentFactor(zoomValue);

      samplePanel.updateBounds(getOriginalShape(), getPolygon());

      return getCurrentMagnification();
   }

   private void updateCurrentFactor(ZoomValue zoomValue)
   {
      String id = zoomValue.getActionCommand();
      BBox bbox = samplePanel.getBpBounds();

      if (bbox == null) return;

      double factor = getCurrentMagnification();

      if (id.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         double width = bbox.getWidth();

         if (width > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getWidth()/width;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         double height = bbox.getHeight();

         if (height > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getHeight()/(double)height;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         double width = bbox.getWidth();
         double height = bbox.getHeight();

         if (height <= 0 || width <= 0)
         {
            factor = 1.0;
         }
         else
         {
            Dimension dim = samplePanel.getSize();

            if (height > width)
            {
               factor = dim.getHeight()/height;
            }
            else
            {
               factor = dim.getWidth()/width;
            }
         }
      }
      else if (zoomValue instanceof PercentageZoomValue)
      {
         factor = ((PercentageZoomValue)zoomValue).getValue();
      }

      zoomComp.setZoom(zoomValue, factor);

      setCurrentMagnification(factor);
   }

   @Override
   public void showZoomChooser()
   {
   }

   public RenderingHints getRenderingHints()
   {
      return canvas == null ? application.getRenderingHints() 
        : canvas.getRenderingHints();
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return shape.getCanvasGraphics();
   }

   public void display(JDRFrame frame)
   {
      canvas = frame.getCanvas();
      shape = canvas.getSelectedShape();
      polygon = null;

      if (shape.isPolygon())
      {
         JOptionPane.showMessageDialog(this, 
           getResources().getMessage("polygon.no_curves"));

         return;
      }

      zoomComp.setZoom(application.getMagnification());
      setCurrentMagnification(application.getMagnification());
      okayButton.setEnabled(false);
      doTaskButton.setEnabled(true);
      infoArea.setText("");

      samplePanel.updateBounds(shape);

      setVisible(true);
   }

   protected void reset()
   {
      polygon = null;
      infoArea.setText("");
      samplePanel.updateBounds(shape);
   }

   public void cancel()
   {
      JDRResources resources = getResources();

      if (task != null)
      {
         if (resources.confirm(this, resources.getMessage("process.confirm.abort"))
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

      if (polygon != null &&
           resources.confirm(this, 
            resources.getMessage("polygon.confirm_discard"))
             != JOptionPane.YES_OPTION)
      {
         return;
      }

      setCurrentMagnification(application.getMagnification());
      shape.getCanvasGraphics().setComponent(canvas);
      setVisible(false);
   }

   public void okay()
   {
      setCurrentMagnification(application.getMagnification());
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
      else if (action.equals("reload"))
      {
         reset();
      }
   }

   private void doTask()
   {
      flatnessField.setEnabled(false);
      okayButton.setEnabled(false);
      resetButton.setEnabled(false);
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

      resetButton.setEnabled(true);
      doTaskButton.setEnabled(true);

      samplePanel.setPolygon(polygon);
      flatnessField.setEnabled(true);
      task = null;
   }

   public void finishedTask(JDRShape polygon, String infoText, int numComponents,
    double xorArea, double perimeterLength, double polyArea, double flatness)
   {
      finishedTask(polygon);
      updateInfoArea(infoText, numComponents, xorArea, perimeterLength, polyArea,
        flatness);
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
     double xorArea, double perimeterLength, double polyArea,
     double flatness)
   {
      if (infoText == null)
      {
         infoArea.setText("");
      }
      else
      {
         JDRResources resources = getResources();
         String unit = polygon.getCanvasGraphics().getStorageUnit().getLabel();

         infoArea.setText(String.format("%s%n%s%n%s%n%s%n%s%n%s",
          resources.getMessage("polygon.flatness_info", flatness),
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
   private JButton doTaskButton, okayButton, resetButton;
   private JTextArea infoArea;
   private ZoomComponent zoomComp;

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

   public BBox getBpBounds()
   {
      return bounds;
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

      flatness = dialog.getFlatness();

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
           numComponents, xorArea, perimeterLength, polyArea, flatness);
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
   private double flatness=1.0;
}
