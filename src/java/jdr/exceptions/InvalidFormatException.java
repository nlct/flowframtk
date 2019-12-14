// File          : InvalidFormatException.java
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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

package com.dickimawbooks.jdr.exceptions;

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

/**
 * Exception thrown when JDR information is incorrectly formatted.
 * @author Nicola L C Talbot
 */
public class InvalidFormatException extends Exception
{
   /**
    * Initialises with the given message.
    */
   public InvalidFormatException(String str)
   {
      this(str, -1);
   }

   public InvalidFormatException(String str, Throwable cause)
   {
      this(str, -1, cause);
   }

   public InvalidFormatException(String str, int lineNum)
   {
      this(str, lineNum, -1);
   }

   public InvalidFormatException(String str, JDRAJR jdr)
   {
      this(str,
           jdr == null ? -1 : jdr.getLineNum(),
           jdr == null ? -1 : jdr.getColumnIndex());
   }

   public InvalidFormatException(String str, int lineNum, Throwable cause)
   {
      this(str, lineNum, -1, cause);
   }

   public InvalidFormatException(String str, JDRAJR jdr, Throwable cause)
   {
      this(str, jdr.getLineNum(), jdr.getColumnIndex(), cause);
   }

   /**
    * Initialises with the given message and line number.
    * @param str message
    * @param line the line number
    */
   public InvalidFormatException(String str, int line, int columnIdx)
   {
      super(str);

      lineNum = line;
      colIdx = columnIdx;
   }

   public InvalidFormatException(String str, int line, int columnIdx, Throwable cause)
   {
      super(str, cause);

      lineNum = line;
      colIdx = columnIdx;
   }

   /**
    * Gets the number of the line being processed when exception
    * was thrown.
    * @return line number or -1 if none specified
    */
   public int getLineNum()
   {
      return lineNum;
   }

   public void setLineNum(int num)
   {
      lineNum = num;
   }

   public int getColumnIndex()
   {
      return colIdx;
   }

   public void setIdentifier(Object name)
   {
      identifier = (name == null ? null : name.toString());
   }

   public String getMessageWithIdentifier(JDRMessageDictionary msgSys)
   {
      String msg = getMessage();

      if (identifier == null)
      {
         return msg;
      }

      if (msgSys == null)
      {
         return String.format("%s (%s)", msg, identifier);
      }

      return msgSys.getStringWithValues("error.with_id",
        new String[]{msg, identifier},
        String.format("%s (%s)", msg, identifier));
   }

   public String getMessageWithIndex(JDRMessageDictionary msgSys)
   {
      String msg = getMessageWithIdentifier(msgSys);

      if (colIdx > -1)
      {
         if (msgSys == null)
         {
            return String.format("Line %d, Column %d: %s", lineNum, colIdx, msg);
         }

         return msgSys.getStringWithValues(
           "error.with_line_and_col",
           new String[]{
             String.format("%d", lineNum),
             String.format("%d", colIdx),
             msg
           },
           String.format("Line %d, Column %d: %s", lineNum, colIdx, msg));
      }

      if (lineNum > 0)
      {
         if (msgSys == null)
         {
            return String.format("Line %d: %s", lineNum, msg);
         }

         return msgSys.getStringWithValues(
           "error.with_line",
           new String[] {String.format("%d", lineNum), msg},
           String.format("Line %d: %s", lineNum, msg));
      }

      return msg;
   }

   private String identifier = null;

   private int lineNum=-1, colIdx=-1;

   public static final String PARSE_BOOLEAN = "parse-boolean";
   public static final String PARSE_INT = "parse-int";
   public static final String PARSE_CHAR = "parse-char";
   public static final String PARSE_STRING = "parse-string";
   public static final String PARSE_BYTE = "parse-byte";
   public static final String PARSE_FLOAT = "parse-float";
   public static final String PARSE_DOUBLE = "parse-double";
   public static final String PARSE_FORMAT = "parse-format";

