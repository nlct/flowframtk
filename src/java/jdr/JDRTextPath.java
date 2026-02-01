// File          : JDRTextPath.java
// Creation Date : 7th July 2009
// Author        : Nicola L C Talbot
//                 http://www.dickimaw-books.com/

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing text flowing along a path.
 * This originally didn't support showing the underlying path so the
 * text paint and outline fill paint were stored in the underlying
 * path's line paint and fill paint. Now that showing the underlying
 * path is supported, the line and fill paint (and stroke) are
 * stored in this class rather than in the underlying shape, which
 * is a little counter-intuitive but it would break backward
 * compatibility (particularly for the JDR/AJR file format) to switch it
 * round.
 * @author Nicola L C Talbot
 */

public class JDRTextPath extends JDRCompoundShape implements JDRTextual
{
   /**
    * Creates a text path.
    * @param path the path
    * @param text the text to go along the path
    */
   public JDRTextPath(JDRShape path, JDRText text)
   {
      super(path.getCanvasGraphics());

      path_ = path;

      JDRStroke oldStroke = path_.getStroke();

      if (oldStroke instanceof JDRBasicStroke)
      {
         showPathStroke = (JDRBasicStroke)oldStroke;
      }

      showPathFillPaint = path_.getShapeFillPaint();
      showPathLinePaint = path_.getLinePaint();

      setStroke(new JDRTextPathStroke(text));
      setTextPaint(text.getTextPaint());
      setOutlineMode(text.isOutline());

      if (isOutline())
      {
         setOutlineFillPaint(text.getOutlineFillPaint());
      }

      if (text.hasDescription())
      {
         description = text.description;
      }
      else if (path.hasDescription())
      {
         description = path.description;
      }

      if (text.hasTag())
      {
         tag = text.tag;

         if (path.hasTag())
         {
            String[] textSplit = tag.split("\\s+");
            String[] pathSplit = path.tag.split("\\s+");

            // unlike to be large arrays

            for (String ps : pathSplit)
            {
               boolean found = false;

               for (String ts : textSplit)
               {
                  if (ps.equals(ts))
                  {
                     found = true;
                     break;
                  }
               }

               if (!found)
               {
                  tag += " " + ps;
               }
            }
         }
      }
      else if (path.hasTag())
      {
         tag = path.tag;
      }
   }

   public JDRTextPath(JDRShape path, JDRTextPathStroke stroke)
   {
      super(path.getCanvasGraphics());

      path_ = path;

      JDRStroke oldStroke = path_.getStroke();

      if (oldStroke instanceof JDRBasicStroke)
      {
         showPathStroke = (JDRBasicStroke)oldStroke;
      }

      showPathFillPaint = path_.getShapeFillPaint();
      showPathLinePaint = path_.getLinePaint();

      setStroke(stroke);
      setTextPaint(path.getLinePaint());

      description = path.description;
      tag = path.tag;
   }

   public JDRTextPath(CanvasGraphics cg, int n, JDRPaint paint, JDRTextPathStroke stroke)
   {
      super(cg);

      path_ = new JDRPath(cg, n);

      setStroke(stroke);
      setTextPaint(paint);
   }

   protected JDRTextPath(CanvasGraphics cg)
   {
      super(cg);
   }

   public static JDRTextPath createFrom(JDRShape shape)
   {
      JDRTextPath textPath = new JDRTextPath(shape.getCanvasGraphics());

      textPath.path_ = shape;

      JDRStroke oldStroke = shape.getStroke();

      if (oldStroke instanceof JDRBasicStroke)
      {
         textPath.showPathStroke = (JDRBasicStroke)oldStroke;
      }

      textPath.showPathFillPaint = shape.getShapeFillPaint();
      textPath.showPathLinePaint = shape.getLinePaint();

      if (!(oldStroke instanceof JDRTextPathStroke))
      {
         textPath.setStroke(new JDRTextPathStroke(shape.getCanvasGraphics()));
      }

      textPath.description = shape.description;
      textPath.tag = shape.tag;

      return textPath;
   }

   public JDRStroke getStroke()
   {
      return path_.getStroke();
   }

   public void setStroke(JDRStroke stroke)
   {
      path_.setStroke(stroke);
   }

   @Override
   public boolean hasBasicStroke()
   {
      return showPath();
   }

   @Override
   public JDRBasicStroke getBasicStroke()
   {
      if (showPath)
      {
         return getShowPathStroke();
      }
      else
      {
         return null;
      }
   }

   @Override
   public void setBasicStroke(JDRBasicStroke stroke)
   {
      setShowPathStroke(stroke);
   }

   public JDRBasicStroke getShowPathStroke()
   {
      if (showPathStroke == null)
      {
         showPathStroke = new JDRBasicStroke(canvasGraphics);
      }

      return showPathStroke;
   }

   public void setShowPathStroke(JDRBasicStroke basicStroke)
   {
      showPathStroke = basicStroke;
   }

