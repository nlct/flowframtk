// File          : JDRDistortShape.java
// Date          : 31 May 2012
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2012 Nicola L.C. Talbot

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

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

public class JDRDistortShape extends JDRCompleteObject
{
   public JDRDistortShape(JDRDistortable object)
   {
      this(object, null);
   }

   public JDRDistortShape(JDRDistortShape shape)
   {
      super(shape);
      distortion = new Point2D[shape.distortion.length];

      makeEqual(shape);
   }

   public JDRDistortShape(JDRDistortable object,
     Point2D[] distortedCorners)
   {
      super(object.getCanvasGraphics());
      underlyingShape = object;
      distortedShape = (JDRDistortable)underlyingShape.clone();

      setSelected(object.isSelected());
      distortedShape.setSelected(object.isSelected());

      distortion = new Point2D.Double[4];

      if (distortedCorners == null)
      {
         BBox bbox = object.getStorageDistortionBounds();

         double minX = bbox.getMinX();
         double minY = bbox.getMinY();
         double maxX = bbox.getMaxX();
         double maxY = bbox.getMaxY();

         distortion[0] = new Point2D.Double(minX, minY);
         distortion[1] = new Point2D.Double(maxX, minY);
         distortion[2] = new Point2D.Double(maxX, maxY);
         distortion[3] = new Point2D.Double(minX, maxY);
      }
      else
      {
         for (int i = 0; i < distortion.length; i++)
         {
            distortion[i] = new Point2D.Double  
            (
               distortedCorners[i].getX(),
               distortedCorners[i].getY()
            );
         }
      }

      updateDistortion();
   }

   public void reset()
   {
      ((JDRCompleteObject)underlyingShape).reset();

      resetDistortion();
   }

   public void resetDistortion()
   {
      BBox bbox = underlyingShape.getStorageDistortionBounds();

      double minX = bbox.getMinX();
      double minY = bbox.getMinY();
      double maxX = bbox.getMaxX();
      double maxY = bbox.getMaxY();

      distortion[0].setLocation(minX, minY);
      distortion[1].setLocation(maxX, minY);
      distortion[2].setLocation(maxX, maxY);
      distortion[3].setLocation(minX, maxY);

      updateDistortion();
   }

   public void setDistortionBounds(Point2D[] newBounds)
   {
      distortion = newBounds;
      updateDistortion();
   }

   public boolean refresh()
   {
      CanvasGraphics cg = getCanvasGraphics();

      boolean orgReplaced = cg.isBitmapReplaced();

      cg.setBitmapReplaced(false);

      boolean success = ((JDRCompleteObject)underlyingShape).refresh();

      if (cg.isBitmapReplaced())
      {
         if (success)
         {
            updateDistortion();
         }
      }
      else
      {
         cg.setBitmapReplaced(orgReplaced);
      }

      return success;
   }

   public JDRCompleteObject getDistortedObject()
   {
      return (JDRCompleteObject)distortedShape;
   }

   public JDRDistortable getUnderlyingObject()
   {
      return underlyingShape;
   }

   public BBox getStorageBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = distortedShape.getStorageBBox();
      box.merge(underlyingShape.getStorageBBox());

