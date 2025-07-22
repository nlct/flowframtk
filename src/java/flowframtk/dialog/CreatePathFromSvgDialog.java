// File          : CreatePathFromSvgDialog.java
// Description   : Dialog box for creating a shape from SVG specs
// Date          : 2020-09-01
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

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

// TODO add right-handed/left-handed coordinate system options

/**
 * Dialog box for converting a shape to a polygon.
 * @author Nicola L C Talbot
 */
public class CreatePathFromSvgDialog extends JDialog 
   implements ActionListener,UnitChangeListener,DocumentListener
{
   public CreatePathFromSvgDialog(FlowframTk application)
   {
      super(application, application.getResources().getMessage("svg_path.title"), true);
      this.application = application;

      init();
   }

   private void init()
   {
      JDRResources resources = application.getResources();

      samplePanel = new CreateFromSVGSamplePanel(this);

      JComponent detailsComp = new JPanel(new BorderLayout());

      detailsComp.add(resources.createAppInfoArea("svg_path.info",
        resources.getMessage("svg_path.dotask")), "North");

      svgField = new JTextArea(10, 24);
      svgField.getDocument().addDocumentListener(this);
      detailsComp.add(new JScrollPane(svgField), "Center");

      JComponent sidePanel = Box.createVerticalBox();
      detailsComp.add(sidePanel, "East");

      JComponent actionPanel = new JPanel();
      sidePanel.add(actionPanel);

      unitLabel = resources.createAppLabel("svg_path.unit");
      actionPanel.add(unitLabel);

      unitBox = new UnitField();
      unitLabel.setLabelFor(unitBox);

      actionPanel.add(unitBox);

      doTaskButton = resources.createDialogButton(
        "svg_path", "dotask", this, null);
      actionPanel.add(doTaskButton);

      defaultUnitInfo = resources.getMessage("svg_path.default_unit_info");
      updateUnitInfo = resources.getMessage("svg_path.update_unit_info");

      unitInfoField = resources.createAppInfoArea(15);
      unitInfoField.setText(defaultUnitInfo);
      sidePanel.add(unitInfoField);

      JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        new JScrollPane(samplePanel), detailsComp);
      splitPane.setResizeWeight(0.5);

      getContentPane().add(splitPane, "Center");

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

   public String getMessage(String key, Object... params)
   {
      return application.getResources().getMessage(key, params);
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return frame.getCanvasGraphics();
   }

   public RenderingHints getRenderingHints()
   {
      return application.getRenderingHints();
   }

   public void display(JDRFrame frame)
   {
      this.frame = frame;

      okayButton.setEnabled(false);
      doTaskButton.setEnabled(true);
      unitBox.setEnabled(true);
      svgField.setEnabled(true);
      svgField.setText("");

      setWorkingShape(null);

      setVisible(true);
   }

   public void cancel()
   {
      if (!confirmClose()) return;

      setVisible(false);
   }

   public void okay()
   {
      if (!confirmClose()) return;

      frame.getCanvas().addObject(shape, getResources().getMessage("undo.new_path"));
      setVisible(false);
   }

   private boolean confirmClose()
   {
      JDRResources resources = getResources();

      if (task != null)
      {
         if (resources.confirm(this, resources.getMessage("process.confirm.abort"))
             != JOptionPane.YES_OPTION)
         {
            return false;
         }

         if (task != null)// may have finished while user was responding
         {
            task.cancel(true);
         }

         task = null;
      }
      else if (modified
         && resources.confirm(this, resources.getMessage("svg_path.confirm.abandon"))
             != JOptionPane.YES_OPTION)
      {
         return false;
      }

      return true;
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
      String svgSpecs = svgField.getText().trim();

      if (svgSpecs.isEmpty())
      {
         getResources().error(this, getResources().getMessage("error.no_path_data"));
         return;
      }

      okayButton.setEnabled(false);
      doTaskButton.setEnabled(false);
      unitBox.setEnabled(false);
      svgField.setEnabled(false);
      setWorkingShape(null);
      task = new CreateShapeTask(this, svgSpecs);
      task.execute();
   }

   public void taskFailed(Exception e)
   {
      finishedTask(null);
      getResources().error(this, e);
   }

   public void finishedTask(JDRPath newShape)
   {
      shape = newShape;

      if (shape == null)
      {
         okayButton.setEnabled(false);
      }
      else
      {
         okayButton.setEnabled(true);

         shape.setStyle(frame.getCurrentLinePaint(), frame.getCurrentFillPaint(), 
            frame.getCurrentStroke());
      }

      unitInfoField.setText(defaultUnitInfo);
      doTaskButton.setEnabled(true);
      unitBox.setEnabled(true);
      svgField.setEnabled(true);

      modified = false;
      task = null;
   }

   public void setWorkingShape(Path2D shape)
   {
      samplePanel.setWorkingShape(shape);
   }

   public void addToBounds(Rectangle rect)
   {
      samplePanel.addToBounds(rect);
   }

   public void updateWorkingShapeBounds()
   {
      samplePanel.updateWorkingShapeBounds();
   }

   public double getValue(double num)
   {
      JDRUnit canvasUnit = getCanvasGraphics().getStorageUnit();
      JDRUnit svgUnit = unitBox.getUnit();

      return canvasUnit.fromUnit(num, svgUnit);
   }

   public void unitChanged(UnitChangeEvent evt)
   {
      if (shape != null)
      {
         unitInfoField.setText(updateUnitInfo);
      }
   }

   private void svgDataChanged()
   {
      modified = true;
   }

   public void changedUpdate(DocumentEvent e)
   {
      svgDataChanged();
   }

   public void insertUpdate(DocumentEvent e)
   {
      svgDataChanged();
   }

   public void removeUpdate(DocumentEvent e)
   {
      svgDataChanged();
   }

   private FlowframTk application;
   private JDRFrame frame;

   private JDRPath shape;

   private CreateFromSVGSamplePanel samplePanel;
   private JButton doTaskButton, okayButton;
   private JTextArea svgField, unitInfoField;
   private UnitField unitBox;
   private JLabel unitLabel;

   boolean modified = false;

   private String defaultUnitInfo, updateUnitInfo;

   private CreateShapeTask task=null;
}

