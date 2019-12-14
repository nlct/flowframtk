package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.dialog.VectorizeBitmapDialog;

public class ScanImage extends JFrame implements Runnable
{
   public ScanImage(JDRFrame frame, JDRBitmap bitmap,
      VectorizeBitmapDialog dialog)
   {
      super(frame.getResources().getString("vectorize.scanning"));

      this.frame = frame;
      this.bitmap = bitmap;

      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
         {
            public void windowClosing(WindowEvent evt)
            {
               abort();
            }
         }
      );

      xInc = dialog.getXInc();

      if (xInc <= 0)
      {
         throw new IllegalArgumentException("xInc must be > 0");
      }

      yInc = dialog.getYInc();

      if (yInc <= 0)
      {
         throw new IllegalArgumentException("yInc must be > 0");
      }

      base = dialog.getBase();

      doStraighten = dialog.isStraightenSelected();

      if (doStraighten)
      {
         tolerance = dialog.getStraightenTolerance();

         if (tolerance < 0)
         {
            throw new IllegalArgumentException("tolerance must be >= 0");
         }
      }

      doSmooth = dialog.isSmoothSelected();

      if (doSmooth)
      {
         chi = dialog.getChi();
         delta = dialog.getDelta();
         gamma = dialog.getGamma();
         rho = dialog.getRho();
         sigma = dialog.getSigma();
         tolFun = dialog.getSmoothTolFun();
         tolX = dialog.getSmoothTol();
         flatness = dialog.getFlatness();
      }

      icSize = new Dimension(bitmap.getIconWidth(), bitmap.getIconHeight());

      scanImagePanel = new ScanImagePanel();

      scanImagePanel.setBackground(Color.white);

      getContentPane().add(new JScrollPane(scanImagePanel), "Center");

      info = new JTextField(
         frame.getResources().getString("vectorize.initializing"));
      info.setEditable(false);

      getContentPane().add(info, "South");

