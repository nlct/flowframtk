// File          : FlowframTkInvoker.java
// Purpose       : Drawing application
// Creation Date : 1st February 2006
// Renamed 2014-03-26: JpgfDraw.java -> FlowframTk.java
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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

import java.io.*;
import java.util.*;
import java.text.*;
import java.net.*;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.InvalidSyntaxException;
import com.dickimawbooks.texjavahelplib.CLISyntaxParser;
import com.dickimawbooks.texjavahelplib.CLIArgValue;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.dialog.WelcomeDialog;

public class FlowframTkInvoker
{
   public FlowframTkInvoker(String[] args)
     throws IOException,URISyntaxException,InvalidFormatException
   {
      this.args = args;
      this.resources = new JDRResources(APP_NAME);
      this.resources.initialise();
      this.filenames = new Vector<String>();
   }

   public void debugMessage(String message)
   {
      resources.debugMessage(message);
   }

   public void debugMessage(Throwable e)
   {
      resources.debugMessage(e);
   }

   /**
    * Prints the version information to STDERR.
    */
   public void versionInfo()
   {
      System.out.println(resources.getAppInfo(false));

      System.exit(0);
   }

   /**
    * Prints the command line syntax to STDERR and quits application.
    */
   public void syntax()
   {
      TeXJavaHelpLib helpLib = resources.getHelpLib();

      System.out.println(
         helpLib.getMessageWithFallback(
        "about.version_date",
        "{0} version {1} ({2})",
         APP_NAME, JDRResources.APP_VERSION, JDRResources.APP_DATE));

      System.out.println();

      System.out.println(helpLib.getMessage("clisyntax.usage",
         helpLib.getMessage("syntax.options", APP_NAME.toLowerCase())));

      System.out.println();

      helpLib.printSyntaxItem(helpLib.getMessage("syntax.settings_info"));

      System.out.println();

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.show_grid", "--[no]show_grid"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.grid_lock", "--[no]grid_lock"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.toolbar", "--[no]toolbar"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.statusbar", "--[no]statusbar"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.rulers", "--[no]rulers"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.paper", "--paper"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.disable_print", "--[no]disable_print"));

      System.out.println();

      helpLib.printSyntaxItem(helpLib.getMessage("syntax.other_options"));

      System.out.println();

      helpLib.printSyntaxItem(
         helpLib.getMessage("clisyntax.help2", "--help", "-h"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("clisyntax.version2", "--version", "-v"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.debug", "--[no]debug"));

      helpLib.printSyntaxItem(
         helpLib.getMessage("syntax.experimental", "--[no]experimental"));

      System.out.println();
      System.out.println(helpLib.getMessage("clisyntax.bugreport",
        "https://github.com/nlct/flowframtk"));

      System.exit(0);
   }

   private JDRAngle parseAngle(String text, int lineNum)
     throws InvalidFormatException
   {
      try
      {
         return new JDRAngle(resources.getMessageDictionary(),
           Double.parseDouble(text), JDRAngle.RADIAN);
      }
      catch (NumberFormatException e)
      {
      }

      try
      {
         return JDRAngle.parse(resources.getMessageDictionary(), text);
      }
      catch (InvalidFormatException e)
      {
         e.setLineNum(lineNum);
         throw e;
      }
   }

   private JDRLength parseLength(String text, int lineNum)
     throws InvalidFormatException
   {
      try
      {
         return new JDRLength(resources.getMessageDictionary(),
            Double.parseDouble(text), JDRUnit.bp);
      }
      catch (NumberFormatException e)
      {
      }

      try
      {
         return JDRLength.parse(resources.getMessageDictionary(), text);
      }
      catch (InvalidFormatException e)
      {
         e.setLineNum(lineNum);
         throw e;
      }
   }

   private JDRLength parseNonNegLength(String text, int lineNum)
     throws InvalidFormatException
   {
      JDRLength length = parseLength(text, lineNum);

      if (length.getValue() < 0)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.no_negative", lineNum), text, lineNum);
      }

      return length;
   }

   private boolean parseBoolean(String text, int lineNum)
     throws InvalidFormatException
   {
      int i;

      try
      {
         i = Integer.parseInt(text);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.boolean_value", lineNum), text, lineNum, e);
      }
   
      if (i < 0 || i > 1)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.boolean_value", lineNum), text, lineNum);
      }
   
      return (i == 1);
   }

   private int parseThreeVal(String text, int lineNum)
     throws InvalidFormatException
   {
      int i;

      try
      {
         i = Integer.parseInt(text);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.three_value", lineNum), text, lineNum, e);
      }
   
      if (i < 0 || i > 2)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.three_value", lineNum), text, lineNum);
      }

      return i;
   }

   public Color parseColor(String value, int lineNum)
     throws InvalidFormatException
   {
      String[] split = value.split(",", 2);

      try
      {
         int rgb = Integer.parseInt(split[0]);
         Color col = new Color(rgb);

         if (split.length > 1)
         {
            int alpha = Integer.parseInt(split[1]);

            int r = col.getRed();
            int g = col.getGreen();
            int b = col.getBlue();

            col = new Color(r, g, b, alpha);
         }

         return col;
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.color", lineNum), value, lineNum, e);
      }
   }

   public void writeColor(PrintWriter out, String key, Color col)
   {
      int alpha = col.getAlpha();

      out.format("%s=%d", key, col.getRGB());

      if (alpha != 255)
      {
         out.format(",%d", alpha);
      }

      out.println();
   }

   public int parseInt(String value, int lineNum)
     throws InvalidFormatException
   {
      try
      {
         return Integer.parseInt(value);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.integer", lineNum), value, lineNum, e);
      }
   }

   public int parseNonNegInt(String value, int lineNum)
     throws InvalidFormatException
   {
      int num = parseInt(value, lineNum);

      if (num < 0)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.no_negative", lineNum), value, lineNum);
      }