   public static final String DESCRIPTION = "description";
   public static final String CAP_STYLE = "cap-style";
   public static final String JOIN_STYLE = "join-style";
   public static final String MITRE_LIMIT = "mitre-limit";
   public static final String WINDING_STYLE = "winding-rule";
   public static final String HALIGN_STYLE = "halign";
   public static final String VALIGN_STYLE = "valign";
   public static final String UNIT_ID = "unit-id";
   public static final String UNIT_NAME = "unit-name";
   public static final String LENGTH = "length";
   public static final String SETTINGS_ID = "settings-id";
   public static final String MARKER_ID = "marker-id";
   public static final String MARKER_SIZE = "marker-size";
   public static final String MARKER_REPEAT = "marker-repeat";
   public static final String MARKER_REVERSED = "marker-reversed";
   public static final String MARKER_AUTO_ORIENT = "marker-auto-orient";
   public static final String MARKER_ORIENT_ANGLE = "marker-orient-angle";
   public static final String MARKER_OVERLAY_FLAG = "marker-overlay-flag";
   public static final String MARKER_OFFSET_FLAG = "marker-offset-flag";
   public static final String MARKER_OFFSET = "marker-offset";
   public static final String MARKER_REPEAT_OFFSET_FLAG = "marker-repeat-offset-flag";
   public static final String MARKER_REPEAT_OFFSET = "marker-repeat-offset";

   public static final String DASH_PATTERN = "dash-pattern";
   public static final String DASH_OFFSET = "dash-offset";
   public static final String LATEX_TEXT = "LaTeX-text";
   public static final String TEXT = "text";
   public static final String VERSION = "version";
   public static final String FORMAT_TAG = "format-tag";
   public static final String INVALID_JDR_FORMAT = "jdr-format";

   public static final String FONT_FAMILY = "font-family";
   public static final String FONT_WEIGHT = "font-weight";
   public static final String FONT_SHAPE = "font-shape";
   public static final String FONT_SIZE = "font-size";
   public static final String FONT_TRANSFORM = "font-transform";
   public static final String TEXT_OUTLINE_FLAG = "text-outline-flag";

   public static final String LATEX_FONT_FLAG = "LaTeX-font-flag";
   public static final String LATEX_FONT_FAMILY = "LaTeX-font-family";
   public static final String LATEX_FONT_WEIGHT = "LaTeX-font-weight";
   public static final String LATEX_FONT_SHAPE = "LaTeX-font-shape";
   public static final String LATEX_FONT_SIZE = "LaTeX-font-size";
   public static final String BITMAP_FILENAME = "bitmap-filename";
   public static final String BITMAP_LATEX_FLAG = "bitmap-LaTeX-flag";
   public static final String BITMAP_TRANSFORM = "bitmap-transform";
   public static final String LATEX_FILENAME = "LaTeX-filename";
   public static final String LATEX_IMAGECMD = "LaTeX-image-cmd";

   public static final String GRID_ID = "grid-id";
   public static final String PAINT_ID = "paint-id";
   public static final String OBJECT_ID = "object-id";
   public static final String PATH_STYLE_ID = "path-style-id";
   public static final String SEGMENT_ID = "segment-id";

   public static final String PEN_WIDTH = "pen-width";

   public static final String GRID_MAJOR = "grid-major";
   public static final String GRID_MINOR = "grid-minor";
   public static final String GRID_SPOKES = "grid-spokes";

   public static final String BEZIER_X0 = "bezier-x0";
   public static final String BEZIER_Y0 = "bezier-y0";
   public static final String BEZIER_X1 = "bezier-x1";
   public static final String BEZIER_Y1 = "bezier-y1";
   public static final String BEZIER_C1X = "bezier-c1x";
   public static final String BEZIER_C1Y = "bezier-c1y";
   public static final String BEZIER_C2X = "bezier-c2x";
   public static final String BEZIER_C2Y = "bezier-c2y";

