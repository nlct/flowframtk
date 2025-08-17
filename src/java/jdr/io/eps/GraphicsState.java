// File          : GraphicsState.java
// Purpose       : class representing an EPS graphics state
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io.eps;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.font.*;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.math.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS graphics state.
 * @author Nicola L C Talbot
 */

// TODO: change fixed English messages to dictionary values
public class GraphicsState implements EPSObject
{
   /**
    * Creates a default graphics state with identity transformation.
    */
   public GraphicsState(EPS eps)
   {
      af = new AffineTransform();
      lineWidth = 1;
      dashPattern = null;
      dashOffset = 0;
      cap = 0;
      join = 0;
      mitreLimit=10;
      paint = new JDRColor(eps.getCanvasGraphics(), Color.black);
      path = new Path2D.Double();
      clippingPath = null;
      font = new EPSFont("Times-Roman", eps.getLaTeXFontBase());
      flatten_ = false;
      flatness = 1.0;

      eps_ = eps;

      transfer = new EPSProc[4];

      for (int i = 0; i < 4; i++)
      {
         transfer[i] = null;
      }
   }

   private GraphicsState()
   {
      af = new AffineTransform();
      lineWidth = 1;
      dashPattern = null;
      dashOffset = 0;
      cap = 0;
      join = 0;
      mitreLimit=10;
      paint = new JDRColor(null, Color.black);
      path = new Path2D.Double();
      clippingPath = null;
      flatten_ = false;
      flatness = 1.0;
      transfer = new EPSProc[4];

      for (int i = 0; i < 4; i++)
      {
         transfer[i] = null;
      }
   }

   /**
    * Creates a default graphics state with given transformation
    * matrix. The transformation matrix is given as a
    * vector with 6 elements.
    * @param flatmatrix the transformation matrix
    */
   public GraphicsState(EPS eps, double[] flatmatrix)
   {
      eps_ = eps;

      af = new AffineTransform(flatmatrix);
      lineWidth = 1;
      dashPattern = null;
      dashOffset = 0;
      cap = 0;
      join = 0;
      mitreLimit=10;
      paint = new JDRColor(eps_.getCanvasGraphics(), Color.black);
      path = new Path2D.Double();
      clippingPath = null;
      font = new EPSFont("Times-Roman", eps.getLaTeXFontBase());
      flatten_ = false;
      flatness = 1.0;

      transfer = new EPSProc[4];

      for (int i = 0; i < 4; i++)
      {
         transfer[i] = null;
      }
   }

   /**
    * Creates a copy of this graphics state.
    * @return a copy of this graphics state
    */
   public Object clone()
   {
      GraphicsState gs = new GraphicsState(eps_);

      gs.af = (AffineTransform)af.clone();
      gs.lineWidth = lineWidth;
      gs.cap = cap;
      gs.join = join;
      if (dashPattern != null)
      {
         int n = dashPattern.length;

         gs.dashPattern = new float[n];

         for (int i = 0; i < n; i++)
         {
            gs.dashPattern[i] = dashPattern[i];
         }
      }
      gs.dashOffset = dashOffset;
      gs.paint = (JDRPaint)paint.clone();
      gs.mitreLimit = mitreLimit;
      gs.path = (Path2D)path.clone();

      if (clippingPath == null)
      {
        gs.clippingPath = null;
      }
      else
      {
         gs.clippingPath = (Path2D)clippingPath.clone();
      }
      gs.font = (EPSFont)font.clone();
      gs.flatten_ = flatten_;
      gs.flatness = flatness;

      gs.eps_ = eps_;

      gs.transfer = new EPSProc[4];

      for (int i = 0; i < 4; i++)
      {
         gs.transfer[i] = transfer[i];
      }

      return gs;
   }

   /**
    * Copies other graphics state into this.
    * @param gs other graphics state
    */
   public void copy(GraphicsState gs)
   {
      af = gs.af;
      lineWidth = gs.lineWidth;
      cap = gs.cap;
      join = gs.join;
      dashPattern = gs.dashPattern;

      dashOffset = gs.dashOffset;
      paint = gs.paint;
      mitreLimit = gs.mitreLimit;
      path = gs.path;
      clippingPath = gs.clippingPath;

      font = gs.font;

      flatten_ = gs.flatten_;
      flatness = gs.flatness;

      transfer = gs.transfer;

      eps_ = gs.eps_;
   }

   /**
    * Reverses the current path.
    * @throws MissingMoveException if path doesn't start with a
    * move to
    * @throws EmptyPathException if path is empty
    * @throws IllFittingPathException if the path is poorly defined
    */
   public void reversePath()
      throws InvalidPathException
   {
      JDRPath jdrpath 
         = (JDRPath)JDRPath.getPath(eps_.getCanvasGraphics(),
            path.getPathIterator(null)).reverse();
      path = jdrpath.getGeneralPath();
   }

   /**
    * Sets the current path to its stroked area.
    * @throws MissingMoveException if path doesn't start with a
    * move to
    * @throws EmptyPathException if path is empty
    * @throws IllFittingPathException if the path is poorly defined
    */
   public void strokePath()
      throws InvalidPathException
   {
      JDRPath jdrpath 
         = JDRPath.getPath(eps_.getCanvasGraphics(), 
                           path.getPathIterator(null));
      path = new Path2D.Double(jdrpath.getBpStrokedArea());
   }

   /**
    * Gets the current font.
    * @return the current font
    */
   public EPSFont getCurrentFont()
   {
      return font;
   }

   /**
    * Sets the current font.
    * @param currentFont the new current font
    */
   public void setCurrentFont(EPSFont currentFont)
   {
      font = currentFont;
   }

