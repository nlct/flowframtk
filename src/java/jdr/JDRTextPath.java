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

      setStroke(new JDRTextPathStroke(text));
      setTextPaint(text.getTextPaint());
      setOutlineMode(text.isOutline());

      if (isOutline())
      {
         setFillPaint(text.getFillPaint());
      }
   }

   public JDRTextPath(JDRShape path, JDRTextPathStroke stroke)
   {
      super(path.getCanvasGraphics());

      path_ = path;

      setStroke(stroke);
      setTextPaint(path.getLinePaint());
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

      if (!(shape.getStroke() instanceof JDRTextPathStroke))
      {
         textPath.setStroke(new JDRTextPathStroke(shape.getCanvasGraphics()));
      }

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

   public JDRPaint getTextPaint()
   {
      return path_.getLinePaint();
   }

   public void setTextPaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
   }

   public JDRPaint getFillPaint()
   {
      return path_.getFillPaint();
   }

   public void setFillPaint(JDRPaint paint)
   {
      path_.setFillPaint(paint);
   }

   public JDRPaint getLinePaint()
   {
      return path_.getLinePaint();
   }

   public void setLinePaint(JDRPaint paint)
   {
      path_.setLinePaint(paint);
   }

   /**
    * Gets the path for this.
    * @return the path
    */
   public JDRShape getJDRShape()
   {
      JDRShape path = (JDRShape)path_.clone();

      if (!(path_ instanceof JDRTextual))
      {
         path.setStroke(new JDRBasicStroke(getCanvasGraphics()));
      }

      return path;
   }

   /**
    * Gets the text for this as a text area.
    * @return the text
    */
   public JDRText getJDRText()
   {
      JDRText text = ((JDRTextPathStroke)getStroke()).getJDRText();

      text.description = description;

      JDRPoint p = getFirstSegment().getStart();

      text.setPosition(p.x, p.y);

      text.setTextPaint(getLinePaint());

      text.setOutlineMode(isOutline);

      if (isOutline)
      {
         text.setFillPaint(getFillPaint());
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

      return grp;
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
   }

   public Object clone()
   {
      JDRTextPath textPath = new JDRTextPath(getCanvasGraphics());

      textPath.makeEqual(this);

      return textPath;
   }

   public JDRShape breakPath()
      throws InvalidShapeException
   {
      return new JDRTextPath(path_.breakPath(), 
        (JDRTextPathStroke)getStroke());
   }

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

      if (parentFrame != null && cg.isEvenPage())
      {
         double hoffset = parentFrame.getEvenXShift();
         double voffset = parentFrame.getEvenYShift();

         if (hoffset != 0.0 || voffset != 0.0)
         {
            shape = AffineTransform.getTranslateInstance(hoffset, voffset)
                  .createTransformedShape(shape);
         }
      }

      BBox box = null;

      if (paint instanceof JDRShading)
      {
         box = new BBox(cg, shape.getBounds2D());
      }

      if (isOutline)
      {
         JDRPaint fill = getFillPaint();

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

   public void print(Graphics2D g2)
   {
      Paint oldPaint = g2.getPaint();

      JDRPaint paint = getTextPaint();

      Shape shape= getBpStrokedArea();

      BBox box = null;

      if (paint instanceof JDRShading)
      {
         box = new BBox(getCanvasGraphics(), shape.getBounds2D());
      }

      g2.setPaint(paint.getPaint(box));
      g2.fill(shape);
      g2.setPaint(oldPaint);
   }

   public BBox getStorageControlBBox()
   {
      return path_.getStorageControlBBox();
   }

   public void savePgf(TeX tex)
    throws IOException
   {
      BBox pathBBox = getStorageBBox();

      if (pathBBox == null) return;

      ExportSettings exportSettings = tex.getExportSettings();

      JDRPaint textPaint = getTextPaint();

      if (textPaint instanceof JDRShading)
      {
         String msg = getCanvasGraphics().warning(
            "warning.pgf-no-text-shading",
            "text shading paint can't be exported to pgf");

         tex.comment(msg);
      }

      if (isOutline())
      {
         String msg = getCanvasGraphics().warning(
            "warning.pgf-no-textpath-outline",
            "text-path outline can't be exported to pgf");

         tex.comment(msg);
      }

      if (((textPaint instanceof JDRShading)
           && (exportSettings.textualShading == 
               ExportSettings.TextualShading.TO_PATH))
       || (isOutline()
           && (exportSettings.textPathOutline ==
               ExportSettings.TextPathOutline.TO_PATH)))
      {
         try
         {
            JDRGroup group = splitText();

            for (int j = 0; j < group.size(); j++)
            {
               JDRGroup grp
                  = ((JDRText)group.get(j)).convertToPath();
               group.set(j, grp.get(0));
            }

            group.mergePaths(null).savePgf(tex);

            return;
         }
         catch (Exception e)
         {
            getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
               MessageInfo.createWarning(e));
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
         getCanvasGraphics().getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createWarning(e));
      }

      tex.println("\\end{pgfscope}");
   }

   public void saveSVG(SVG svg, String attr)
   throws IOException
   {
      JDRPaint paint = getTextPaint();

      Shape shape = getStorageStrokedArea();

      svg.print("   <path "+attr+" d=\"");

      svg.saveStoragePathData(shape);

      svg.println(" \"");

      svg.println("      "+getFillPaint().svgFill());
      svg.println("   />");
   }

   public void saveEPS(PrintWriter out)
   throws IOException
   {
      JDRPaint paint = getTextPaint();

      Shape shape = getBpStrokedArea();

      EPS.fillPath(shape, paint, out);
   }

   public JDRShape outlineToPath()
      throws InvalidShapeException
   {
      JDRShape shape = super.outlineToPath();

      shape.setStroke(new JDRBasicStroke(getCanvasGraphics()));

      if (description.isEmpty())
      {
         shape.description = getText();
      }
      else
      {
         shape.description = description;
      }

      return shape;
   }

   /**
   * Gets a new text path object with a full path as the underlying
   * shape.
   */
   public JDRShape getFullPath()
      throws InvalidShapeException
   {
      JDRTextPath newShape = new JDRTextPath(path_.getFullPath(),
        (JDRTextPathStroke)getStroke().clone());

      if (description.isEmpty())
      {
         newShape.description = getText();
      }
      else
      {
         newShape.description = description;
      }

      return newShape;
   }

   public JDRCompleteObject getFullObject()
      throws InvalidShapeException
   {
      return getFullPath();
   }

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

   public JDRShape toPolygon(double flatness)
      throws InvalidShapeException
   {
      JDRTextPath path = new JDRTextPath(path_.toPolygon(flatness),
         (JDRTextPathStroke)getStroke().clone());

      return path;
   }

   public JDRShape reverse()
      throws InvalidShapeException
   {
      JDRTextPath path = new JDRTextPath(path_.reverse(),
         (JDRTextPathStroke)getStroke().clone());

      return path;
   }

   public JDRShape exclusiveOr(JDRShape path)
   throws InvalidShapeException
   {
      return new JDRTextPath(path_.exclusiveOr(path),
         (JDRTextPathStroke)getStroke().clone());
   }

   public JDRShape pathUnion(JDRShape path)
   throws InvalidShapeException
   {
      return new JDRTextPath(path_.pathUnion(path),
         (JDRTextPathStroke)getStroke().clone());
   }

   public JDRShape intersect(JDRShape path)
   throws InvalidShapeException
   {
      return new JDRTextPath(path_.intersect(path),
         (JDRTextPathStroke)getStroke().clone());
   }

   public JDRShape subtract(JDRShape path)
   throws InvalidShapeException
   {
      return new JDRTextPath(path_.subtract(path),
         (JDRTextPathStroke)getStroke().clone());
   }

   /**
    * Gets string representation of this textpath.
    * @return string representation of this textpath
    */
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

   public JDRTextual getTextual()
   {
      return this;
   }

   public boolean hasTextual()
   {
      return true;
   }

   public JDRShape getUnderlyingShape()
   {
      return path_;
   }

   public void setUnderlyingShape(JDRShape shape)
   {
      path_ = shape;
   }

   public JDRPathIterator getIterator()
   {
      return path_.getIterator();
   }

   public JDRPointIterator getPointIterator()
   {
      return path_.getPointIterator();
   }

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

   public void open()
   throws InvalidPathException
   {
      path_.open();
   }

   public void open(boolean removeLastSegment)
   throws InvalidPathException
   {
      path_.open(removeLastSegment);
   }

   public void close(JDRPathSegment segment)
      throws InvalidPathException,IllFittingPathException
   {
      path_.close(segment);
   }

   public boolean isClosed()
   {
      return path_.isClosed();
   }

   public boolean segmentHasEnd(JDRPathSegment segment)
   {
      return path_.segmentHasEnd(segment);
   }

   public int getIndex(JDRPathSegment segment)
   {
      return path_.getIndex(segment);
   }

   public int getLastIndex(JDRPathSegment segment)
   {
      return path_.getLastIndex(segment);
   }

   public JDRPathSegment get(int index)
      throws ArrayIndexOutOfBoundsException
   {
      return path_.get(index);
   }

   public JDRPathSegment getLastSegment()
   {
      return path_.getLastSegment();
   }

   public JDRPathSegment getFirstSegment()
   {
      return path_.getLastSegment();
   }

   public JDRPoint getFirstControl()
   {
      return path_.getFirstControl();
   }

   public JDRPoint getLastControl()
   {
      return path_.getLastControl();
   }

   public void stopEditing()
   {
      path_.stopEditing();
   }

   public int getSelectedControlIndex()
   {
      return path_.getSelectedControlIndex();
   }

   protected void selectControl(JDRPoint p, int pointIndex, 
      int segmentIndex)
   {
      path_.selectControl(p, pointIndex, segmentIndex);
   }

   public JDRPathSegment getSelectedSegment()
   {
      return path_.getSelectedSegment();
   }

   public JDRPoint getSelectedControl()
   {
      return path_.getSelectedControl();
   }

   public int getSelectedIndex()
   {
      return path_.getSelectedIndex();
   }

   public Path2D getGeneralPath()
   {
      return path_.getGeneralPath();
   }

   public JDRPathSegment setSegment(int index, JDRPathSegment segment)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.setSegment(index, segment);
   }

   public void add(JDRSegment s)
   throws InvalidPathException
   {
      path_.add(s);
   }

   public JDRPoint addPoint()
   {
      return path_.addPoint();
   }

   public void makeContinuous(boolean atStart, boolean equiDistant)
   {
      path_.makeContinuous(atStart, equiDistant);
   }

   public void convertSegment(int idx, JDRPathSegment segment)
   throws InvalidPathException
   {
      path_.convertSegment(idx, segment);
   }

   public JDRPathSegment remove(int i)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.remove(i);
   }

   public JDRPathSegment remove(JDRPathSegment segment)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.remove(segment);
   }

   public JDRPathSegment removeSelectedSegment()
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.removeSelectedSegment();
   }

   public JDRSegment removeSegment(int i)
      throws ArrayIndexOutOfBoundsException,InvalidPathException
   {
      return path_.removeSegment(i);
   }

   public void translateControl(JDRPathSegment segment, JDRPoint p,
      double x, double y)
   {
      path_.translateControl(segment, p, x, y);
   }

   public void translateParams(double shiftX, double shiftY)
   {
   }

   public void scaleParams(Point2D p, double factorX, double factorY)
   {
   }

   public void shearParams(Point2D p, double factorX, double factorY)
   {
   }

   public void rotateParams(Point2D p, double angle)
   {
   }

   public void transformParams(double[] matrix)
   {
   }

   public boolean showPath()
   {
      return false;
   }

   public boolean hasSymmetricPath()
   {
      return path_.hasSymmetricPath();
   }

   public void fade(double value)
   {
      getTextPaint().fade(value);
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      return path_.getSymmetricPath();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "TextPath:"+eol;

      str += "Underlying shape: "+path_.info();

      return str;
   }

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

   public int getTotalPathSegments()
   {
      if (path_ instanceof JDRCompoundShape)
      {
         return ((JDRCompoundShape)path_).getTotalPathSegments();
      }

      return path_.size();
   }

   protected void setSelectedElements(int segmentIndex, int controlIndex,
      JDRPathSegment segment, JDRPoint control)
   {
      path_.setSelectedElements(segmentIndex, controlIndex, segment, control);
   }

   public int getObjectFlag()
   {
      int flag = (super.getObjectFlag() & ~SELECT_FLAG_NON_TEXTUAL_SHAPE)
       | SELECT_FLAG_TEXTUAL | SELECT_FLAG_TEXTPATH;

      if (isOutline)
      {
         flag = flag | SELECT_FLAG_OUTLINE;
      }

      return flag;
   }

   public void setOutlineMode(boolean enable)
   {
      isOutline = enable;
   }

   public boolean isOutline()
   {
      return isOutline;
   }

   public void reset()
   {
      ((JDRTextPathStroke)getStroke()).reset();
   }

   private static JDRTextPathListener textPathListener = new JDRTextPathListener();

   private volatile JDRShape path_;

   private volatile boolean isOutline = false;
}