   public static final String LINE_X0 = "line-x0";
   public static final String LINE_Y0 = "line-y0";
   public static final String LINE_X1 = "line-x1";
   public static final String LINE_Y1 = "line-y1";

   public static final String SEGMENT_X0 = "move-x0";
   public static final String SEGMENT_Y0 = "move-y0";
   public static final String SEGMENT_X1 = "move-x1";
   public static final String SEGMENT_Y1 = "move-y1";

   public static final String PARTIAL_BEZIER_CX = "partial-bezier-cx";
   public static final String PARTIAL_BEZIER_CY = "partial-bezier-cy";

   public static final String ALPHA = "alpha";

   public static final String RED = "red";
   public static final String GREEN = "green";
   public static final String BLUE = "blue";

   public static final String CYAN = "cyan";
   public static final String MAGENTA = "magenta";
   public static final String YELLOW = "yellow";
   public static final String BLACK = "black";

   public static final String HUE = "hue";
   public static final String SATURATION = "saturation";
   public static final String BRIGHTNESS = "brightness";

   public static final String GRAY = "gray";

   public static final String CMYK = "cmyk";
   public static final String HSB = "hsb";
   public static final String RGB = "rgb";

   public static final String GRADIENT_START = "gradient-start";
   public static final String GRADIENT_END = "gradient-end";
   public static final String GRADIENT_DIRECTION = "gradient-direction";

   public static final String RADIAL_GRADIENT_START = "radial-gradient-start";
   public static final String RADIAL_GRADIENT_END = "radial-gradient-end";
   public static final String RADIAL_GRADIENT_LOCATION = "radial-gradient-location";

   public static final String GROUP_SIZE = "group-size";

   public static final String PATH_OPEN_CLOSE_ID = "path-open-close-id";
   public static final String PATH_SIZE = "path-size";
   public static final String PATH_START_X = "path-start-x";
   public static final String PATH_START_Y = "path-start-y";

   public static final String PATH_ANCHOR_INDEX = "path-anchor-index";

   public static final String ROTATIONAL_SHAPE = "rotational-shape";
   public static final String ROTATIONAL_ANCHOR_X = "rotational-anchor-x";
   public static final String ROTATIONAL_ANCHOR_Y = "rotational-anchor-y";
   public static final String ROTATIONAL_ANGLE = "rotational-angle";
   public static final String ROTATIONAL_REPLICAS = "rotational-replicas";
   public static final String ROTATIONAL_SINGLE = "rotational-single";
   public static final String ROTATIONAL_SHOW_ORIGINAL = "rotational-show-original";

   public static final String SCALED_SHAPE = "scaled-shape";
   public static final String SCALED_ANCHOR_X = "scaled-anchor-x";
   public static final String SCALED_ANCHOR_Y = "scaled-anchor-y";
   public static final String SCALED_ADJUST_X = "scaled-adjust-x";
   public static final String SCALED_ADJUST_Y = "scaled-adjust-y";
   public static final String SCALED_FACTOR_X = "scaled-factor-x";
   public static final String SCALED_FACTOR_Y = "scaled-factor-y";
   public static final String SCALED_REPLICAS = "scaled-replicas";
   public static final String SCALED_SINGLE = "scaled-single";
   public static final String SCALED_SHOW_ORIGINAL = "scaled-show-original";

   public static final String SPIRAL_SHAPE = "spiral-shape";
   public static final String SPIRAL_ANCHOR_X = "spiral-anchor-x";
   public static final String SPIRAL_ANCHOR_Y = "spiral-anchor-y";
   public static final String SPIRAL_ADJUST_X = "spiral-adjust-x";
   public static final String SPIRAL_ADJUST_Y = "spiral-adjust-y";
   public static final String SPIRAL_ANGLE = "spiral-angle";
   public static final String SPIRAL_DISTANCE = "spiral-distance";
   public static final String SPIRAL_REPLICAS = "spiral-replicas";
   public static final String SPIRAL_SINGLE = "spiral-single";
   public static final String SPIRAL_SHOW_ORIGINAL = "spiral-show-original";