class CreateFromSVGSamplePanel extends JPanel implements Scrollable
{
   public CreateFromSVGSamplePanel(CreatePathFromSvgDialog dialog)
   {
      super(null);

      this.dialog = dialog;

      Toolkit tk = Toolkit.getDefaultToolkit();
      viewportDim = tk.getScreenSize();
      viewportDim.width = viewportDim.width/2;
      viewportDim.height = viewportDim.height/2;

      setSize(viewportDim);
      setPreferredSize(viewportDim);

      setBackground(Color.WHITE);
   }

   public Dimension getPreferredScrollableViewportSize()
   {
      return viewportDim;
   }

   public int getScrollableBlockIncrement(Rectangle visibleRect,
     int orientation, int direction)
   {
      int currentPos = 0;
      int inc;

      if (orientation == SwingConstants.HORIZONTAL)
      {
         currentPos = visibleRect.x;
         inc = viewportDim.width;
      }
      else
      {
         currentPos = visibleRect.y;
         inc = viewportDim.height;
      }

      if (direction < 0)
      {
         int newPosition = currentPos - (currentPos/inc) * inc;

         return newPosition==0 ? inc : newPosition;
      }
      else
      {
         return ((currentPos/inc)+1)*inc - currentPos;
      }
   }

   public int getScrollableUnitIncrement(Rectangle visibleRect,
     int orientation, int direction)
   {
      int currentPos = 0;
      int inc = 100;

      if (orientation == SwingConstants.HORIZONTAL)
      {
         currentPos = visibleRect.x;
      }
      else
      {
         currentPos = visibleRect.y;
      }

      if (direction < 0)
      {
         int newPosition = currentPos - (currentPos/inc) * inc;

         return newPosition==0 ? inc : newPosition;
      }
      else
      {
         return ((currentPos/inc)+1)*inc - currentPos;
      }
   }

   public boolean getScrollableTracksViewportHeight()
   {
      return false;
   }

   public boolean getScrollableTracksViewportWidth()
   {
      return false;
   }