   /**
    * Converts the given text into a {@link JDRText} object using
    * the current font and transformation matrix.
    * @param text the text for the new text area
    * @return the new text area
    * @throws NoCurrentPointException if there is no current point
    * @throws InvalidFormatException if transfer function has 
    * invalid format
    * @throws NoninvertibleTransformException if there is an
    * attemp to invert a non-invertible transform matrix
    * @throws IOException if transfer function encounters I/O error
    */
   public JDRText getJDRText(String text)
      throws NoCurrentPointException,
             NoninvertibleTransformException,
             InvalidFormatException,
             IOException
   {
      Point2D currentPosition = getCurrentPoint();

      if (currentPosition == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Point2D cp = new Point2D.Double();

      itransform(currentPosition, cp);

      Point2D displacement = new Point2D.Double();

      CanvasGraphics cg = eps_.getCanvasGraphics();

      JDRText jdrtext;

      if (cg.getGraphics() == null)
      {
         BufferedImage buffImage = new BufferedImage(1, 1,
            BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = buffImage.createGraphics();

         cg.setGraphicsDevice(g2);

         jdrtext = font.getJDRText(cg, text, af,
            cp, displacement);

         g2.dispose();
         cg.setGraphicsDevice(null);
      }
      else
      {
         jdrtext = font.getJDRText(cg, text, af,
            cp, displacement);
      }

      path.moveTo((float)(currentPosition.getX()+displacement.getX()),
                  (float)(currentPosition.getY()+displacement.getY()));

      jdrtext.setTextPaint(getPaint());

      return jdrtext;
   }

   /**
    * Returns the shift caused by showing the given text in the 
    * current font.
    * @param text the given text
    * @return the shift (as a point)
    */
   public Point2D getTextWidth(String text)
      throws NoninvertibleTransformException
   {
      Point2D currentPosition = getCurrentPoint();

      if (currentPosition == null)
      {
         currentPosition = new Point2D.Double(0, 0);
      }

      Point2D displacement = new Point2D.Double();
      CanvasGraphics cg = eps_.getCanvasGraphics();

      if (cg.getGraphics() == null)
      {
         BufferedImage buffImage = new BufferedImage(1, 1,
            BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = buffImage.createGraphics();

         cg.setGraphicsDevice(g2);

         JDRText jdrtext = font.getJDRText(cg, text, af,
            currentPosition, displacement);

         g2.dispose();

         cg.setGraphicsDevice(null);
      }
      else
      {
         JDRText jdrtext = font.getJDRText(cg, text, af,
            currentPosition, displacement);
      }

      idtransform(displacement, displacement);

      return displacement;
   }

   /**
    * Converts the outline of the given text to a path and 
    * appends to current path. This corresponds to the
    * PostScript command: <code>string</code> <em>text</em>
    * <code>boolean</code> <em>type</em> <code>charpath</code>.
    * This method currently ignores the <code>type</code> parameter
    * and always assumes a value of <code>false</code> (stroking
    * it.)
    * @param text the text to be converted to a path
    * @param type currently not used
    * @throws NoninvertibleTransformException if the transformation 
    * matrix can't be inverted
    * @throws NoCurrentPointException if there is no current point
    */
   public void charPath(String text, boolean type)
     throws NoninvertibleTransformException,
            NoCurrentPointException
   {
      Point2D currentPosition = getCurrentPoint();

      if (currentPosition == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Point2D cp = new Point2D.Double();

      itransform(currentPosition, cp);

      Point2D displacement = new Point2D.Double();
      Shape charpath;

      if (eps_.getCanvasGraphics().getGraphics() == null)
      {
         BufferedImage buffImage = new BufferedImage(1, 1,
            BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = buffImage.createGraphics();
         eps_.getCanvasGraphics().setGraphicsDevice(g2);

         JDRText jdrtext = font.getJDRText(eps_.getCanvasGraphics(), text, af,
            cp, displacement);

         charpath = jdrtext.getOutline(g2.getFontRenderContext());

         g2.dispose();
         eps_.getCanvasGraphics().setGraphicsDevice(null);
      }
      else
      {
         Graphics2D g2 = eps_.getCanvasGraphics().getGraphics();

         JDRText jdrtext = font.getJDRText(eps_.getCanvasGraphics(), text, af,
            cp, displacement);

         charpath = jdrtext.getOutline(g2.getFontRenderContext());
      }

      appendPath(new Path2D.Double(charpath));
   }

   /**
    * Sets the current line width.
    * @param width the new current line width
    * @see #getLineWidth()
    */
   public void setLineWidth(double width)
   {
      lineWidth = width;
   }

   /**
    * Sets the flatten flag.
    */
   public void flatten()
   {
      flatten_ = true;
   }

   /**
    * Sets the flatness.
    * @param value the flatness value
    * @see #getFlatness()
    */
   public void setFlatness(double value)
   {
      flatness = value;
   }

   /**
    * Gets the current flatness value.
    * @return the current flatness value
    * @see #setFlatness(double)
    */
   public double getFlatness()
   {
      return flatness;
   }

   /**
    * Gets the current line width.
    * @return the current line width
    * @see #setLineWidth(double)
    */
   public double getLineWidth()
   {
      return lineWidth;
   }

   /**
    * Sets the current cap style. This may be one of
    * {@link #CAP_BUTT}, {@link #CAP_SQUARE} or {@link #CAP_ROUND}.
    * @param value the cap style
    * @see #getCap()
    */
   public void setCap(int value)
   {
      if (value < 0 || value > 3)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.CAP_STYLE, value,
           getCanvasGraphics());
      }

      cap = value;
   }

   /**
    * Gets the current cap style.
    * @return the current cap style
    * @see #setCap(int)
    */
   public int getCap()
   {
      return cap;
   }

   /**
    * Sets the current join style. This may be one of
    * {@link #JOIN_MITRE}, {@link #JOIN_BEVEL} or {@link #JOIN_ROUND}.
    * @param value the join style
    * @see #getJoin()
    */
   public void setJoin(int value)
   {
      if (value < 0 || value > 3)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.JOIN_STYLE, value,
            getCanvasGraphics());
      }

      join = value;
   }

   /**
    * Gets the current join style.
    * @return the current join style
    * @see #setJoin(int)
    */
   public int getJoin()
   {
      return join;
   }

   /**
    * Sets the current paint.
    * @param p the paint
    * @see #getPaint()
    */
   public void setPaint(JDRPaint p)
   {
      paint = p;
   }

   /**
    * Sets the transfer function for all colour components.
    * @param transFunction the transfer function
    */
   public void setTransfer(EPSProc transFunction)
   {
      for (int i = 0; i < 4; i++)
      {
         transfer[i] = transFunction;
      }
   }

   /**
    * Sets the colour transfer function.
    * @param red the red transfer function
    * @param green the green transfer function
    * @param blue the blue transfer function
    * @param grey the grey transfer function
    */
   public void setColorTransfer(EPSProc red, EPSProc green,
      EPSProc blue, EPSProc grey)
   {
      transfer[0] = red;
      transfer[1] = green;
      transfer[2] = blue;
      transfer[3] = grey;
   }

   /**
    * Gets the colour transfer functions.
    */
   public EPSProc[] getColorTransfer()
   {
      return transfer;
   }

   /**
    * Gets the transfer function.
    */
   public EPSProc getTransfer()
   {
      return transfer[3];
   }

   /**
    * Gets the current paint.
    * @return the current paint
    * @see #setPaint(JDRPaint)
    */
   public JDRPaint getPaint()
      throws InvalidFormatException,
             NoninvertibleTransformException,
             IOException
   {
      EPSStack stack = eps_.getStack();

      if (paint instanceof JDRGray)
      {
         if (transfer[3] == null)
         {
            return paint;
         }

         double grey = ((JDRGray)paint).getGray();

         stack.pushDouble(grey);

         stack.execProc(transfer[3]);

         grey = stack.popDouble();

         return new JDRGray(eps_.getCanvasGraphics(), grey);
      }
      else if (paint instanceof JDRColor)
      {
         JDRColor c = (JDRColor)paint;

         if (transfer[0] == null && transfer[1] == null &&
             transfer[2] == null)
         {
            return paint;
         }

         double red = c.getRed();
         double green = c.getGreen();
         double blue = c.getBlue();

         if (transfer[0] != null)
         {
            stack.pushDouble(red);
            stack.execObject(transfer[0]);
            red = stack.popDouble();
         }

         if (transfer[1] != null)
         {
            stack.pushDouble(green);
            stack.execObject(transfer[1]);
            green = stack.popDouble();
         }

         if (transfer[2] != null)
         {
            stack.pushDouble(blue);
            stack.execObject(transfer[2]);
            blue = stack.popDouble();
         }

         return new JDRColor(eps_.getCanvasGraphics(),
            red, green, blue);
      }
      else if (paint instanceof JDRColorCMYK)
      {
         JDRColorCMYK c = (JDRColorCMYK)paint;

         double cyan = c.getCyan();
         double magenta = c.getYellow();
         double yellow = c.getMagenta();
         double black = c.getKey();

         if (transfer[0] != null)
         {
            stack.pushDouble(cyan);
            stack.execObject(transfer[0]);
            cyan = stack.popDouble();
         }

         if (transfer[1] != null)
         {
            stack.pushDouble(magenta);
            stack.execObject(transfer[1]);
            magenta = stack.popDouble();
         }

         if (transfer[2] != null)
         {
            stack.pushDouble(yellow);
            stack.execObject(transfer[2]);
            yellow = stack.popDouble();
         }

         if (transfer[3] != null)
         {
            stack.pushDouble(black);
            stack.execObject(transfer[2]);
            black = stack.popDouble();
         }

         return new JDRColorCMYK(eps_.getCanvasGraphics(),
            cyan, magenta, yellow, black);
      }

      return paint;
   }

   /**
    * Sets the current mitre limit.
    * @param value the mitre limit (can't be less than 1)
    * @see #getMitreLimit
    */
   public void setMitreLimit(double value)
   {
      if (value < 1)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.MITRE_LIMIT, value,
           getCanvasGraphics());
      }