      return box;
   }

   public BBox getStorageControlBBox()
   {
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = distortedShape.getStorageBBox();
      box.merge(underlyingShape.getStorageBBox());

      DoubleDimension size = cg.getStoragePointSize();
      double radiusX = size.getWidth()*0.5;
      double radiusY = size.getHeight()*0.5;

      double minX = distortion[0].getX()-radiusX;
      double minY = distortion[0].getY()-radiusY;
      double maxX = minX+radiusX;
      double maxY = minY+radiusY;

      for (int i = 1; i < distortion.length; i++)
      {
         minX = Math.min(minX, distortion[i].getX());
         maxX = Math.max(maxX, distortion[i].getX());
         minY = Math.min(minY, distortion[i].getY());
         maxY = Math.max(maxY, distortion[i].getY());
      }

      box.merge(minX, minY, maxX, maxY);

      return box;
   }

   public int getHotspotFromStoragePoint(Point2D storagePoint)
   {
      return underlyingShape.getHotspotFromStoragePoint(storagePoint);
   }

   public int getHotspotFromBpPoint(Point2D bpPoint)
   {
      return underlyingShape.getHotspotFromBpPoint(bpPoint);
   }

   public int getHotspotFromComponentPoint(Point2D compPoint)
   {
      return underlyingShape.getHotspotFromComponentPoint(compPoint);
   }

   public JDRPoint getTopLeftHS()
   {
      return underlyingShape.getTopLeftHS();
   }

   public JDRPoint getBottomLeftHS()
   {
      return underlyingShape.getBottomLeftHS();
   }

   public JDRPoint getBottomRightHS()
   {
      return underlyingShape.getBottomRightHS();
   }

   public JDRPoint getTopRightHS()
   {
      return underlyingShape.getTopRightHS();
   }

   public JDRPoint getCentreHS()
   {
      return underlyingShape.getCentreHS();
   }

   public BBox getDragBBox()
   {
      return underlyingShape.getDragBBox();
   }

   public Point2D.Double getMidPoint()
   {
      BBox box = underlyingShape.getStorageDistortionBounds();

      return new Point2D.Double(box.getMidX(),
                                box.getMidY());
   }

   public Point2D.Double getTransformedMidPoint()
   {
      if (midDistort == null)
      {
         updateMidDistort();
      }

      return midDistort;
   }

    private AffineTransform updateTrans(AffineTransform af,
      double orgX1, double orgY1, double orgX2, double orgY2, double orgX3, double orgY3,
      double trX1, double trY1, double trX2, double trY2, double trX3, double trY3)
    {
       double eta00 = orgX1;
       double eta01 = orgY1;
       double eta10 = orgX2;
       double eta11 = orgY2;
       double eta20 = orgX3;
       double eta21 = orgY3;

       double subdet1 = eta10*eta21 - eta11*eta20;
       double subdet2 = eta00*eta21 - eta01*eta20;
       double subdet3 = eta00*eta11 - eta01*eta10;

       double det = subdet1 - subdet2 + subdet3;

       if (det == 0)
       {
          return null;
       }

       double alpha1 = trX1;
       double alpha2 = trX2;
       double alpha3 = trX3;

       double detX = (alpha2*eta21 - eta11*alpha3)
                   - (alpha1*eta21 - eta01*alpha3)
                   + (alpha1*eta11 - alpha2*eta01);

       double detY = (eta10*alpha3 - alpha2*eta20)
                   - (alpha3*eta00 - alpha1*eta20)
                   + (alpha2*eta00 - alpha1*eta10);

       double detZ = alpha1*subdet1 - alpha2*subdet2 + alpha3*subdet3;

       double m00 = detX/det;
       double m01 = detY/det;
       double m02 = detZ/det;

       alpha1 = trY1;
       alpha2 = trY2;
       alpha3 = trY3;

       detX = (alpha2*eta21 - eta11*alpha3)
            - (alpha1*eta21 - eta01*alpha3)
            + (alpha1*eta11 - alpha2*eta01);

       detY = (eta10*alpha3 - alpha2*eta20)
            - (alpha3*eta00 - alpha1*eta20)
            + (alpha2*eta00 - alpha1*eta10);

       detZ = alpha1*subdet1 - alpha2*subdet2 + alpha3*subdet3;

       double m10 = detX/det;
       double m11 = detY/det;
       double m12 = detZ/det;

       if (af == null)
       {
          af = new AffineTransform(m00, m10, m01, m11, m02, m12);
       }
       else
       {
          af.setTransform(m00, m10, m01, m11, m02, m12);
       }

       return af;
    }

   public void updateDistortion()
   {
      updateMidDistort();

      BBox box = underlyingShape.getStorageDistortionBounds();

      double minX = box.getMinX();
      double minY = box.getMinY();

      double midX = box.getMidX();
      double midY = box.getMidY();

      double maxX = box.getMaxX();
      double maxY = box.getMaxY();

      Path2D.Double upper = new Path2D.Double();
      upper.moveTo(minX, minY);
      upper.lineTo(maxX, minY);
      upper.lineTo(midX, midY);
      upper.closePath();

      Path2D.Double right = new Path2D.Double();
      right.moveTo(maxX, minY);
      right.lineTo(maxX, maxY);
      right.lineTo(midX, midY);
      right.closePath();

      Path2D.Double lower = new Path2D.Double();
      lower.moveTo(maxX, maxY);
      lower.lineTo(minX, maxY);
      lower.lineTo(midX, midY);
      lower.closePath();

      Path2D.Double left = new Path2D.Double();
      left.moveTo(minX, maxY);
      left.lineTo(minX, minY);
      left.lineTo(midX, midY);
      left.closePath();

      upperTrans = updateTrans(upperTrans, 
         minX, minY, maxX, minY, midX, midY,
         distortion[0].getX(), distortion[0].getY(),
         distortion[1].getX(), distortion[1].getY(),
         midDistort.getX(), midDistort.getY());

      rightTrans = updateTrans(rightTrans,
         maxX, minY, maxX, maxY, midX, midY,
         distortion[1].getX(), distortion[1].getY(),
         distortion[2].getX(), distortion[2].getY(),
         midDistort.getX(), midDistort.getY());

      lowerTrans = updateTrans(lowerTrans,
         maxX, maxY, minX, maxY, midX, midY,
         distortion[2].getX(), distortion[2].getY(),
         distortion[3].getX(), distortion[3].getY(),
         midDistort.getX(), midDistort.getY());

      leftTrans = updateTrans(leftTrans,
         minX, maxY, minX, minY, midX, midY,
         distortion[3].getX(), distortion[3].getY(),
         distortion[0].getX(), distortion[0].getY(),
         midDistort.getX(), midDistort.getY());

      distortedShape.distort(underlyingShape,
        new Shape[] {upper, right, lower, left},
        new AffineTransform[] {upperTrans, rightTrans, lowerTrans, leftTrans});
   }

   private void updateMidDistort()
   {
      double lambda = (distortion[0].getY()-distortion[2].getY())
                    * (distortion[3].getX()-distortion[1].getX())
                    - (distortion[0].getX()-distortion[2].getX())
                    * (distortion[3].getY()-distortion[1].getY());

      double x, y;

      if (lambda == 0)
      {
         BBox box = underlyingShape.getStorageDistortionBounds();

         x = box.getMidX();
         y = box.getMidY();
      }
      else
      {
          x =
            (
               (distortion[0].getX()-distortion[2].getX())
             * (distortion[3].getX()-distortion[1].getX())
             * (distortion[3].getY()-distortion[0].getY())
             + (distortion[3].getX()-distortion[1].getX())
             * (distortion[0].getY()-distortion[2].getY())
             * distortion[0].getX()
             - (distortion[0].getX()-distortion[2].getX())
             * (distortion[3].getY()-distortion[1].getY())
             * distortion[3].getX()
            )
            /lambda;

         y = (x-distortion[0].getX())
                  * (distortion[0].getY()-distortion[2].getY())
                  / (distortion[0].getX()-distortion[2].getX())
                  + distortion[0].getY();
      }

      if (midDistort == null)
      {
         midDistort = new Point2D.Double(x, y);
      }
      else
      {
         midDistort.setLocation(x, y);
      }
   }

   public void drawControls(boolean endPoint)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      Paint oldPaint = g2.getPaint();

      double storageToCompX = cg.storageToComponentX(1.0);
      double storageToCompY = cg.storageToComponentY(1.0);

      AffineTransform oldAf = g2.getTransform();

      g2.scale(storageToCompX, storageToCompY);
      distortedShape.draw(null);

      g2.setTransform(oldAf);

      DoubleDimension pointSize = cg.getComponentPointSize();

      Ellipse2D.Double circle 
         = new Ellipse2D.Double(0,0,pointSize.getWidth(),pointSize.getHeight());

      double radiusX = pointSize.getWidth()*0.5;
      double radiusY = pointSize.getHeight()*0.5;

      Path2D.Double boundary = null;

      for (int i = 0; i < distortion.length; i++)
      {
         double x = storageToCompX * distortion[i].getX();
         double y = storageToCompY * distortion[i].getY();

         if (boundary == null)
         {
            boundary = new Path2D.Double();
            boundary.moveTo(x, y);
         }
         else
         {
            boundary.lineTo(x, y);
         }

         if (selectedPoint == distortion[i])
         {
            g2.setPaint(JDRPoint.selectColor);
         }
         else
         {
            g2.setPaint(JDRPoint.controlColor);
         }

         circle.x = x-radiusX;
         circle.y = y-radiusY;

         g2.draw(circle);

         circle.x++;
         circle.y++;
         circle.width-=2;
         circle.height-=2;

         g2.setPaint(JDRPoint.innerColour);
         g2.draw(circle);

         circle.width+=2;
         circle.height+=2;
      }

      boundary.closePath();

      Stroke oldStroke = g2.getStroke();
      g2.setStroke(JDRSegment.guideStroke);
      g2.draw(boundary);

      g2.setPaint(oldPaint);
      g2.setStroke(oldStroke);
   }

   public void draw(FlowFrame parentFrame)
   {
      distortedShape.draw(parentFrame);
   }

   public void print(Graphics2D g2)
   {
      distortedShape.print(g2);
   }

   public int psLevel()
   {
      return underlyingShape.psLevel();
   }

   public void saveEPS(PrintWriter out)
    throws IOException
   {
      getDistortedObject().saveEPS(out);
   }

   public void saveSVG(SVG svg, String attr)
    throws IOException
   {
      BBox bbox = underlyingShape.getStorageDistortionBounds();

      double minX = bbox.getMinX();
      double minY = bbox.getMinY();

      double midX = bbox.getMidX();
      double midY = bbox.getMidY();

      double maxX = bbox.getMaxX();
      double maxY = bbox.getMaxY();

      Path2D.Double upper = new Path2D.Double();

      upper.moveTo(minX, minY);
      upper.lineTo(maxX, minY);
      upper.lineTo(midX, midY);
      upper.closePath();

      Path2D.Double right = new Path2D.Double();

      right.moveTo(maxX, minY);
      right.lineTo(maxX, maxY);
      right.lineTo(midX, midY);
      right.closePath();

      Path2D.Double lower = new Path2D.Double();

      lower.moveTo(maxX, maxY);
      lower.lineTo(minX, maxY);
      lower.lineTo(midX, midY);
      lower.closePath();

      Path2D.Double left = new Path2D.Double();

      left.moveTo(minX, maxY);
      left.lineTo(minX, minY);
      left.lineTo(midX, midY);
      left.closePath();

      int id = hashCode();

      if (upperTrans != null)
      {
         svg.println("<g>");

         Shape clipShape = upperTrans.createTransformedShape(upper);

         svg.println("<clipPath id=\"upper"+id+"\">");
         svg.print("<path d=\"");
         svg.saveStoragePathData(clipShape);
         svg.println("\" />");
         svg.println("</clipPath>");

         svg.println("<g transform=\"matrix("+upperTrans.getScaleX()
         +" "+upperTrans.getShearY()
         +" "+upperTrans.getShearX()
         +" "+upperTrans.getScaleY()
         +" "+upperTrans.getTranslateX()
         +" "+upperTrans.getTranslateY()
         +")\" >");

         underlyingShape.saveSVG(svg, "clip-path=\"url(#upper"+id+"\""+" "+attr);

         svg.println("</g>");
         svg.println("</g>");
      }

      if (rightTrans != null)
      {
         svg.println("<g>");

         Shape clipShape = rightTrans.createTransformedShape(right);

         svg.println("<clipPath id=\"right"+id+"\">");
         svg.print("<path d=\"");
         svg.saveStoragePathData(clipShape);
         svg.println("\" />");
         svg.println("</clipPath>");

         svg.println("<g transform=\"matrix("+rightTrans.getScaleX()
         +" "+rightTrans.getShearY()
         +" "+rightTrans.getShearX()
         +" "+rightTrans.getScaleY()
         +" "+rightTrans.getTranslateX()
         +" "+rightTrans.getTranslateY()
         +")\" >");

         underlyingShape.saveSVG(svg, "clip-path=\"url(#right"+id+"\""+" "+attr);

         svg.println("</g>");
         svg.println("</g>");
      }

      if (lowerTrans != null)
      {
         svg.println("<g>");

         Shape clipShape = lowerTrans.createTransformedShape(lower);

         svg.println("<clipPath id=\"lower"+id+"\">");
         svg.print("<path d=\"");
         svg.saveStoragePathData(clipShape);
         svg.println("\" />");
         svg.println("</clipPath>");

         svg.println("<g transform=\"matrix("+lowerTrans.getScaleX()
         +" "+lowerTrans.getShearY()
         +" "+lowerTrans.getShearX()
         +" "+lowerTrans.getScaleY()
         +" "+lowerTrans.getTranslateX()
         +" "+lowerTrans.getTranslateY()
         +")\" >");

         underlyingShape.saveSVG(svg, "clip-path=\"url(#lower"+id+"\""+" "+attr);

         svg.println("</g>");
         svg.println("</g>");
      }

      if (leftTrans != null)
      {
         svg.println("<g>");

         Shape clipShape = leftTrans.createTransformedShape(left);

         svg.println("<clipPath id=\"left"+id+"\">");
         svg.print("<path d=\"");
         svg.saveStoragePathData(clipShape);
         svg.println("\" />");
         svg.println("</clipPath>");

         svg.println("<g transform=\"matrix("+leftTrans.getScaleX()
         +" "+leftTrans.getShearY()
         +" "+leftTrans.getShearX()
         +" "+leftTrans.getScaleY()
         +" "+leftTrans.getTranslateX()
         +" "+leftTrans.getTranslateY()
         +")\" >");

         underlyingShape.saveSVG(svg, "clip-path=\"url(#left"+id+"\""+" "+attr);

         svg.println("</g>");
         svg.println("</g>");
      }
   }

   public void savePgf(TeX tex)
     throws IOException
   {
      AffineTransform pgfAf = tex.getTransform();

      CanvasGraphics cg = getCanvasGraphics();

      BBox bbox = underlyingShape.getStorageDistortionBounds();

      double minX = bbox.getMinX();
      double minY = bbox.getMinY();

      double midX = bbox.getMidX();
      double midY = bbox.getMidY();

      double maxX = bbox.getMaxX();
      double maxY = bbox.getMaxY();

      Path2D.Double upper = new Path2D.Double();

      upper.moveTo(minX, minY);
      upper.lineTo(maxX, minY);
      upper.lineTo(midX, midY);
      upper.closePath();

      Path2D.Double right = new Path2D.Double();

      right.moveTo(maxX, minY);
      right.lineTo(maxX, maxY);
      right.lineTo(midX, midY);
      right.closePath();

      Path2D.Double lower = new Path2D.Double();

      lower.moveTo(maxX, maxY);
      lower.lineTo(minX, maxY);
      lower.lineTo(midX, midY);
      lower.closePath();

      Path2D.Double left = new Path2D.Double();

      left.moveTo(minX, maxY);
      left.lineTo(minX, minY);
      left.lineTo(midX, midY);
      left.closePath();

      if (upperTrans != null)
      {
         AffineTransform af = new AffineTransform(pgfAf);
         af.concatenate(upperTrans);

         tex.println("\\begin{pgfscope}");

         Shape clipShape = af.createTransformedShape(upper);

         tex.printQuickPath(cg, clipShape);
         tex.println("\\pgfusepathqclip");

         tex.println(tex.transform(cg, upperTrans));

         underlyingShape.savePgf(tex);

         tex.println("\\end{pgfscope}");
      }

      if (rightTrans != null)
      {
         AffineTransform af = new AffineTransform(pgfAf);
         af.concatenate(rightTrans);

         tex.println("\\begin{pgfscope}");

         Shape clipShape = af.createTransformedShape(right);

         tex.printQuickPath(cg, clipShape);
         tex.println("\\pgfusepathqclip");

         tex.println(tex.transform(cg, rightTrans));

         underlyingShape.savePgf(tex);

         tex.println("\\end{pgfscope}");
      }

      if (lowerTrans != null)
      {
         AffineTransform af = new AffineTransform(pgfAf);
         af.concatenate(lowerTrans);

         tex.println("\\begin{pgfscope}");

         Shape clipShape = af.createTransformedShape(lower);

         tex.printQuickPath(cg, clipShape);
         tex.println("\\pgfusepathqclip");

         tex.println(tex.transform(cg, lowerTrans));

         underlyingShape.savePgf(tex);

         tex.println("\\end{pgfscope}");
      }

      if (leftTrans != null)
      {
         AffineTransform af = new AffineTransform(pgfAf);
         af.concatenate(leftTrans);

         tex.println("\\begin{pgfscope}");

         Shape clipShape = af.createTransformedShape(left);

         tex.printQuickPath(cg, clipShape);
         tex.println("\\pgfusepathqclip");

         tex.println(tex.transform(cg, leftTrans));

         underlyingShape.savePgf(tex);

         tex.println("\\end{pgfscope}");
      }

   }

   public void fade(double value)
   {
      underlyingShape.fade(value);
   }

   public JDRPattern getPattern()
   {
      return underlyingShape.getPattern();
   }

   public Object[] getDescriptionInfo()
   {
      return underlyingShape.getDescriptionInfo();
   }

   public boolean hasPattern()
   {
      return underlyingShape.hasPattern();
   }

   public boolean hasSymmetricPath()
   {
      return underlyingShape.hasSymmetricPath();
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return underlyingShape.getSymmetricPath();
   }

   public boolean hasShape()
   {
      return underlyingShape.hasShape();
   }

   public boolean hasTextual()
   {
      return underlyingShape.hasTextual();
   }

   public JDRTextual getTextual()
   {
      return underlyingShape.getTextual();
   }

   public void transform(double[] matrix)
   {
      underlyingShape.transform(matrix);

      AffineTransform af = new AffineTransform(matrix);

      Point2D[] dest = new Point2D.Double[distortion.length];

      af.transform(distortion, 0, dest, 0, distortion.length);

      for (int i = 0; i < distortion.length; i++)
      {
         distortion[i].setLocation(dest[i]);
      }

      updateDistortion();
   }

   public void translate(double x, double y)
   {
      underlyingShape.translate(x, y);

      for (int i = 0; i < distortion.length; i++)
      {
         distortion[i].setLocation(distortion[i].getX()+x,
                                   distortion[i].getY()+y);
      }

      updateDistortion();
   }

   public void shear(Point2D p, double shearx, double sheary)
   {
      underlyingShape.shear(p, shearx, sheary);

      AffineTransform af = new AffineTransform();
      af.translate(-p.getX(), -p.getY());
      af.shear(shearx, sheary);
      af.translate(p.getX(), p.getY());

      Point2D[] dest = new Point2D.Double[distortion.length];

      af.transform(distortion, 0, dest, 0, distortion.length);

      for (int i = 0; i < distortion.length; i++)
      {
         distortion[i].setLocation(dest[i]);
      }

      updateDistortion();
   }

   public void scale(Point2D p, double factorX, double factorY)
   {
      underlyingShape.scale(p, factorX, factorY);

      AffineTransform af = new AffineTransform();
      af.translate(-p.getX(), -p.getY());
      af.scale(factorX, factorY);
      af.translate(p.getX(), p.getY());

      Point2D[] dest = new Point2D.Double[distortion.length];

      af.transform(distortion, 0, dest, 0, distortion.length);


      for (int i = 0; i < distortion.length; i++)
      {
         distortion[i].setLocation(dest[i]);
      }

      updateDistortion();
   }

   public void rotate(Point2D p, double angle)
   {
      underlyingShape.rotate(p, angle);

      AffineTransform af = AffineTransform.getRotateInstance(
         angle, p.getX(), p.getY());

      Point2D[] dest = new Point2D.Double[distortion.length];

      af.transform(distortion, 0, dest, 0, distortion.length);


      for (int i = 0; i < distortion.length; i++)
      {
         distortion[i].setLocation(dest[i]);
      }

      updateDistortion();
   }

   public Object clone()
   {
      JDRDistortShape obj = new JDRDistortShape
      (
         (JDRDistortable)underlyingShape.clone(),
         distortion
      );

      return obj;
   }

   public void makeEqual(JDRObject object)
   {
      super.makeEqual(object);

      JDRDistortShape distObj = (JDRDistortShape)object;

      underlyingShape.makeEqual(distObj.underlyingShape);

      selectedPoint = null;

      for (int i = 0; i < distortion.length; i++)
      {
         if (distortion[i] == null)
         {
            distortion[i] = new Point2D.Double(distObj.distortion[i].getX(),
              distObj.distortion[i].getY());
         }
         else
         {
            distortion[i].setLocation(distObj.distortion[i]);
         }

         if (distObj.selectedPoint == distObj.distortion[i])
         {
            selectedPoint = distortion[i];
         }
      }

      if (distObj.midDistort == null)
      {
         midDistort = null;
      }
      else if (midDistort == null)
      {
         midDistort = new Point2D.Double(
            distObj.midDistort.getX(),
            distObj.midDistort.getY());
      }
      else
      {
         midDistort.setLocation(distObj.midDistort);
      }

      if (distObj.upperTrans == null)
      {
         upperTrans = null;
      }
      else
      {
         upperTrans = (AffineTransform)distObj.upperTrans.clone();
      }

      if (distObj.lowerTrans == null)
      {
         lowerTrans = null;
      }
      else
      {
         lowerTrans = (AffineTransform)distObj.lowerTrans.clone();
      }

      if (distObj.leftTrans == null)
      {
         leftTrans = null;
      }
      else
      {
         leftTrans = (AffineTransform)distObj.leftTrans.clone();
      }

      if (distObj.rightTrans == null)
      {
         rightTrans = null;
      }
      else
      {
         rightTrans = (AffineTransform)distObj.rightTrans.clone();
      }
   }

   public boolean equals(Object o)
   {
      if (!super.equals(o)) return false;

      if (!(o instanceof JDRDistortShape))
      {
         return false;
      }

      JDRDistortShape distObj = (JDRDistortShape)o;

      if (!underlyingShape.equals(distObj.underlyingShape))
      {
         return false;
      }

      for (int i = 0; i < distortion.length; i++)
      {
         if (!distortion[i].equals(distObj.distortion[i]))
         {
            return false;
         }
      }

      return true;
   }

   public Shape getDistortionBoundary()
   {
      Path2D path = new Path2D.Double();

      path.moveTo(distortion[0].getX(), distortion[0].getY());

      for (int i = 1; i < distortion.length; i++)
      {
         path.lineTo(distortion[i].getX(), distortion[i].getY());
      }

      path.closePath();

      return path;
   }

   public JDRPath getDistortionQuadrilateral()
   {
      try
      {
         return JDRPath.getPath(getCanvasGraphics(),
           getDistortionBoundary().getPathIterator(null));
      }
      catch (InvalidFormatException e)
      {
      }

      return null;
   }

   public int getNumDistortionPoints()
   {
      return distortion.length;
   }

   public Point2D getDistortionPoint(int i)
   {
      return distortion[i];
   }

   public int getDistortionIndex(Point2D p)
   {
      for (int i = 0; i < distortion.length; i++)
      {
         if (distortion[i] == p)
         {
            return i;
         }
      }

      return -1;
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "Distorted Object:"+eol;

      str += "Distortion: ";

      for (int i = 0; i < distortion.length; i++)
      {
         str += "("+distortion[i].getX()+","+distortion[i].getY()+") ";
      }

      str += eol;

      str += "Mid Distort: "+midDistort+eol;
      str += "Upper trans: "+upperTrans+eol;
      str += "Right trans: "+rightTrans+eol;
      str += "Lower trans: "+lowerTrans+eol;
      str += "Left trans: "+leftTrans+eol;

      str += "Underlying Object: "+eol+underlyingShape.info();

      return str+super.info();
   }

   public void setEditMode(boolean mode)
   {
      super.setEditMode(mode);

      selectedPoint = null;
   }

   public void setSelectedPoint(Point2D p)
   {
      selectedPoint = p;
   }

   public void selectControl(double x, double y)
   {
      CanvasGraphics cg = getCanvasGraphics();

      DoubleDimension size = cg.getStoragePointSize();

      double radiusX = size.getWidth()*0.5;
      double radiusY = size.getHeight()*0.5;

      for (int i = 0; i < distortion.length; i++)
      {
         double minX = distortion[i].getX()-radiusX;
         double minY = distortion[i].getY()-radiusY;
         double maxX = distortion[i].getX()+radiusX;
         double maxY = distortion[i].getY()+radiusY;

         if (minX <= x && x <= maxX && minY <= y && y <= maxY)
         {
            selectedPoint = distortion[i];
            return;
         }
      }

      selectedPoint = null;
   }

   public Point2D getSelectedPoint()
   {
      return selectedPoint;
   }

   public JDRPoint getControlFromStoragePoint(
     double storagePointX, double storagePointY, boolean endPoint)
   {
      return underlyingShape.getControlFromStoragePoint(
        storagePointX, storagePointY, endPoint);
   }

   public int getObjectFlag()
   {
      return super.getObjectFlag() | SELECT_FLAG_DISTORTED;
   }


   private JDRDistortable underlyingShape;

   private JDRDistortable distortedShape;

   private Point2D[] distortion;

   private Point2D selectedPoint;

   private Point2D.Double midDistort;

   private AffineTransform upperTrans, lowerTrans, leftTrans, rightTrans;
}