   public void addToBounds(Rectangle rect)
   {
      if (bounds == null)
      {
         bounds = rect;
      }
      else
      {
         bounds.add(rect);
      }

      int w = (int)Math.max(viewportDim.width, bounds.width);
      int h = (int)Math.max(viewportDim.height, bounds.height);

      setSize(new Dimension(w, h));
      repaint(rect);
   }

   public void setWorkingShape(Path2D newShape)
   {
      workingShape = newShape;

      updateWorkingShapeBounds();
   }

   public void updateWorkingShapeBounds()
   {
      if (workingShape != null && workingShape.getCurrentPoint() != null)
      {
         bounds = workingShape.getBounds2D().getBounds();

         Dimension dim = getPreferredSize();

         int w = (int)Math.max(viewportDim.width, bounds.width);
         int h = (int)Math.max(viewportDim.height, bounds.height);

         setSize(new Dimension(w, h));
         repaint();
      }
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      if (workingShape == null)
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

      g2.setPaint(Color.BLACK);
      g2.draw(workingShape);

      g2.setTransform(oldAf);
      g2.setPaint(oldPaint);
      g2.setRenderingHints(oldHints);
   }

   private CreatePathFromSvgDialog dialog;
   private Path2D workingShape;
   private Rectangle bounds = null;
   private Dimension viewportDim;
}

class CreateShapeTask extends SwingWorker<JDRPath,Rectangle>
{
   public CreateShapeTask(CreatePathFromSvgDialog dialog, String specs)
   {
      super();
      this.dialog = dialog;
      this.specs = specs;
   }

   protected JDRPath doInBackground() 
      throws InterruptedException,InvalidFormatException
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      Path2D workingShape = new Path2D.Double();

      dialog.setWorkingShape(workingShape);

      double prevX = 0.0;
      double prevY = 0.0;

      Point2D prevCubic = null;
      Point2D prevQuad = null;