      pack();
      setLocationRelativeTo(frame);
   }

   public void performScan()
   {
      thread = new Thread(this);
      thread.start();
   }

   public boolean isBaseRGB(int rgb)
   {
      return rgb == base.getRGB();
   }

   public void run()
   {
      setVisible(true);

      try
      {
         doScan();
      }
      catch (InterruptedException e)
      {
         frame.getResources().error(this,
            frame.getResources().getString("vectorize.aborted"));
      }
      catch (Exception e)
      {
         frame.getResources().error(this,
            frame.getResources().getString("vectorize.failed"));
      }

      setVisible(false);

      thread = null;
   }

   private void doScan()
      throws InvalidPathException,InterruptedException
   {
      CanvasGraphics cg = frame.getCanvasGraphics();

      Image image = bitmap.getImage();

      buffImage = new BufferedImage(icSize.width, yInc,
         BufferedImage.TYPE_INT_ARGB);

      area = new Area();

      GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 6);

      y0 = 0;
      y1 = yInc;

      Color background = new Color(255-base.getRed(),
                              255-base.getGreen(),
                              255-base.getBlue());

      while (y0 < icSize.height)
      {
         checkForAbort();

         if (y1 >= icSize.height)
         {
            y1 = icSize.height-1;
         }

         int dy = y1 - y0;

         if (dy <= 0) break;

         info.setText("y0="+y0+", y1="+y1);

         Graphics g = buffImage.createGraphics();

         g.setColor(background);
         g.fillRect(0, 0, icSize.width, yInc);
         g.setClip(0, 0, icSize.width, dy);

         g.translate(0, -y0);

         g.drawImage(image, 0, 0, null);

         g.dispose();

         int x0 = 0;
         int x1 = xInc;

         while (x0 < icSize.width)
         {
            checkForAbort();

            if (x1 >= icSize.width)
            {
               x1 = icSize.width-1;
            }

            int dx = x1 - x0;

            if (dx <= 0) break;

            boolean topLeft = isBaseRGB(buffImage.getRGB(x0, 0));
            boolean topRight = isBaseRGB(buffImage.getRGB(x1-1, 0));
            boolean bottomLeft = isBaseRGB(buffImage.getRGB(x0, dy-1));
            boolean bottomRight = isBaseRGB(buffImage.getRGB(x1-1, dy-1));

            if (!topLeft && !topRight && !bottomLeft && !bottomRight)
            {
               x0 = x1;
               x1 += xInc;
               continue;
            }

            path.reset();

            if (topLeft)
            {
               if (topRight)
               {
                  path.moveTo((float)x0, (float)y0);
                  path.lineTo((float)x1, (float)y0);
               }
               else
               {
                  path.moveTo((float)x0, (float)y0);
                  path.lineTo((float)(x0+0.5*dx), (float)y0);
               }
            }
            else
            {
               if (topRight)
               {
                  path.moveTo((float)(x0+0.5*dx), (float)y0);
                  path.lineTo((float)x1, (float)y0);
               }
               else
               {
                  path.moveTo((float)x0, (float)(y0+0.5*dy));
                  path.lineTo((float)x1, (float)(y0+0.5*dy));
               }
            }

            if (bottomRight)
            {
               if (bottomLeft)
               {
                  path.lineTo((float)x1, (float)y1);
                  path.lineTo((float)x0, (float)y1);
               }
               else
               {
                  path.lineTo((float)x1, (float)y1);
                  path.lineTo((float)(x0+0.5*dx), (float)y1);
               }
            }
            else
            {
               if (bottomLeft)
               {
                  path.lineTo((float)(x0+0.5*dx), (float)y1);
                  path.lineTo((float)x0, (float)y1);
               }
               else
               {
                  path.lineTo((float)x1, (float)(y0+0.5*dx));
                  path.lineTo((float)x0, (float)(y0+0.5*dx));
               }
            }

            path.closePath();

            checkForAbort();

            area.add(new Area(path));

            repaint();

            x0 = x1;
            x1 += xInc;
         }

         y0 = y1;
         y1 += yInc;
      }

      checkForAbort();

      double bpToStorage = cg.bpToStorage(1.0);

      jdrpath = JDRPath.getPath(cg, area.getPathIterator(
         AffineTransform.getScaleInstance(bpToStorage, bpToStorage)));

      jdrpath.setLinePaint(new JDRColor(cg, base));
      jdrpath.setFillPaint(new JDRTransparent(cg));
      jdrpath.setStroke(new JDRBasicStroke(cg));

      if (!isValidPath())
      {
         frame.getResources().error(this,
            frame.getResources().getString("vectorize.failed"));
         setVisible(false);
         return;
      }

      if (doStraighten)
      {
         checkForAbort();

         info.setText(
            frame.getResources().getString("vectorize.straightening"));

         jdrpath.smoothLines(tolerance);

         if (!isValidPath())
         {
            frame.getResources().error(this,
               frame.getResources().getString("vectorize.failed"));
            setVisible(false);
            return;
         }
      }

      area = null;

      scanImagePanel.repaint();

      if (doSmooth)
      {
         checkForAbort();

         info.setText(frame.getResources().getString("vectorize.smoothing"));
         smooth();

         if (!isValidPath())
         {
            frame.getResources().error(this,
               frame.getResources().getString("vectorize.failed"));
            setVisible(false);
            return;
         }
      }

      frame.addObject(jdrpath,
         frame.getResources().getString("undo.vectorize"));
   }

   private boolean isValidPath()
   {
      try
      {
         Shape shape = jdrpath.getStorageStrokedPath();

         return true;
      }
      catch (IllegalPathStateException e)
      {
         System.err.println("Invalid path");

         int n = jdrpath.size();

         System.err.println("size: "+n);

         for (int i = 0; i < n; i++)
         {
            System.err.println(jdrpath.get(i));
         }

         return false;
      }
   }

   private void smooth()
      throws InvalidPathException,InterruptedException
   {
      int n = jdrpath.size();

      if (n == 0)
      {
         throw new EmptyPathException(getResources().getMessageSystem());
      }

      double maxLength = Math.sqrt(xInc*xInc+yInc*yInc);

      int windingRule = GeneralPath.WIND_NON_ZERO;

      JDRStroke stroke = jdrpath.getStroke();

      if (stroke != null)
      {
         windingRule = ((JDRBasicStroke)stroke).getWindingRule();
      }

      smoothingPath = null;

      GeneralPath subPath = null;

      int subPathSize = 0;

      JDRSegment prevSegment = null;

      Point2D gradient = null;

      for (int i = 0; i < n; i++)
      {
         checkForAbort();

         JDRSegment segment = (JDRSegment)jdrpath.get(i);

         double x0 = segment.getStartX();
         double x1 = segment.getEndX();

         double y0 = segment.getStartY();
         double y1 = segment.getEndY();

         double dx = x0 - x1;
         double dy = y0 - y1;

         double length = Math.sqrt(dx*dx + dy*dy);

         if (length > maxLength || segment.isGap())
         {
            if (subPath == null)
            {
               if (smoothingPath == null)
               {
                  smoothingPath = new GeneralPath(windingRule, n);
                  smoothingPath.moveTo((float)x0, (float)y0);
               }

               smoothingPath.lineTo((float)x1, (float)y1);
            }
            else if (subPathSize == 1)
            {
               if (smoothingPath == null)
               {
                  smoothingPath = subPath;
               }
               else
               {
                  smoothingPath.append(subPath, true);
               }

               smoothingPath.lineTo((float)x1, (float)y1);
               subPath = null;
               subPathSize = 0;
            }
            else
            {
               smoothSubPath(gradient, subPath, subPathSize);
               subPath = null;
               subPathSize = 0;
            }

            scanImagePanel.repaint();
         }
         else
         {
            if (subPath == null)
            {
               subPath = new GeneralPath();

               subPath.moveTo((float)x0, (float)y0);
               subPathSize = 1;

               if (prevSegment != null)
               {
                  gradient = prevSegment.getdP1();
               }
            }

            subPath.lineTo((float)x1, (float)y1);
            subPathSize++;
         }

         prevSegment = segment;
      }

      if (subPath != null)
      {
         smoothSubPath(gradient, subPath, subPathSize);
         subPath = null;
      }

      if (jdrpath.isClosed())
      {
         smoothingPath.closePath();
      }

      CanvasGraphics cg = frame.getCanvasGraphics();

      JDRPaint linePaint = jdrpath.getLinePaint();
      JDRPaint fillPaint = jdrpath.getFillPaint();
      jdrpath = JDRPath.getPath(cg, smoothingPath.getPathIterator(null));

      jdrpath.setStroke(stroke);
      jdrpath.setLinePaint(linePaint);
      jdrpath.setFillPaint(fillPaint);

      scanImagePanel.repaint();
   }

   public void smoothSubPath(Point2D gradient, GeneralPath subPath, int n)
      throws InterruptedException
   {
      double[] x = new double[n];
      double[] y = new double[n];

      PathIterator pi = subPath.getPathIterator(null);

      double[] coords = new double[6];
      int i = 0;

      while (!pi.isDone())
      {
         checkForAbort();

         int type = pi.currentSegment(coords);

         x[i] = coords[0];
         y[i] = coords[1];

         pi.next();
         i++;
      }

      smoothSubPath(gradient, x, y, 0, n-1);
   }

   private void smoothSubPath(Point2D gradient, double[] x, double[] y,
      int startIdx, int endIdx)
   throws InterruptedException
   {
      bestBezier = null;
      int bestIdx = -1;

      double bestResult = Double.MAX_VALUE;

      bezier = new CubicCurve2D.Double();

      for (int i = endIdx; i > startIdx+1; i--)
      {
         checkForAbort();

         double result = fitBezier(gradient, x, y, startIdx, i);

         if (result < bestResult)
         {
            bestIdx = i;
            bestResult = result;

            if (bestBezier == null)
            {
               bestBezier = new CubicCurve2D.Double();
            }

            bestBezier.setCurve(bezier);

            scanImagePanel.repaint();
         }

         info.setText("best="+bestResult+", current="+result);
      }

      bezier = null;

      if (bestBezier == null || bestIdx == -1)
      {
         for (int i = startIdx; i <= endIdx; i++)
         {
            checkForAbort();

            if (smoothingPath == null)
            {
               smoothingPath = new GeneralPath();
               smoothingPath.moveTo((float)x[i], (float)y[i]);
            }
            else
            {
               smoothingPath.lineTo((float)x[i], (float)y[i]);
            }
         }

         return;
      }

      if (smoothingPath == null)
      {
         smoothingPath = new GeneralPath(bestBezier);
      }
      else
      {
         smoothingPath.append(bestBezier, true);
      }

      scanImagePanel.repaint();

      if (bestIdx < endIdx)
      {
         smoothSubPath(gradient, x, y, bestIdx, endIdx);
      }
   }

   private double bezierFitFunction(Point2D gradient, 
      double c1x, double c1y, 
      double c2x, double c2y, double[] polygonX, double[] polygonY,
      int startIdx, int endIdx)
   throws InterruptedException
   {
      double p0x = polygonX[startIdx];
      double p0y = polygonY[startIdx];
      double p1x = polygonX[endIdx];
      double p1y = polygonY[endIdx];

      GeneralPath curvePath = new GeneralPath(GeneralPath.WIND_NON_ZERO,
        3);

      curvePath.moveTo((float)p0x, (float)p0y);
      curvePath.curveTo((float)c1x, (float)c1y,
                        (float)c2x, (float)c2y,
                        (float)p1x, (float)p1y);
      checkForAbort();

      PathIterator pi = curvePath.getPathIterator(null, flatness);

      double f = 0;

      double[] coords = new double[6];

      while (!pi.isDone())
      {
         checkForAbort();

         // type should always be a line or move segment

         int type = pi.currentSegment(coords);
         pi.next();

         if (type != PathIterator.SEG_LINETO)
         {
            continue;
         }

         if (pi.isDone())
         {
            // skip last point since it coincides with last point
            // of polygon

            break;
         }

         double minDist = Double.MAX_VALUE;

         double x = coords[0];
         double y = coords[1];

         for (int i = startIdx+1; i < endIdx; i++)
         {
            checkForAbort();

            double xDiff = x - polygonX[i];
            double yDiff = y - polygonY[i];

            double dist = xDiff*xDiff + yDiff*yDiff;

            if (dist < minDist)
            {
               minDist = dist;
            }
         }

         f += minDist;
      }

      if (gradient != null)
      {
         double dPx = 3*(c1x - p0x) - gradient.getX();
         double dPy = 3*(c1y - p0y) - gradient.getY();

         f *= dPx*dPx + dPy*dPy;
      }

      // bias towards replacing as many points as possible

      int denom = endIdx-startIdx+1;

      denom = denom*denom*denom*denom;

      return f/denom;
   }

   private double fitBezier(Point2D gradient, 
      double[] polygonX, double[] polygonY,
      int startIdx, int endIdx)
   throws InterruptedException
   {
      double p0x = polygonX[startIdx];
      double p0y = polygonY[startIdx];
      double p1x = polygonX[endIdx];
      double p1y = polygonY[endIdx];

      int idx = (int)Math.round(0.5*(endIdx-startIdx))+startIdx;

      if (idx > endIdx)   idx = endIdx;
      if (idx < startIdx) idx = startIdx;

      bezier.setCurve(p0x, p0y,
        polygonX[idx], polygonY[idx],
        polygonX[idx], polygonY[idx],
        p1x, p1y);

      // form initial simplex

      params = new CurveFitParams[5];

      params[4] = new CurveFitParams(bezier);
      params[4].computeY(gradient, polygonX, polygonY, startIdx, endIdx);

      // x[0] = control1.x, x[1] = control1.y, x[2] = control2.x,
      // x[3] = control2.y

      double[] x;

      for (int i = 0; i < 4; i++)
      {
         x = new double[4];

         params[4].getParams(x);

         x[i] += delta;

         params[i] = new CurveFitParams(x);
         params[i].computeY(gradient, polygonX, polygonY, startIdx, endIdx);
      }

      double[] centroid = new double[4];
      double[] xReflect = new double[4];
      double[] xExpand = new double[4];
      double[] xContract = new double[4];
      double yReflect, yExpand, yContract;

      int bestIdx=0;

      int iter = 0;

      for (iter = 2; iter <= maxIter; iter++)
      {
         checkForAbort();

         // sort

         Arrays.sort(params);

         // reflect

         for (int i = 0; i < 4; i++)
         {
            centroid[i] = params[i].getMean();
            xReflect[i] = centroid[i] 
                        + rho*(centroid[i]-params[4].x[i]);
         }

         yReflect = bezierFitFunction(gradient, xReflect[0], xReflect[1],
            xReflect[2], xReflect[3],
            polygonX, polygonY, startIdx, endIdx);

         if (yReflect >= params[0].y && yReflect < params[3].y)
         {
            // accept reflection point

            params[4].setParams(xReflect);
            params[4].y = yReflect;
         }
         else
         {
            if (yReflect < params[0].y)
            {
               // expand

               for (int i = 0; i < 4; i++)
               {
                  xExpand[i] = centroid[i]
                             + chi*(xReflect[i]-centroid[i]);
               }

               yExpand = bezierFitFunction(gradient, xExpand[0], xExpand[1],
                  xExpand[2], xExpand[3],
                  polygonX, polygonY, startIdx, endIdx);

               if (yExpand < yReflect)
               {
                  // accept expansion point

                  params[4].setParams(xExpand);
                  params[4].y = yExpand;
               }
               else
               {
                  // accept reflection point

                  params[4].setParams(xReflect);
                  params[4].y = yReflect;
               }
            }
            else
            {
               // contract

               boolean shrink = false;

               if (params[3].y <= yReflect && yReflect < params[4].y)
               {
                  // contract outside

                  for (int i = 0; i < 4; i++)
                  {
                     xContract[i] = centroid[i]
                                  + gamma*(xReflect[i]-centroid[i]);
                  }

                  yContract = bezierFitFunction(gradient, xContract[0],
                     xContract[1], xContract[2], xContract[3],
                     polygonX, polygonY, startIdx, endIdx);

                  if (yContract <= yReflect)
                  {
                     // accept contraction point

                     params[4].setParams(xContract);
                     params[4].y = yContract;
                  }
                  else
                  {
                     shrink = true;
                  }
               }
               else
               {
                  // contract inside

                  for (int i = 0; i < 4; i++)
                  {
                     xContract[i] = centroid[i]
                                  + gamma*(centroid[i] - params[4].x[i]);
                  }

                  yContract = bezierFitFunction(gradient, xContract[0],
                     xContract[1], xContract[2], xContract[3],
                     polygonX, polygonY, startIdx, endIdx);

                  if (yContract <= params[4].y)
                  {
                     // accept contraction point

                     params[4].setParams(xContract);
                     params[4].y = yContract;
                  }
                  else
                  {
                     shrink = true;
                  }
               }

               if (shrink)
               {
                  // shrink

                  for (int j = 1; j < 5; j++)
                  {
                     for (int i = 0; i < 4; i++)
                     {
                        params[j].x[i] = params[0].x[i]
                           + sigma*(params[j].x[i] - params[0].x[i]);
                     }

                     params[j].computeY(gradient, polygonX, polygonY, startIdx, endIdx);
                  }
               }
            }
         }

         double maxY = 0;
         double minY = Double.MAX_VALUE;

         for (int i = 0; i < 5; i++)
         {
            if (params[i].y > maxY)
            {
               maxY = params[i].y;
            }

            if (params[i].y < minY)
            {
               minY = params[i].y;
               bestIdx = i;
            }
         }

         // evaluate stopping criterion

         double overallMax = 0;

         for (int i = 0; i < 4; i++)
         {
            double min = params[0].x[i];
            double max = min;

            for (int j = 1; j < 5; j++)
            {
               if (params[j].x[i] < min)
               {
                  min = params[j].x[i];
               }

               if (params[j].x[i] > max)
               {
                  max = params[j].x[i];
               }
            }

            double diff = Math.abs(min-max);

            if (diff > overallMax)
            {
               overallMax = diff;
            }
         }

         if (overallMax < tolX)
         {
            break;
         }

         if (Math.abs(maxY-minY)/maxY < tolFun)
         {
            break;
         }

         for (int i = 0; i < 5; i++)
         {
            params[i].updateBezier();
            scanImagePanel.repaint();
         }
      }

      if (iter == maxIter)
      {
         System.err.println("Exceeded maximum iterations");
      }

      params[bestIdx].updateBezier();

      double bestY = params[bestIdx].y;

      params = null;

      return bestY;
   }

   class CurveFitParams implements Comparable
   {
      public CurveFitParams(double[] x)
      {
         this.x = x;
      }

      public CurveFitParams(double c1x, double c1y, double c2x, double c2y)
      {
         x = new double[4];

         x[0] = c1x;
         x[1] = c1y;
         x[2] = c2x;
         x[3] = c2y;
      }

      public CurveFitParams(CubicCurve2D.Double curve)
      {
         this(curve.ctrlx1, curve.ctrly1, curve.ctrlx2, curve.ctrly2);
      }

      public void getParams(double[] fetchedValues)
      {
         for (int i = 0; i < 4; i++)
         {
            fetchedValues[i] = x[i];
         }
      }

      public void setParams(double[] newValues)
      {
         for (int i = 0; i < 4; i++)
         {
            x[i] = newValues[i];
         }
      }

      public int compareTo(Object o)
      {
         CurveFitParams params = (CurveFitParams)o;

         if (y == params.y) return 0;

         return (y < params.y ? -1 : 1);
      }

      public void updateBezier()
      {
         bezier.ctrlx1 = x[0];
         bezier.ctrly1 = x[1];
         bezier.ctrlx2 = x[2];
         bezier.ctrly2 = x[3];
      }

      public void draw(Graphics g)
      {
         Graphics2D g2 = (Graphics2D)g;

         g2.draw(new CubicCurve2D.Double(bezier.x1, bezier.y1,
            x[0], x[1], x[2], x[3], bezier.x2, bezier.y2));
      }

      public void computeY(Point2D gradient,
        double[] polygonX, double[] polygonY,
        int startIdx, int endIdx)
      throws InterruptedException
      {
         y = bezierFitFunction(gradient, x[0], x[1], x[2], x[3],
            polygonX, polygonY, startIdx, endIdx);
      }

      public double getMean()
      {
         double sum = 0;

         for (int i = 0; i < x.length; i++)
         {
            sum += x[i];
         }

         return sum/x.length;
      }

      protected double[] x;
      protected double y;
   }

   class ScanImagePanel extends JPanel
   {
      public ScanImagePanel()
      {
         super();
      }

      public void paintComponent(Graphics g)
      {
         super.paintComponent(g);

         Graphics2D g2 = (Graphics2D)g;

         g.setColor(Color.black);

         if (area != null)
         {
            g2.draw(area);
         }

         if (smoothingPath != null)
         {
            g2.draw(smoothingPath);
         }

         if (bestBezier != null)
         {
            g.setColor(Color.green);
            g2.draw(bestBezier);
         }

         try
         {
            if (params != null)
            {
               int n = params.length;

               g.setColor(Color.blue);

               for (int i = 0; i < n; i++)
               {
                  CurveFitParams p = params[i];

                  if (p != null)
                  {
                     p.draw(g);
                  }
               }
            }
         }
         catch (NullPointerException e)
         {
         }
      }

      public Dimension getPreferredSize()
      {
         return icSize;
      }
   }

   public synchronized void abort()
   {
      int result = JOptionPane.showConfirmDialog(this, 
         frame.getResources().getString("vectorize.confirm_abort"),
         frame.getResources().getString("warning.title"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.WARNING_MESSAGE);

      if (result != JOptionPane.YES_OPTION)
      {
         return;
      }

      abortNow = true;

      if (thread != null)
      {
         thread.interrupt();
      }
   }

   protected synchronized void checkForAbort() throws InterruptedException
   {
      if (abortNow)
      {
         throw new InterruptedException("Process Aborted");
      }
   }

   public JDRResources getResources()
   {
      return frame.getResources();
   }

   private JDRBitmap bitmap;
   private BufferedImage buffImage = null;
   private Dimension icSize;

   private int xInc, yInc, y0, y1;
   private Color base;
   private double tolerance;

   private boolean doSmooth=true, doStraighten=true;

   private volatile Area area = null;
   private volatile JDRPath jdrpath = null;
   private volatile GeneralPath smoothingPath = null;
   private volatile CubicCurve2D.Double bezier = null, bestBezier = null;
   private volatile CurveFitParams[] params = null;

   private JTextField info;
   private JDRFrame frame;
   private ScanImagePanel scanImagePanel;

   private volatile boolean abortNow = false;
   private Thread thread = null;

   private double flatness = 1.0;

   // Nelder-Mead simplex algorithm parameters:

   private double chi=2, delta = 0.01, gamma=0.5, rho=1, sigma=0.5,
      maxIter = 200, maxFunEvals = 1000, tolFun = 1e-6, tolX = 1e-6;
}