      return num;
   }

   public int parseNonNegInt(int maxVal, String value, int lineNum)
     throws InvalidFormatException
   {
      int i = parseInt(value, lineNum);

      if (i < 0 || i > maxVal)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.bounded_integer", maxVal, lineNum), value, lineNum);
      }

      return i;
   }

   public int parseMinInt(int minVal, String value, int lineNum)
     throws InvalidFormatException
   {
      int i = parseInt(value, lineNum);

      if (i < minVal)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
              minVal == 1 ?
               "error.conf.positive_only" :
               "error.conf.number_badrange", lineNum), value, lineNum);
      }

      return i;
   }

   public long parseLong(String value, int lineNum)
     throws InvalidFormatException
   {
      try
      {
         return Long.parseLong(value);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.integer", lineNum), value, lineNum, e);
      }
   }

   public double parseDouble(String value, int lineNum)
     throws InvalidFormatException
   {
      try
      {
         return Double.parseDouble(value);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.number", lineNum), value, lineNum, e);
      }
   
   }

   public double parseNonNegDouble(String value, int lineNum)
     throws InvalidFormatException
   {
      double num;

      try
      {
         num = Double.parseDouble(value);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.number", lineNum), value, lineNum, e);
      }
   
      if (num < 0)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.no_negative", lineNum), value, lineNum);
      }
   
      return num;
   }

   public double parseMinDouble(double minValue, String value, int lineNum)
     throws InvalidFormatException
   {
      double num;

      try
      {
         num = Double.parseDouble(value);
      }
      catch (NumberFormatException e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.number", lineNum), value, lineNum, e);
      }
   
      if (num < minValue)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.number_badrange", lineNum), value, lineNum);
      }
   
      return num;
   }

   public Path2D parsePath(String value, int lineNum)
     throws InvalidConfigValueException
   {
      Path2D path;
      int type;

      StringTokenizer st = new StringTokenizer(value, " ");

      if (!st.hasMoreTokens())
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.invalid_path", lineNum), value, lineNum);
      }

      String token = st.nextToken();

      try
      {
         path = new Path2D.Double(Integer.parseInt(token));
      }
      catch (Exception e)
      {
         throw new InvalidConfigValueException(
            resources.getMessage(
               "error.conf.invalid_path.rule", lineNum), value, lineNum, e);
      }

      while (st.hasMoreTokens())
      {
         token = st.nextToken();

         try
         {
            type = Integer.parseInt(token);
         }
         catch (NumberFormatException e)
         {
            throw new InvalidConfigValueException(
              resources.getMessage(
                "error.conf.invalid_path.segment", 
                token, lineNum), value, lineNum, e);
         }

         switch (type)
         {
            case PathIterator.SEG_MOVETO:

              try
              {
                 path.moveTo(Double.parseDouble(st.nextToken()),
                             Double.parseDouble(st.nextToken()));
              }
              catch (Exception e)
              {
                 throw new InvalidConfigValueException(
                  resources.getMessage(
                   "error.conf.invalid_path.coord", type, lineNum),
                    value, lineNum, e);
              }

            break;
            case PathIterator.SEG_LINETO:

              try
              {
                 path.lineTo(Double.parseDouble(st.nextToken()),
                             Double.parseDouble(st.nextToken()));
              }
              catch (Exception e)
              {
                 throw new InvalidConfigValueException(
                  resources.getMessage(
                   "error.conf.invalid_path.coord", type, lineNum),
                    value, lineNum, e);
              }

            break;
            case PathIterator.SEG_QUADTO:

              try
              {
                 path.quadTo(Double.parseDouble(st.nextToken()),
                             Double.parseDouble(st.nextToken()),
                             Double.parseDouble(st.nextToken()),
                             Double.parseDouble(st.nextToken()));
              }
              catch (Exception e)
              {
                 throw new InvalidConfigValueException(
                  resources.getMessage(
                   "error.conf.invalid_path.coord", type, lineNum),
                    value, lineNum, e);
              }

            break;
            case PathIterator.SEG_CUBICTO:

              try
              {
                 path.curveTo(Double.parseDouble(st.nextToken()),
                              Double.parseDouble(st.nextToken()),
                              Double.parseDouble(st.nextToken()),
                              Double.parseDouble(st.nextToken()),
                              Double.parseDouble(st.nextToken()),
                              Double.parseDouble(st.nextToken()));
              }
              catch (Exception e)
              {
                 throw new InvalidConfigValueException(
                  resources.getMessage(
                   "error.conf.invalid_path.coord", type, lineNum),
                    value, lineNum, e);
              }

            break;
            case PathIterator.SEG_CLOSE:
              path.closePath();
            break;
            default:
            throw new InvalidConfigValueException(
              resources.getMessage(
                "error.conf.invalid_path.segment", token, lineNum),
                 value, lineNum);
         }
      }

      return path;
   }

   public void writePath(PrintWriter writer, Shape shape)
    throws IOException
   {
      PathIterator pi = shape.getPathIterator(null);

      double[] coords = new double[6];

      writer.print(pi.getWindingRule());

      while (!pi.isDone())
      {
         int type = pi.currentSegment(coords);

         writer.print(" ");
         writer.print(type);

         switch (type)
         {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
               writer.print(" ");
               writer.print(coords[0]);
               writer.print(" ");
               writer.print(coords[1]);
            break;
            case PathIterator.SEG_QUADTO:
               for (int i = 0; i < 4; i++)
               {
                  writer.print(" ");
                  writer.print(coords[i]);
               }
            break;
            case PathIterator.SEG_CUBICTO:
               for (int i = 0; i < 6; i++)
               {
                  writer.print(" ");
                  writer.print(coords[i]);
               }
            break;
         }

         pi.next();
      }
   }

   public boolean loadConfig()
      throws IOException,InvalidFormatException
   {
      return loadConfig(settings.configFile);
   }

   public boolean loadConfig(File file)
      throws IOException,InvalidFormatException
   {
      // Reads in settings from config file, only sets the settings
      // that are specified in file.

      BufferedReader in = new BufferedReader(
         new FileReader(file));

      settings.configFile = file;

      ExportSettings exportSettings = settings.getExportSettings();
      ImportSettings importSettings = settings.getImportSettings();

      setStartupInfo(file.toString());
      debugMessage("reading: "+file);

      String s;
      int line = 0;
      boolean doneRobot=false;
      boolean diffVersion=true;

      JDRMarker startComposite = null;
      JDRMarker midComposite   = null;
      JDRMarker endComposite   = null;

      boolean antiAlias = settings.isAntiAliasOn();
      boolean renderQuality = settings.isRenderQualityOn();

      float[] dashPattern = null;
      float dashOffset = 0f;

      int majorDivisions = 100;
      int subDivisions = 10;
      int spokes = 8;
      int gridType = JDRGrid.GRID_RECTANGULAR;

      JDRUnit gridUnit = JDRUnit.bp;

      Path2D gridPath = null;

      String texEditorFont = "Monospaced";
      int texEditorFontSize = 12;

      String annoteFontName = "SansSerif";
      int annoteFontSize = 10;

      Font annoteFont = null;

      // Keep track of error messages (don't abort for invalid
      // formats or unknown keys)

      StringBuilder messages = null;

      boolean foundFlowFramv2_0 = false;

      try
      {
         while ((s=in.readLine()) != null)
         {
            line++;
            if (s.charAt(0) == '#') continue;
   
            try
            {
               StringTokenizer t = new StringTokenizer(s, "=");
      
               if (!t.hasMoreTokens())
               {
                  throw new InvalidFormatException(
                     resources.getMessage(
                        "error.invalid_format", line));
               }
      
               String key = t.nextToken();
      
               String value = "";
      
               if (t.hasMoreTokens())
               {
                  value = t.nextToken();
               }
      
               if (key.equals("initsettings"))
               {
                  settings.initSettings= parseThreeVal(value, line);
      
                  if (settings.initSettings
                         == FlowframTkSettings.INIT_DEFAULT)
                  {
                     break;
                  }
      
               }
               else if (key.equals("version"))
               {
                  diffVersion = !value.equals(JDRResources.APP_VERSION);
               }
               else if (key.equals("dict_lang"))
               {
                  // dict_lang and help_lang now in languages.conf
                  // Too late to do anything about it now but save
                  // for restart
   
                  resources.setDictionary(value);
               }
               else if (key.equals("help_lang"))
               {
                  resources.setHelpSet(value);
               }
               else if (key.equals("preview_bitmaps"))
               {
                  settings.previewBitmaps = parseBoolean(value, line);
               }
               else if (key.equals("show_grid"))
               {
                  settings.setDisplayGrid(parseBoolean(value, line));
               }
               else if (key.equals("lock_grid"))
               {
                  settings.setGridLock(parseBoolean(value, line));
               }
               else if (key.equals("show_tools"))
               {
                  settings.setShowToolBar(parseBoolean(value, line));
               }
               else if (key.equals("show_rulers"))
               {
                  settings.setShowRulers(parseBoolean(value, line));
               }
               else if (key.equals("show_status"))
               {
                  settings.showStatus = parseBoolean(value, line);
               }
               else if (key.equals("status_show_zoom"))
               {
                  settings.showStatusZoom = parseBoolean(value, line);
               }
               else if (key.equals("status_show_pos"))
               {
                  settings.showStatusPosition = parseBoolean(value, line);
               }
               else if (key.equals("status_show_mod"))
               {
                  settings.showStatusModified = parseBoolean(value, line);
               }
               else if (key.equals("status_show_lock"))
               {
                  settings.showStatusLock = parseBoolean(value, line);
               }
               else if (key.equals("status_show_unit"))
               {
                  settings.showStatusUnit = parseBoolean(value, line);
               }
               else if (key.equals("status_show_info"))
               {
                  settings.showStatusInfo = parseBoolean(value, line);
               }
               else if (key.equals("status_show_help"))
               {
                  settings.showStatusHelp = parseBoolean(value, line);
               }
               else if (key.equals("dragscale"))
               {
                  settings.enableDragScale = parseBoolean(value, line);
               }
               else if (key.equals("canvasclickexitspathedit"))
               {
                  settings.canvasClickExitsPathEdit = parseBoolean(value, line);
               }
               else if (key.equals("selectcontrolignoreslock"))
               {
                  settings.selectControlIgnoresLock = parseBoolean(value, line);
               }
               else if (key.equals("antialias"))
               {
                  antiAlias = parseBoolean(value, line);
               }
               else if (key.equals("render_quality"))
               {
                  renderQuality = parseBoolean(value, line);
               }
               else if (key.equals("startdir_type"))
               {
                  settings.startDirType = parseThreeVal(value, line);
               }
               else if (key.equals("startdir"))
               {
                  settings.startDir=value;
               }
               else if (key.equals("normalsize"))
               {
                  settings.setLaTeXNormalSize(parseNonNegInt(value, line));
               }
               else if (key.equals("latexfontupdate"))
               {
                  settings.updateLaTeXFonts = parseBoolean(value, line);
               }
               else if (key.equals("autoupdateanchors"))
               {
                  settings.autoUpdateAnchors = parseBoolean(value, line);
               }
               else if (key.equals("autoescapespchars"))
               {
                  settings.autoEscapeSpChars = parseBoolean(value, line);
               }
               else if (key.equals("save_jdrsettings"))
               {
                  settings.setSaveSettings(parseThreeVal(value, line));
               }
               else if (key.equals("use_jdrsettings"))
               {
                  settings.setUseSettingsOnLoad(parseThreeVal(value, line));
               }
               else if (key.equals("warn_load_old"))
               {
                  settings.warnOnOldJdr = parseBoolean(value, line);
               }
               else if (key.equals("tool"))
               {
                  settings.setTool(value);
               }
               else if (key.equals("paper"))
               {
                  StringTokenizer pt = new StringTokenizer(value, " ");
      
                  String pvalue = pt.nextToken();
      
                  if (pvalue.equals("user"))
                  {
                     try
                     {
                        pvalue = pt.nextToken();
                        JDRLength w = parseNonNegLength(pvalue, line);
   
                        pvalue = pt.nextToken();
                        JDRLength h = parseNonNegLength(pvalue, line);
   
                        settings.setPaper(new JDRPaper(w, h));
                     }
                     catch (Exception e)
                     {
                        throw new InvalidFormatException(
                           resources.getMessage(
                              "error.invalid_paper_dimension", value));
                     }
                  }
                  else
                  {
                     JDRPaper paper=JDRPaper.getPredefinedPaper(value);
   
                     if (paper != null)
                     {
                        settings.setPaper(paper);
                     }
                     else
                     {
                        throw new InvalidFormatException(
                          resources.getMessage(
                             "error.unknown_papersize", value));
                     }
                  }
               }
               else if (key.equals("fontname"))
               {
                  settings.fontFamily=value;
               }
               else if (key.equals("fontsize"))
               {
                  settings.setFontSize(parseNonNegLength(value, line));
               }
               else if (key.equals("fontshape"))
               {
                  settings.setFontShape(
                     parseNonNegInt(JDRFont.MAX_SHAPE_ID, value, line));
               }
               else if (key.equals("fontseries"))
               {
                  settings.setFontSeries(
                     parseNonNegInt(JDRFont.MAX_SERIES_ID, value, line));
               }
               else if (key.equals("latexfontname"))
               {
                  settings.latexFontFamily=value;
               }
               else if (key.equals("latexfontseries"))
               {
                  settings.latexFontSeries=value;
               }
               else if (key.equals("latexfontshape"))
               {
                  settings.latexFontShape=value;
               }
               else if (key.equals("latexfontsize"))
               {
                  settings.latexFontSize=value;
               }
               else if (key.equals("capstyle"))
               {
                  settings.getStroke().setCapStyle(parseThreeVal(value, line));
               }
               else if (key.equals("joinstyle"))
               {
                  settings.getStroke().setJoinStyle(parseThreeVal(value, line));
               }
               else if (key.equals("windingrule"))
               {
                  settings.getStroke().setWindingRule(
                     parseBoolean(value, line) ? 1 : 0);
               }
               else if (key.equals("penwidth"))
               {
                  settings.getStroke().setPenWidth(
                     parseNonNegLength(value, line));
               }
               else if (key.equals("mitrelimit"))
               {
                  settings.getStroke().setMitreLimit(
                    parseMinDouble(1.0, value, line));
               }
               else if (key.equals("startarrow"))
               {
                  settings.getStroke().setStartArrow(
                     parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("startarrowsize"))
               {
                  settings.getStroke().setStartArrowSize(
                     parseNonNegLength(value, line));
               }
               else if (key.equals("startarrowwidth"))
               {
                  settings.getStroke().setStartArrowWidth(
                     parseNonNegLength(value, line));
               }
               else if (key.equals("startarrowrepeat"))
               {
                  settings.getStroke().setStartArrowRepeat(
                     parseMinInt(1, value, line));
               }
               else if (key.equals("startarrowdouble"))
               {
                  // provide compatibility with older versions
   
                  settings.getStroke().setStartArrowRepeat(
                     parseBoolean(value, line) ? 2 : 1);
               }
               else if (key.equals("startarrowreverse"))
               {
                  settings.getStroke().setStartArrowReverse(
                     parseBoolean(value, line));
               }
               else if (key.equals("startarroworient"))
               {
                  settings.getStroke().setStartArrowAutoOrient(
                     parseBoolean(value, line));
               }
               else if (key.equals("startarrowuseroffsetenabled"))
               {
                  settings.getStroke().setStartUserOffsetEnabled(
                    parseBoolean(value, line));
               }
               else if (key.equals("startoverlaid"))
               {
                  settings.getStroke().setStartOverlay(
                     parseBoolean(value, line));
               }
               else if (key.equals("startarrowuseroffset"))
               {
                  settings.getStroke().setStartOffset(
                    parseLength(value, line));
               }
               else if (key.equals("startarrowrepeatoffsetenabled"))
               {
                  settings.getStroke().setStartRepeatOffsetEnabled(
                     parseBoolean(value, line));
               }
               else if (key.equals("startarrowrepeatoffset"))
               {
                  settings.getStroke().setStartRepeatOffset(
                     parseLength(value, line));
               }
               else if (key.equals("startarrowpaint"))
               {
                  settings.getStroke().setStartArrowColour(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("startarrowangle"))
               {
                  settings.getStroke().setStartArrowAngle(
                     parseAngle(value, line));
               }
               else if (key.equals("secondarystartarrow"))
               {
                  startComposite = JDRMarker.getPredefinedMarker(
                     settings.getCanvasGraphics(),
                     parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("secondarystartarrowsize"))
               {
                  startComposite.setSize(parseNonNegLength(value, line));
               }
               else if (key.equals("secondarystartarrowwidth"))
               {
                  startComposite.setWidth(parseNonNegLength(value, line));
               }
               else if (key.equals("secondarystartarrowrepeat"))
               {
                  startComposite.setRepeated(parseMinInt(1, value, line));
               }
               else if (key.equals("secondarystartarrowreverse"))
               {
                  startComposite.setReversed(parseBoolean(value, line));
               }
               else if (key.equals("secondarystartarroworient"))
               {
                  startComposite.setOrient(parseBoolean(value, line));
               }
               else if (key.equals("secondarystartarrowuseroffsetenabled"))
               {
                  startComposite.enableUserOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondarystartarrowuseroffset"))
               {
                  startComposite.setOffset(parseLength(value, line));
               }
               else if (key.equals("secondarystartarrowrepeatoffsetenabled"))
               {
                  startComposite.enableUserRepeatOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondarystartarrowrepeatoffset"))
               {
                  startComposite.setRepeatOffset(parseLength(value, line));
               }
               else if (key.equals("secondarystartarrowpaint"))
               {
                  startComposite.setFillPaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("secondarystartarrowangle"))
               {
                  startComposite.setAngle(parseAngle(value, line));
               }
               else if (key.equals("midarrow"))
               {
                  settings.getStroke().setMidArrow(
                    parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("midarrowsize"))
               {
                  settings.getStroke().setMidArrowSize(
                    parseNonNegLength(value, line));
               }
               else if (key.equals("midarrowwidth"))
               {
                  settings.getStroke().setMidArrowWidth(
                    parseNonNegLength(value, line));
               }
               else if (key.equals("midarrowrepeat"))
               {
                  settings.getStroke().setMidArrowRepeat(
                     parseMinInt(1, value, line));
               }
               else if (key.equals("midarrowreverse"))
               {
                  settings.getStroke().setMidArrowReverse(
                     parseBoolean(value, line));
               }
               else if (key.equals("midarroworient"))
               {
                  settings.getStroke().setMidArrowAutoOrient(
                     parseBoolean(value, line));
               }
               else if (key.equals("midarrowuseroffsetenabled"))
               {
                  settings.getStroke().setMidUserOffsetEnabled(
                    parseBoolean(value, line));
               }
               else if (key.equals("midoverlaid"))
               {
                  settings.getStroke().setMidOverlay(
                    parseBoolean(value, line));
               }
               else if (key.equals("midarrowuseroffset"))
               {
                  settings.getStroke().setMidOffset(parseLength(value, line));
               }
               else if (key.equals("midarrowrepeatoffsetenabled"))
               {
                  settings.getStroke().setMidRepeatOffsetEnabled(
                     parseBoolean(value, line));
               }
               else if (key.equals("midarrowrepeatoffset"))
               {
                  settings.getStroke().setMidRepeatOffset(
                     parseLength(value, line));
               }
               else if (key.equals("midarrowpaint"))
               {
                  settings.getStroke().setMidArrowColour(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("midarrowangle"))
               {
                  settings.getStroke().setMidArrowAngle(
                     parseAngle(value, line));
               }
               else if (key.equals("secondarymidarrow"))
               {
                  midComposite = JDRMarker.getPredefinedMarker(
                    settings.getCanvasGraphics(),
                    parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("secondarymidarrowsize"))
               {
                  midComposite.setSize(parseNonNegLength(value, line));
               }
               else if (key.equals("secondarymidarrowwidth"))
               {
                  midComposite.setWidth(parseNonNegLength(value, line));
               }
               else if (key.equals("secondarymidarrowrepeat"))
               {
                  midComposite.setRepeated(parseMinInt(1, value, line));
               }
               else if (key.equals("secondarymidarrowreverse"))
               {
                  midComposite.setReversed(parseBoolean(value, line));
               }
               else if (key.equals("secondarymidarroworient"))
               {
                  midComposite.setOrient(parseBoolean(value, line));
               }
               else if (key.equals("secondarymidarrowuseroffsetenabled"))
               {
                  midComposite.enableUserOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondarymidarrowuseroffset"))
               {
                  midComposite.setOffset(parseLength(value, line));
               }
               else if (key.equals("secondarymidarrowrepeatoffsetenabled"))
               {
                  midComposite.enableUserRepeatOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondarymidarrowrepeatoffset"))
               {
                  midComposite.setRepeatOffset(parseLength(value, line));
               }
               else if (key.equals("secondarymidarrowpaint"))
               {
                  midComposite.setFillPaint(
                    loadPaintConfig(settings, value,line));
               }
               else if (key.equals("secondarymidarrowangle"))
               {
                  midComposite.setAngle(parseAngle(value, line));
               }
               else if (key.equals("endarrow"))
               {
                  settings.getStroke().setEndArrow(
                     parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("endarrowsize"))
               {
                  settings.getStroke().setEndArrowSize(
                     parseNonNegLength(value, line));
               }
               else if (key.equals("endarrowwidth"))
               {
                  settings.getStroke().setEndArrowWidth(
                     parseNonNegLength(value, line));
               }
               else if (key.equals("endarrowrepeat"))
               {
                  settings.getStroke().setEndArrowRepeat(
                     parseMinInt(1, value, line));
               }
               else if (key.equals("endarrowdouble"))
               {
                  // provide compatibility with older versions
   
                  settings.getStroke().setEndArrowRepeat(
                     parseBoolean(value, line) ? 2 : 1);
               }
               else if (key.equals("endarrowreverse"))
               {
                  settings.getStroke().setEndArrowReverse(
                     parseBoolean(value, line));
               }
               else if (key.equals("endarroworient"))
               {
                  settings.getStroke().setEndArrowAutoOrient(
                     parseBoolean(value, line));
               }
               else if (key.equals("endarrowuseroffsetenabled"))
               {
                  settings.getStroke().setEndUserOffsetEnabled(
                     parseBoolean(value, line));
               }
               else if (key.equals("endoverlaid"))
               {
                  settings.getStroke().setEndOverlay(parseBoolean(value, line));
               }
               else if (key.equals("endarrowuseroffset"))
               {
                  settings.getStroke().setEndOffset(
                    parseLength(value, line));
               }
               else if (key.equals("endarrowrepeatoffsetenabled"))
               {
                  settings.getStroke().setEndRepeatOffsetEnabled(
                     parseBoolean(value, line));
               }
               else if (key.equals("endarrowrepeatoffset"))
               {
                  settings.getStroke().setEndRepeatOffset(
                    parseLength(value, line));
               }
               else if (key.equals("endarrowpaint"))
               {
                  settings.getStroke().setEndArrowColour(
                     loadPaintConfig(settings, value,line));
               }
               else if (key.equals("endarrowangle"))
               {
                  settings.getStroke().setEndArrowAngle(
                     parseAngle(value, line));
               }
               else if (key.equals("secondaryendarrow"))
               {
                  endComposite = JDRMarker.getPredefinedMarker(
                    settings.getCanvasGraphics(),
                    parseNonNegInt(JDRMarker.maxMarkers(), value, line));
               }
               else if (key.equals("secondaryendarrowsize"))
               {
                  endComposite.setSize(parseNonNegLength(value, line));
               }
               else if (key.equals("secondaryendarrowwidth"))
               {
                  endComposite.setWidth(parseNonNegLength(value, line));
               }
               else if (key.equals("secondaryendarrowrepeat"))
               {
                  endComposite.setRepeated(parseMinInt(1, value, line));
               }
               else if (key.equals("secondaryendarrowreverse"))
               {
                  endComposite.setReversed(parseBoolean(value, line));
               }
               else if (key.equals("secondaryendarroworient"))
               {
                  endComposite.setOrient(parseBoolean(value, line));
               }
               else if (key.equals("secondaryendarrowuseroffsetenabled"))
               {
                  endComposite.enableUserOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondaryendarrowuseroffset"))
               {
                  endComposite.setOffset(parseLength(value, line));
               }
               else if (key.equals("secondaryendarrowrepeatoffsetenabled"))
               {
                  endComposite.enableUserRepeatOffset(parseBoolean(value, line));
               }
               else if (key.equals("secondaryendarrowrepeatoffset"))
               {
                  endComposite.setRepeatOffset(parseLength(value, line));
               }
               else if (key.equals("secondaryendarrowpaint"))
               {
                  endComposite.setFillPaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("secondaryendarrowangle"))
               {
                  endComposite.setAngle(parseAngle(value, line));
               }
               else if (key.equals("dashoffset"))
               {
                  dashOffset = (float)parseNonNegDouble(value, line);
               }
               else if (key.equals("dash"))
               {
                  t = new StringTokenizer(value, ",");
      
                  value = t.nextToken();
      
                  int n = parseNonNegInt(value, line);
      
                  if (n == 0)
                  {
                     dashPattern = null;
                  }
                  else
                  {
                     dashPattern = new float[n];
      
                     value = "";
      
                     for (int j = 0; j < n; j++)
                     {
                        if (!t.hasMoreTokens())
                        {
                           throw new InvalidFormatException(
                              resources.getMessage(
                           "error.conf.no_more_tokens", line));
                        }
   
                        value = t.nextToken();
      
                        dashPattern[j] = (float)parseNonNegDouble(value, line);
                     }
      
                     if (t.hasMoreTokens())
                     {
                        throw new InvalidFormatException(
                           resources.getMessage(
                           "error.conf.too_many_tokens", line));
                     }
                  }
               }
               else if (key.equals("linepaint"))
               {
                  settings.setLinePaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("shapefillpaint") || key.equals("fillpaint"))
               {
                  settings.setShapeFillPaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("outlinefillpaint"))
               {
                  settings.setOutlineFillPaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("textpaint"))
               {
                  settings.setTextPaint(
                     loadPaintConfig(settings, value, line));
               }
               else if (key.equals("storageunit"))
               {
                  try
                  {
                     settings.getCanvasGraphics().setStorageUnit(
                        (byte)parseInt(value, line));
                  }
                  catch (JdrIllegalArgumentException e)
                  {
                     throw new InvalidFormatException(
                         resources.getMessage(
                          "error.conf.number_badrange", line));
                  }
   
               }
               else if (key.equals("gridunit"))
               {
                  gridUnit = JDRUnit.getUnit((byte)parseInt(value, line));
   
                  if (gridUnit == null)
                  {
                     throw new InvalidFormatException(
                         resources.getMessage(
                          "error.conf.number_badrange", line));
                  }
               }
               else if (key.equals("grid"))
               {
                  gridType = parseNonNegInt(4, value, line);
               }
               else if (key.equals("majordivisions"))
               {
                  majorDivisions = parseMinInt(1, value, line);
               }
               else if (key.equals("subdivisions"))
               {
                  subDivisions = parseNonNegInt(value, line);
               }
               else if (key.equals("spokes"))
               {
                  spokes = parseMinInt(1, value, line);
               }
               else if (key.equals("grid-path"))
               {
                  gridPath = parsePath(value, line);
               }
               else if (key.equals("controlsize"))
               {
                  settings.setPointSize(parseNonNegLength(value, line));
               }
               else if (key.equals("scalecontrols"))
               {
                  settings.setScaleControlPoints(parseBoolean(value, line));
               }
               else if (key.equals("widest_char"))
               {
                  CanvasTextField.widestChar = value;
               }
               else if (key.equals("norm_x"))
               {
                  CanvasGraphics.normTransformX = parseDouble(value, line);
               }
               else if (key.equals("norm_y"))
               {
                  CanvasGraphics.normTransformY = parseDouble(value, line);
               }
               else if (key.equals("hruler_height"))
               {
                  settings.setHRulerHeight(parseMinInt(1, value, line));
               }
               else if (key.equals("vruler_width"))
               {
                  settings.setVRulerWidth(parseMinInt(1, value, line));
               }
               else if (key.equals("robot"))
               {
                  if (parseBoolean(value, line))
                  {
                     try
                     {
                        settings.robot = new Robot();
                     }
                     catch (AWTException awte)
                     {
                        resources.warning(null, new String[] {
                           resources.getMessage("warning.no_robot"),
                           awte.getMessage()});
                        settings.robot = null;
                     }
                     catch (SecurityException sexc)
                     {
                        resources.warning(null, new String[] {
                           resources.getMessage("warning.no_robot"),
                           sexc.getMessage()});
                        settings.robot = null;
                     }
                  }
                  else
                  {
                     settings.robot=null;
                  }
      
                  doneRobot=true;
               }
               else if (key.equals("ruler_format"))
               {
                  String[] split = value.split("\t");
   
                  if (split.length < 2)
                  {
                     throw new InvalidFormatException(
                       resources.getMessage(
                          "error.invalid_ruler_pattern", value));
                  }
   
                  settings.setRulerFormat(split[0], 
                     Locale.forLanguageTag(split[1]));
               }
               else if (key.equals("ruler_font"))
               {
                  settings.setRulerFont(Font.decode(value));
               }
               else if (key.equals("unicode"))
               {
                  settings.setUnicodeRanges(value);
               }
               else if (key.equals("look_and_feel"))
               {
                  settings.setLookAndFeel(value);
               }
               else if (key.equals("button_style"))
               {
                  settings.setButtonStyle(value);
                  resources.setButtonStyle(settings.getButtonStyle());
               }
               else if (key.equals("dialog_button_style"))
               {
                  settings.setDialogButtonStyle(parseInt(value, line));
                  resources.setDialogButtonStyle(settings.getDialogButtonStyle());
               }
               else if (key.equals("canvas_split"))
               {
                  settings.setCanvasSplit(parseInt(value, line));
               }
               else if (key.equals("canvas_first"))
               {
                  settings.setCanvasFirst(parseBoolean(value, line));
               }
               else if (key.equals("texeditorfont"))
               {
                  texEditorFont = value;
               }
               else if (key.equals("texeditorfontsize"))
               {
                  texEditorFontSize = parseNonNegInt(value, line);
               }
               else if (key.equals("syntaxhighlight"))
               {
                  settings.setSyntaxHighlighting(parseBoolean(value, line));
               }
               else if (key.equals("commenthighlight"))
               {
                  settings.setCommentHighlight(parseColor(value, line));
               }
               else if (key.equals("cshighlight"))
               {
                  settings.setControlSequenceHighlight(parseColor(value, line));
               }
               else if (key.equals("texeditorwidth"))
               {
                  settings.setTeXEditorWidth(parseInt(value, line));
               }
               else if (key.equals("texeditorheight"))
               {
                  settings.setTeXEditorHeight(parseInt(value, line));
               }
               else if (key.equals("vectorizenotregion"))
               {
                  settings.setVectorizeNotRegion(parseColor(value, line));
               }
               else if (key.equals("vectorizeline"))
               {
                  settings.setVectorizeLine(parseColor(value, line));
               }
               else if (key.equals("vectorizeconnector"))
               {
                  settings.setVectorizeConnector(parseColor(value, line));
               }
               else if (key.equals("vectorizedrag"))
               {
                  settings.setVectorizeDrag(parseColor(value, line));
               }
               else if (key.equals("vectorizecontrol"))
               {
                  settings.setVectorizeControlColor(parseColor(value, line));
               }
               else if (key.equals("vectorizecontrolsize"))
               {
                  settings.setVectorizeControlSize(parseInt(value, line));
               }
               else if (key.equals("status_font"))
               {
                  settings.setStatusFont(Font.decode(value));
               }
               else if (key.equals("contents_annote"))
               {
                  FlowFrame.showFrameContents = parseBoolean(value, line);
                  settings.setAnnoteFrameContentsOn(FlowFrame.showFrameContents);
               }
               else if (key.equals("contents_annote_font"))
               {
                  FlowFrame.contentFont = Font.decode(value);
                  settings.setFrameContentsAnnoteFont(FlowFrame.contentFont);
               }
               else if (key.equals("contents_annote_col"))
               {
                  settings.setFrameContentsAnnoteFontColour(parseColor(value, line));
               }
               else if (key.equals("scale_annote"))
               {
                  JDRCompleteObject.scaleAnnotations = parseBoolean(value, line);
                  settings.setScaleAnnotationsOn(JDRCompleteObject.scaleAnnotations);
               }
               else if (key.equals("annote_typeblock_pos"))
               {
                  settings.setAnnotationTypeblockPos(parseInt(value, line));
                  FlowFrame.typeblockAnnotePos = settings.getAnnotationTypeblockPos();
               }
               else if (key.equals("annote_flow_pos"))
               {
                  settings.setAnnotationFlowPos(parseInt(value, line));
                  FlowFrame.flowAnnotePos = settings.getAnnotationFlowPos();
               }
               else if (key.equals("annote_static_pos"))
               {
                  settings.setAnnotationStaticPos(parseInt(value, line));
                  FlowFrame.staticAnnotePos = settings.getAnnotationStaticPos();
               }
               else if (key.equals("annote_dynamic_pos"))
               {
                  settings.setAnnotationDynamicPos(parseInt(value, line));
                  FlowFrame.dynamicAnnotePos = settings.getAnnotationDynamicPos();
               }
               else if (key.equals("annote_font"))
               {
                  annoteFont = Font.decode(value);
               }
               else if (key.equals("annotefont"))// deprecated
               {
                  annoteFontName = value;
               }
               else if (key.equals("annotesize"))// deprecated
               {
                  annoteFontSize = parseNonNegInt(value, line);
               }
               else if (key.equals("controlselected"))
               {
                  JDRPoint.selectColor = new Color(parseInt(value, line));
               }
               else if (key.equals("controlunselected"))
               {
                  JDRPoint.controlColor = new Color(parseInt(value, line));
               }
               else if (key.equals("adjustselected"))
               {
                  JDRPatternAdjustPoint.patternAdjustSelectColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("adjustunselected"))
               {
                  JDRPatternAdjustPoint.patternAdjustColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("anchorselected"))
               {
                  JDRPatternAnchorPoint.patternAnchorSelectColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("anchorunselected"))
               {
                  JDRPatternAnchorPoint.patternAnchorColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("symmetryselected"))
               {
                  JDRSymmetryLinePoint.symmetryPointColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("symmetryunselected"))
               {
                  JDRSymmetryLinePoint.symmetrySelectedColor 
                     = new Color(parseInt(value, line));
               }
               else if (key.equals("relativefontsizes"))
               {
                  settings.setRelativeFontDeclarations(parseBoolean(value, line));
               }
               else if (key.equals("flowframe_abs_pages"))
               {
                  settings.setUseAbsolutePages(parseBoolean(value, line));
               }
               else if (key.equals("verticaltoolbar"))
               {
                  settings.setVerticalToolBarLocation(value);
               }
               else if (key.equals("relative_bitmaps"))
               {
                  settings.setRelativeBitmaps(parseBoolean(value, line));
               }
               else if (key.equals("bitmap_default_cs"))
               {
                  settings.setDefaultBitmapCommand(value);
               }
               // Import Settings
               else if (key.equals("import_markers"))
               {
                  importSettings.markers = ImportSettings.Markers.valueOf(value);
               }
               else if (key.equals("import_paper"))
               {
                  importSettings.paper = ImportSettings.Paper.valueOf(value);
               }
               else if (key.equals("import_text_mappings"))
               {
                  importSettings.useMappings = parseBoolean(value, line);
               }
               else if (key.equals("import_parse_maths"))
               {
                  importSettings.parseMaths = parseBoolean(value, line);
               }
               else if (key.equals("import_extract_bitmaps"))
               {
                  importSettings.extractBitmaps = parseBoolean(value, line);
               }
               // Export Settings
               else if (key.equals("png_alpha"))
               {
                  exportSettings.pngUseAlpha = parseBoolean(value, line);
               }
               else if (key.equals("png_encap")) // old version
               {
                  if (parseBoolean(value, line))
                  {
                     exportSettings.bounds = ExportSettings.Bounds.IMAGE;
                  }
               }
               else if (key.equals("use_typeblock_as_bbox")) // old version
               {
                  if (parseBoolean(value, line))
                  {
                     exportSettings.bounds = ExportSettings.Bounds.TYPEBLOCK;
                  }
               }
               else if (key.equals("export_bounds"))
               {// replaces png_encap and use_typeblock_as_bbox
                  exportSettings.bounds = ExportSettings.Bounds.valueOf(value);
               }
               else if (key.equals("object_markup"))
               {
                  exportSettings.objectMarkup
                   = ExportSettings.ObjectMarkup.valueOf(value);
               }
               else if (key.equals("shapeparhpadding"))
               {
                  exportSettings.shapeparUseHpadding = parseBoolean(value, line);
               }
               else if (key.equals("pdfinfo"))
               {
                  exportSettings.usePdfInfo = parseBoolean(value, line);
               }
               else if (key.equals("srcfilecomment"))
               {
                  exportSettings.writeSrcFilename = parseBoolean(value, line);
               }
               else if (key.equals("datecomment"))
               {
                  exportSettings.writeDateComment = parseBoolean(value, line);
               }
               else if (key.equals("textualshadingexport"))
               {
                  // old setting is int
                  if (value.equals("0"))
                  {
                     exportSettings.textualShading
                       = ExportSettings.TextualShading.AVERAGE;
                  }
                  else if (value.equals("1"))
                  {
                     exportSettings.textualShading
                       = ExportSettings.TextualShading.START;
                  }
                  else if (value.equals("2"))
                  {
                     exportSettings.textualShading
                       = ExportSettings.TextualShading.END;
                  }
                  else if (value.equals("3"))
                  {
                     exportSettings.textualShading
                       = ExportSettings.TextualShading.TO_PATH;
                  }
                  else
                  {
                     exportSettings.textualShading
                        = ExportSettings.TextualShading.valueOf(value);
                  }
               }
               else if (key.equals("textpathoutlineexport"))
               {
                  // old setting is int
                  if (value.equals("0"))
                  {
                     exportSettings.textPathOutline
                      = ExportSettings.TextPathOutline.TO_PATH;
                  }
                  else if (value.equals("0"))
                  {
                     exportSettings.textPathOutline
                      = ExportSettings.TextPathOutline.IGNORE;
                  }
                  else
                  {
                     exportSettings.textPathOutline
                      = ExportSettings.TextPathOutline.valueOf(value);
                  }
               }
               else if (key.equals("textareaoutlineexport"))
               {
                  exportSettings.textAreaOutline
                   = ExportSettings.TextAreaOutline.valueOf(value);
               }
               else if (key.equals("strokeshadingexport"))
               {
                  exportSettings.strokeShading
                   = ExportSettings.StrokeShading.valueOf(value);
               }
               else if (key.equals("strokemarkersexport"))
               {
                  exportSettings.markers
                   = ExportSettings.Markers.valueOf(value);
               }
               else if (key.equals("latex_app"))
               {
                  exportSettings.dviLaTeXApp = value;
               }
               else if (key.equals("latex_opts"))
               {
                  exportSettings.dviLaTeXOptions = value.split("\t");
               }
               else if (key.equals("pdflatex_app"))
               {
                  exportSettings.pdfLaTeXApp = value;
               }
               else if (key.equals("pdflatex_opts"))
               {
                  exportSettings.pdfLaTeXOptions = value.split("\t");
               }
               else if (key.equals("dvips_app"))
               {
                  exportSettings.dvipsApp = value;
               }
               else if (key.equals("dvips_opts"))
               {
                  exportSettings.dvipsOptions = value.split("\t");
               }
               else if (key.equals("dvisvgm_app"))
               {
                  exportSettings.dvisvgmApp = value;
               }
               else if (key.equals("dvisvgm_opts"))
               {
                  exportSettings.dvisvgmOptions = value.split("\t");
               }
               else if (key.equals("libgs"))
               {
                  exportSettings.libgs = value;
               }
               else if (key.equals("pdftopng_app"))
               {
                  exportSettings.pdftopngApp = value;
               }
               else if (key.equals("pdftopng_opts"))
               {
                  exportSettings.pdftopngOptions = value.split("\t");
               }
               else if (key.equals("support_eps_svg_export"))
               {// backward compatibility
                  settings.setSupportExportEpsSvg(parseBoolean(value, line));
               }
               else if (key.equals("support_eps_export"))
               {
                  settings.setSupportExportEps(parseBoolean(value, line));
               }
               else if (key.equals("support_svg_export"))
               {
                  settings.setSupportExportSvg(parseBoolean(value, line));
               }
               else if (key.equals("timeout"))
               {
                  exportSettings.timeout = parseLong(value, line);
               }
               else if (key.equals("workertimeout"))
               {
                  settings.setSwingWorkerTimeout(parseLong(value, line));
               }
               else if (key.equals("flowfram_v2.0"))
               {
                  settings.setHasMinimumFlowFramSty2_0(parseBoolean(value, line));
                  foundFlowFramv2_0 = true;
               }
               else
               {
                  throw new InvalidFormatException(
                     resources.getMessage(
                        "error.conf.unknown_key", key, line));
               }
            }
            catch (InvalidFormatException e)
            {
               if (resources.isDebuggingOn())
               {
                  e.printStackTrace();
               }

               if (messages == null)
               {
                  messages = new StringBuilder(e.getMessage());
               }
               else
               {
                  messages.append(String.format("%n%s", e.getMessage()));
               }
            }
         }

         if (!foundFlowFramv2_0)
         {
            // The simplest way of determining if flowfram v2.0 or
            // above is installed is to test if flowframtkutils.sty
            // is on TeX's path.

            try
            {
               String p = resources.getHelpLib().kpsewhich("flowframtkutils.sty");

               if (p != null && !p.isEmpty())
               {
                  settings.setHasMinimumFlowFramSty2_0(true);
               }
            }
            catch (Throwable e)
            {
               if (resources.isDebuggingOn())
               {
                  e.printStackTrace();
               }

               messages.append(String.format("%n%s", e.getMessage()));
            }
         }

         if (messages != null && resources.isDebuggingOn())
         {
            resources.error(messages.toString());
         }
      }
      finally
      {
         in.close();
      }

      settings.setTeXEditorFont(texEditorFont, texEditorFontSize);

      if (annoteFont == null)
      {
         annoteFont = new Font(annoteFontName, Font.PLAIN, annoteFontSize);
      }

      settings.setAnnoteFont(annoteFont);
      JDRCompleteObject.annoteFont = annoteFont;

      settings.setRendering(antiAlias, renderQuality);

      settings.getStartMarker().setCompositeMarker(
         startComposite);
      settings.getMidMarker().setCompositeMarker(
         midComposite);
      settings.getEndMarker().setCompositeMarker(
         endComposite);

      settings.setDashPattern(dashPattern, dashOffset);

      switch (gridType)
      {
         case JDRGrid.GRID_RECTANGULAR:
            settings.setGrid(
             new JDRRectangularGrid(settings.getCanvasGraphics(),
             gridUnit, majorDivisions, subDivisions));
         break;
         case JDRGrid.GRID_RADIAL:
            settings.setGrid(
              new JDRRadialGrid(settings.getCanvasGraphics(),
              gridUnit, majorDivisions, subDivisions, spokes));
         break;
         case JDRGrid.GRID_ISO:
            settings.setGrid(
             new JDRIsoGrid(settings.getCanvasGraphics(),
               gridUnit, majorDivisions, subDivisions));
         break;
         case JDRGrid.GRID_TSCHICHOLD:
            settings.setGrid(
             new JDRTschicholdGrid(settings.getCanvasGraphics(),
               gridUnit, majorDivisions, subDivisions));
         break;
         case JDRGrid.GRID_PATH:
             settings.setGrid(
              new JDRPathGrid(settings.getCanvasGraphics(),
                gridUnit, majorDivisions, subDivisions, gridPath));
         break;
      }

      if (!doneRobot || diffVersion)
      {
         // no robot setting found, try initialising robot
         try
         {
            settings.robot = new Robot();
         }
         catch (AWTException awte)
         {
            resources.warning(null, new String[] {
               resources.getMessage("warning.no_robot"),
               awte.getMessage()});
            settings.robot = null;
         }
         catch (SecurityException sexc)
         {
            resources.warning(null, new String[] {
               resources.getMessage("warning.no_robot"),
               sexc.getMessage()});
            settings.robot = null;
         }
      }

      return diffVersion;
   }

   public void saveResources(Vector<File> recentFiles)
     throws IOException,InvalidFormatException
   {
      PrintWriter out = new PrintWriter(
        new FileWriter(new File(resources.getUserConfigDirName(),
                                "startup.conf")));

      out.println(settings.getConfFontSetting(
         resources.getStartUpInfoFont()));
      out.println(settings.getConfFontSetting(
         resources.getStartUpVersionFont()));
      out.close();

      saveDictionaryConfig();

      saveRecentFiles(recentFiles);

      if (getSettings().initSettings == FlowframTkSettings.INIT_USER)
      {
         /*
          * Only save the settings that aren't governed by the
          * INIT_USER flag. These means reloading the config file
          * but remember those settings: 
          */
          
          FlowframTkSettings orgSettings = new FlowframTkSettings(resources);

          copyRemembered(getSettings(), orgSettings);

          debugMessage("updating application settings");
          loadConfig();

          copyRemembered(orgSettings, getSettings());

          saveUserSettings();
      }
      else
      {
         saveUserSettings();
      }

      saveAccelerators();

      saveTextModeMappings();

      saveMathModeMappings();
   }

   private void writeRememberedSettings(PrintWriter out)
      throws IOException
   {
      out.println("version="+JDRResources.APP_VERSION);
      out.println("robot="+(settings.robot==null?0:1));

      ExportSettings exportSettings = settings.exportSettings;
      ImportSettings importSettings = settings.importSettings;

      out.println("latex_app=" + exportSettings.dviLaTeXApp);
      out.println("latex_opts=" 
       + String.join("\t", exportSettings.dviLaTeXOptions));

      out.println("pdflatex_app=" + exportSettings.pdfLaTeXApp);
      out.println("pdflatex_opts="
       + String.join("\t", exportSettings.pdfLaTeXOptions));

      out.println("dvips_app=" + exportSettings.dvipsApp);
      out.println("dvips_opts="
       + String.join("\t", exportSettings.dvipsOptions));

      out.println("dvisvgm_app=" + exportSettings.dvisvgmApp);
      out.println("dvisvgm_opts="
        + String.join("\t", exportSettings.dvisvgmOptions));

      saveIfNotNullOrEmpty(out, "libgs", settings.getLibgs());

      out.println("support_eps_export="
       + (settings.isSupportExportEpsEnabled() ? 1 : 0));

      out.println("support_svg_export="
       + (settings.isSupportExportSvgEnabled() ? 1 : 0));

      saveIfNotNullOrEmpty(out, "pdftopng_app", exportSettings.pdftopngApp);

      if (exportSettings.pdftopngOptions != null)
      {
         out.println("pdftopng_opts="
          + String.join("\t", exportSettings.pdftopngOptions));
      }

      if (settings.hasMinimumFlowFramSty2_0())
      {
         out.println("flowfram_v2.0=1");
      }

      out.println("timeout="+exportSettings.timeout);

      out.println("workertimeout="+settings.getSwingWorkerTimeout());

      out.println("export_bounds="+exportSettings.bounds);

      out.println("import_markers="+importSettings.markers);
      out.println("import_paper="+importSettings.paper);
      out.println("import_text_mappings="+(importSettings.useMappings?1:0));
      out.println("import_parse_maths="+(importSettings.parseMaths?1:0));
      out.println("import_extract_bitmaps="+(importSettings.extractBitmaps?1:0));

      out.println("unicode="+settings.getUnicodeRangesSpec());
      out.println("look_and_feel="+settings.getLookAndFeel());
      out.println("button_style="+settings.getButtonStyle());
      out.println("dialog_button_style="+settings.getDialogButtonStyle());
      out.println("canvas_split="+settings.getCanvasSplit());
      out.println("canvas_first="+(settings.isCanvasFirst() ? 1 : 0));

      out.println("hruler_height="+settings.getHRulerHeight());
      out.println("vruler_width="+settings.getVRulerWidth());

      out.println("ruler_format="
         +settings.getRulerFormat().toLocalizedPattern()+"\t"
         +settings.getRulerLocale().toLanguageTag());

      out.println("ruler_font="+settings.getRulerSetting());
      out.println("annote_font="+settings.getAnnoteSetting());

      out.println("contents_annote="
       +(settings.isAnnoteFrameContentsOn()?1:0));
      out.println("contents_annote_font="+settings.getFrameContentsAnnoteSetting());
      writeColor(out, "contents_annote_col", settings.getFrameContentsAnnoteFontColour());

      out.println("scale_annote="
       +(settings.isScaleAnnotationsOn()?1:0));

      out.println("annote_typeblock_pos="+settings.getAnnotationTypeblockPos());
      out.println("annote_flow_pos="+settings.getAnnotationFlowPos());
      out.println("annote_static_pos="+settings.getAnnotationStaticPos());
      out.println("annote_dynamic_pos="+settings.getAnnotationDynamicPos());

      out.println("status_font="+settings.getStatusSetting());

      out.println("texeditorfont="+settings.getTeXEditorFontName());
      out.println("texeditorfontsize="+settings.getTeXEditorFontSize());
      out.println("syntaxhighlight="
       +(settings.isSyntaxHighlightingOn()?1:0));

      writeColor(out, "commenthighlight", settings.getCommentHighlight());
      writeColor(out, "cshighlight", settings.getControlSequenceHighlight());

      out.println("texeditorwidth="+settings.getTeXEditorWidth());
      out.println("texeditorheight="+settings.getTeXEditorHeight());

      writeColor(out, "vectorizenotregion",
       settings.getVectorizeNotRegion());
      writeColor(out, "vectorizeline",
       settings.getVectorizeLine());
      writeColor(out, "vectorizeconnector",
       settings.getVectorizeConnector());
      writeColor(out, "vectorizedrag",
       settings.getVectorizeDrag());
      writeColor(out, "vectorizecontrol",
       settings.getVectorizeControlColor());
      out.println("vectorizecontrolsize="+settings.getVectorizeControlSize());

      out.println("preview_bitmaps="+(settings.previewBitmaps?1:0));

      out.println("verticaltoolbar="
        + settings.getVerticalToolBarLocation());

      out.println("initsettings="+settings.initSettings);
   }

   private void copyRemembered(FlowframTkSettings orgSettings,
      FlowframTkSettings newSettings)
   {
      // in case user has just upgraded
      newSettings.setDictId(orgSettings.getDictId());
      newSettings.setHelpId(orgSettings.getHelpId());

      newSettings.robot = orgSettings.robot;
      newSettings.setLaTeXApp(orgSettings.getLaTeXApp());
      newSettings.setPdfLaTeXApp(orgSettings.getPdfLaTeXApp());
      newSettings.setDvipsApp(orgSettings.getDvipsApp());
      newSettings.setDvisvgmApp(orgSettings.getDvisvgmApp());
      newSettings.setLibgs(orgSettings.getLibgs());
      newSettings.setSupportExportEps(orgSettings.isSupportExportEpsEnabled());
      newSettings.setSupportExportSvg(orgSettings.isSupportExportSvgEnabled());
      newSettings.setPdfToPngApp(orgSettings.getPdfToPngApp());
      newSettings.setPdfToPngOptions(orgSettings.getPdfToPngOptions());

      newSettings.setUnicodeRanges(orgSettings.getUnicodeRanges());
      newSettings.setLookAndFeel(orgSettings.getLookAndFeel());
      newSettings.setButtonStyle(orgSettings.getButtonStyle());
      newSettings.setDialogButtonStyle(orgSettings.getDialogButtonStyle());
      newSettings.setCanvasSplit(orgSettings.getCanvasSplit());
      newSettings.setCanvasFirst(orgSettings.isCanvasFirst());

      newSettings.setHRulerHeight(orgSettings.getHRulerHeight());
      newSettings.setVRulerWidth(orgSettings.getVRulerWidth());

      newSettings.setRulerFormat(orgSettings.getRulerFormat(),
         orgSettings.getRulerLocale());

      newSettings.setRulerFont(orgSettings.getRulerFont());
      newSettings.setAnnoteFont(orgSettings.getAnnoteFont());
      newSettings.setStatusFont(orgSettings.getStatusFont());

      newSettings.setTeXEditorFont(orgSettings.getTeXEditorFontName(),
         orgSettings.getTeXEditorFontSize());

      newSettings.setSyntaxHighlighting(orgSettings.isSyntaxHighlightingOn());
      newSettings.setCommentHighlight(orgSettings.getCommentHighlight());
      newSettings.setControlSequenceHighlight(
         orgSettings.getControlSequenceHighlight());
      newSettings.setTeXEditorWidth(orgSettings.getTeXEditorWidth());
      newSettings.setTeXEditorHeight(orgSettings.getTeXEditorHeight());

      newSettings.setVectorizeNotRegion(orgSettings.getVectorizeNotRegion());
      newSettings.setVectorizeLine(orgSettings.getVectorizeLine());
      newSettings.setVectorizeConnector(orgSettings.getVectorizeConnector());
      newSettings.setVectorizeDrag(orgSettings.getVectorizeDrag());

      newSettings.setMaxProcessTime(orgSettings.getMaxProcessTime());
      newSettings.previewBitmaps = orgSettings.previewBitmaps;

      newSettings.setVerticalToolBarLocation(
         orgSettings.getVerticalToolBarLocation());

      newSettings.initSettings = orgSettings.initSettings;
   }

   public void saveDictionaryConfig()
      throws IOException
   {
      resources.saveLanguageSettings();
   }

   public void saveConfig(File file)
      throws IOException
   {
      if (gui == null)
      {
         return;
      }

      // saves settings to config file

      PrintWriter out = new PrintWriter(
         new FileWriter(file));
      debugMessage("writing: "+file);

      writeRememberedSettings(out);

      if (settings.initSettings==FlowframTkSettings.INIT_DEFAULT)
      {
         out.close();
         return;
      }

      out.println("status_show_zoom="
        + (gui.isStatusZoomVisible() ? 1 : 0));
      out.println("status_show_pos="
        + (gui.isStatusPositionVisible() ? 1 : 0));
      out.println("status_show_mod="
        + (gui.isStatusModifiedVisible() ? 1 : 0));
      out.println("status_show_lock="
        + (gui.isStatusLockVisible() ? 1 : 0));
      out.println("status_show_unit="
        + (gui.isStatusUnitVisible() ? 1 : 0));
      out.println("status_show_info="
        + (gui.isStatusInfoVisible() ? 1 : 0));
      out.println("status_show_help="
        + (gui.isStatusHelpVisible() ? 1 : 0));

      CanvasGraphics cg = settings.getCanvasGraphics();

      JDRGrid grid = cg.getGrid();

      out.println("norm_x="+CanvasGraphics.normTransformX);
      out.println("norm_y="+CanvasGraphics.normTransformY);

      out.println("widest_char="+CanvasTextField.widestChar);

      out.println("show_tools="+(settings.showToolBar?1:0));
      out.println("show_status="+(settings.showStatus?1:0));
      out.println("dragscale="+(settings.enableDragScale?1:0));
      out.println("canvasclickexitspathedit="+(settings.canvasClickExitsPathEdit?1:0));
      out.println("selectcontrolignoreslock="+(settings.selectControlIgnoresLock?1:0));
      out.println("antialias="+(settings.isAntiAliasOn()?1:0));
      out.println("render_quality="+(settings.isRenderQualityOn()?1:0));

      out.println("controlsize="+cg.getPointSize());
      out.println("scalecontrols="+
        (cg.isScaleControlPointsEnabled() ? 1 : 0));

      out.println("controlselected="+JDRPoint.selectColor.getRGB());
      out.println("controlunselected="+JDRPoint.controlColor.getRGB());
      out.println("adjustselected="
         + JDRPatternAdjustPoint.patternAdjustSelectColor.getRGB());
      out.println("adjustunselected="
         + JDRPatternAdjustPoint.patternAdjustColor.getRGB());
      out.println("anchorselected="
         + JDRPatternAnchorPoint.patternAnchorSelectColor.getRGB());
      out.println("anchorunselected="
         + JDRPatternAnchorPoint.patternAnchorColor.getRGB());
      out.println("symmetryselected="
         + JDRSymmetryLinePoint.symmetryPointColor.getRGB());
      out.println("symmetryunselected="
         + JDRSymmetryLinePoint.symmetrySelectedColor.getRGB());

      out.println("relative_bitmaps="+(settings.useRelativeBitmaps()? 1: 0));
      out.println("bitmap_default_cs="+settings.getDefaultBitmapCommand());

      out.println("storageunit="+cg.getStorageUnitID());

      out.println("grid="+grid.getID());
      out.println("gridunit="+grid.getMainUnit().getID());

      if (grid instanceof JDRRectangularGrid)
      {
         JDRRectangularGrid g = (JDRRectangularGrid)grid;
         out.println("majordivisions="+(int)g.getMajorInterval());
         out.println("subdivisions="+g.getSubDivisions());
      }
      else if (grid instanceof JDRRadialGrid)
      {
         JDRRadialGrid g = (JDRRadialGrid)grid;
         out.println("majordivisions="+(int)g.getMajorInterval());
         out.println("subdivisions="+g.getSubDivisions());
         out.println("spokes="+g.getSpokes());
      }
      else if (grid instanceof JDRIsoGrid)
      {
         JDRIsoGrid g = (JDRIsoGrid)grid;
         out.println("majordivisions="+(int)g.getMajorInterval());
         out.println("subdivisions="+g.getSubDivisions());
      }
      else if (grid instanceof JDRTschicholdGrid)
      {
         JDRTschicholdGrid g = (JDRTschicholdGrid)grid;
         out.println("majordivisions="+(int)g.getMajorInterval());
         out.println("subdivisions="+g.getSubDivisions());
      }
      else if (grid instanceof JDRPathGrid)
      {
         JDRPathGrid g = (JDRPathGrid)grid;
         out.println("majordivisions="+(int)g.getMajorInterval());
         out.println("subdivisions="+g.getSubDivisions());
         out.print("grid-path=");
         writePath(out, g.getShape());
         out.println();
      }

      out.println("show_grid="+(cg.isGridDisplayed()?1:0));
      out.println("lock_grid="+(cg.isGridLocked()?1:0));
      out.println("show_rulers="+(cg.showRulers()?1:0));
      out.println("startdir_type="+settings.startDirType);

      if (settings.startDirType == FlowframTkSettings.STARTDIR_LAST)
      {
         out.println("startdir="+gui.getCurrentDirectory());
      }
      else
      {
         out.println("startdir="+settings.startDir);
      }

      out.println("save_jdrsettings=" +settings.getSaveSettings());
      out.println("use_jdrsettings="
         +(settings.getUseSettingsOnLoad()));
      out.println("warn_load_old="+(settings.warnOnOldJdr?1:0));

      out.println("normalsize="+((int)settings.getNormalSize()));

      out.println("latexfontupdate="
         +(settings.updateLaTeXFonts?1:0));
      out.println("autoupdateanchors="
         +(settings.autoUpdateAnchors?1:0));
      out.println("autoescapespchars="
         +(settings.autoEscapeSpChars?1:0));

      out.println("tool="+cg.getToolString());

      if (cg.getPaperID() == JDRPaper.ID_USER)
      {
         out.println("paper=user "+cg.getPaperWidth()+" "
            +cg.getPaperHeight());
      }
      else
      {
         out.println("paper="+cg.getPaperName());
      }

      out.println("relativefontsizes=" + (settings.useRelativeFontDeclarations() ? 1 : 0));

      // Export settings

      ExportSettings exportSettings = settings.exportSettings;

      out.println("shapeparhpadding=" + (exportSettings.shapeparUseHpadding ? 1 : 0));
      out.println("pdfinfo="+(exportSettings.usePdfInfo ? 1 : 0));
      out.println("srcfilecomment="+(exportSettings.writeSrcFilename ? 1 : 0));
      out.println("datecomment="+(exportSettings.writeDateComment ? 1 : 0));
      out.println("png_alpha="+(exportSettings.pngUseAlpha ? 1 : 0));
      out.println("textualshadingexport="
        + exportSettings.textualShading);
      out.println("textpathoutlineexport="
        + exportSettings.textPathOutline);
      out.println("textareaoutlineexport="
        + exportSettings.textAreaOutline);
      out.println("strokeshadingexport="
        + exportSettings.strokeShading);
      out.println("strokemarkersexport="
        + exportSettings.markers);
      out.println("object_markup="
        + exportSettings.objectMarkup);

      out.println("flowframe_abs_pages="
        + (settings.useAbsolutePages() ? 1 : 0));

      // Font Styles

      out.println("fontname="+settings.getFontFamily());
      out.println("fontshape="+settings.getFontShape());
      out.println("fontseries="+settings.getFontSeries());
      out.println("fontsize="+settings.getFontSize());
      out.println("latexfontname="+settings.getLaTeXFontFamily());
      out.println("latexfontshape="+settings.getLaTeXFontShape());
      out.println("latexfontseries="+settings.getLaTeXFontSeries());
      out.println("latexfontsize="+settings.getLaTeXFontSize());
      out.println("textpaint="
         +savePaintConfig(settings.getTextPaint()));

      // Path Styles

      out.println("capstyle="+settings.getStroke().getCapStyle());
      out.println("joinstyle="+settings.getStroke().getJoinStyle());
      out.println("windingrule="+settings.getStroke().getWindingRule());
      out.println("penwidth="+settings.getStroke().getPenWidth());
      out.println("mitrelimit="+settings.getStroke().getMitreLimit());

      out.println("dashoffset="
         +settings.getStroke().getDashPattern().getStorageOffset());

      float[] pattern = settings.getStroke().getDashPattern().getStoragePattern();

      if (pattern==null)
      {
         out.println("dash=0");
      }
      else
      {
         int n = pattern.length;
         out.print("dash="+n);


         for (int i = 0; i < n; i++)
         {
            out.print(","+pattern[i]);
         }
         out.println();
      }

      out.println("linepaint="
         +savePaintConfig(settings.getLinePaint()));
      out.println("shapefillpaint="
         +savePaintConfig(settings.getShapeFillPaint()));
      out.println("outlinefillpaint="
         +savePaintConfig(settings.getOutlineFillPaint()));

      // Start Markers

      out.println("startarrow="+settings.getStroke().getStartArrowType());
      out.println("startarrowsize="+settings.getStroke().getStartArrowSize());
      out.println("startarrowrepeat="
         + (settings.getStroke().getStartArrowRepeated()));
      out.println("startarrowreverse="
         + (settings.getStroke().getStartArrowReverse() ? 1 : 0));
      out.println("startarroworient="
         + (settings.getStroke().getStartArrowAutoOrient() ? 1 : 0));
      out.println("startarrowangle="
         + settings.getStroke().getStartArrowAngle());
      out.println("startarrowuseroffsetenabled="
         + (settings.getStroke().getStartUserOffsetEnabled() ? 1 : 0));
      out.println("startarrowuseroffset="
         + settings.getStroke().getStartOffset());
      out.println("startarrowrepeatoffsetenabled="
         + (settings.getStroke().getStartRepeatOffsetEnabled() ? 1 : 0));
      out.println("startarrowrepeatoffset="
         + settings.getStroke().getStartRepeatOffset());

      JDRMarker startMarker = settings.getStroke().getStartArrow();

      JDRLength markerWidth = startMarker.getWidth();

      if (markerWidth != null)
      {
         out.println("startarrowwidth="+markerWidth);
      }

      JDRPaint paint = startMarker.getFillPaint();

      if (paint != null)
      {
         out.println("startarrowpaint="+savePaintConfig(paint));
      }


      // Mid Markers

      out.println("midarrow="+settings.getStroke().getMidArrowType());
      out.println("midarrowsize="+settings.getStroke().getMidArrowSize());
      out.println("midarrowrepeat="
         + (settings.getStroke().getMidArrowRepeated()));
      out.println("midarrowreverse="
         + (settings.getStroke().getMidArrowReverse() ? 1 : 0));
      out.println("midarroworient="
         + (settings.getStroke().getMidArrowAutoOrient() ? 1 : 0));
      out.println("midarrowangle="
         + settings.getStroke().getMidArrowAngle());
      out.println("midarrowuseroffsetenabled="
         + (settings.getStroke().getMidUserOffsetEnabled() ? 1 : 0));
      out.println("midarrowuseroffset="
         + settings.getStroke().getMidOffset());
      out.println("midarrowrepeatoffsetenabled="
         + (settings.getStroke().getMidRepeatOffsetEnabled() ? 1 : 0));
      out.println("midarrowrepeatoffset="
         + settings.getStroke().getMidRepeatOffset());

      JDRMarker midMarker = settings.getStroke().getMidArrow();

      markerWidth = midMarker.getWidth();

      if (markerWidth != null)
      {
         out.println("midarrowwidth="+markerWidth);
      }

      paint = midMarker.getFillPaint();

      if (paint!=null)
      {
         out.println("midarrowpaint="+savePaintConfig(paint));
      }

      // End Markers

      out.println("endarrow="+settings.getStroke().getEndArrowType());
      out.println("endarrowsize="+settings.getStroke().getEndArrowSize());
      out.println("endarrowrepeat="
         + (settings.getStroke().getEndArrowRepeated()));
      out.println("endarrowreverse="
         + (settings.getStroke().getEndArrowReverse() ? 1 : 0));
      out.println("endarroworient="
         + (settings.getStroke().getEndArrowAutoOrient() ? 1 : 0));
      out.println("endarrowangle="
         + settings.getStroke().getEndArrowAngle());
      out.println("endarrowuseroffsetenabled="
         + (settings.getStroke().getEndUserOffsetEnabled() ? 1 : 0));
      out.println("endarrowuseroffset="
         + settings.getStroke().getEndOffset());
      out.println("endarrowrepeatoffsetenabled="
         + (settings.getStroke().getEndRepeatOffsetEnabled() ? 1 : 0));
      out.println("endarrowrepeatoffset="
         + settings.getStroke().getEndRepeatOffset());

      JDRMarker endMarker = settings.getStroke().getEndArrow();

      markerWidth = endMarker.getWidth();

      if (markerWidth != null)
      {
         out.println("endarrowwidth="+markerWidth);
      }

      paint = endMarker.getFillPaint();

      if (paint != null)
      {
         out.println("endarrowpaint="+savePaintConfig(paint));
      }

      // Composite Markers

      JDRMarker startComposite 
         = settings.getStroke().getStartArrow().getCompositeMarker();
      JDRMarker midComposite 
         = settings.getStroke().getMidArrow().getCompositeMarker();
      JDRMarker endComposite 
         = settings.getStroke().getEndArrow().getCompositeMarker();

      if (startComposite != null)
      {
         out.println("secondarystartarrow="+startComposite.getType());
         out.println("secondarystartarrowsize="
            +startComposite.getSize());
         out.println("secondarystartarrowrepeat="
            +startComposite.getRepeated());
         out.println("secondarystartarrowreverse="
            +(startComposite.isReversed() ? 1 : 0));
         out.println("secondarystartarroworient="
            +(startComposite.getAutoOrient() ? 1 : 0));
         out.println("secondarystartarrowangle="
            +startComposite.getAngle());

         markerWidth = startComposite.getWidth();

         if (markerWidth != null)
         {
            out.println("secondarystartarrowwidth="+markerWidth);
         }

         paint = startComposite.getFillPaint();

         if (paint != null)
         {
            out.println("secondarystartarrowpaint="
               + savePaintConfig(paint));
         }

         out.println("secondarystartarrowuseroffsetenabled="
            + (startComposite.isUserOffsetEnabled() ? 1 : 0));

         out.println("secondarystartarrowuseroffset="
            + startComposite.getOffset());

         out.println("secondarystartarrowrepeatoffsetenabled="
            + (startComposite.isUserRepeatOffsetEnabled() ? 1 : 0));

         out.println("secondarystartarrowrepeatoffset="
            + startComposite.getRepeatOffset());

         out.println("startoverlaid="
            + (startMarker.isOverlaid() ? 1 : 0));
      }

      if (midComposite != null)
      {
         out.println("secondarymidarrow="+midComposite.getType());
         out.println("secondarymidarrowsize="
            +midComposite.getSize());
         out.println("secondarymidarrowrepeat="
            +midComposite.getRepeated());
         out.println("secondarymidarrowreverse="
            +(midComposite.isReversed() ? 1 : 0));
         out.println("secondarymidarroworient="
            +(midComposite.getAutoOrient() ? 1 : 0));
         out.println("secondarymidarrowangle="
            +midComposite.getAngle());

         markerWidth = midComposite.getWidth();

         if (markerWidth != null)
         {
            out.println("secondarymidarrowwidth="+markerWidth);
         }

         paint = midComposite.getFillPaint();

         if (paint != null)
         {
            out.println("secondarymidarrowpaint="
               + savePaintConfig(paint));
         }

         out.println("secondarymidarrowuseroffsetenabled="
            + (midComposite.isUserOffsetEnabled() ? 1 : 0));

         out.println("secondarymidarrowuseroffset="
            + midComposite.getOffset());

         out.println("secondarymidarrowrepeatoffsetenabled="
            + (midComposite.isUserRepeatOffsetEnabled() ? 1 : 0));

         out.println("secondarymidarrowrepeatoffset="
            + midComposite.getRepeatOffset());

         out.println("midoverlaid="
            + (midMarker.isOverlaid() ? 1 : 0));
      }

      if (endComposite != null)
      {
         out.println("secondaryendarrow="+endComposite.getType());
         out.println("secondaryendarrowsize="
            +endComposite.getSize());
         out.println("secondaryendarrowrepeat="
            +endComposite.getRepeated());
         out.println("secondaryendarrowreverse="
            +(endComposite.isReversed() ? 1 : 0));
         out.println("secondaryendarroworient="
            +(endComposite.getAutoOrient() ? 1 : 0));
         out.println("secondaryendarrowangle="
            +endComposite.getAngle());

         markerWidth = endComposite.getWidth();

         if (markerWidth != null)
         {
            out.println("secondaryendarrowwidth="+markerWidth);
         }

         if (endComposite.getFillPaint() != null)
         {
            out.println("secondaryendarrowpaint="
               + savePaintConfig(endComposite.getFillPaint()));
         }

         out.println("secondaryendarrowuseroffsetenabled="
            + (endComposite.isUserOffsetEnabled() ? 1 : 0));

         out.println("secondaryendarrowuseroffset="
            + endComposite.getOffset());

         out.println("secondaryendarrowrepeatoffsetenabled="
            + (endComposite.isUserRepeatOffsetEnabled() ? 1 : 0));

         out.println("secondaryendarrowrepeatoffset="
            + endComposite.getRepeatOffset());

         out.println("endoverlaid="
            + (endMarker.isOverlaid() ? 1 : 0));
      }

      out.close();
   }

   public void saveUserSettings() throws IOException
   {
      File file = getUserSettingsFile();

      saveConfig(file);
   }

   private void saveIfNotNullOrEmpty(PrintWriter out, String key, String value)
    throws IOException
   {
      if (value != null && !value.isEmpty())
      {
         out.println(key+"="+value);
      }
   }

   private String savePaintConfig(JDRPaint paint)
   {
      return JDRAJR.getPaintLoader().getConfigString(paint);
   }

   private JDRPaint loadPaintConfig(
      FlowframTkSettings settings, String str, int line)
      throws InvalidFormatException
   {
      JDRPaint paint=null;

      try
      {
         paint = JDR.getPaintLoader().parseConfig(
            settings.getCanvasGraphics(), str);
      }
      catch (InvalidFormatException e)
      {
         throw new InvalidFormatException(String.format("%s%n%s", 
            resources.getMessage("error.bad_paint_format", line),
               e.getMessage()));
      }

      return paint;
   }

   public void saveAccelerators() throws IOException
   {
      File file = getAcceleratorFile();

      if (file != null)
      {
         PrintWriter writer = null;

         try
         {
            writer = new PrintWriter(file);
            resources.saveAccelerators(writer);
         }
         finally
         {
            if (writer != null)
            {
               writer.close();
            }
         }
      }
   }

   public void saveTextModeMappings() throws IOException
   {
      File file = getTextMappingsFile();

      if (file != null)
      {

         PrintWriter writer = null;

         try
         {
            writer = new PrintWriter(file);

            getSettings().saveTextModeMappings(writer);
         }
         finally
         {
            if (writer != null)
            {
               writer.close();
            }
         }
      }
   }

   public void saveMathModeMappings() throws IOException
   {
      File file = getMathMappingsFile();

      if (file != null)
      {
         PrintWriter writer = null;

         try
         {
            writer = new PrintWriter(file);

            getSettings().saveMathModeMappings(writer);
         }
         finally
         {
            if (writer != null)
            {
               writer.close();
            }
         }
      }
   }

   public void saveRecentFiles(Vector<File> recentFiles)
   {
      File file = getRecentListFile();

      if (file == null) return;

      int n = recentFiles.size();

      if (n == 0) return;

      try
      {
         PrintWriter out = new PrintWriter(file);

         for (int i = 0; i < n && i < 10; i++)
         {
            out.println(recentFiles.get(i).toString());
         }

         out.close();
      }
      catch (Exception e)
      {
         resources.error(e);
      }
   }

   public Vector<File> loadRecentFiles() throws IOException
   {
      Vector<File> recentFiles = new Vector<File>(10);

      File propfile = getRecentListFile();

      if (propfile != null)
      {
         BufferedReader in = null;

         try
         {
            in = new BufferedReader(new FileReader(propfile));

            String s;

            while ((s=in.readLine()) != null)
            {
               if (!s.isEmpty())
               {
                  File f = new File(s);

                  // check not already added

                  if (!recentFiles.contains(f)) 
                  {
                     recentFiles.add(f);
                  }
               }
            }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }
      }

      return recentFiles;
   }

   public JDRResources getResources()
   {
      return resources;
   }

   public String getConfigDirName()
   {
      return resources.getUserConfigDirName();
   }

   public File getConfigPreambleFile()
   {
      return new File(getConfigDirName(), "preamble.tex");
   }

   public File getAcceleratorFile()
   {
      return new File(getConfigDirName(), "accelerators.prop");
   }

   public File getTextMappingsFile()
   {
      return new File(getConfigDirName(), "textmappings.prop");
   }

   public File getMathMappingsFile()
   {
      return new File(getConfigDirName(), "mathmappings.prop");
   }

   public File getUserSettingsFile()
   {
      return new File(getConfigDirName(), "flowframtk.conf");
   }

   public File getRecentListFile()
   {
      return new File(getConfigDirName(), "recentfiles");
   }

   private boolean loadResources()
      throws IOException,URISyntaxException
   {
      File file;

      boolean diffVersion = true;

      // is there a config file in usersettings?

      String userconfname=null;

      String usersettings = getConfigDirName();

      if (usersettings != null)
      {
         file = new File(usersettings, "flowframtk.conf");

         userconfname = file.getCanonicalPath();

         if (userconfname != null && file.exists() && file.isFile())
         {
            try
            {
               diffVersion = loadConfig(file);
            }
            catch (InvalidFormatException e)
            {
               resources.error(e);
            }
         }
         else
         {
            file = new File(usersettings, "jpgfdraw.conf");

            if (userconfname != null && file.exists() && file.isFile())
            {
               try
               {
                  diffVersion = loadConfig(file);
               }
               catch (InvalidFormatException e)
               {
                  resources.error(e);
               }
            }
            else
            {
               debugMessage("No config file found, usersettings: "
                + usersettings);

               // no setting found, try initialising robot

               try
               {
                  settings.setRobot(new Robot());
               }
               catch (AWTException awte)
               {
                  resources.warning(null, new String[] {
                     resources.getMessage("warning.no_robot"),
                     awte.getMessage()});
                  settings.setRobot(null);
               }
               catch (SecurityException se)
               {
                  resources.warning(null, new String[] {
                     resources.getMessage("warning.no_robot"),
                     se.getMessage()});
                  settings.setRobot(null);
               }
            }
         }
      }

      try
      {
         startup.setVersion(resources.getMessage(
           "about.version", getName(), getVersion()),
           resources.getMessage("about.copyright")
              +String.format(" %s-%s Nicola L.C. Talbot",
               JDRResources.START_COPYRIGHT_YEAR, JDRResources.COPYRIGHT_YEAR),
           resources.getMessage("about.disclaimer", getName())
         );
      }
      catch (IllegalStateException e)
      {
         debugMessage(e);
      }

      // Load accelerators if they exist

      file = new File(usersettings, "accelerators.prop");

      if (file.exists())
      {
         debugMessage("loading "+file);

         BufferedReader reader = null;

         try
         {
            reader = new BufferedReader(new FileReader(file));
            resources.initialiseAccelerators(diffVersion, reader);
         }
         catch (IOException e)
         {
            resources.error(e);
         }
         finally
         {
            if (reader != null)
            {
               reader.close();
            }
         }
      }

      // Load text mappings if they exist

      file = new File(usersettings, "textmappings.prop");

      settings.loadTextModeMappings(file);

      // Load math mappings if they exist

      file = new File(usersettings, "mathmappings.prop");

      settings.loadMathModeMappings(file);

      // Need to set L&F before creating components

      String lookAndFeel = settings.getLookAndFeel();

      if (lookAndFeel == null || lookAndFeel.isEmpty()
       || lookAndFeel.equals("null"))
      {
         lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
         settings.setLookAndFeel(lookAndFeel);
      }

      try
      {
         UIManager.setLookAndFeel(lookAndFeel);
      }
      catch (InstantiationException e)
      {
         settings.setLookAndFeel("");
         e.printStackTrace();
      }
      catch (ClassNotFoundException e)
      {
         settings.setLookAndFeel("");
         e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
         settings.setLookAndFeel("");
         e.printStackTrace();
      }
      catch (UnsupportedLookAndFeelException e)
      {
         settings.setLookAndFeel("");
         e.printStackTrace();
      }

      // Need to load dictionary before creating message system.

      settings.setMessageSystem((JDRGuiMessage)resources.getMessageSystem());

      // load Java to LaTeX font family mappings

      String latexmappings=null;

      if (usersettings != null)
      {
         file = new File(usersettings, "latexfontmap");

         if (!file.exists())
         {
            file = new File(usersettings, "latexfontmap.prop");
         }

         latexmappings = file.getCanonicalPath();

         if (latexmappings != null && file.exists()
             && file.isFile())
         {
            try
            {
               LaTeXFont.loadJavaMappings(settings.getMessageSystem(), file);
            }
            catch (InvalidFormatException e)
            {
               resources.error(e);
            }
         }
      }

      return diffVersion;
   }

   private void loadStartupInfo() throws IOException
   {
      String usersettings = resources.getUserConfigDirName();

      if (usersettings == null)
      {
         return;
      }

      File file = new File(usersettings, "startup.conf");

      if (!file.exists())
      {
         return;
      }

      BufferedReader in = null;

      String infoFont = null;
      String versionFont = null;

      try
      {
         in = new BufferedReader(new FileReader(file));

         String line;

         while ((line = in.readLine()) != null)
         {
            if (line.isEmpty() || line.startsWith("#"))
            {
               continue;
            }

            if (infoFont == null)
            {
               infoFont = line;
            }
            else if (versionFont == null)
            {
               versionFont = line;
               break;
            }
         }
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }

      if (infoFont != null)
      {
         resources.setStartUpInfoFont(infoFont);
      }

      if (versionFont != null)
      {
         resources.setStartUpVersionFont(versionFont);
      }
   }

   public void incStartupProgress()
   {
      try
      {
         startup.incProgress();
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void incStartupProgress(String text)
   {
      try
      {
        startup.incProgress(text);
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void setStartupDeterminate(int n)
   {
      try
      {
         startup.setDeterminate(n);
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void setStartupIndeterminate()
   {
      try
      {
         startup.setIndeterminate();
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void setStartupInfo(String text)
   {
      if (startup != null && startup.isVisible())
      {
         try
         {
            startup.setInfo(text);
         }
         catch (IllegalStateException e)
         {
         }
      }
   }

   public int getStartupProgress()
   {
      return startup.getProgress();
   }

   private CLISyntaxParser createCLISyntaxParser()
   {
      return new CLISyntaxParser(resources.getHelpLib(), args, "-h", "-v")
      {             
         @Override  
         protected int argCount(String arg)
         {
            if (arg.equals("--paper") || arg.equals("-paper"))
            {
               return 3;
            }
            else if (arg.equals("--in") || arg.equals("-i")
             || arg.equals("--verbosity") || arg.equals("-verbosity")
               )
            {
               return 1;
            }

            return 0;
         }

         @Override
         public boolean setDebugOption(String option, Integer value)
         throws InvalidSyntaxException
         {
            resources.debugMode = true;
            resources.getMessageSystem().setVerbosity(2);

            return true;
         }

         @Override
         protected boolean preparseCheckArg()
         throws InvalidSyntaxException
         {
            if (super.preparseCheckArg())
            {
               return true;
            }

            if (originalArgList[preparseIndex].equals("--paper")
             || originalArgList[preparseIndex].equals("-paper")
              )
            {
               // make sure the queue has three arguments

               String option = originalArgList[preparseIndex];

               if (preparseIndex < originalArgList.length - 1)
               {
                  preparseIndex++;
                  deque.add(option);

                  String paperId = originalArgList[preparseIndex];
                  deque.add(paperId);

                  preparseIndex++;

                  if (paperId.equals("user"))
                  {
                     if (preparseIndex < originalArgList.length - 1)
                     {
                        deque.add(originalArgList[preparseIndex]);
                        preparseIndex++;
                     }
                     else
                     {
                        throw new InvalidSyntaxException(
                          resources.getMessage( "error.missing_paper_width"));
                     }

                     if (preparseIndex < originalArgList.length - 1)
                     {
                        deque.add(originalArgList[preparseIndex]);
                        preparseIndex++;
                     }
                     else
                     {
                        throw new InvalidSyntaxException(
                          resources.getMessage( "error.missing_paper_height"));
                     }
                  }
                  else
                  {
                     deque.add("");
                     deque.add("");
                  }
               }
               else
               {
                  throw new InvalidSyntaxException(getHelpLib().getMessage(
                    "error.clisyntax.missing.value", option));
               }
            }
            else if (originalArgList[preparseIndex].equals("-version"))
            {
               versionInfo();
            }
            else if (originalArgList[preparseIndex].equals("-help"))
            {
               syntax();
            }
            else if (originalArgList[preparseIndex].equals("-debug"))
            {
               return setDebugModeOption(originalArgList[preparseIndex], null);
            }
            else if (originalArgList[preparseIndex].equals("-nodebug")
                  || originalArgList[preparseIndex].equals("--nodebug")
                    )
            {
               resources.debugMode = false;
            }
            else if (originalArgList[preparseIndex].equals("--verbosity")
             || originalArgList[preparseIndex].equals("-verbosity")
               )
            {
               String option = originalArgList[preparseIndex];

               if (preparseIndex < originalArgList.length - 1)
               {
                  preparseIndex++;

                  try
                  {
                     resources.getMessageSystem().setVerbosity(
                       Integer.parseInt(originalArgList[preparseIndex]));
                  }
                  catch (NumberFormatException e)
                  {
                     throw new InvalidSyntaxException(
                        resources.getMessage("error.with_found",
                          resources.getMessage("error.invalid_verbosity"),
                          originalArgList[preparseIndex]), e);
                  }
               }
               else
               {
                  throw new InvalidSyntaxException(getHelpLib().getMessage(
                    "error.clisyntax.missing.value", option));
               }
            }
            else if (originalArgList[preparseIndex].startsWith("--verbosity="))
            {
               String[] split = originalArgList[preparseIndex].split("=", 2);
               String option = split[0];

               try
               {
                  resources.getMessageSystem().setVerbosity(
                    Integer.parseInt(split[1]));
               }
               catch (NumberFormatException e)
               {
                  throw new InvalidSyntaxException(
                     resources.getMessage("error.with_found",
                       resources.getMessage("error.invalid_verbosity"),
                       split[1]), e);
               }
            }
            else
            {
               return false;
            }

            return true;
         }

         @Override
         protected void help()
         {
            syntax();
         }

         @Override
         protected void version()
         {
            versionInfo();
         }

         @Override
         protected void parseArg(String arg)
         throws InvalidSyntaxException
         {
            // if no option specified, assume input file

            try
            {
               // Discard any problem file but continue to open
               // GUI if possible

               filenames.add((new File(arg)).getCanonicalPath());
            }
            catch (IOException e)
            {
               resources.error(e.getMessage());
            }
            catch (Exception e)
            {
               resources.error(e);
            }
         }

         @Override
         protected boolean parseArg(String arg, CLIArgValue[] returnVals)
         throws InvalidSyntaxException
         {
            if (arg.equals("--disable_print")
             || arg.equals("-disable_print")
             )
            {
               disablePrint = true;
            }
            else if (arg.equals("--nodisable_print")
                  || arg.equals("-nodisable_print")
             )
            {
               disablePrint = false;
            }
            else if (arg.equals("--show_grid")
                  || arg.equals("-show_grid")
             )
            {
               settings.setDisplayGrid(true);
            }
            else if (arg.equals("--noshow_grid")
                  || arg.equals("-noshow_grid")
             )
            {
               settings.setDisplayGrid(false);
            }
            else if (arg.equals("--grid_lock")
                  || arg.equals("-grid_lock")
             )
            {
               settings.setGridLock(true);
            }
            else if (arg.equals("--nogrid_lock")
                  || arg.equals("-nogrid_lock")
             )
            {
               settings.setGridLock(false);
            }
            else if (arg.equals("--toolbar")
                  || arg.equals("-toolbar")
             )
            {
               settings.showToolBar = true;
            }
            else if (arg.equals("--notoolbar")
                  || arg.equals("-notoolbar")
             )
            {
               settings.showToolBar = false;
            }
            else if (arg.equals("--statusbar")
                  || arg.equals("-statusbar")
             )
            {
               settings.setShowStatus(true);
            }
            else if (arg.equals("--nostatusbar")
                  || arg.equals("-nostatusbar")
             )
            {
               settings.setShowStatus(false);
            }
            else if (arg.equals("--rulers")
                  || arg.equals("-rulers")
             )
            {
               settings.setShowRulers(true);
            }
            else if (arg.equals("--norulers")
                  || arg.equals("-norulers")
             )
            {
               settings.setShowRulers(false);
            }
            else if (arg.equals("--experimental")
                  || arg.equals("-experimental")
             )
            {
               experimentalMode = true;
            }
            else if (arg.equals("--noexperimental")
                  || arg.equals("-noexperimental")
             )
            {
               experimentalMode = true;
            }
            else if (arg.startsWith("-"))
            {
               throw new InvalidSyntaxException(
                getHelpLib().getMessage("error.syntax.unknown_option", arg));
            }
            else if (isArg(arg, "--paper", "-paper", returnVals))
            {
               if (returnVals[0] == null)
               {
                  throw new InvalidSyntaxException(
                     getHelpLib().getMessage("error.clisyntax.missing.value", arg));
               }

               String paperId = returnVals[0].toString();

               if (paperId.equals("user"))
               {
                  try
                  {
                     if (returnVals[1] == null)
                     {
                        throw new InvalidSyntaxException(
                           resources.getMessage(
                              "error.missing_paper_width"));
                     }

                     JDRLength paperWidth = JDRLength.parse(
                        resources.getMessageDictionary(),
                        returnVals[1].toString());

                     if (returnVals[2] == null)
                     {
                        throw new InvalidSyntaxException(
                           resources.getMessage(
                              "error.missing_paper_height"));
                     }

                     JDRLength paperHeight = JDRLength.parse(
                        resources.getMessageDictionary(),
                        returnVals[2].toString());

                     if (paperHeight.getValue() <= 0)
                     {
                        throw new InvalidValueException(
                          InvalidFormatException.LENGTH, returnVals[2].toString(), 
                          resources.getMessageDictionary());
                     }

                     settings.setPaper(new JDRPaper(paperWidth, paperHeight));
                  }
                  catch (InvalidValueException e)
                  {
                     throw new InvalidSyntaxException(
                      getHelpLib().getMessage("error.clisyntax.invalid.syntax", arg), e);
                  }
                  catch (JdrIllegalArgumentException e)
                  {
                     throw new InvalidSyntaxException(
                        resources.getMessage("error.paper_dimension"));
                  }
               }
               else
               {
                  JDRPaper paper = JDRPaper.getPredefinedPaper(paperId);

                  if (paper == null)
                  {
                     throw new InvalidSyntaxException(
                       resources.getMessage(
                          "error.unknown_papersize", paperId));
                  }
                  else
                  {
                     settings.setPaper(paper);
                  }
               }
            }
            else if (isArg(arg, "--in", "-i", returnVals))
            {
               try
               {
                  // Discard any problem file but continue to open
                  // GUI if possible

                  filenames.add((new File(arg)).getCanonicalPath());
               }
               catch (IOException e)
               {
                  resources.error(e.getMessage());
               }
               catch (Exception e)
               {
                  resources.error(e);
               }
            }
            else
            {
               return false;
            }

            return true;
         }
      };
   }

   private void createAndShowGUI()
   throws InvalidSyntaxException
   {
      CLISyntaxParser cliParser = createCLISyntaxParser();
      cliParser.preparse();

      try
      {
         loadStartupInfo();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      startup = new StartupProgress(resources);

      settings = new FlowframTkSettings(resources);

      boolean showWelcome = false;

      try
      {
         showWelcome = loadResources();
      }
      catch (IOException e)
      {
         resources.error((Component)null, e);
         e.printStackTrace();
      }
      catch (URISyntaxException e)
      {
         resources.error((Component)null, e);
         e.printStackTrace();
      }
      catch (Throwable e)
      {
         resources.internalError(null, e);
      }

      // process command line arguments

      cliParser.parseArgs();

      String usersettings = getConfigDirName();

      if (usersettings != null)
      {
         logFile = new File(usersettings, "flowframtk.log");
      }
      
      gui = new FlowframTk(this);

      gui.initialiseGuiAndShow(showWelcome);
   }

   public void showWelcome()
   {
      // new version

      (new WelcomeDialog(this)).setVisible(true);
   }

   public String getName()
   {
      return APP_NAME;
   }

   public String getVersion()
   {
      return JDRResources.APP_VERSION;
   }

   public Vector<String> getFilenames()
   {
      return filenames;
   }

   public boolean isPrintDisabled()
   {
      return disablePrint;
   }

   public File getLogFile()
   {
      return logFile;
   }

   public FlowframTk getGUI()
   {
      return gui;
   }

   public FlowframTkSettings getSettings()
   {
      return settings;
   }

   public boolean isExperimentalMode()
   {
      return experimentalMode;
   }

   public static void main(String[] args)
   {
      final String[] invokerArgs = args;

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            FlowframTkInvoker invoker = null;

            try
            {
               invoker = new FlowframTkInvoker(invokerArgs);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               JOptionPane.showMessageDialog(null, e.toString(),
                 "Error", JOptionPane.ERROR_MESSAGE);
               System.exit(JDRResources.EXIT_FATAL_ERROR);
            }

            JDRResources resources = invoker.getResources();

            try
            {
               JDRGuiMessage.create(resources);
               invoker.createAndShowGUI();
            }
            catch (InvalidSyntaxException e)
            {
               System.err.println(e.getMessage());
               resources.error(e);
               System.exit(JDRResources.EXIT_FATAL_ERROR);
            }
            catch (Throwable e)
            {
               String msg = e.getMessage();

               if (msg == null)
               {
                  msg = e.getClass().getSimpleName();
               }

               System.err.println(msg);

               if (resources == null)
               {
                  e.printStackTrace();
                  System.exit(JDRResources.EXIT_FATAL_ERROR);
               }
               else
               {
                  resources.internalError(null, msg, e);
               }
            }
         }
      });
   }

   public static final String APP_NAME = "FlowframTk";

   private FlowframTkSettings settings;

   private boolean experimentalMode = false;

   private String[] args;

   private Vector<String> filenames;

   private File logFile;

   public boolean disablePrint=false;

   private FlowframTk gui;

   private StartupProgress startup=null;

   private JDRResources resources;
}