      int nextCommand = -1;
      int n = specs.length();

// https://www.w3.org/TR/SVG/paths.html#PathData
      for (currentIdx = 0; currentIdx < n; )
      {
         int prevIdx = currentIdx;

         int cp = specs.codePointAt(currentIdx);
         currentIdx += Character.charCount(cp);

         if (Character.isWhitespace(cp)) continue;

         int nextIdx = currentIdx;

         double x = prevX;
         double y = prevY;

         boolean updateRect = true;

         int command = nextCommand;

         if (cp == 'M' || cp == 'm' || cp == 'Z' || cp == 'z'
           || cp == 'L' || cp == 'l' || cp == 'H' || cp == 'h'
           || cp == 'V' || cp == 'v' || cp == 'C' || cp == 'c'
           || cp == 'S' || cp == 's' || cp == 'Q' || cp == 'q'
           || cp == 'T' || cp == 't' || cp == 'A' || cp == 'a')
         {
            command = cp;
            commandIdx = prevIdx;
         }
         else
         {
            currentIdx = prevIdx;
         }

         if (command == 'M' || command == 'm')
         {
            x = readLength(false);
            y = readLength();

            if (cp == 'm')
            {
               x += prevX;
               y += prevY;

               nextCommand = 'l';
            }
            else
            {
               nextCommand = 'L';
            }

            workingShape.moveTo(x, y);
            prevCubic = null;
            prevQuad = null;
            updateRect = false;
         }
         else if (command == 'Z' || command == 'z')
         {
            workingShape.closePath();
            prevCubic = null;
            prevQuad = null;
         }
         else if (command == 'L' || command == 'l')
         {
            x = readLength(false);
            y = readLength();

            if (command == 'l')
            {
               x += prevX;
               y += prevY;
            }

            workingShape.lineTo(x, y);

            nextCommand = command;
            prevCubic = null;
            prevQuad = null;
         }
         else if (command == 'H' || command == 'h')
         {
            x = readLength(false);
            y = 0;

            if (command == 'h')
            {
               x += prevX;
               y += prevY;
            }

            workingShape.lineTo(x, y);

            nextCommand = command;
            prevCubic = null;
            prevQuad = null;
         }
         else if (command == 'V' || command == 'v')
         {
            x = 0;
            y = readLength(false);

            if (command == 'v')
            {
               x += prevX;
               y += prevY;
            }

            workingShape.lineTo(x, y);

            nextCommand = command;
            prevCubic = null;
            prevQuad = null;
         }
         else if (command == 'C' || command == 'c')
         {
            double c1x, c1y, c2x, c2y;

            c1x = readLength(false);
            c1y = readLength();
            c2x = readLength();
            c2y = readLength();
            x = readLength();
            y = readLength();

            if (command == 'c')
            {
               c1x += prevX;
               c1y += prevY;
               c2x += prevX;
               c2y += prevY;
               x += prevX;
               y += prevY;
            }

            workingShape.curveTo(c1x, c1y, c2x, c2y, x, y);

            nextCommand = command;
            prevCubic = new Point2D.Double(3*(x-c2x), 2*(y-c2y));
            prevQuad = null;
         }
         else if (command == 'S' || command == 's')
         {
            double c1x, c1y, c2x, c2y;

            if (prevCubic == null)
            {
               c1x = prevX;
               c1y = prevY;
            }
            else
            {
               c1x = prevCubic.getX()/3 + prevX;
               c1y = prevCubic.getY()/3 + prevY;
            }

            c2x = readLength(false);
            c2y = readLength(false);
            x = readLength(false);
            y = readLength(false);

            if (command == 'c')
            {
               c2x += prevX;
               c2y += prevY;
               x += prevX;
               y += prevY;
            }

            workingShape.curveTo(c1x, c1y, c2x, c2y, x, y);

            nextCommand = command;
            prevCubic = new Point2D.Double(3*(x-c2x), 2*(y-c2y));
            prevQuad = null;
         }
         else if (command == 'Q' || command == 'q')
         {
            double cx, cy;

            cx = readLength(false);
            cy = readLength();
            x = readLength();
            y = readLength();

            if (command == 'q')
            {
               cx += prevX;
               cy += prevY;
               x += prevX;
               y += prevY;
            }

            workingShape.quadTo(cx, cy, x, y);
            nextCommand = command;
            prevCubic = null;
            prevQuad = new Point2D.Double(2*(x-cx), 2*(y-cy));
         }
         else if (command == 'T' || command == 't')
         {
            double cx, cy;

            if (prevQuad == null)
            {
               cx = prevX;
               cy = prevY;
            }
            else
            {
               cx = prevQuad.getX()/2 + prevX;
               cy = prevQuad.getY()/2 + prevY;
            }

            x = readLength(false);
            y = readLength();

            if (command == 't')
            {
               x += prevX;
               y += prevY;
            }

            workingShape.quadTo(cx, cy, x, y);
            nextCommand = command;
            prevCubic = null;
            prevQuad = null;
         }
         else if (command == 'A' || command == 'a')
         {
            double rx, ry, angle;
            boolean largeArc, sweep;

            rx = readLength(false);
            ry = readLength();

            angle = readDouble();

            largeArc = readFlag();
            sweep = readFlag();

            x = readLength();
            y = readLength();

            // TODO
System.err.println("arc not yet implemented");

            nextCommand = command;
            prevCubic = null;
            prevQuad = null;
         }
         else
         {
            throw new InvalidFormatException(
              dialog.getMessage("error.string_parse.invalid_command",
                specs.substring(commandIdx, currentIdx), 
                new String(Character.toChars(cp))
             ));
         }

         prevX = x;
         prevY = y;

         if (updateRect)
         {
            double minX = Math.min(prevX, x);
            double minY = Math.min(prevY, y);
            double maxX = Math.max(prevX, x);
            double maxY = Math.max(prevY, y);

            publish(new Rectangle((int)minX, (int)minY, 
              (int)Math.ceil(maxX-minX), (int)Math.ceil(maxY-minY)));
         }
      }

      dialog.updateWorkingShapeBounds();

      JDRPath path = JDRPath.getPath(dialog.getCanvasGraphics(),
        workingShape.getPathIterator(null));

