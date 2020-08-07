// File          : FlowframTkSettings.java
// Purpose       : Application settings
// Creation Date : 1st February 2006
// Renamed 2014-03-26: JpgfDrawSettings.java -> FlowframTkSettings.java
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
package com.dickimawbooks.flowframtk;

import java.awt.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Vector;
import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JSplitPane;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;

public class FlowframTkSettings
{
   public FlowframTkSettings(JDRResources resources)
   {
      init(resources);
   }

   private void init(JDRResources resources)
   {
      this.resources = resources;
      canvasGraphics = new CanvasGraphics(resources.getMessageSystem());
      dialogButtonStyle = resources.getDialogButtonStyle();

      startDir = ".";
      String home = System.getenv("HOME");
      if (home != null)
      {
         startDir = home;
      }
      else
      {
         home = System.getenv("USERPROFILE");
         if (home != null) startDir = home;
      }

      fontSize= new JDRLength(resources.getMessageDictionary(), 10, JDRUnit.pt);

      stroke = new JDRBasicStroke(canvasGraphics);
      linePaint = new JDRColor(canvasGraphics, Color.black);
      fillPaint = new JDRTransparent(canvasGraphics);
      textPaint = new JDRColor(canvasGraphics, Color.black);

      rulerLocale = Locale.getDefault();
      rulerFormat = (DecimalFormat)NumberFormat.getNumberInstance(rulerLocale);
   }


   public void setMessageSystem(JDRGuiMessage messageSystem)
   {
      canvasGraphics.setMessageSystem(messageSystem);
   }

   public DecimalFormat getRulerFormat()
   {
      return rulerFormat;
   }

   public Locale getRulerLocale()
   {
      return rulerLocale;
   }

   public void setRulerFormat(String pattern, Locale locale)
    throws IllegalArgumentException
   {
      if (rulerLocale.equals(locale))
      {
         rulerFormat.applyLocalizedPattern(pattern);
         return;
      }

      rulerLocale = locale;
      rulerFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
      rulerFormat.applyLocalizedPattern(pattern);
   }

   public void setRulerFormat(DecimalFormat format, Locale locale)
   {
      rulerFormat = format;
      rulerLocale = locale;
   }

   public void setBrowseUtil(BrowseUtil util)
   {
      canvasGraphics.setBrowseUtil(util);
   }

   public void setBitmapChooser(JFileChooser fileChooser)
   {
      canvasGraphics.setBitmapChooser(fileChooser);
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
      stroke.setCanvasGraphics(cg);
      linePaint.setCanvasGraphics(cg);
      fillPaint.setCanvasGraphics(cg);
      textPaint.setCanvasGraphics(cg);
   }

   public JDRPaper getPaper()
   {
      return canvasGraphics.getPaper();
   }

   public void setPaper(JDRPaper paper)
   {
      canvasGraphics.setPaper(paper);
   }

   public void setShowRulers(boolean showRulers)
   {
      canvasGraphics.setShowRulers(showRulers);
   }

   public boolean showRulers()
   {
      return canvasGraphics.showRulers();
   } 

   public void setShowStatus(boolean flag)
   {
      showStatus = flag;
   }

   public boolean showStatus()
   {
      return showStatus;
   }

   public void setShowToolBar(boolean flag)
   {
      showToolBar = flag;
   }

   public boolean showToolBar()
   {
      return showToolBar;
   }

   public void setRobot(Robot r)
   {
      robot = r;
   }

   public Robot getRobot()
   {
      return robot;
   }

   public void setGridLock(boolean flag)
   {
      canvasGraphics.setGridLock(flag);
   }

   public boolean isGridLocked()
   {
      return canvasGraphics.isGridLocked();
   }

   public void setDisplayGrid(boolean flag)
   {
      canvasGraphics.setDisplayGrid(flag);
   }

   public boolean isGridDisplayed()
   {
      return canvasGraphics.isGridDisplayed();
   }

   public void setPointSize(JDRLength size)
   {
      canvasGraphics.setPointSize(size);
   }

   public void setNormalSize(double size)
   {
      canvasGraphics.setLaTeXNormalSize(size);
   }

   public double getNormalSize()
   {
      return canvasGraphics.getLaTeXNormalSize();
   }