   public void setShowPath(boolean show, JDRBasicStroke basicStroke)
   {
      showPath = show;
      showPathStroke = basicStroke;
   }

   public void setShowPath(boolean show)
   {
      showPath = show;

      if (show)
      {
         if (showPathStroke == null)
         {
            showPathStroke = new JDRBasicStroke(getCanvasGraphics());
         }

         if (showPathFillPaint == null)
         {
            showPathFillPaint = new JDRTransparent(getCanvasGraphics());
         }

         if (showPathLinePaint == null)
         {
            showPathLinePaint = new JDRColor(getCanvasGraphics());
         }
      }
   }

   @Override
   public boolean showPath()
   {
      return showPath;
   }

   public void setShowPathLinePaint(JDRPaint paint)
   {
      showPathLinePaint = paint;
   }

   public JDRPaint getShowPathLinePaint()
   {
      if (showPathLinePaint == null)
      {
         showPathLinePaint = new JDRColor(canvasGraphics);
      }

      return showPathLinePaint;
   }

   public void setShowPathFillPaint(JDRPaint paint)
   {
      showPathFillPaint = paint;
   }

   public JDRPaint getShowPathFillPaint()
   {
      if (showPathFillPaint == null)
      {
         showPathFillPaint = new JDRTransparent(canvasGraphics);
      }

      return showPathFillPaint;
   }

   @Override
   public JDRPaint getTextPaint()
   {
      return path_.getLinePaint();
   }