      return path;
   }

   private double readLength()
    throws InvalidFormatException
   {
      return readLength(true);
   }

   private double readLength(boolean checkInitialComma)
    throws InvalidFormatException
   {
      int nextIdx = readNumberString(currentIdx, checkInitialComma);
      double length = getUnitValue(currentIdx, nextIdx);
      currentIdx = nextIdx;

      return length;
   }

   private double readDouble()
    throws InvalidFormatException
   {
      return readDouble(true);
   }

   private double readDouble(boolean checkInitialComma)
    throws InvalidFormatException
   {
      int nextIdx = readNumberString(currentIdx, checkInitialComma);
      double value = getDouble(currentIdx, nextIdx);
      currentIdx = nextIdx;

      return value;
   }

   private boolean readFlag()
    throws InvalidFormatException
   {
      return readFlag(true);
   }

   private boolean readFlag(boolean checkInitialComma)
    throws InvalidFormatException
   {
      int nextIdx = readNumberString(currentIdx, checkInitialComma);
      boolean flag = getFlag(currentIdx, nextIdx);
      currentIdx = nextIdx;

      return flag;
   }

   private int getInteger(int startIdx, int endIdx) 
    throws InvalidFormatException
   {
      try
      {
         return Integer.valueOf(specs.substring(startIdx, endIdx).trim());
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException(
           dialog.getMessage("error.string_parse.invalid_number",
             specs.substring(commandIdx, startIdx), 
             specs.substring(startIdx, 
               (int)Math.min(specs.length(), endIdx+1)
             )
          ), e);
      }
   }

   private double getDouble(int startIdx, int endIdx) 
    throws InvalidFormatException
   {
      try
      {
         return Double.valueOf(specs.substring(startIdx, endIdx).trim());
      }
      catch (NumberFormatException e)
      {
         throw new InvalidFormatException(
           dialog.getMessage("error.string_parse.invalid_number",
             specs.substring(commandIdx, startIdx), 
             specs.substring(startIdx, 
               (int)Math.min(specs.length(), endIdx+1)
             )
          ), e);
      }
   }

   private double getUnitValue(int startIdx, int endIdx) 
    throws InvalidFormatException
   {
      return dialog.getValue(getDouble(startIdx, endIdx));
   }

   private boolean getFlag(int startIdx, int endIdx)
    throws InvalidFormatException
   {
      int flag = getInteger(startIdx, endIdx);

      if (flag == 1) return true;
      if (flag == 0) return false;

      throw new InvalidFormatException(
        dialog.getMessage("error.string_parse.invalid_flag",
          specs.substring(commandIdx, startIdx), 
          flag
       ));
   }

   private int readNumberString(int startIdx)
   {
      return readNumberString(startIdx, true);
   }

   private int readNumberString(int startIdx, boolean checkInitialComma)
   {
      boolean checkSign = true;
      boolean checkDecimal = true;
      boolean checkExp = false;
      int n = specs.length();

      boolean start = true;
      boolean expFound = false;

      for (int i = startIdx; i < n; )
      {
         int cp = specs.codePointAt(i);
         i += Character.charCount(cp);

         if (start) 
         {
            if (Character.isWhitespace(cp))
            {
               continue;
            }

            if (checkInitialComma && cp == ',')
            {
               checkInitialComma = false;
               continue;
            }
         }

         start = false;

         boolean found = false;

         if (checkSign)
         {
            checkSign = false;

            if (cp == '+' || cp == '-')
            {
               found = true;
            }
         }

         if (!found)
         {
            if (Character.isDigit(cp))
            {
               found = true;

               if (!expFound)
               {
                  checkExp = true;
               }
            }
            else if (checkDecimal && cp == '.')
            {
               checkDecimal = false;
               checkExp = false;
               found = true;
            }
            else if (checkExp && (cp == 'E' || cp == 'e'))
            {
               expFound = true;
               checkExp = false;
               checkSign = true;
               checkDecimal = false;
               found = true;
            }
         }

         if (!found)
         {
            return i;
         }
      }

      return n;
   }

   @Override
   protected void process(java.util.List<Rectangle> chunks)
   {
      for (Rectangle rect : chunks)
      {
         dialog.addToBounds(rect);
      }
   }

   public void done()
   {
      dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      try
      {
         dialog.finishedTask(get());
      }
      catch (Exception e)
      {
         dialog.taskFailed(e);
      }
   }

   private String specs;
   private CreatePathFromSvgDialog dialog;
   private int commandIdx = 0, currentIdx = 0;
}