   public static final String SYMMETRIC_SHAPE = "symmetric-shape";
   public static final String SYMMETRIC_ANCHORED = "symmetric-anchored";
   public static final String SYMMETRIC_LINE_X0 = "symmetric-line-x0";
   public static final String SYMMETRIC_LINE_X1 = "symmetric-line-x1";
   public static final String SYMMETRIC_LINE_Y0 = "symmetric-line-y0";
   public static final String SYMMETRIC_LINE_Y1 = "symmetric-line-y1";

   public static final String SYMMETRIC_CLOSED = "symmetric-closed";
   public static final String SYMMETRIC_CLOSE_ANCHORED = "symmetric-close-anchored";

   public static final String TEXT_PATH_SHAPE = "text-path-shape";

   public static final String TEXT_PATH_VERSION_UNSUPPORTED = "text-path-unsupported-version";
   public static final String TEXT_PATH_TRANSFORM = "text-path-transform";
   public static final String TEXT_PATH_LATEX_FLAG = "text-path-LaTeX-flag";
   public static final String TEXT_PATH_HALIGN = "text-path-halign";
   public static final String TEXT_PATH_VALIGN = "text-path-valign";
   public static final String TEXT_PATH_LATEX_TEXT = "text-path-LaTeX-text";
   public static final String TEXT_PATH_TEXT = "text-path-text";
   public static final String TEXT_PATH_DELIM_L = "text-path-delim-left";
   public static final String TEXT_PATH_DELIM_R = "text-path-delim-right";

   public static final String FRAME_FLAG = "frame-flag";
   public static final String FRAME_TYPE = "frame-type";
   public static final String FRAME_BORDER_FLAG = "frame-border-flag";
   public static final String FRAME_IDL = "frame-idl";
   public static final String FRAME_PAGELIST = "frame-page-list";
   public static final String FRAME_MARGIN_TOP = "frame-margin-top";
   public static final String FRAME_MARGIN_BOTTOM = "frame-margin-bottom";
   public static final String FRAME_MARGIN_LEFT = "frame-margin-left";
   public static final String FRAME_MARGIN_RIGHT = "frame-margin-right";
   public static final String FRAME_SHAPE = "frame-shape";
   public static final String FRAME_VALIGN = "frame-valign";
   public static final String FRAME_CONTENTS = "frame-contents";
   public static final String FRAME_EVEN_X_SHIFT = "frame-even-x-shift";
   public static final String FRAME_EVEN_Y_SHIFT = "frame-even-y-shift";

   public static final String PAPER_ID = "paper-id";
   public static final String PAPER_WIDTH = "paper-width";
   public static final String PAPER_HEIGHT = "paper-height";
   public static final String PAPER_PORTRAIT_FLAG = "paper-portrait-flag";

   public static final String SETTING_SHOW_GRID = "setting-show-grid";
   public static final String SETTING_GRID_LOCK = "setting-grid-lock";
   public static final String SETTING_SHOW_RULERS = "setting-show-rulers";
   public static final String SETTING_TOOL = "setting-tool";
   public static final String SETTING_NORMALSIZE = "setting-normalsize";
   public static final String SETTING_POINTSIZE = "setting-pointsize";
   public static final String SETTING_SCALE_POINT = "setting-scale-control";
   public static final String SETTING_PREAMBLE = "setting-preamble";
   public static final String SETTING_MID_PREAMBLE = "setting-mid-preamble";
   public static final String SETTING_END_PREAMBLE = "setting-end-preamble";
   public static final String SETTING_DOCCLASS = "setting-cls";
   public static final String SETTING_ABS_PAGES = "setting-absolute-pages";

   public static final String ANGLE_VALUE = "angle-value";
   public static final String ANGLE_ID = "angle-id";
   public static final String ANGLE = "angle";
}