   @Override
   public void setTextPaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
   }

   @Deprecated
   public JDRPaint getFillPaint()
   {
      return getOutlineFillPaint();
   }

   @Override
   public JDRPaint getOutlineFillPaint()
   {
      return path_.getShapeFillPaint();
   }

   @Override
   public JDRPaint getShapeFillPaint()
   {
      return getShowPathFillPaint();
   }

   @Deprecated
   public void setFillPaint(JDRPaint paint)
   {
      path_.setShapeFillPaint(paint);
   }

   @Override
   public void setOutlineFillPaint(JDRPaint paint)
   {
      path_.setShapeFillPaint(paint);
   }

   @Override
   public void setShapeFillPaint(JDRPaint paint)
   {
      setShowPathFillPaint(paint);
   }

   @Override
   public JDRPaint getLinePaint()
   {
      return getShowPathLinePaint();
   }

   @Override
   public void setLinePaint(JDRPaint paint)
   {
      setShowPathLinePaint(paint);
   }

   /**
    * Gets the path for this.
    * @return the path
    */
   public JDRShape getJDRShape()
   {
      JDRShape path = (JDRShape)path_.clone();

      path.editMode = editMode;
      path.selected = selected;

      assignShowPathAttributesToShape(path);

      return path;
   }

   protected void assignShowPathAttributesToShape(JDRShape path)
   {
      if (!(path_ instanceof JDRTextual))
      {
         if (showPathStroke == null)
         {
            path.setStroke(new JDRBasicStroke(getCanvasGraphics()));
         }
         else
         {
            path.setStroke((JDRBasicStroke)showPathStroke.clone());
         }

         if (showPathFillPaint != null)
         {
            path.setShapeFillPaint((JDRPaint)showPathFillPaint.clone());
         }

         if (showPathLinePaint != null)
         {
            path.setLinePaint((JDRPaint)showPathLinePaint.clone());
         }
      }
   }

   /**
    * Gets the text for this as a text area.
    * @return the text
    */
   public JDRText getJDRText()
   {
      JDRText text = ((JDRTextPathStroke)getStroke()).getJDRText();

      text.description = description;

      text.editMode = editMode;
      text.selected = selected;

      JDRPoint p = getFirstSegment().getStart();

      text.setPosition(p.x, p.y);

      text.setTextPaint(getLinePaint());

      text.setOutlineMode(isOutline);

      if (isOutline)
      {
         text.setOutlineFillPaint(getOutlineFillPaint());
      }

      return text;
   }

   /**
    * Splits this into a group containing path and text area.
    * @return the group containing the underlying path and text
    */
   public JDRGroup separate()
   {
      JDRShape path = getJDRShape();
      JDRText text = getJDRText();

      text.updateBounds();

      JDRGroup group = new JDRGroup(getCanvasGraphics());

      group.description = description;

      group.add(path);
      group.add(text);

      return group;
   }


   public JDRGroup splitText()
     throws InvalidShapeException
   {
      JDRGroup grp = ((JDRTextPathStroke)getStroke()).split(this);

      if (description.isEmpty())
      {
         grp.description = getText();
      }
      else
      {
         grp.description = description;
      }

      if (showPath)
      {
         JDRShape path = getJDRShape();

         grp.add(path);
      }

      return grp;
   }

   @Override
   public JDRCompleteObject clip(Rectangle2D clipBounds)
      throws UnableToClipException
   {
      try
      {
         return splitText().clip(clipBounds);
      }
      catch (InvalidShapeException e)
      {
         throw new UnableToClipException(
            canvasGraphics.getMessageWithFallback(
              "error.clip_failed", "Clip failed"
            ), e
         );
      }
   }

   @Override
   public void drawClipDraft()
   {
      try
      {
         splitText().drawClipDraft();
      }
      catch (InvalidShapeException e)
      {
         canvasGraphics.debugMessage(e);
      }
   }

   /**
    * Sets horizontal alignment.
    * @param align the alignment
    * @see JDRTextPathStroke#setHAlign(int)
    */
   public void setHAlign(int align)
   {
      ((JDRTextPathStroke)getStroke()).setHAlign(align);
   }

   /**
    * Sets vertical alignment.
    * @param align the alignment
    * @see JDRTextPathStroke#setVAlign(int)
    */
   public void setVAlign(int align)
   {
      ((JDRTextPathStroke)getStroke()).setVAlign(align);
   }

   /**
    * Sets horizontal and vertical alignment.
    * @param halign the horizontal alignment
    * @param valign the vertical alignment
    * @see #setVAlign(int)
    * @see #setHAlign(int)
    * @see JDRTextPathStroke#setVAlign(int)
    */
   public void setAlign(int halign, int valign)
   {
      setHAlign(halign);
      setVAlign(valign);
   }

   /**
    * Gets horizontal alignment.
    * @return the horizontal alignment
    * @see #setHAlign(int)
    * @see #getVAlign()
    * @see JDRTextPathStroke#getHAlign()
    */
   public int getHAlign()
   {
      return ((JDRTextPathStroke)getStroke()).getHAlign();
   }

   /**
    * Gets vertical alignment.
    * @return the vertical alignment
    * @see #setVAlign(int)
    * @see #getHAlign()
    * @see JDRTextPathStroke#getVAlign()
    */
   public int getVAlign()
   {
      return ((JDRTextPathStroke)getStroke()).getVAlign();
   }

   public void setText(String text, String latexText)
   {
      ((JDRTextPathStroke)getStroke()).setText(text, latexText);
   }

   public void setText(String text)
   {
      ((JDRTextPathStroke)getStroke()).setText(text);
   }

   public void setLaTeXText(String latexText)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXText(latexText);
   }

   public String getText()
   {
      return ((JDRTextPathStroke)getStroke()).getText();
   }

   public String getLaTeXText()
   {
      return ((JDRTextPathStroke)getStroke()).getLaTeXText();
   }

   public void setFont(String name, int series, 
     int shape, JDRLength size)
   {
      ((JDRTextPathStroke)getStroke()).setFont(name, series,
        shape, size);
   }

   public void setFontFamily(String name)
   {
      ((JDRTextPathStroke)getStroke()).setFontFamily(name);
   }

   public void setFontSeries(int series)
   {
      ((JDRTextPathStroke)getStroke()).setFontSeries(series);
   }

   public void setFontShape(int shape)
   {
      ((JDRTextPathStroke)getStroke()).setFontShape(shape);
   }

   public void setFontSize(JDRLength size)
   {
      ((JDRTextPathStroke)getStroke()).setFontSize(size);
   }

   public String getFontFamily()
   {
      return ((JDRTextPathStroke)getStroke()).getFontFamily();
   }

   public int getFontSeries()
   {
      return ((JDRTextPathStroke)getStroke()).getFontSeries();
   }

   public int getFontShape()
   {
      return ((JDRTextPathStroke)getStroke()).getFontShape();
   }

   public JDRLength getFontSize()
   {
      return ((JDRTextPathStroke)getStroke()).getFontSize();
   }

   public Font getFont()
   {
      return ((JDRTextPathStroke)getStroke()).getFont();
   }

   public JDRFont getJDRFont()
   {
      return ((JDRTextPathStroke)getStroke()).getJDRFont();
   }

   public void setLaTeXFamily(String name)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXFamily(name);
   }

   public void setLaTeXSize(String size)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXSize(size);
   }

   public void setLaTeXSeries(String series)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXSeries(series);
   }

   public void setLaTeXShape(String shape)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXShape(shape);
   }

   public void setLaTeXFont(String family, String size, 
      String series, String shape)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXFont(family, size,
         series, shape);
   }

   public void setLaTeXFont(LaTeXFont ltxFont)
   {
      ((JDRTextPathStroke)getStroke()).setLaTeXFont(ltxFont);
   }

   public String getLaTeXFamily()
   {
      return ((JDRTextPathStroke)getStroke()).getLaTeXFamily();
   }

   public String getLaTeXShape()
   {
      return ((JDRTextPathStroke)getStroke()).getLaTeXShape();
   }

   public String getLaTeXSeries()
   {
      return ((JDRTextPathStroke)getStroke()).getLaTeXSeries();
   }

   public String getLaTeXSize()
   {
      return ((JDRTextPathStroke)getStroke()).getLaTeXSize();
   }

   public void makeEqual(JDRObject object)
   {
      JDRTextPath textPath = (JDRTextPath)object;

      super.makeEqual(textPath);

      if (path_ == null)
      {
         path_ = (JDRShape)textPath.getUnderlyingShape().clone();
      }
      else
      {
         path_.makeEqual(textPath.getUnderlyingShape());
      }

      isOutline = textPath.isOutline;

      textPath.assignShowPathAttributesToTextPath(this);
   }

   protected void assignShowPathAttributesToTextPath(JDRTextPath textPath)
   {
      textPath.showPath = showPath;

      if (showPathStroke == null)
      {
         textPath.showPathStroke = new JDRBasicStroke(canvasGraphics);
      }
      else
      {
         textPath.showPathStroke = (JDRBasicStroke)showPathStroke.clone();
      }

      if (showPathFillPaint == null)
      {
         textPath.showPathFillPaint = null;
      }
      else if (textPath.showPathFillPaint == null)
      {
         textPath.showPathFillPaint = (JDRPaint)showPathFillPaint.clone();
      }
      else
      {
         textPath.showPathFillPaint.makeEqual(showPathFillPaint);
      }

      if (showPathLinePaint == null)
      {
         textPath.showPathLinePaint = null;
      }
      else if (textPath.showPathLinePaint == null)
      {
         textPath.showPathLinePaint = (JDRPaint)showPathLinePaint.clone();
      }
      else
      {
         textPath.showPathLinePaint.makeEqual(showPathLinePaint);
      }
   }

   @Override
   public Object clone()
   {
      JDRTextPath textPath = new JDRTextPath(getCanvasGraphics());

      textPath.makeEqual(this);

      return textPath;
   }

   @Override
   public JDRShape breakPath()
      throws InvalidShapeException
   {
      JDRTextPath newShape = new JDRTextPath(path_.breakPath(), 
        (JDRTextPathStroke)getStroke());

      newShape.description = description;
      newShape.tag = tag;

      assignShowPathAttributesToTextPath(newShape);

      return newShape;
   }

   @Override
   public void draw(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      Paint oldPaint = cg.getPaint();

      JDRPaint paint = getTextPaint();

      Shape shape = getStorageStrokedArea();

      boolean doShift = false;
      AffineTransform shiftAf = null;

      BBox box = null;

      if (parentFrame != null && cg.isEvenPage())
      {
         doShift = true;

         double hoffset = parentFrame.getEvenXShift();
         double voffset = parentFrame.getEvenYShift();

         if (hoffset != 0.0 || voffset != 0.0)
         {
            shiftAf = AffineTransform.getTranslateInstance(hoffset, voffset);

            shape = shiftAf.createTransformedShape(shape);
         }
      }

      if (showPath)
      {
         // ensure variables are initialised
         getShowPathStroke();
         getShowPathFillPaint();
         getShowPathLinePaint();

         if (showPathFillPaint instanceof JDRShading
          || showPathLinePaint instanceof JDRShading)
         {
            box = path_.getStorageBBox();

            if (doShift)
            {
               box.translate(parentFrame.getEvenXShift(),
                             parentFrame.getEvenYShift());
            }
         }

         Shape pathShape = path_.getGeneralPath();

         if (shiftAf != null)
         {
            pathShape = shiftAf.createTransformedShape(pathShape);
         }

         if (!(showPathFillPaint instanceof JDRTransparent))
         {
            cg.setPaint(showPathFillPaint.getPaint(box));
            cg.fill(pathShape);
         }

         if (isStroked())
         {
            JDRPaint textPaint = path_.getLinePaint();
            JDRStroke tpStroke = getStroke();

            path_.setLinePaint(showPathLinePaint);
            path_.setStroke(showPathStroke);

            if (showPathLinePaint instanceof JDRTransparent)
            {
               if (showPathStroke.hasMarkers())
               {
                  showPathStroke.drawMarkers(path_);
               }
            }
            else
            {
               cg.setPaint(showPathLinePaint.getPaint(box));

               showPathStroke.drawStoragePath(path_, path_.getGeneralPath());
            }

            path_.setLinePaint(textPaint);
            path_.setStroke(tpStroke);
         }

      }

      if (paint instanceof JDRShading)
      {
         box = new BBox(cg, shape.getBounds2D());
      }

      if (isOutline)
      {
         JDRPaint fill = getOutlineFillPaint();

         if (fill != null && !(fill instanceof JDRTransparent))
         {
            cg.setPaint(fill.getPaint(box));
            cg.fill(shape);
         }

         cg.setPaint(paint.getPaint(box));

         Stroke oldStroke = cg.getStroke();
         cg.setStroke(JDRText.getOutlineStroke(cg));

         cg.draw(shape);
         cg.setStroke(oldStroke);
      }
      else
      {
         cg.setPaint(paint.getPaint(box));

         cg.fill(shape);
      }

      cg.setPaint(oldPaint);
   }

   @Override
   public void drawDraft(FlowFrame parentFrame)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();
      Paint oldPaint = g2.getPaint();

      path_.drawDraft(parentFrame);

      Shape shape = getComponentStrokedPath();

      g2.setPaint(getTextPaint().getPaint(getComponentBBox()));
      g2.fill(shape);
      g2.setPaint(oldPaint);
   }

   @Override
   public void print(Graphics2D g2)
   {
      Paint oldPaint = g2.getPaint();
      CanvasGraphics cg = getCanvasGraphics();

      BBox box = null;

      if (showPath)
      {
         // ensure variables are initialised
         getShowPathStroke();
         getShowPathFillPaint();
         getShowPathLinePaint();

         if (showPathFillPaint instanceof JDRShading
          || showPathLinePaint instanceof JDRShading)
         {
            box = path_.getBpBBox();
         }

         Path2D path2d = path_.getBpGeneralPath();

         if (!(showPathFillPaint instanceof JDRTransparent))
         {
            g2.setPaint(showPathFillPaint.getPaint(box));
            g2.fill(path2d);
         }

         if (isStroked())
         {
            JDRPaint textPaint = path_.getLinePaint();
            JDRStroke tpStroke = getStroke();

            path_.setLinePaint(showPathLinePaint);
            path_.setStroke(showPathStroke);

            if (showPathLinePaint instanceof JDRTransparent)
            {
               showPathStroke.printMarkers(g2, path_);
            }
            else
            {
               g2.setPaint(showPathLinePaint.getPaint(box));
               showPathStroke.printPath(g2, path_, path2d);
            }

            path_.setLinePaint(textPaint);
            path_.setStroke(tpStroke);
         }
      }

      JDRPaint paint = getTextPaint();

      Shape shape = getBpStrokedArea();

      if (paint instanceof JDRShading)
      {
         box = new BBox(cg, shape.getBounds2D());
      }

      if (isOutline)
      {
         JDRPaint fill = getOutlineFillPaint();

         if (fill != null && !(fill instanceof JDRTransparent))
         {
            g2.setPaint(fill.getPaint(box));
            g2.fill(shape);
         }

         g2.setPaint(paint.getPaint(box));
         g2.draw(shape);
      }
      else
      {
         g2.setPaint(paint.getPaint(box));
         g2.fill(shape);
      }

      g2.setPaint(oldPaint);
   }

   @Override
   public BBox getStorageControlBBox()
   {
      return path_.getStorageControlBBox();
   }

   public void savePgf(TeX tex)
    throws IOException
   {
      if (showPath)
      {
         // ensure variables are initialised
         getShowPathStroke();
         getShowPathFillPaint();
         getShowPathLinePaint();

         getJDRShape().savePgf(tex);
      }

      BBox pathBBox = getStorageBBox();

      if (pathBBox == null) return;

      ExportSettings exportSettings = tex.getExportSettings();

      CanvasGraphics cg = getCanvasGraphics();

      JDRPaint textPaint = getTextPaint();

      if (textPaint instanceof JDRShading)
      {
         String setting = exportSettings.textualShading.toString().toLowerCase();

         String msg = cg.getMessageWithFallback(
            "warning.pgf-no-text-shading",
            "Text shading paint can''t be exported to pgf: using export setting {0}",
            cg.getMessageDictionary().getMessageWithFallback(
             "export.textualshading."+setting,
             setting));

         cg.getMessageSystem().getPublisher().publishMessages(
             MessageInfo.createMessage(msg));

         tex.comment(msg);
      }

      if (isOutline())
      {
         String setting = exportSettings.textPathOutline.toString().toLowerCase();

         String msg = cg.getMessageWithFallback(
            "warning.pgf-no-textpath-outline",
            "Text-path outline can't be exported to pgf: using export setting {0}",
            cg.getMessageDictionary().getMessageWithFallback(
             "export.textualshading."+setting,
             setting));

         cg.getMessageSystem().getPublisher().publishMessages(
             MessageInfo.createMessage(msg));

         tex.comment(msg);
      }

      if (((textPaint instanceof JDRShading)
           && (exportSettings.textualShading == 
               ExportSettings.TextualShading.TO_PATH))
       || (isOutline()
           && (exportSettings.textPathOutline ==
               ExportSettings.TextPathOutline.TO_PATH)))
      {
         JDRShape shape = null;

         try
         {
            JDRGroup group = splitText();

            for (int j = 0; j < group.size(); j++)
            {
               JDRGroup grp
                  = ((JDRText)group.get(j)).convertToPath();
               group.set(j, grp.get(0));
            }

            shape = group.mergePaths(null);
            shape.setDescription(getDescription());
            shape.setTag(getTag());
         }
         catch (Exception e)
         {
            cg.getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
         }

         if (shape != null)
         {
            shape.savePgf(tex);
            return;
         }
      }

      if (textPaint instanceof JDRShading)
      {
         JDRShading shading = (JDRShading)textPaint;

         switch (exportSettings.textualShading)
         {
            case AVERAGE:
               textPaint 
                  = shading.getStartColor().average(shading.getEndColor());
            break;
            case END:
               textPaint = shading.getEndColor();
            break;
            case START:
            default:
               textPaint = shading.getStartColor();
         }
      }

      tex.println("\\begin{pgfscope}");

      JDRTextPathStroke s = (JDRTextPathStroke)getStroke();

      try
      {
         s.savePgf(tex, textPaint, getFullPath());
      }
      catch (InvalidShapeException e)
      {
         s.savePgf(tex, textPaint, this);
         cg.getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createWarning(e));
      }

      tex.println("\\end{pgfscope}");
   }

   @Override
   public void writeSVGdefs(SVG svg) throws IOException
   {
      JDRPaint paint = getTextPaint();

      paint.writeSVGdefs(svg);

      if (isOutline && getOutlineFillPaint() != null)
      {
         getOutlineFillPaint().writeSVGdefs(svg);
      }

      getStroke().writeSVGdefs(svg, path_);

      if (showPath)
      {
         getShowPathStroke().writeSVGdefs(svg, path_);
         getShowPathLinePaint().writeSVGdefs(svg);
         getShowPathFillPaint().writeSVGdefs(svg);
      }
   }

   @Override
   public void saveSVG(SVG svg, String attr)
   throws IOException
   {
      JDRPaint paint = getTextPaint();

      JDRTextPathStroke stroke = (JDRTextPathStroke)getStroke();

      if (isOutline
            && svg.getExportSettings().textPathOutline
                 == ExportSettings.TextPathOutline.TO_PATH)
      {
         if (showPath)
         {
            // ensure variables are initialised
            getShowPathStroke();
            getShowPathFillPaint();
            getShowPathLinePaint();

            getJDRShape().saveSVG(svg, attr);
         }

         Shape shape = getStorageStrokedArea();

         svg.print("   <path "+attr+" d=\"");

         svg.saveStoragePathData(shape);

         svg.println(" \"");

         svg.println("      "+paint.svgLine());

         JDRPaint fillPaint = getOutlineFillPaint();

         if (fillPaint != null)
         {
            svg.println("      "+fillPaint.svgFill());
         }

         svg.println("   >");

         svg.print("<title>");
         svg.print(svg.encodeContent(getText()));
         svg.println("</title>");
         svg.println("</path>");
      }
      else
      {
         svg.print("    <text ");
         svg.print(attr);
         svg.print(" ");
         svg.print(paint.svgFill());
         svg.print(" ");
         svg.print(stroke.getJDRFont().svg());
         svg.println(">");

         svg.println("      <textPath href=\"#"+stroke.getPathID());

         if (showPath)
         {
            // ensure variables are initialised
            getShowPathStroke();
            getShowPathFillPaint();
            getShowPathLinePaint();

            svg.print(" ");
            svg.print(paint.svgFill());

            svg.print(" ");
            svg.print(paint.svgLine());

            if (!(showPathLinePaint instanceof JDRTransparent))
            {
               svg.print(" ");
               svg.print(showPathStroke.svg(showPathLinePaint));
            }
         }

         svg.println("\">");

         if (description != null && !description.isEmpty())
         {
            svg.print("<title>");
            svg.print(svg.encodeContent(description));
            svg.println("</title>");
         }

         svg.println(svg.encodeContent(getText()));

         svg.println("      </textPath>");

         svg.println("    </text>");
      }
   }

   @Override
   public void saveEPS(PrintWriter out)
   throws IOException
   {
      if (showPath)
      {
         // ensure variables are initialised
         getShowPathStroke();
         getShowPathFillPaint();
         getShowPathLinePaint();

         getJDRShape().saveEPS(out);
      }

      JDRPaint paint = getTextPaint();

      Shape shape = getBpStrokedArea();

      EPS.fillPath(shape, paint, out);
   }

   @Override
   public JDRShape outlineToPath()
      throws InvalidShapeException
   {
      // ignore showPath setting?

      JDRShape shape = super.outlineToPath();

      shape.setStroke(new JDRBasicStroke(getCanvasGraphics()));

      if (hasDescription())
      {
         shape.description = description;
      }
      else
      {
         shape.description = getText();
      }

      shape.tag = tag;

      return shape;
   }

   /**
   * Gets a new text path object with a full path as the underlying
   * shape.
   */
   @Override
   public JDRShape getFullPath()
      throws InvalidShapeException
   {
      JDRTextPath newShape = new JDRTextPath(path_.getFullPath(),
        (JDRTextPathStroke)getStroke().clone());

      if (hasDescription())
      {
         newShape.description = description;
      }
      else
      {
         newShape.description = getText();
      }

      newShape.tag = tag;

      assignShowPathAttributesToTextPath(newShape);

      return newShape;
   }

   @Override
   public JDRCompleteObject getFullObject()
      throws InvalidShapeException
   {
      return getFullPath();
   }

   @Override
   public JDRObjectLoaderListener getListener()
   {
      return textPathListener;
   }

   public double[] getTransformation(double[] mtx)
   {
      return ((JDRTextPathStroke)getStroke()).getTransformation(mtx);
   }

   public void setTransformation(double[] mtx)
   {
      ((JDRTextPathStroke)getStroke()).setTransformation(mtx);
   }

   @Override
   public JDRShape toPolygon(double flatness)
      throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.toPolygon(flatness),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   @Override
   public JDRShape reverse()
      throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.reverse(),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   @Override
   public JDRShape exclusiveOr(JDRShape shape)
   throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.exclusiveOr(shape),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   @Override
   public JDRShape pathUnion(JDRShape shape)
   throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.pathUnion(shape),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   @Override
   public JDRShape intersect(JDRShape shape)
   throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.intersect(shape),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   @Override
   public JDRShape subtract(JDRShape shape)
   throws InvalidShapeException
   {
      JDRTextPath textPath = new JDRTextPath(path_.subtract(shape),
         (JDRTextPathStroke)getStroke().clone());

      textPath.description = description;
      textPath.tag = tag;

      assignShowPathAttributesToTextPath(textPath);

      return textPath;
   }

   /**
    * Gets string representation of this textpath.
    * @return string representation of this textpath
    */
   @Override
   public String toString()
   {
      String str = "TextPath: text="+getText();

      str += ", size="+size()+", segments=[";

      for (int i = 0; i < size(); i++)
      {
         str += get(i)+",";
      }

      str += "]";

      return str;
   }

   @Override
   public JDRTextual getTextual()
   {
      return this;
   }

   @Override
   public boolean hasTextual()
   {
      return true;
   }

   @Override
   public JDRShape getUnderlyingShape()
   {
      return path_;
   }

   @Override
   public void setUnderlyingShape(JDRShape shape)
   {
      path_ = shape;
   }

   @Override
   public JDRPathIterator getIterator()
   {
      return path_.getIterator();
   }

   @Override
   public JDRPointIterator getPointIterator()
   {
      return path_.getPointIterator();
   }

   @Override
   public int size()
   {
      return path_.size();
   }

   public int getCapacity()
   {
      return path_.getCapacity();
   }

   public void setCapacity(int capacity)
      throws IllegalArgumentException
   {
      path_.setCapacity(capacity);
   }

   @Override
   public void open()
   throws InvalidPathException
   {
      path_.open();
   }

   @Override
   public void open(boolean removeLastSegment)
   throws InvalidPathException
   {
      path_.open(removeLastSegment);
   }

   @Override
   public void close(JDRPathSegment segment)
      throws InvalidPathException,IllFittingPathException
   {
      path_.close(segment);
   }

   @Override
   public boolean isClosed()
   {
      return path_.isClosed();
   }

   @Override
   public boolean segmentHasEnd(JDRPathSegment segment)
   {
      return path_.segmentHasEnd(segment);
   }

   @Override
   public int getIndex(JDRPathSegment segment)
   {
      return path_.getIndex(segment);
   }

   @Override
   public int getLastIndex(JDRPathSegment segment)
   {
      return path_.getLastIndex(segment);
   }

   @Override
   public JDRPathSegment get(int index)
      throws ArrayIndexOutOfBoundsException
   {
      return path_.get(index);
   }

   @Override
   public JDRPathSegment getLastSegment()
   {
      return path_.getLastSegment();
   }

   @Override
   public JDRPathSegment getFirstSegment()
   {
      return path_.getLastSegment();
   }

   @Override
   public JDRPoint getFirstControl()
   {
      return path_.getFirstControl();
   }

   @Override
   public JDRPoint getLastControl()
   {
      return path_.getLastControl();
   }

   @Override
   public void stopEditing()
   {
      path_.stopEditing();
   }

   @Override
   public int getSelectedControlIndex()
   {
      return path_.getSelectedControlIndex();
   }

   @Override
   protected void selectControl(JDRPoint p, int pointIndex, 
      int segmentIndex)
   {
      path_.selectControl(p, pointIndex, segmentIndex);
   }

   @Override
   public JDRPathSegment getSelectedSegment()
   {
      return path_.getSelectedSegment();
   }

   @Override
   public JDRPoint getSelectedControl()
   {
      return path_.getSelectedControl();
   }

   @Override
   public int getSelectedIndex()
   {
      return path_.getSelectedIndex();
   }

   @Override
   public Path2D getGeneralPath()
   {
      return path_.getGeneralPath();
   }

   @Override
   public Shape getStorageStrokedPath()
   {
      Shape shape = super.getStorageStrokedPath();

      if (showPath)
      {
         Shape strokedPath = getShowPathStroke().getStorageStrokedPath(path_);

         if (!(shape instanceof Path2D))
         {
            shape = new Path2D.Double(shape);
         }

         ((Path2D)shape).append(strokedPath, false);
      }

      return shape;
   }

   @Override
   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.setSegment(index, segment);
   }

   @Override
   public void add(JDRSegment s)
   throws InvalidPathException
   {
      path_.add(s);
   }

   @Override
   public JDRPoint addPoint()
   {
      return path_.addPoint();
   }

   @Override
   public void makeContinuous(boolean atStart, boolean equiDistant)
   {
      path_.makeContinuous(atStart, equiDistant);
   }

   @Override
   public void convertSegment(int idx, JDRPathSegment segment)
   throws InvalidPathException
   {
      path_.convertSegment(idx, segment);
   }

   @Override
   public JDRPathSegment remove(int i)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.remove(i);
   }

   @Override
   public JDRPathSegment remove(JDRPathSegment segment)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.remove(segment);
   }

   @Override
   public JDRPathSegment removeSelectedSegment()
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.removeSelectedSegment();
   }

   @Override
   public JDRSegment removeSegment(int i)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.removeSegment(i);
   }

   @Override
   public void translateControl(JDRPathSegment segment, JDRPoint p,
      double x, double y)
   {
      path_.translateControl(segment, p, x, y);
   }

   @Override
   public void translateParams(double shiftX, double shiftY)
   {
   }

   @Override
   public void scaleParams(Point2D p, double factorX, double factorY)
   {
   }

   @Override
   public void shearParams(Point2D p, double factorX, double factorY)
   {
   }

   @Override
   public void rotateParams(Point2D p, double angle)
   {
   }

   @Override
   public void transformParams(double[] matrix)
   {
   }

   @Override
   public void transformParams(AffineTransform af)
   {
   }

   @Override
   public boolean hasSymmetricPath()
   {
      return path_.hasSymmetricPath();
   }

   @Override
   public void fade(double value)
   {
      getTextPaint().fade(value);
   }

   @Override
   public JDRSymmetricPath getSymmetricPath()
   {
      return path_.getSymmetricPath();
   }

   @Override
   public String info(String prefix)
   {
      JDRMessage msgSys = getCanvasGraphics().getMessageSystem();
      String eol = String.format("%n");

      StringBuilder builder = new StringBuilder();

      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
       "objectinfo.textpath", "Text-Path:"));

      builder.append(eol);
      builder.append(prefix);

      if (isOutline())
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.textual.outline_on", "Outline mode on"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.textual.outline_off", "Outline mode off"));
      }

      builder.append(eol);
      builder.append(prefix);

      if (showPath)
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.textpath.show_path_on", "Show path on"));
      }
      else
      {
         builder.append(msgSys.getMessageWithFallback(
          "objectinfo.textpath.show_path_off", "Show path off"));
      }

      builder.append(eol);
      builder.append(getShowPathStroke().info(prefix));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
           "objectinfo.path.line_paint", "Line paint: {0}",
            getShowPathLinePaint().info()));

      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
           "objectinfo.path.fill_paint", "Fill paint: {0}",
            getShowPathFillPaint().info()));

      builder.append(eol);
      builder.append(super.info(prefix));
      builder.append(eol);
      builder.append(prefix);

      builder.append(msgSys.getMessageWithFallback(
        "objectinfo.underlying", "Underlying object:")
      );

      builder.append(eol);
      builder.append(path_.info(prefix+prefix));

      return builder.toString();
   }

   @Override
   public Object[] getDescriptionInfo()
   {
      String text = getText();
      String latexText = getLaTeXText();

      if (latexText == null || latexText.isEmpty())
      {
         latexText = text;
      }

      return new Object[] {text, latexText};
   }

   @Override
   public int getTotalPathSegments()
   {
      if (path_ instanceof JDRCompoundShape)
      {
         return ((JDRCompoundShape)path_).getTotalPathSegments();
      }

      return path_.size();
   }

   @Override
   protected void setSelectedElements(int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control)
   {
      path_.setSelectedElements(segmentIndex, controlIndex, segment, control);
   }

   @Override
   public int getObjectFlag()
   {
      int flag = ((super.getObjectFlag() & ~SELECT_FLAG_NON_TEXTUAL_SHAPE)
       | SELECT_FLAG_TEXTUAL | SELECT_FLAG_TEXTPATH);

      if (isOutline)
      {
         flag = (flag | SELECT_FLAG_OUTLINE);
      }

      return flag;
   }

   @Override
   public void setOutlineMode(boolean enable)
   {
      isOutline = enable;
   }

   @Override
   public boolean isOutline()
   {
      return isOutline;
   }

   @Override
   public void reset()
   {
      ((JDRTextPathStroke)getStroke()).reset();
   }

   private static JDRTextPathListener textPathListener = new JDRTextPathListener();

   private volatile JDRShape path_;

   private volatile boolean isOutline = false;

   private boolean showPath = false;
   private JDRBasicStroke showPathStroke;
   private JDRPaint showPathFillPaint, showPathLinePaint;
}