      mitreLimit = value;
   }

   /**
    * Gets the current mitre limit.
    * @return the current mitre limit
    * @see #setMitreLimit(double)
    */
   public double getMitreLimit()
   {
      return mitreLimit;
   }

   /**
    * Sets the current dash pattern. 
    * A <code>null</code> or zero length <code>pattern</code> 
    * indicates a solid line, otherwise <code>pattern</code>
    * should store the dash pattern as a sequence of 
    * <em>dash length</em> <em>gap length</em> pairs.
    * @param pattern the dash pattern
    * @param offset the dash offset
    * @see #getDashPattern()
    */
   public void setDashPattern(double[] pattern, double offset)
   {
      if (pattern == null || pattern.length == 0)
      {
         dashPattern = null;
      }
      else
      {
         double sse = 0;

         dashPattern = new float[pattern.length];
         for (int i = 0; i < pattern.length; i++)
         {
            dashPattern[i] = (float)pattern[i];
            sse += pattern[i]*pattern[i];
         }

         if (sse == 0) dashPattern = null;
      }

      dashOffset = (float)offset;
   }

   /**
    * Gets the current dash pattern.
    * @return the current dash pattern
    * @see #setDashPattern(double[], double)
    */
   public DashPattern getDashPattern()
   {
      return new DashPattern(eps_.getCanvasGraphics(),
         dashPattern, dashOffset);
   }

   /**
    * Gets the current stroke using non-zero winding rule.
    * @return the current stroke
    */
   public JDRStroke getStroke()
   {
      return getStroke(Path2D.WIND_NON_ZERO);
   }

   /**
    * Gets the current stroke using given winding rule. The
    * winding rule may be either 
    * <code>Path2D.WIND_EVEN_ODD</code> or
    * <code>Path2D.WIND_NON_ZERO</code>
    * @param windingRule the winding rule to use, which must be
    * <code>Path2D.WIND_EVEN_ODD</code> or
    * <code>Path2D.WIND_NON_ZERO</code>
    * @return the current stroke
    */
   public JDRStroke getStroke(int windingRule)
   {
      int capStyle=0;

      switch (cap)
      {
         case CAP_BUTT :
            capStyle = BasicStroke.CAP_BUTT;
         break;
         case CAP_SQUARE :
            capStyle = BasicStroke.CAP_SQUARE;
         break;
         case CAP_ROUND :
            capStyle = BasicStroke.CAP_ROUND;
         break;
      }

      int joinStyle=0;

      switch (join)
      {
         case JOIN_BEVEL :
            joinStyle = BasicStroke.JOIN_BEVEL;
         break;
         case JOIN_MITRE :
            joinStyle = BasicStroke.JOIN_MITER;
         break;
         case JOIN_ROUND :
            joinStyle = BasicStroke.JOIN_ROUND;
         break;
      }

      double linewidthfactor = (Math.abs(af.getScaleX())
                             +  Math.abs(af.getScaleY()))*0.5;

      return new JDRBasicStroke(eps_.getCanvasGraphics(),
            new JDRLength(eps_.getCanvasGraphics(),
               Math.max(1.0,Math.abs(lineWidth*linewidthfactor)),
               JDRUnit.bp),
            capStyle, joinStyle, mitreLimit,
            getDashPattern(), windingRule);
   }

   /**
    * Sets the current transformation matrix.
    * The current transformation matrix is stored as
    * a 1 dimensional vector with 6 elements.
    * @param matrix the current transformation matrix
    */
   public void setTransform(double[] matrix)
   {
      af.setTransform(matrix[0], matrix[1], matrix[2], matrix[3],
        matrix[4], matrix[5]);
      //font.setTransform(matrix);
   }

   /**
    * Gets the current transformation matrix.
    * @return the current transformation matrix
    */
   public AffineTransform getTransform()
   {
      return af;
   }

   /**
    * Translates the current transformation matrix.
    * @param x the x shift
    * @param y the y shift
    */
   public void translate(double x, double y)
   {
      af.translate(x, y);
      //font.translate(x, y);
   }

   /**
    * Scales the current transformation matrix.
    * @param sx the x scale factor
    * @param sy the y scale factor
    */
   public void scale(double sx, double sy)
   {
      af.scale(sx, sy);
      //font.scale(sx, sy);
   }

   /**
    * Rotates the current transformation matrix.
    * @param angle the angle of rotation in radians
    */
   public void rotate(double angle)
   {
      af.rotate(angle);
      //font.rotate(angle);
   }

   /**
    * Concatenates the current transformation matrix with the
    * given matrix.
    * @param matrix the matrix to be concatenated with the
    * current transformation matrix
    */
   public void concat(double[] matrix)
   {
      AffineTransform trans = new AffineTransform(matrix);
      af.concatenate(trans);
      //font.concatenate(trans);
   }

   /**
    * Transforms the given <code>ptSrc</code> and stores the
    * result in <code>ptDst</code> using the current transformation
    * matrix.
    * @param ptSrc the specified point to be transformed
    * @param ptDst the point that stores the result of the
    * transformation
    */
   public void transform(Point2D ptSrc, Point2D ptDst)
   {
      af.transform(ptSrc, ptDst);
   }

   /**
    * Transforms the relative distance vector given by 
    * <code>ptSrc</code> and stores the
    * result in <code>ptDst</code> using the current transformation
    * matrix.
    * @param ptSrc the distance vector to be delta transformed
    * @param ptDst the resulting transformed distance vector
    */
   public void dtransform(Point2D ptSrc, Point2D ptDst)
   {
      af.deltaTransform(ptSrc, ptDst);
   }

   /**
    * Computes the inverse transformation of <code>ptSrc</code>
    * and stores the result in <code>ptDst</code>.
    * @param ptSrc the specified point to be transformed
    * @param ptDst the point that stores the result of the
    * transformation
    * @throws NoninvertibleTransformException if the current
    * transformation matrix can not be inverted
    */
   public void itransform(Point2D ptSrc, Point2D ptDst)
      throws NoninvertibleTransformException
   {
      af.inverseTransform(ptSrc, ptDst);
   }

   /**
    * Computes the inverse delta transformation of <code>ptSrc</code>
    * and stores the result in <code>ptDst</code>.
    * @param ptSrc the distance vector to be transformed
    * @param ptDst the transformed distance vector
    * @throws NoninvertibleTransformException if the current
    * transformation matrix can not be inverted
    */
   public void idtransform(Point2D ptSrc, Point2D ptDst)
      throws NoninvertibleTransformException
   {
      af.createInverse().deltaTransform(ptSrc, ptDst);
   }

   /**
    * Appends given path to the current path.
    * @param p the new path to append to the current path
    */
   public void appendPath(Path2D p)
   {
      PathIterator pi = p.getPathIterator(null);
      float[] coords = new float[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               path.moveTo(coords[0], coords[1]);
            break;
            case PathIterator.SEG_LINETO :
               path.lineTo(coords[0], coords[1]);
            break;
            case PathIterator.SEG_CUBICTO :
               path.curveTo(coords[0], coords[1],
                       coords[2], coords[3],
                       coords[4], coords[5]);
            break;
            case PathIterator.SEG_QUADTO :
               Point2D p0 = getCurrentPoint();
               Point2D p1 = new Point2D.Double(coords[0], coords[1]);
               Point2D p2 = new Point2D.Double(coords[2], coords[3]);
               double c1x = (2*p1.getX()+p0.getX())/3;
               double c1y = (2*p1.getY()+p0.getY())/3;
               double c2x = (p2.getX()+2*p1.getX())/3;
               double c2y = (p2.getY()+2*p1.getY())/3;

               path.curveTo((float)c1x, (float)c1y,
                            (float)c2x, (float)c2y,
                            (float)p2.getX(), (float)p2.getY());
            break;
            case PathIterator.SEG_CLOSE :
               path.closePath();
            break;
         }
         pi.next();
      }
   }

   /**
    * Sets the current path.
    * @param p the new current path
    * @see #getGeneralPath()
    */
   public void setGeneralPath(Path2D p)
   {
      path = p;
   }

   /**
    * Gets the current path.
    * @return the current path
    * @see #setGeneralPath(Path2D)
    */
   public Path2D getGeneralPath()
   {
      return path;
   }

   /**
    * Paints the area inside the specified rectangle.
    */
   public JDRPath rectFill(double x, double y, double width,
      double height)
   throws InvalidFormatException,IOException,
          NoninvertibleTransformException
   {
      Rectangle2D rect = new Rectangle2D.Double(x, y, width,
        height);

      JDRPath jdrpath = null;

      jdrpath = JDRPath.getPath(eps_.getCanvasGraphics(),
         rect.getPathIterator(af, flatness));

      JDRPaint fillPaint = getPaint();
      JDRPaint linePaint = new JDRTransparent(eps_.getCanvasGraphics());

      jdrpath.setLinePaint(linePaint);
      jdrpath.setFillPaint(fillPaint);

      return jdrpath;
   }

   /**
    * Strokes the the specified rectangle.
    */
   public JDRPath rectStroke(double x, double y, double width,
      double height)
   throws InvalidFormatException,IOException,
          NoninvertibleTransformException
   {
      JDRPath jdrpath = null;

      Rectangle2D rect = new Rectangle2D.Double(
         x, y, width, height);

      jdrpath = JDRPath.getPath(eps_.getCanvasGraphics(),
         rect.getPathIterator(af, flatness));

      JDRPaint linePaint = getPaint();
      JDRPaint fillPaint = new JDRTransparent(eps_.getCanvasGraphics());

      jdrpath.setLinePaint(linePaint);
      jdrpath.setFillPaint(fillPaint);

      return jdrpath;
   }

   /**
    * Starts a new path. This sets the current path to an empty
    * path.
    */
   public void newPath()
   {
      path = new Path2D.Double();
   }

   /**
    * Gets the transformed current point.
    * @return the transformed current point
    */
   public Point2D getCurrentPoint()
   {
      return path.getCurrentPoint();
   }

   /**
    * Move the current point and add point to
    * the current path.
    * @param x the specified x co-ordinate
    * @param y the specified y co-ordinate
    */
   public void moveTo(double x, double y)
   {
      Point2D.Double p = new Point2D.Double(x, y);
      af.transform(p, p);
      path.moveTo((float)p.getX(), (float)p.getY());
   }

   /**
    * Move the current point by a relative amount and add new point to
    * the current path.
    * @param x the specified x shift
    * @param y the specified y shift
    * @throws NoCurrentPointException if there is no current point
    */
   public void rmoveTo(double x, double y)
      throws NoCurrentPointException
   {
      Point2D oldP = getCurrentPoint();

      if (oldP == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      double oldX = oldP.getX();
      double oldY = oldP.getY();

      Point2D.Double p = new Point2D.Double(x, y);
      af.deltaTransform(p, p);

      path.moveTo((float)(oldX+p.getX()), (float)(oldY+p.getY()));
   }

   /**
    * Move the current point and add a line to that point in
    * the current path.
    * @param x the specified x co-ordinate
    * @param y the specified y co-ordinate
    * @throws NoCurrentPointException if there is no current point
    */
   public void lineTo(double x, double y)
      throws NoCurrentPointException
   {
      if (getCurrentPoint() == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Point2D.Double p = new Point2D.Double(x, y);
      af.transform(p, p);
      path.lineTo((float)p.getX(), (float)p.getY());
   }

   /**
    * Move the current point by a relative amount and add a line to 
    * the new point in the current path.
    * @param x the specified x shift
    * @param y the specified y shift
    * @throws NoCurrentPointException if there is no current point
    */
   public void rlineTo(double x, double y)
      throws NoCurrentPointException
   {
      Point2D oldP = getCurrentPoint();

      if (oldP == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      double oldX = oldP.getX();
      double oldY = oldP.getY();

      Point2D.Double p = new Point2D.Double(x, y);
      af.deltaTransform(p, p);

      path.lineTo((float)(oldX+p.getX()), (float)(oldY+p.getY()));
   }

   /**
    * Move the current point to (<code>x3</code>, <code>y3</code>) 
    * and add a cubic B&eacute;zier curve to the current path.
    * @param x1 the co-ordinates of the first B&eacute;zier control
    * point 
    * @param y1 the co-ordinates of the first B&eacute;zier control
    * point
    * @param x2 the co-ordinates of the second B&eacute;zier control
    * point 
    * @param y2 the co-ordinates of the second B&eacute;zier control
    * point
    * @param x3 the co-ordinates of the end point
    * point 
    * @param y3 the co-ordinates of the end point
    * @throws NoCurrentPointException if there is no current point
    */
   public void curveTo(double x1, double y1, double x2, double y2,
      double x3, double y3)
   throws NoCurrentPointException
   {
      if (getCurrentPoint() == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Point2D.Double p1 = new Point2D.Double(x1, y1);
      af.transform(p1, p1);
      Point2D.Double p2 = new Point2D.Double(x2, y2);
      af.transform(p2, p2);
      Point2D.Double p3 = new Point2D.Double(x3, y3);
      af.transform(p3, p3);
      path.curveTo((float)p1.getX(), (float)p1.getY(),
                   (float)p2.getX(), (float)p2.getY(),
                   (float)p3.getX(), (float)p3.getY());
   }

   /**
    * Move the current point by (<code>x3</code>, <code>y3</code>) 
    * and add a cubic B&eacute;zier curve to the current path.
    * @param x1 the relative co-ordinates of the first B&eacute;zier control
    * point 
    * @param y1 the relative co-ordinates of the first B&eacute;zier control
    * point
    * @param x2 the relative co-ordinates of the second B&eacute;zier control
    * point 
    * @param y2 the relative co-ordinates of the second B&eacute;zier control
    * point
    * @param x3 the relative co-ordinates of the end point
    * point 
    * @param y3 the relative co-ordinates of the end point
    * @throws NoCurrentPointException if there is no current point
    */
   public void rcurveTo(double x1, double y1, double x2, double y2,
      double x3, double y3)
      throws NoCurrentPointException
   {
      Point2D p = getCurrentPoint();

      if (p == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      double oldX = p.getX();
      double oldY = p.getY();

      Point2D.Double p1 = new Point2D.Double(x1, y1);
      af.deltaTransform(p1, p1);
      Point2D.Double p2 = new Point2D.Double(x2, y2);
      af.deltaTransform(p2, p2);
      Point2D.Double p3 = new Point2D.Double(x3, y3);
      af.deltaTransform(p3, p3);

      path.curveTo((float)(oldX+p1.getX()), (float)(oldY+p1.getY()),
                   (float)(oldX+p2.getX()), (float)(oldY+p2.getY()),
                   (float)(oldX+p3.getX()), (float)(oldY+p3.getY()));
   }

   /**
    * Move the current point to (<code>x2</code>, <code>y2</code>) 
    * and add a cubic B&eacute;zier curve to the current path formed
    * from the given quadratic B&eacute;zier control points.
    * (The curve is converted to a cubic B&eacute;zier curve
    * because {@link JDRPath} does not use quadratic B&eacute;zier
    * segments.)
    * @param x1 the co-ordinates of the first quadratic B&eacute;zier control
    * point 
    * @param y1 the co-ordinates of the first quadratic B&eacute;zier control
    * point
    * @param x2 the co-ordinates of the end point
    * point 
    * @param y2 the co-ordinates of the end point
    * @throws NoCurrentPointException if there is no current point
    */
   public void quadTo(double x1, double y1, double x2, double y2)
      throws NoCurrentPointException
   {
      Point2D p0 = getCurrentPoint();

      if (p0 == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Point2D.Double p1 = new Point2D.Double(x1, y1);
      af.transform(p1, p1);
      Point2D.Double p2 = new Point2D.Double(x2, y2);
      af.transform(p2, p2);

      path.quadTo((float)p1.getX(), (float)p1.getY(),
                  (float)p2.getX(), (float)p2.getY());
/*
      double c1x = (2*p1.getX()+p0.getX())/3;
      double c1y = (2*p1.getY()+p0.getY())/3;
      double c2x = (p2.getX()+2*p1.getX())/3;
      double c2y = (p2.getY()+2*p1.getY())/3;

      path.curveTo((float)c1x, (float)c1y,
                   (float)c2x, (float)c2y,
                   (float)p2.getX(), (float)p2.getY());
*/
      double c1x = (2*x1+p0.getX())/3;
      double c1y = (2*y1+p0.getY())/3;
      double c2x = (x2+2*x1)/3;
      double c2y = (y2+2*y1)/3;

      path.curveTo((float)c1x, (float)c1y,
                   (float)c2x, (float)c2y,
                   (float)x2, (float)y2);
   }

   /**
    * Move the current point by (<code>x2</code>, <code>y2</code>) 
    * and add a cubic B&eacute;zier curve to the current path formed
    * from the given relative quadratic B&eacute;zier control points.
    * (The curve is converted to a cubic B&eacute;zier curve
    * because {@link JDRPath} does not use quadratic B&eacute;zier
    * segments.)
    * @param x1 the relative co-ordinates of the first quadratic B&eacute;zier control
    * point 
    * @param y1 the relative co-ordinates of the first quadratic B&eacute;zier control
    * point
    * @param x2 the relative co-ordinates of the end point
    * point 
    * @param y2 the relative co-ordinates of the end point
    * @throws NoCurrentPointException if there is no current point
    */
   public void rquadTo(float x1, float y1, float x2, float y2)
      throws NoCurrentPointException
   {
      Point2D p0 = getCurrentPoint();

      if (p0 == null)
      {
         throw new NoCurrentPointException(eps_);
      }


      Point2D.Double p1 = new Point2D.Double(x1, y1);
      af.deltaTransform(p1, p1);
      Point2D.Double p2 = new Point2D.Double(x2, y2);
      af.deltaTransform(p2, p2);

      path.quadTo((float)(p0.getX()+p1.getX()),
                  (float)(p0.getY()+p1.getY()),
                  (float)(p0.getX()+p2.getX()),
                  (float)(p0.getY()+p2.getY()));
/*
      Point2D.Double p1 = new Point2D.Double(x1+p0.getX(),
         y1+p0.getY());
      Point2D.Double p2 = new Point2D.Double(x2+p0.getX(),
         y2+p0.getY());

      double c1x = (2*p1.getX()+p0.getX())/3;
      double c1y = (2*p1.getY()+p0.getY())/3;
      double c2x = (p2.getX()+2*p1.getX())/3;
      double c2y = (p2.getY()+2*p1.getY())/3;

      path.curveTo((float)c1x, (float)c1y,
                   (float)c2x, (float)c2y,
                   (float)p2.getX(), (float)p2.getY());
*/
   }

   /**
    * Adds an arc from <code>startAngle</code> to
    * <code>endAngle</code>, with centre 
    * (<code>x</code>, <code>y</code>) and given radius.
    * This is equivalent to the PostScript command:
    * <em>x</em> <em>y</em> <em>radius</em> <em>startAngle</em>
    * <em>endAngle</em> <code>arc</code>.
    * @param x the co-ordinates of the centre point
    * @param y the co-ordinates of the centre point
    * @param radius the radius of the arc
    * @param startAngle the starting angle (in degrees)
    * @param endAngle the ending angle (in degrees)
    * @see #arcn(double, double, double, double, double)
    */
   public void arc(double x, double y, double radius,
      double startAngle, double endAngle)
   {
      if (startAngle < 0)
      {
         startAngle = (double)(360*((int)-startAngle/360+1))+startAngle;
      }
      else if (startAngle > 360)
      {
         startAngle = startAngle-(double)(360*((int)startAngle/360));
      }

      if (endAngle < 0)
      {
         endAngle = (double)(360*((int)-endAngle/360+1))+endAngle;
      }
      else if (endAngle > 360)
      {
         endAngle = endAngle-(double)(360*((int)endAngle/360));
      }

      double extent = Math.abs(startAngle-endAngle);

      if (startAngle > endAngle)
      {
         extent = 360 - extent;
      }

      Arc2D arc = new Arc2D.Double(new Rectangle2D.Double(
            -radius, -radius, 2*radius, 2*radius),
            startAngle, extent, Arc2D.OPEN);
      AffineTransform aft = new AffineTransform(1,0,0,-1,0,0);
      aft.translate(x, -y);

      Point2D p = getCurrentPoint();

      PathIterator pi = arc.getPathIterator(aft);
      float[] coords = new float[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               if (p == null)
               {
                  moveTo(coords[0], coords[1]);
               }
               else
               {
                  try
                  {
                     lineTo(coords[0], coords[1]);
                  }
                  catch (NoCurrentPointException e)
                  {
                  }
                  p = null;
               }
            break;
            case PathIterator.SEG_LINETO :
               try
               {
                  lineTo(coords[0], coords[1]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_CUBICTO :
               try
               {
                  curveTo(coords[0], coords[1],
                          coords[2], coords[3],
                          coords[4], coords[5]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_QUADTO :
               try
               {
                  quadTo(coords[0], coords[1],
                         coords[2], coords[3]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_CLOSE :
                  closePath();
            break;
         }
         pi.next();
      }
   }

   /**
    * Adds an arc from <code>startAngle</code> to
    * <code>endAngle</code>, with centre 
    * (<code>x</code>, <code>y</code>) and given radius in the
    * negative direction.
    * This is equivalent to the PostScript command:
    * <em>x</em> <em>y</em> <em>radius</em> <em>startAngle</em>
    * <em>endAngle</em> <code>arcn</code>.
    * @param x the co-ordinates of the centre point
    * @param y the co-ordinates of the centre point
    * @param radius the radius of the arc
    * @param startAngle the starting angle (in degrees)
    * @param endAngle the ending angle (in degrees)
    * @see #arc(double, double, double, double, double)
    * @throws NoCurrentPointException if there is no current point
    */
   public void arcn(double x, double y, double radius,
      double startAngle, double endAngle)
   {
      if (startAngle < 0)
      {
         startAngle = (double)(360*((int)-startAngle/360+1))+startAngle;
      }
      else if (startAngle > 360)
      {
         startAngle = startAngle-(double)(360*((int)startAngle/360));
      }

      if (endAngle < 0)
      {
         endAngle = (double)(360*((int)-endAngle/360+1))+endAngle;
      }
      else if (endAngle > 360)
      {
         endAngle = endAngle-(double)(360*((int)endAngle/360));
      }

      double extent = Math.abs(startAngle-endAngle);

      if (startAngle < endAngle)
      {
         extent = 360 - extent;
      }

      Arc2D arc = new Arc2D.Double(new Rectangle2D.Double(
               -radius, -radius, 2*radius, 2*radius),
               startAngle, -extent, Arc2D.OPEN);
      AffineTransform aft = new AffineTransform(1,0,0,-1,0,0);
      aft.translate(x, -y);

      Point2D p = getCurrentPoint();

      PathIterator pi = arc.getPathIterator(aft);
      float[] coords = new float[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               if (p == null)
               {
                  moveTo(coords[0], coords[1]);
               }
               else
               {
                  try
                  {
                     lineTo(coords[0], coords[1]);
                  }
                  catch (NoCurrentPointException e)
                  {
                  }
                  p = null;
               }
            break;
            case PathIterator.SEG_LINETO :
               try
               {
                  lineTo(coords[0], coords[1]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_CUBICTO :
               try
               {
                  curveTo(coords[0], coords[1],
                          coords[2], coords[3],
                          coords[4], coords[5]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_QUADTO :
               try
               {
                  quadTo(coords[0], coords[1],
                         coords[2], coords[3]);
               }
               catch (NoCurrentPointException e)
               {
               }
            break;
            case PathIterator.SEG_CLOSE :
               closePath();
            break;
         }
         pi.next();
      }
   }

   /**
    * Appends tangent arc to current path and sets the two tangent
    * points in user space. This is equivalent to the PostScript
    * command: <em>x<sub>1</sub></em> <em>y<sub>1</sub></em>
    * <em>x<sub>2</sub></em> <em>y<sub>2</sub></em>
    * <em>radius</em> <code>arcto</code> 
    * <em>xt<sub>1</sub></em> <em>yt<sub>1</sub></em>
    * <em>xt<sub>2</sub></em> <em>yt<sub>2</sub></em>.
    * @param x1 the starting point of the arc
    * @param y1 the starting point of the arc
    * @param x2 the end point of the arc
    * @param y2 the end point of the arc
    * @param radius the radius of the arc
    * @param tangent1 on exit contains 
    * <em>xt<sub>1</sub></em> <em>yt<sub>1</sub></em>
    * (the first tangent point)
    * @param tangent2 on exit contains 
    * <em>xt<sub>2</sub></em> <em>yt<sub>2</sub></em>
    * (the second tangent point)
    * @throws NoCurrentPointException if there is no current point
    */
   public void arcTo(double x1, double y1,
      double x2, double y2, double radius,
      Point2D tangent1, Point2D tangent2)
      throws NoCurrentPointException
   {
      Point2D p0 = getCurrentPoint();

      if (p0 == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      double x0 = p0.getX();
      double y0 = p0.getY();

      // deal with special cases
      double deltaX = x1 - x0;
      double deltaY = y1 - y0;
      double deltaX2 = x2 - x1;
      double deltaY2 = y2 - y1;

      int line1=0;
      int line2=0;

      try
      {
         double r = 1/deltaY;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            line1 = 1;// line1 is approximately horizontal
         }
      }
      catch (ArithmeticException e)
      {
         line1 = 1;// line1 is horizontal
      }

      try
      {
         double r = 1/deltaY2;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            line2 = 1;// line2 is approximately horizontal
         }
      }
      catch (ArithmeticException e)
      {
         line2 = 1;// line2 is horizontal
      }

      try
      {
         double r = 1/deltaX;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            line1 = -1;// line1 is approximately vertical
         }
      }
      catch (ArithmeticException e)
      {
         line1 = -1;// line1 is vertical
      }

      try
      {
         double r = 1/deltaX2;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            line2 = -1;// line2 is approximately vertical
         }
      }
      catch (ArithmeticException e)
      {
         line2 = -1;// line2 is vertical
      }

      if ((line1 == -1 && line2 == -1) || (line1 == 1 && line2 == 1))
      {
         lineTo(x1, y1);
         tangent1.setLocation(x1, y1);
         tangent2.setLocation(x1, y1);
         return;
      }

      double xt, yt, xs, ys, cx, cy;

      if (line1 == 1 && line2 == -1)
      {
         // line1 is horizontal, line2 is vertical

         xt = x1 + Math.signum(x0-x1)*radius;
         yt = y1;

         xs = x1;
         ys = y1 + Math.signum(y2-y1)*radius;

         cx = xt;
         cy = ys;
      }
      else if (line1 == -1 && line2 == 1)
      {
         // line1 is vertical, line2 is horizontal

         xt = x1;
         yt = y1 + Math.signum(y0-y1)*radius;

         xs = x1 + Math.signum(x2-x1)*radius;
         ys = y1;

         cx = xs;
         cy = yt;
      }
      else if (line1 == -1)
      {
         // line1 is vertical
         double m2 = deltaY2/deltaX2;

         double k4 = -Math.signum(x2-x1)*radius;
         double k2 = radius/Math.sqrt(m2*m2+1);
         double k3 = y2 - m2*x2;

         cx = x1 - k4;
         xt = x1;
         xs = -m2*k2 + x1 + Math.signum(x2-x1)*radius;
         cy = -m2*m2*k2 + k3 - k2 + m2*xt;
         yt = cy;
         ys = cy + k2;

         double t = (yt-y0)/(y1-y0);
         double s = (ys-y1)/(y2-y1);
   
         if (t > 1 && (s >= 0 && s <= 1))
         {
            k2 = -k2;

            xs = -m2*k2 + x1 + Math.signum(x2-x1)*radius;
            cy = -m2*m2*k2 + k3 - k2 + m2*xt;
            yt = cy;
            ys = cy + k2;
         }
      }
      else if (line2 == -1)
      {
         // line2 is vertical
         double m1 = deltaY/deltaX;

         double k1 = -Math.signum(y0-y2)*radius;
         double k5 = radius/Math.sqrt(m1*m1+1);
         double k4 = -m1*k5;
         double k6 = y1 - m1*x1;

         xs = x1;
         cx = x1 - k1;
         xt = cx + k4;
         yt = k6 + m1*k4 - m1*k1;
         cy = yt + (xt - cx)/m1;
         ys = cy;

         double t = (yt-y0)/(y1-y0);
         double s = (ys-y1)/(y2-y1);
   
         if (s < 0 && (t >= 0 && t <= 1))
         {
            k5 = -k5;
            k4 = -m1*k5;

            xt = cx + k4;
            yt = k6 + m1*k4 - m1*k1;
            cy = yt + (xt - cx)/m1;
            ys = cy;
         }
      }
      else
      {
         double m1 = deltaY/deltaX;
         double m2 = deltaY2/deltaX2;
   
         double m1_m2 = m1-m2;
   
         double k6 = y1 - m1*x1;
         double k3 = y2 - m2*x2;
   
         double k2 = radius/Math.sqrt(m2*m2+1);
         double k1 = -m2*k2;
   
         double k5 = radius/Math.sqrt(m1*m1+1);
         double k4 = -m1*k5;
   
         yt = m2*(m1*k1-m1*k4-k6)/m1_m2
                   + m1*(k3-k2+k5)/m1_m2;
   
         xt = (yt - k6)/m1;
   
         cy = -m2*k4 + m2*k1 + k3 - k2 + m2*xt;
   
         ys = cy + k2;
   
         cx = xt - k4;
   
         xs = k1 + cx;
   
         double t = (yt-y0)/(y1-y0);
         double s = (ys-y1)/(y2-y1);
   
         boolean redo=false;
   
         if (t > 1 && s < 0)
         {
            k5 = -k5;
            k2 = -k2;
            redo=true;
         }
         else if (t > 1 && (s >= 0 && s <= 1))
         {
            k2 = -k2;
            redo=true;
         }
         else if (s < 0 && (t >= 0 && t <= 1))
         {
            k5 = -k5;
            redo=true;
         }
   
         if (redo)
         {
            k1 = -m2*k2;
            k4 = -m1*k5;
   
            yt = m2*(m1*k1-m1*k4-k6)/m1_m2
                   + m1*(k3-k2+k5)/m1_m2;
            xt = (yt - k6)/m1;
            cy = -m2*k4 + m2*k1 + k3 - k2 + m2*xt;
            ys = cy + k2;
            cx = xt - k4;
            xs = k1 + cx;
         }
      }

      deltaX = xt - cx;
      deltaY = yt - cy;

      double start;

      boolean isVert = false;

      try
      {
         double r = 1/deltaX;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            isVert = true;
         }
      }
      catch (ArithmeticException e)
      {
         isVert = true;
      }

      if (isVert)
      {
         if (cy < yt)
         {
            start = Math.PI*0.5;
         }
         else
         {
            start = Math.PI*1.5;
         }
      }
      else
      {
         start = Math.atan2(deltaY, deltaX);
      }

      deltaX = xs - cx;
      deltaY = ys - cy;

      isVert = false;

      try
      {
         double r = 1/deltaX;

         if (r == Double.POSITIVE_INFINITY
          || r == Double.NEGATIVE_INFINITY)
         {
            isVert = true;
         }
      }
      catch (ArithmeticException e)
      {
         isVert = true;
      }

      double end;

      if (isVert)
      {
         if (cy < ys)
         {
            end = Math.PI*0.5;
         }
         else
         {
            end = Math.PI*1.5;
         }
      }
      else
      {
         end = Math.atan2(deltaY, deltaX);
      }

      if (((ys-yt)/(xs-xt)) > 0)
      {
         arcn(cx, cy, radius, Math.toDegrees(start), Math.toDegrees(end));
      }
      else
      {
         arc(cx, cy, radius, Math.toDegrees(start), Math.toDegrees(end));
      }

      tangent1.setLocation(xt, yt);
      tangent2.setLocation(xs, ys);
   }

    /**
    * Closes current path.
    */
   public void closePath()
   {
      path.closePath();
   }

   /**
    * Appends shape to current path. If <code>connect</code> is 
    * <code>true</code> a connecting line is drawn from the current
    * path to the given shape, otherwise a move is issued from
    * the current path to the given shape.
    * @param s the shape to append to the current path
    * @param connect indicates whether to connect the two shapes
    * with a line
    */
   public void append(Shape s, boolean connect)
   {
      path.append(s, connect);
   }

   /**
    * Sets the clipping path to the current path using non zero
    * winding rule.
    */
   public void clip()
   {
      clippingPath = new Path2D.Double(path);
      ((Path2D)clippingPath).setWindingRule(Path2D.WIND_NON_ZERO);
   }

   /**
    * Sets the clipping path to the current path using even odd
    * winding rule.
    */
   public void eoclip()
   {
      clippingPath = new Path2D.Double(path);
      ((Path2D)clippingPath).setWindingRule(Path2D.WIND_EVEN_ODD);
   }

   /**
    * Intersects the area inside the clipping path with
    * the rectangle specified by the parameters.
    * @param x the minimum x co-ordinate of the rectangle
    * @param y the minimum y co-ordinate of the rectangle
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    */
   public void clipRect(double x, double y, double width,
      double height)
   {
      Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
      clip(rect);
   }

   /**
    * Intersects the area inside the clipping path with the
    * given shape.
    * @param shape the required shape
    */
   public void clip(Shape shape)
   {
      if (clippingPath == null)
      {
         clippingPath = new Path2D.Double(shape, af);
      }
      else
      {
         Area area = new Area(clippingPath);
         area.intersect(new Area(af.createTransformedShape(shape)));

         clippingPath = new Path2D.Double(area);
      }
   }

   /**
    * Creates a shaped path, using the shading dictionary.
    * Can only implement linear or radial shading type. If another
    * type is found or if a function type other than 2 is found, 
    * the shading is ignored, and a warning message
    * is issued.
    * @param dict the shading dictionary
    * @throws InvalidFormatException if an invalid 
    * format is found in the dictionary
    */
   public JDRPath createShadedPath(EPSFile file, EPSDict dict)
   throws InvalidFormatException
   {
      JDRPath jdrpath = JDRPath.getPath(eps_.getCanvasGraphics(),
         clippingPath.getPathIterator(null));

      jdrpath.getStroke().setWindingRule(
         clippingPath.getWindingRule());

      BBox box = jdrpath.getStorageBBox();

      int type;

      try
      {
         type = dict.getInt("/ShadingType");
      }
      catch (Exception e)
      {
         throw new InvalidFormatException(
            "Number required for /ShadingType", e);
      }

      String colorSpace = dict.getProc("/ColorSpace").toString();

      if (colorSpace == null)
      {
         eps_.warning(file,
            "No /ColorSpace found, no fill colour set");
         return jdrpath;
      }

      double[] coords = dict.getDoubleArray("/Coords");

      if (coords == null)
      {
         throw new InvalidFormatException("No /Coords found");
      }

      EPSDict funcDict = dict.getDict("/Function");

      if (funcDict == null)
      {
         eps_.warning(file, "No /Function found, no fill colour set");
         return jdrpath;
      }

      int function = funcDict.getInt("/FunctionType");

      if (function != 2)
      {
         eps_.warning(file,
            "Don't know how to deal with function type "+function);
         return jdrpath;
      }

      double[] c0array = funcDict.getDoubleArray("/C0");

      if (c0array == null)
      {
         eps_.warning(file, "Can't find /C0 in /Function");
         return jdrpath;
      }

      double[] c1array = funcDict.getDoubleArray("/C1");

      if (c1array == null)
      {
         eps_.warning(file, "Can't find /C1 in /Function");
         return jdrpath;
      }

      JDRPaint start=null;
      JDRPaint end=null;

      if (colorSpace.equals("/DeviceRGB"))
      {
         start = new JDRColor(eps_.getCanvasGraphics(),
                              c0array[0],
                              c0array[1],
                              c0array[2]);
         end   = new JDRColor(eps_.getCanvasGraphics(),
                              c1array[0],
                              c1array[1],
                              c1array[2]);
      }
      else if (colorSpace.equals("/DeviceCMYK"))
      {
         start = new JDRColorCMYK(eps_.getCanvasGraphics(),
                                  c0array[0],
                                  c0array[1],
                                  c0array[2],
                                  c0array[3]);

         end = new JDRColorCMYK(eps_.getCanvasGraphics(),
                                c1array[0],
                                c1array[1],
                                c1array[2],
                                c1array[3]);
      }
      else if (colorSpace.equals("/DeviceGray"))
      {
         double gray0 = c0array[0];
         double gray1 = c1array[0];

         start = new JDRColor(eps_.getCanvasGraphics(), gray0, gray0, gray0);
         end = new JDRColor(eps_.getCanvasGraphics(), gray1, gray1, gray1);
      }
      else
      {
         eps_.warning(file,
            "Don't know what to do with colour space "+colorSpace);
         return jdrpath;
      }

      JDRPaint paint = null;

      if (type == 2)
      {
         // linear
         double x0, x1, y0, y1;

         try
         {
            x0 = coords[0];
            y0 = coords[1];
            x1 = coords[2];
            y1 = coords[3];
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException(
               "Can't parse shading co-ordinates", e);
         }

         double dx = x0-x1;
         double dy = y0-y1;

         int direction;

         if (dx == 0)
         {
            direction = (y0 > y1 ? JDRGradient.NORTH
                                 : JDRGradient.SOUTH);
         }
         else
         {
            double gradient = dy/dx;
            double min=Math.abs(gradient);
            direction = (x0 < x1 ? JDRGradient.EAST
                                 : JDRGradient.WEST);

            double diff = Math.abs(gradient-1);
            if (diff < min)
            {
               direction = (x0 < x1 ? JDRGradient.NORTH_EAST
                                    : JDRGradient.SOUTH_WEST);
               min = diff;
            }

            diff = Math.abs(gradient+1);
            if (diff < min)
            {
               direction = (x0 < x1 ? JDRGradient.SOUTH_EAST
                                    : JDRGradient.NORTH_WEST);
            }
         }

         paint = new JDRGradient(direction, start, end);
      }
      else if (type == 3)
      {
         // radial
         double x0, x1, y0, y1, r0, r1;

         try
         {
            x0 = coords[0];
            y0 = coords[1];
            r0 = coords[2];
            x1 = coords[3];
            y1 = coords[4];
            r1 = coords[5];
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException(
               "Can't parse shading co-ordinates", e);
         }

         Point2D centre = new Point2D.Double(0.5*(x0+x1),0.5*(y0+y1));

         int location = JDRRadial.CENTER;

         double thirdWidth = box.getWidth()/3;
         double thirdHeight = box.getHeight()/3;

         x0 = box.getMinX()+thirdWidth;
         y0 = box.getMinY()+thirdHeight;

         x1 = box.getMinX()+2*thirdWidth;
         y1 = box.getMinY()+2*thirdHeight;

         af.transform(centre, centre);

         double y = centre.getY();
         double x = centre.getX();

         if (y < y0)
         {
            if (x < x0)
            {
               location = JDRRadial.SOUTH_WEST;
            }
            else if (x > x1)
            {
               location = JDRRadial.SOUTH_EAST;
            }
            else
            {
               location = JDRRadial.SOUTH;
            }
         }
         else if (y > y1)
         {
            if (x < x0)
            {
               location = JDRRadial.NORTH_WEST;
            }
            else if (x > x1)
            {
               location = JDRRadial.NORTH_EAST;
            }
            else
            {
               location = JDRRadial.NORTH;
            }
         }
         else
         {
            if (x < x0)
            {
               location = JDRRadial.WEST;
            }
            else if (x > x1)
            {
               location = JDRRadial.EAST;
            }
         }

         paint = new JDRRadial(location, start, end);
      }
      else
      {
         eps_.warning(file, "Unable to implement shading type "+type);
      }

      if (paint != null)
      {
         jdrpath.setLinePaint(new JDRTransparent(eps_.getCanvasGraphics()));
         jdrpath.setFillPaint(paint);
      }

      return jdrpath;
   }

   /**
    * Gets the path iterator for the current path (flattening if
    * {@link #flatten()} has been called).
    * @return the path iterator for the current path
    */
   public PathIterator getPathIterator()
   {
      return getPathIterator(null);
   }

   /**
    * Gets the path iterator for the transformed current path, 
    * (flattening if {@link #flatten()} has been called).
    * @param aft the <code>AffineTransform</code> with which to 
    * transform the current path
    * @return the path iterator for the transformed current path
    */
   public PathIterator getPathIterator(AffineTransform aft)
   {
      if (flatten_)
      {
         return path.getPathIterator(aft, flatness);
      }
      else
      {
         return path.getPathIterator(aft);
      }
   }

   /**
    * Prints the given path specification.
    * This is provided for debugging.
    * @param pi the path iterator describing the path to be displayed
    */
   public void printPath(PathIterator pi)
   {
      float[] coords = new float[6];

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         switch (type)
         {
            case PathIterator.SEG_MOVETO :
               eps_.getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createVerbose(1,
                  "M: ("+coords[0]+", "+coords[1]+")"));
            break;
            case PathIterator.SEG_LINETO :
               eps_.getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createVerbose(1, 
                  "L: ("+coords[0]+", "+coords[1]+")"));
            break;
            case PathIterator.SEG_CUBICTO :
               eps_.getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createVerbose(1, 
                  "C: ("+coords[0]+", "+coords[1]+")("
                       +coords[2]+", "+coords[3]+")("
                       +coords[4]+", "+coords[5]+")"));
            break;
            case PathIterator.SEG_QUADTO :
               eps_.getMessageSystem().getPublisher().publishMessages(
                 MessageInfo.createVerbose(1,
                  "Q: ("+coords[0]+", "+coords[1]+")("
                      +coords[2]+", "+coords[3]+")"));
            break;
            case PathIterator.SEG_CLOSE :
               eps_.getMessageSystem().getPublisher().publishMessages(
                  MessageInfo.createVerbose(1, "Z"));
            break;
         }
         pi.next();
      }
   }

   /**
    * Prints the given path specification.
    * This is provided for debugging.
    * @param shape the shape describing the path to be displayed
    */
   public void printPath(Shape shape)
   {
      printPath(shape.getPathIterator(null));
   }

   /**
    * Prints the current path specification.
    * This is provided for debugging.
    */
   public void printCurrentPath()
   {
      printPath(getPathIterator());
   }

   /**
    * Creates a stroked path from the current graphics state.
    * @return the stroked path
    * @throws InvalidPathException if path can't be constructed
    * @throws InvalidFormatException if transfer function has 
    * invalid format
    * @throws NoninvertibleTransformException if transfer function
    * attempts a non-invertible transform
    * @throws IOException if transfer function encounters I/O error
    */
   public JDRPath createStrokedPath()
   throws InvalidFormatException,
          NoninvertibleTransformException,
          IOException
   {
      return createStrokedPath(Path2D.WIND_NON_ZERO);
   }

   /**
    * Creates a stroked path from the current graphics state 
    * using the given winding rule.
    * @param windingRule the winding rule, which must be
    * <code>Path2D.WIND_EVEN_ODD</code> or
    * <code>Path2D.WIND_NON_ZERO</code>
    * @return the stroked path
    * @throws InvalidPathException if path can't be constructed
    * @throws InvalidFormatException if transfer function has 
    * invalid format
    * @throws NoninvertibleTransformException if transfer function
    * attempts a non-invertible transform
    * @throws IOException if transfer function encounters I/O error
    */
   public JDRPath createStrokedPath(int windingRule)
   throws InvalidFormatException,
          NoninvertibleTransformException,
          IOException
   {
      JDRPath jdrpath;
      JDRStroke stroke = getStroke(windingRule);

      if (clippingPath == null)
      {
         try
         {
            jdrpath = JDRPath.getPath(eps_.getCanvasGraphics(),
               getPathIterator());
            jdrpath.setStroke(stroke);
         }
         catch (EmptyPathException e)
         {
            return null;
         }
      }
      else
      {
         Area area = new Area(clippingPath);
         Shape trpath = stroke.createStrokedShape(path, JDRUnit.bp);
         Area area2 = new Area(trpath);
         area.intersect(area2);

         try
         {
            jdrpath = JDRPath.getPath(eps_.getCanvasGraphics(),
              area.getPathIterator(null));
         }
         catch (EmptyPathException e)
         {
            return null;
         }
      }

      jdrpath.setFillPaint(new JDRTransparent(eps_.getCanvasGraphics()));
      jdrpath.setLinePaint(getPaint());

      return jdrpath;
   }

   /**
    * Creates a filled path from the current graphics state 
    * using the given winding rule.
    * @param windingRule the winding rule, which must be one of
    * <code>Path2D.WIND_EVEN_ODD</code> or
    * <code>Path2D.WIND_NON_ZERO</code>
    * @return the filled path
    * @throws InvalidPathException if path can't be constructed
    * @throws InvalidFormatException if transfer function has 
    * invalid format
    * @throws NoninvertibleTransformException if transfer function
    * attempts a non-invertible transform
    * @throws IOException if transfer function encounters I/O error
    */
   public JDRPath createFilledPath(int windingRule)
   throws InvalidPathException,
          InvalidFormatException,
          NoninvertibleTransformException,
          IOException
   {
      JDRPath jdrpath = createStrokedPath(windingRule);

      if (jdrpath == null) return null;

      JDRPaint fillPaint = jdrpath.getLinePaint();
      JDRPaint linePaint = jdrpath.getFillPaint();
      jdrpath.setLinePaint(linePaint);
      jdrpath.setFillPaint(fillPaint);

      return jdrpath;
   }

   /**
    * Creates a filled path with non zero winding rule.
    */
   public JDRPath createFilledPath()
   throws InvalidPathException,
          InvalidFormatException,
          NoninvertibleTransformException,
          IOException
   {
      return createFilledPath(Path2D.WIND_NON_ZERO);
   }

   /**
    * Gets the bounds of the current path.
    * @return the bounds of the current path
    */
   public Rectangle2D getBounds2D()
   {
      return path.getBounds2D();
   }

   /**
    * Gets the bounding box of the current path.
    * @return the bounding box of the current path
    * @throws NoCurrentPointException if there is no current point
    */
   public Rectangle2D getBBox()
      throws NoCurrentPointException,
             NoninvertibleTransformException
   {
      if (getCurrentPoint() == null)
      {
         throw new NoCurrentPointException(eps_);
      }

      Rectangle2D bounds = path.createTransformedShape(
         af.createInverse()).getBounds2D();

      return bounds;
   }

   public EPSName pstype()
   {
      return new EPSName("gstate");
   }

   public String toString()
   {
      String str = "GraphicsState[af="+af+",linewidth="+lineWidth
      +",mitrelimit="+mitreLimit+",cap="+cap+",join="+join
      +",dashoffset="+dashOffset+",dashpattern=(";

      if (dashPattern != null)
      {
         for (int i = 0; i < dashPattern.length; i++)
         {
            if (i != 0)
            {
               str += ",";
            }

            str += ""+dashPattern[i];
         }
      }

      str += "),paint="+paint+",path="+path+",clip="+clippingPath
          + ",font="+font+",currentpoint="+getCurrentPoint();

      str += "]";

      return str;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return eps_.getCanvasGraphics();
   }

   private AffineTransform af;
   private double lineWidth=1, mitreLimit;
   private int cap, join;
   private float[] dashPattern;
   private float dashOffset;
   private JDRPaint paint;
   private Path2D path;
   private Path2D clippingPath;
   private EPSFont font;

   private EPSProc[] transfer;

   /**
    * Join style.
    */
   public static final int JOIN_MITRE=0, JOIN_ROUND=1, JOIN_BEVEL=2;
   /**
    * Cap style.
    */
   public static final int CAP_BUTT=0, CAP_ROUND=1, CAP_SQUARE=2;
   private boolean flatten_=false;

   private double flatness = 1.0;
   private EPS eps_;
}
