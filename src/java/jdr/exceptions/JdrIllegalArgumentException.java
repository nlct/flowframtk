// File          : JdrIllegalArgumentException.java
// Creation Date : 2014-03-27
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
import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when an illegal argument was given.
 * @author Nicola L C Talbot
 */
public class JdrIllegalArgumentException extends IllegalArgumentException
{
   public JdrIllegalArgumentException(String identifier, 
      int found, JDRMessageDictionary msgSys)
   {
      this(identifier, ""+found, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      double found, JDRMessageDictionary msgSys)
   {
      this(identifier, ""+found, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      float found, JDRMessageDictionary msgSys)
   {
      this(identifier, ""+found, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      byte found, JDRMessageDictionary msgSys)
   {
      this(identifier, ""+found, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      char found, JDRMessageDictionary msgSys)
   {
      this(identifier, ""+found, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      String found, JDRMessageDictionary msgSys)
   {
      this(identifier, found, -1, -1, msgSys);
   }

   public JdrIllegalArgumentException(String identifier, 
      String found, JDRMessageDictionary msgSys, Throwable cause)
   {
      this(identifier, found, -1, -1, msgSys, cause);
   }


   public JdrIllegalArgumentException(String identifier, String found, JDRAJR jdr)
   {
      this(identifier, found, jdr.getLineNum(), jdr.getColumnIndex(),
           jdr.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, String found, JDRAJR jdr, Throwable cause)
   {
      this(identifier, found, jdr.getLineNum(), jdr.getColumnIndex(),
           jdr.getMessageDictionary(), cause);
   }

   public JdrIllegalArgumentException(String identifier, 
      int found, CanvasGraphics cg)
   {
      this(identifier, ""+found, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      double found, CanvasGraphics cg)
   {
      this(identifier, ""+found, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      float found, CanvasGraphics cg)
   {
      this(identifier, ""+found, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      byte found, CanvasGraphics cg)
   {
      this(identifier, ""+found, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      char found, CanvasGraphics cg)
   {
      this(identifier, ""+found, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      String found, CanvasGraphics cg)
   {
      this(identifier, found, -1, -1, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String identifier, 
      String found, CanvasGraphics cg, Throwable cause)
   {
      this(identifier, found, -1, -1, cg.getMessageDictionary(), cause);
   }

   /**
    * Initialises with the given message and line number.
    * @param str message
    * @param line the line number
    */

   public JdrIllegalArgumentException(String type, String found, 
      int line, int columnIdx, JDRMessageDictionary msgSys)
   {
      super(msgSys == null ? String.format("Invalid %s", type) :
            msgSys.getMessageWithFallback("error.invalid_"+type, 
            String.format("Invalid %s", type)));

      lineNum = line;
      colIdx = columnIdx;
      setIdentifier(type);
      setFound(found);
   }

   public JdrIllegalArgumentException(String type, String found, 
      int line, int columnIdx, JDRMessageDictionary msgSys, Throwable cause)
   {
      super(msgSys == null ? String.format("Invalid %s", type) :
            msgSys.getMessageWithFallback("error.invalid_"+type, 
            String.format("Invalid %s", type)), cause);

      lineNum = line;
      colIdx = columnIdx;
      setIdentifier(type);
      setFound(found);
   }

   public JdrIllegalArgumentException(String type, String found, 
      int line, int columnIdx, CanvasGraphics cg)
   {
      this(type, found, line, columnIdx, cg.getMessageDictionary());
   }

   public JdrIllegalArgumentException(String type, String found, 
      int line, int columnIdx, CanvasGraphics cg, Throwable cause)
   {
      this(type, found, line, columnIdx, cg.getMessageDictionary(), cause);
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

   public int getColIndex()
   {
      return colIdx;
   }

   public void setIdentifier(String name)
   {
      identifier = name;
   }

   public String getIdentifier()
   {
      return identifier;
   }

   public String getFound()
   {
      return found;
   }

   public void setFound(String found)
   {
      this.found = found;
   }

   public String getMessage(JDRMessageDictionary msgSys)
   {
      String msg = getMessage();

      if (msg == null)
      {
         return getMessage();
      }

      if (found != null)
      {
         msg = msgSys.getMessageWithFallback(
          "error.with_found",
          "{0} (found ''{1}'')",
           msg, found);
      }

      if (lineNum > -1)
      {
         if (colIdx > -1)
         {
            msg = msgSys.getMessageWithFallback(
                  "error.with_line_and_col",
                  "Line {0}, Column {1}: {2}",
                  lineNum, colIdx, msg);
         }
         else
         {
            msg = msgSys.getMessageWithFallback(
                  "error.with_line",
                  "Line {0}: {1}",
                  lineNum, msg);
         }
      }

      return msg;
   }

   private int lineNum=-1, colIdx=-1;

   private String found, identifier;

   public static final String FONT_WEIGHT = "font-weight";
   public static final String FONT_SHAPE = "font-shape";
   public static final String FONT_SIZE = "font-size";
   public static final String PEN_WIDTH = "pen-width";
   public static final String WINDING_RULE = "winding-rule";
   public static final String MITRE_LIMIT = "mitre-limit";
   public static final String CAP_STYLE = "cap-style";
   public static final String JOIN_STYLE = "join-style";
   public static final String REPEAT = "repeat";
   public static final String MARKER_ID = "marker-id";
   public static final String COMPOSITE_MARKER = "composite-marker";
   public static final String FADE = "fade";
   public static final String RED = "red";
   public static final String GREEN = "green";
   public static final String BLUE = "blue";
   public static final String ALPHA = "alpha";
   public static final String CYAN = "cyan";
   public static final String MAGENTA = "magenta";
   public static final String YELLOW = "yellow";
   public static final String BLACK = "black";
   public static final String GREY = "grey";
   public static final String HUE = "hue";
   public static final String SATURATION = "saturation";
   public static final String BRIGHTNESS = "brightness";
   public static final String SHADING_START = "shading-start";
   public static final String SHADING_END = "shading-end";
   public static final String SHADING_MID = "shading-mid";
   public static final String SHADING_DIRECTION = "shading-direction";
   public static final String SHADING_LOCATION = "shading-location";
   public static final String CONVERT_SHADING = "convert-shading";

   public static final String GRID_LISTENER = "grid-listener";
   public static final String PAINT_LISTENER = "paint-listener";
   public static final String PATH_STYLE_LISTENER = "path-style-listener";
   public static final String OBJECT_LISTENER = "object-listener";
   public static final String SEGMENT_LISTENER = "segment-listener";

   public static final String SETTINGS_ID = "settings-id";
   public static final String VERSION = "version";

   public static final String HALIGN = "halign";
   public static final String VALIGN = "valign";

   public static final String PATTERN_REPLICAS = "pattern-replicas";
   public static final String PATTERN_SCALE_X = "pattern-scale-x";
   public static final String PATTERN_SCALE_Y = "pattern-scale-y";

   public static final String GRID_ID = "grid-id";
   public static final String GRID_MAJOR = "grid-major";
   public static final String GRID_SUBDIVISIONS = "grid-subdivisions";
   public static final String GRID_SPOKES = "grid-spokes";

   public static final String FRAME_TYPE = "frame-type";
   public static final String FRAME_SHAPE = "frame-shape";
   public static final String FRAME_VALIGN = "frame-valign";
   public static final String FRAME_CONTENTS_TYPE = "frame-contents-type";

   public static final String PAPER_ID = "paper-id";
   public static final String PAPER_WIDTH = "paper-width";
   public static final String PAPER_HEIGHT = "paper-height";

   public static final String SETTING_TOOL_ID = "setting-tool-id";
   public static final String SETTING_TOOL_NAME = "setting-tool-name";
   public static final String SETTING_POINTSIZE = "setting-point-size";
   public static final String SETTING_NORMALSIZE = "setting-normal-size";
   public static final String USE_SETTINGS_ON_LOAD_ID = "use-settings-on-load-id";

   public static final String UNIT_ID = "unit-id";

   public static final String ANGLE_ID = "angle-id";

   public static final String RADIUS = "radius";

   public static final String UNSUPPORTED_VERSION = "unsupported-version";

   public static final String CLOSE_TYPE = "close-type";
   public static final String OPTIMIZE = "optimize";
}