   public void setLinePaint(JDRPaint paint)
   {
      linePaint = paint;
   }

   public void setFillPaint(JDRPaint paint)
   {
      fillPaint = paint;
   }

   public void setTextPaint(JDRPaint paint)
   {
      textPaint = paint;
   }

   public JDRPaint getLinePaint()
   {
      return linePaint;
   }

   public JDRPaint getFillPaint()
   {
      return fillPaint;
   }

   public JDRPaint getTextPaint()
   {
      return textPaint;
   }

   public int getUseSettingsOnLoad()
   {
      return canvasGraphics.getUseSettingsOnLoad();
   }

   public void setUseSettingsOnLoad(int setting)
   {
      canvasGraphics.setUseSettingsOnLoad(setting);
   }

   public int getSaveSettings()
   {
      return saveJDRsettings;
   }

   public void setSaveSettings(int setting)
   {
      saveJDRsettings = setting;
   }

   public void setRendering(boolean antialias, boolean renderquality)
   {
      this.antialias     = antialias;
      this.renderquality = renderquality;

      if (antialias)
      {
         renderHints =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
      }
      else
      {
         renderHints =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_OFF);
      }

      if (renderquality)
      {
         renderHints.add(new RenderingHints(
                           RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY));
      }
      else
      {
         renderHints.add(new RenderingHints(
                           RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_SPEED));
      }
   }

   public RenderingHints getRenderingHints()
   {
      return renderHints;
   }

   public boolean isAntiAliasOn()
   {
      return antialias;
   }

   public boolean isRenderQualityOn()
   {
      return renderquality;
   }

   public JDRUnit getStorageUnit()
   {
      return canvasGraphics.getStorageUnit();
   }

   public void setDashPattern(float[] pattern, float offset)
   {
      stroke.setDashPattern(new DashPattern(canvasGraphics, pattern, offset));
   }

   public void setStartMarker(JDRMarker marker)
   {
      marker.setCanvasGraphics(canvasGraphics);
      stroke.setStartArrow(marker);
   }

   public void setMidMarker(JDRMarker marker)
   {
      marker.setCanvasGraphics(canvasGraphics);
      stroke.setMidArrow(marker);
   }

   public void setEndMarker(JDRMarker marker)
   {
      marker.setCanvasGraphics(canvasGraphics);
      stroke.setEndArrow(marker);
   }

   public JDRMarker getStartMarker()
   {
      return stroke.getStartArrow();
   }

   public JDRMarker getMidMarker()
   {
      return stroke.getMidArrow();
   }

   public JDRMarker getEndMarker()
   {
      return stroke.getEndArrow();
   }

   public JDRBasicStroke getStroke()
   {
      if (stroke == null)
      {
         stroke = new JDRBasicStroke(canvasGraphics);
      }

      return stroke;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      this.stroke = stroke;
   }

   public String getFontFamily()
   {
      return fontFamily;
   }

   public void setFontFamily(String name)
   {
      fontFamily = name;
   }

   public JDRLength getFontSize()
   {
      return fontSize;
   }

   public void setFontSize(double newValue, JDRUnit newUnit)
   {
      fontSize.setValue(newValue, newUnit);
   }

   public void setFontSize(JDRLength value)
   {
      fontSize = value;
   }

   public int getFontShape()
   {
      return fontShape;
   }

   public int getFontSeries()
   {
      return fontSeries;
   }

   public void setFontShape(int shape)
     throws JdrIllegalArgumentException
   {
      if (shape < 0 || shape > JDRFont.MAX_SHAPE_ID)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.FONT_SHAPE, shape, 
            resources.getMessageDictionary());
      }

      fontShape = shape;
   }

   public void setFontSeries(int series)
     throws JdrIllegalArgumentException
   {
      if (series < 0 || series > JDRFont.MAX_SERIES_ID)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.FONT_WEIGHT, series, 
            resources.getMessageDictionary());
      }

      fontSeries = series;
   }

   public String getLaTeXFontFamily()
   {
      return latexFontFamily;
   }

   public void setLaTeXFontFamily(String text)
   {
      latexFontFamily = text;
   }

   public String getLaTeXFontShape()
   {
      return latexFontShape;
   }

   public void setLaTeXFontShape(String text)
   {
      latexFontShape = text;
   }

   public String getLaTeXFontSeries()
   {
      return latexFontSeries;
   }

   public void setLaTeXFontSeries(String text)
   {
      latexFontSeries = text;
   }

   public String getLaTeXFontSize()
   {
      return latexFontSize;
   }

   public void setLaTeXFontSize(String text)
   {
      latexFontSize = text;
   }

   public void setGrid(JDRGrid grid)
   {
      canvasGraphics.setGrid(grid);
   }

   public void setScaleControlPoints(boolean flag)
   {
      canvasGraphics.setScaleControlPoints(flag);
   }

   public void setTool(String toolName)
   {
      canvasGraphics.setTool(toolName);
   }

   public void setTool(int toolId)
   {
      canvasGraphics.setTool(toolId);
   }

   public int getTool()
   {
      return canvasGraphics.getTool();
   }

   public void setLaTeXNormalSize(int normalsize)
   {
      canvasGraphics.setLaTeXNormalSize(normalsize);
   }

   public int getHRulerHeight()
   {
      return hRulerHeight;
   }

   public int getVRulerWidth()
   {
      return vRulerWidth;
   }

   public void setHRulerHeight(int h)
   {
      hRulerHeight = h;
   }

   public void setVRulerWidth(int w)
   {
      vRulerWidth = w;
   }

   public JDRMessage getMessageSystem()
   {
      return canvasGraphics.getMessageSystem();
   }

   public void setRelativeBitmaps(boolean flag)
   {
      relativeBitmaps = flag;
   }

   public boolean useRelativeBitmaps()
   {
      return relativeBitmaps;
   }

   public void setDefaultBitmapCommand(String cmdName)
   {
      bitmapCommand = cmdName;
   }

   public String getDefaultBitmapCommand()
   {
      return bitmapCommand;
   }

   public String getLaTeXApp()
   {
      return latexApp;
   }

   public void setLaTeXApp(String path)
   {
      latexApp = path;
   }

   public String getPdfLaTeXApp()
   {
      return pdflatexApp;
   }

   public void setPdfLaTeXApp(String path)
   {
      pdflatexApp = path;
   }

   public String getDvipsApp()
   {
      return dvipsApp;
   }

   public void setDvipsApp(String path)
   {
      dvipsApp = path;
   }

   public String getDvisvgmApp()
   {
      return dvisvgmApp;
   }

   public void setDvisvgmApp(String path)
   {
      dvisvgmApp = path;
   }

   public String getLibgs()
   {
      return libgs;
   }

   public void setLibgs(String libgs)
   {
      this.libgs = libgs;
   }

   public long getMaxProcessTime()
   {
      return maxProcessTime;
   }

   public void setMaxProcessTime(long millisecs)
   {
      maxProcessTime = millisecs;
   }

   public String applyTextModeMappings(String original, Vector<String> styNames)
   {
      if (!autoEscapeSpChars)
      {
         return original;
      }

      return textModeMappings.applyMappings(original, styNames);
   }

   public String applyMathModeMappings(String original, Vector<String> styNames)
   {
      if (!autoEscapeMathChars)
      {
         return original;
      }

      return mathModeMappings.applyMappings(original, styNames);
   }

   public void saveTextModeMappings(PrintWriter out)
     throws IOException
   {
      textModeMappings.save(out);
   }

   public void loadTextModeMappings(File file)
     throws IOException
   {
      if (file.exists())
      {
         textModeMappings = TextModeMappings.load(resources, file);
      }
      else
      {
         createDefaultTextModeMappings();
      }
   }

   public void createDefaultTextModeMappings()
   {
      textModeMappings = TextModeMappings.createDefaultMappings(resources);
   }

   public void saveMathModeMappings(PrintWriter out)
     throws IOException
   {
      mathModeMappings.save(out);
   }

   public void loadMathModeMappings(File file)
     throws IOException
   {
      if (file.exists())
      {
         mathModeMappings = MathModeMappings.load(resources, file);
      }
      else
      {
         createDefaultMathModeMappings();
      }
   }

   public void createDefaultMathModeMappings()
   {
      mathModeMappings = MathModeMappings.createDefaultMappings(resources);
   }

   public TextModeMappings getTextModeMappings()
   {
      return textModeMappings;
   }

   public MathModeMappings getMathModeMappings()
   {
      return mathModeMappings;
   }

   public int[][] getUnicodeRanges()
   {
      return unicodeRanges;
   }

   public void setUnicodeRanges(int[][] ranges)
   {
      unicodeRanges = ranges;
   }

   public String getUnicodeRangesSpec()
   {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < unicodeRanges.length; i++)
      {
         if (i > 0)
         {
            builder.append(',');
         }

         builder.append(String.format("%X-%X", 
            unicodeRanges[i][0], unicodeRanges[i][1]));
      }

      return builder.toString();
   }

   public void setUnicodeRanges(String specs)
     throws NumberFormatException
   {
      String[] split = specs.split(",");

      int[][] ranges = new int[split.length][2];

      for (int i = 0; i < split.length; i++)
      {
         String[] rangeSplit = split[i].split("-", 2);

         ranges[i][0] = Integer.parseInt(rangeSplit[0], 16);
         ranges[i][1] = Integer.parseInt(rangeSplit[1], 16);
      }

      unicodeRanges = ranges;
   }

   public void setDefaultUnicodeRanges()
   {
      unicodeRanges = new int[][]
      {
        new int[] {0x0020, 0x007E},// Basic Latin
        new int[] {0x20A0, 0x20BA},// Currency Symbols
        new int[] {0x2701, 0x27BF},// Dingbats
        new int[] {0x2460, 0x24FF},// Enclosed alphanumerics
        new int[] {0x2000, 0x206F},// General Punctuation
        new int[] {0x00A0, 0x00FD},// Latin 1 supplement
        new int[] {0x0100, 0x017F},// Latin extended A
        new int[] {0x1E00, 0x1EFF},// Latin extended Additional
        new int[] {0x0180, 0x024F},// Latin extended B
        new int[] {0x2C60, 0x2C7F},// Latin extended C
        new int[] {0x2100, 0x214F},// Letterlike symbols
        new int[] {0x1D400, 0x1D7FF},// Mathematical Alphanumeric
        new int[] {0x2200, 0x22FF},// Mathematical Operators
        new int[] {0x27C0, 0x27EF},// Miscellaneous Mathematical Symbols A
        new int[] {0x2980, 0x29FF},// Miscellaneous Mathematical Symbols B
        new int[] {0x2600, 0x26FF},// Miscellaneous Symbols
        new int[] {0x2300, 0x23F3},// Miscellaneous Symbols
        new int[] {0x2150, 0x2189},// Number forms
        new int[] {0x2070, 0x209C},// Subscripts and superscripts
        new int[] {0x2A00, 0x2AFF}// Supplemental Mathematical Operators
      };
   }

   public static String getConfFontSetting(Font font)
   {
      if (font == null) return "";

      String style = "";

      if (font.isBold())
      {
         style = "bold";
      }

      if (font.isItalic())
      {
         style += "italic";
      }

      if (style.isEmpty())
      {
         style = "plain";
      }

      return font.getName()+"-"+style+"-"+font.getSize();
   }

   public void setRulerFont(String name, int style, int size)
   {
      setRulerFont(new Font(name, style, size));
   }

   public void setRulerFont(String name, int size)
   {
      setRulerFont(name, Font.PLAIN, size);
   }

   public void setRulerFont(Font font)
   {
      rulerFont = font;
   }

   public String getRulerSetting()
   {
      return getConfFontSetting(rulerFont);
   }

   public Font getRulerFont()
   {
      return rulerFont;
   }

   public void setAnnoteFont(String name, int style, int size)
   {
      setAnnoteFont(new Font(name, style, size));
   }

   public void setAnnoteFont(String name, int size)
   {
      setAnnoteFont(name, Font.PLAIN, size);
   }

   public void setAnnoteFont(Font font)
   {
      annoteFont = font;
   }

   public String getAnnoteSetting()
   {
      return getConfFontSetting(annoteFont);
   }

   public Font getAnnoteFont()
   {
      return annoteFont;
   }

   public void setStatusFont(String name, int style, int size)
   {
      setStatusFont(new Font(name, style, size));
   }

   public void setStatusFont(String name, int size)
   {
      setStatusFont(name, Font.PLAIN, size);
   }

   public void setStatusFont(Font font)
   {
      statusFont = font;
   }

   public String getStatusSetting()
   {
      return getConfFontSetting(statusFont);
   }

   public Font getStatusFont()
   {
      return statusFont;
   }

   public int getStatusHeight()
   {
      return statusHeight;
   }

   public void setStatusHeight(int height)
   {
      statusHeight = height;
   }

   public int getStatusPositionWidth()
   {
      return statusPositionWidth;
   }

   public void setStatusPositionWidth(int width)
   {
      statusPositionWidth = width;
   }

   public int getStatusUnitWidth()
   {
      return statusUnitWidth;
   }

   public void setStatusUnitWidth(int width)
   {
      statusUnitWidth = width;
   }

   public int getStatusModifiedWidth()
   {
      return statusModifiedWidth;
   }

   public void setStatusModifiedWidth(int width)
   {
      statusModifiedWidth = width;
   }

   public void setTeXEditorFont(String name, int size)
   {
      texEditorFont = new Font(name, Font.PLAIN, size);
   }

   public Font getTeXEditorFont()
   {
      if (texEditorFont == null)
      {
         setTeXEditorFont("Monospaced", 12);
      }

      return texEditorFont;
   }

   public String getTeXEditorFontName()
   {
      return texEditorFont == null ? "Monospaced" : texEditorFont.getFamily();
   }

   public int getTeXEditorFontSize()
   {
      return texEditorFont == null ? 12 : texEditorFont.getSize();
   }

   public boolean isSyntaxHighlightingOn()
   {
      return syntaxHighlightingEnabled;
   }

   public void setSyntaxHighlighting(boolean enabled)
   {
      syntaxHighlightingEnabled = enabled;
   }

   public Color getCommentHighlight()
   {
      return commentHighlight;
   }

   public void setCommentHighlight(Color color)
   {
      commentHighlight = color;
   }

   public void setCommentHighlight(int rgb)
   {
      setCommentHighlight(new Color(rgb));
   }

   public Color getControlSequenceHighlight()
   {
      return csHighlight;
   }

   public void setControlSequenceHighlight(Color color)
   {
      csHighlight = color;
   }

   public void setControlSequenceHighlight(int rgb)
   {
      setControlSequenceHighlight(new Color(rgb));
   }

   public void setTeXEditorWidth(int width)
   {
      texEditorWidth = width;
   }

   public int getTeXEditorWidth()
   {
      return texEditorWidth;
   }

   public void setTeXEditorHeight(int height)
   {
      texEditorHeight = height;
   }

   public int getTeXEditorHeight()
   {
      return texEditorHeight;
   }

   public Color getVectorizeNotRegion()
   {
      return vectorizeNotRegionColor;
   }

   public void setVectorizeNotRegion(Color col)
   {
      vectorizeNotRegionColor = col;
   }

   public void setVectorizeNotRegion(int rgb)
   {
      vectorizeNotRegionColor = new Color(rgb);
   }

   public Color getVectorizeLine()
   {
      return vectorizeLineColor;
   }

   public void setVectorizeLine(Color col)
   {
      vectorizeLineColor = col;
   }

   public void setVectorizeLine(int rgb)
   {
      vectorizeLineColor = new Color(rgb);
   }

   public Color getVectorizeConnector()
   {
      return vectorizeConnectorColor;
   }

   public void setVectorizeConnector(Color col)
   {
      vectorizeConnectorColor = col;
   }

   public void setVectorizeConnector(int rgb)
   {
      vectorizeConnectorColor = new Color(rgb);
   }

   public Color getVectorizeDrag()
   {
      return vectorizeDragColor;
   }

   public void setVectorizeDrag(Color col)
   {
      vectorizeDragColor = col;
   }

   public void setVectorizeDrag(int rgb)
   {
      vectorizeDragColor = new Color(rgb);
   }

   public Color getVectorizeControlColor()
   {
      return vectorizeControlColor;
   }

   public void setVectorizeControlColor(Color col)
   {
      vectorizeControlColor = col;
   }

   public void setVectorizeControlColor(int rgb)
   {
      vectorizeControlColor = new Color(rgb);
   }

   public int getVectorizeControlSize()
   {
      return vectorizeControlSize;
   }

   public void setVectorizeControlSize(Number size)
   {
      vectorizeControlSize = size.intValue();
   }

   public void setVectorizeControlSize(int size)
   {
      vectorizeControlSize = size;
   }

   public boolean useHPaddingShapepar()
   {
      return shapeparUseHpadding;
   }

   public void setHPaddingShapepar(boolean flag)
   {
      shapeparUseHpadding = flag;
   }

   public boolean useRelativeFontDeclarations()
   {
      return useRelativeFontDeclarations;
   }

   public void setRelativeFontDeclarations(boolean use)
   {
      useRelativeFontDeclarations = use;
   }

   public boolean usePdfInfo()
   {
      return usePdfInfo;
   }

   public void setUsePdfInfoEnabled(boolean enabled)
   {
      usePdfInfo = enabled;
   }

   public void setTextPathExportOutlineSetting(int flag)
   {
      textPathExportOutlineSetting = flag;
   }

   public int getTextPathExportOutlineSetting()
   {
      return textPathExportOutlineSetting;
   }

   public void setTextualExportShadingSetting(int flag)
   {
      textualExportShadingSetting = flag;
   }

   public int getTextualExportShadingSetting()
   {
      return textualExportShadingSetting;
   }

   public String getVerticalToolBarLocation()
   {
      return verticalToolBarLocation;
   }

   public void setVerticalToolBarLocation(String location)
   {
      if (location.equals("West") || location.equals("East"))
      {
         verticalToolBarLocation = location;
      }
      else
      {
         throw new IllegalArgumentException(
            "Invalid vertical toolbar location '"+location+"'");
      }
   }

   public boolean useAbsolutePages()
   {
      return canvasGraphics.useAbsolutePages();
   }

   public void setUseAbsolutePages(boolean flag)
   {
      canvasGraphics.setUseAbsolutePages(flag);
   }

   public String getLookAndFeel()
   {
      return lookAndFeel; 
   }

   public void setLookAndFeel(String name)
   {
      lookAndFeel = name;
   }

   public String getButtonStyle()
   {
      return buttonStyle;
   }

   public void setButtonStyle(String name)
   {
      buttonStyle = name;
   }

   public int getDialogButtonStyle()
   {
      return dialogButtonStyle;
   }

   public void setDialogButtonStyle(int style)
   {
      dialogButtonStyle = style;
   }

   public String getDictId()
   {
      return resources.getDictLocaleId();
   }

   public void setDictId(String id)
   {
      resources.setDictLocaleId(id);
   }

   public String getHelpId()
   {
      return resources.getHelpLocaleId();
   }

   public void setHelpId(String id)
   {
      resources.setHelpLocaleId(id);
   }

   public int getCanvasSplit()
   {
      return canvasSplit;
   }

   public void setCanvasSplit(int split)
   {
      if (split == JSplitPane.HORIZONTAL_SPLIT
       || split == JSplitPane.VERTICAL_SPLIT)
      {
         canvasSplit = split;
         return;
      }

      throw new IllegalArgumentException(
        "Invalid JSplitPane split value "+split);
   }

   public boolean isCanvasFirst()
   {
      return canvasFirst;
   }

   public void setCanvasFirst(boolean isFirst)
   {
      canvasFirst = isFirst;
   }

   public JDRResources getResources()
   {
      return resources;
   }

   public boolean isExportPngEncap()
   {
      return pngexportencap;
   }

   public void setExportPngEncap(boolean isencap)
   {
      pngexportencap = isencap;
   }

   public boolean useExportPngAlpha()
   {
      return pngExportUseAlpha;
   }

   public void setExportPngAlpha(boolean usealpha)
   {
      pngExportUseAlpha = usealpha;
   }

   private CanvasGraphics canvasGraphics;

   public boolean showToolBar=true;
   public boolean showStatus=true;
   private boolean antialias=true, renderquality=true;
   public boolean enableDragScale=false;
   public String startDir;
   public int startDirType=STARTDIR_LAST;
   public static final int STARTDIR_CWD=0, STARTDIR_LAST=1,
      STARTDIR_NAMED=2;

   public int saveJDRsettings=JDRAJR.ALL_SETTINGS;
   public boolean warnOnOldJdr=true;

   public int initSettings=INIT_LAST;

   public static final int INIT_DEFAULT=0, INIT_LAST=1,
      INIT_USER=2;

   private JDRBasicStroke stroke;
   private JDRPaint linePaint, fillPaint, textPaint;

   public String fontFamily="SansSerif";
   private int fontSeries = JDRFont.SERIES_MEDIUM;
   private int fontShape  = JDRFont.SHAPE_UPRIGHT;
   private JDRLength fontSize;

   public String latexFontFamily="\\sffamily";
   public String latexFontSeries="\\mdseries";
   public String latexFontSize="\\normalsize";
   public String latexFontShape="\\upshape";

   public boolean updateLaTeXFonts=true;
   public boolean autoUpdateAnchors=true;
   public boolean autoEscapeSpChars=true;
   public boolean autoEscapeMathChars=true;

   public int pgfHalign = JDRText.PGF_HALIGN_LEFT;
   public int pgfValign = JDRText.PGF_VALIGN_BASE;

   public boolean previewBitmaps;

   private boolean relativeBitmaps = false;

   private String bitmapCommand = "\\includegraphics";

   private boolean pngExportUseAlpha = false;

   private boolean pngexportencap = true;

   public Robot robot=null;

   private int hRulerHeight = 25;
   private int vRulerWidth = 25;

   private RenderingHints renderHints = new RenderingHints(
         RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_OFF);

   private DecimalFormat rulerFormat;

   private Locale rulerLocale;

   private String latexApp = null;
   private String pdflatexApp = null;
   private String dvipsApp = null;
   private String dvisvgmApp = null;

   private String libgs = null;

   private long maxProcessTime = 300000L;

   private TextModeMappings textModeMappings;

   private MathModeMappings mathModeMappings;

   private int[][] unicodeRanges;

   private Font texEditorFont = null;

   private Font rulerFont = null;

   private Font annoteFont = null;

   private Font statusFont = new Font("Dialog", Font.PLAIN, 10);

   private int statusHeight=0, statusPositionWidth=0,
               statusUnitWidth=0, statusModifiedWidth=0;

   private boolean syntaxHighlightingEnabled = true;

   private Color commentHighlight = Color.GRAY;
   private Color csHighlight = Color.BLUE;

   private int texEditorWidth = 8, texEditorHeight = 10;
   private int preambleEditorWidth = 0, preambleEditorHeight = 0;

   private Color vectorizeNotRegionColor = new Color(204, 204, 204, 220);
   private Color vectorizeLineColor = Color.RED;
   private Color vectorizeConnectorColor = Color.CYAN;
   private Color vectorizeDragColor = Color.MAGENTA;
   private Color vectorizeControlColor = Color.ORANGE;
   private int vectorizeControlSize = 4;

   private boolean shapeparUseHpadding = false;

   private boolean useRelativeFontDeclarations = true;

   private boolean usePdfInfo = false;

   private int textPathExportOutlineSetting
      = TeX.TEXTPATH_EXPORT_OUTLINE_TO_PATH;

   private int textualExportShadingSetting
      = TeX.TEXTUAL_EXPORT_SHADING_AVERAGE;

   private String verticalToolBarLocation = "West";

   private String lookAndFeel = null;

   private int canvasSplit = JSplitPane.HORIZONTAL_SPLIT;

   private boolean canvasFirst = true;

   private String buttonStyle = "default";
   private int dialogButtonStyle;

   public File configFile = null;

   public boolean showStatusZoom = true;
   public boolean showStatusPosition = true;
   public boolean showStatusModified = true;
   public boolean showStatusLock = true;
   public boolean showStatusUnit = true;
   public boolean showStatusInfo = true;
   public boolean showStatusHelp = true;

   public boolean useTypeblockAsBoundingBox = false;

   public boolean canvasClickExitsPathEdit = false;

   public boolean selectControlIgnoresLock = false;

   private JDRResources resources;
}

