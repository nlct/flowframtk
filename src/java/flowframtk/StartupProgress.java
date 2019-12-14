// File          : StartUpProgress.java
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
import java.awt.geom.*;
import java.awt.font.*;

import com.dickimawbooks.jdrresources.*;

public class StartupProgress
{
   public StartupProgress(JDRResources resources)
   {
      this.resources = resources;
      renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);

      infoFont = resources.getStartUpInfoFont();

      versionFont = resources.getStartUpVersionFont();

      try
      {
         refresh();
      }
      catch (IllegalStateException e)
      {
      }
   }

   public void refresh() throws IllegalStateException
   {
      Graphics2D g = getGraphics();

      if (g != null && splash.isVisible())
      {
         g.setRenderingHints(renderHints);

         drawInfo(g);
         drawProgress(g);

         g.dispose();

         splash.update();
      }
   }

   public void refreshInfo() throws IllegalStateException
   {
      Graphics2D g = getGraphics();

      if (g != null && splash.isVisible())
      {
         g.setRenderingHints(renderHints);

         drawInfo(g);

         g.dispose();

         splash.update();
      }
   }

   public void refreshProgress() throws IllegalStateException
   {
      Graphics2D g = getGraphics();

      if (g != null && splash.isVisible())
      {
         g.setRenderingHints(renderHints);

         drawProgress(g);

         g.dispose();

         splash.update();
      }
   }

   public Graphics2D getGraphics() throws IllegalStateException
   {
      return splash == null ? null : splash.createGraphics();
   }

   public void drawVersion(Graphics2D g, String version, String copyright,
     String disclaimer)
   {
      g.setPaint(Color.black);

      g.setFont(versionFont);

      Rectangle2D maxChar = infoFont.getMaxCharBounds(g.getFontRenderContext());

      FontMetrics metrics = g.getFontMetrics();

      int y = (int)Math.ceil(metrics.getMaxAscent()+metrics.getLeading())
            + margin;
      int x = ICON_WIDTH+margin;

      g.drawString(version, x, y);

      int dy = g.getFontMetrics().getLeading()+margin/2;

      g.setFont(infoFont);

      maxChar = infoFont.getMaxCharBounds(g.getFontRenderContext());

      y += dy + maxChar.getHeight();

      g.drawString(copyright, x, y);

      y += dy + maxChar.getHeight();

      g.drawString(disclaimer, x, y);

      y += dy + maxChar.getHeight();

      g.drawString("http://www.dickimaw-books.com/", x, y);

      y += dy + maxChar.getHeight();

      g.drawString(getResources().getString("about.see_licence"), x, y);

      String translator = getResources().getString("about.translator", null);

      if (translator != null && !translator.isEmpty())
      {
         String translatedBy = getResources().getStringWithValue(
            "about.translated_by", translator);
         String translatorUrl = getResources().getString(
            "about.translator_url", null);
         String translatorInfo = getResources().getString(
            "about.translator_info", null);

         y = ICON_HEIGHT+margin;
         x = margin;

         g.drawString(translatedBy, x, y);

         if (translatorUrl != null && !translatorUrl.isEmpty())
         {
            y += dy + maxChar.getHeight();
            g.drawString(translatorUrl, x, y);
         }

         if (translatorInfo != null && !translatorInfo.isEmpty())
         {
            y += dy + maxChar.getHeight();
            g.drawString(translatorInfo, x, y);
         }
      }
   }

   public void drawInfo(Graphics2D g) throws IllegalStateException
   {
      if (info == null) return;

      Dimension size = splash.getSize();

      g.setFont(infoFont);
      g.setPaint(background);

      Rectangle2D maxChar = infoFont.getMaxCharBounds(g.getFontRenderContext());

      int y = (int)size.getHeight()-progressHeight-2*margin;

      int clipX = margin;
      int clipY = (int)Math.round(y+maxChar.getY());
      int clipWidth = (int)size.getWidth()-2*margin;
      int clipHeight = (int)Math.round(maxChar.getHeight());

      g.clipRect(margin, clipY, clipWidth, clipHeight);
      g.fillRect(margin, clipY, clipWidth, clipHeight);

      g.setPaint(Color.black);

      g.drawString(info, margin, y);
   }

   public void drawProgress(Graphics2D g) throws IllegalStateException
   {
      Dimension size = splash.getSize();

      int y = size.height-margin-progressHeight;
      double width = size.getWidth()-2.0*margin;

      if (max > 0 && progress > 0)
      {
         g.setPaint(Color.blue);

         g.drawRect(margin, y, (int)width, progressHeight);

         double f = Math.min(1.0, ((double)progress/(double)max));

         g.fillRect(margin, y, (int)Math.round(width*f), progressHeight);
      }
   }

   public void incProgress() throws IllegalStateException
   {
      progress++;
      refreshProgress();
   }

   public void incProgress(String text) throws IllegalStateException
   {
      progress++;
      info = text;
      refresh();
   }

   public void setInfo(String text) throws IllegalStateException
   {
      info = text;
      refreshInfo();
   }

   public void setDeterminate(int n) throws IllegalStateException
   {
      max = n;
      progress = 0;
      refreshProgress();
   }

   public void setIndeterminate() throws IllegalStateException
   {
      max = -1;
      progress = 0;
      refreshProgress();
   }

   public void setVersion(String version, String copyright,
     String disclaimer) throws IllegalStateException
   {
      Graphics2D g = getGraphics();

      if (g != null && splash.isVisible())
      {
         g.setRenderingHints(renderHints);

         drawVersion(g, version, copyright, disclaimer);

         g.dispose();

         splash.update();
      }
   }

   public JDRResources getResources()
   {
      return resources;
   }

   public int getProgress()
   {
      return progress;
   }

   public boolean isVisible()
   {
      return splash.isVisible();
   }

   private final SplashScreen splash = SplashScreen.getSplashScreen();

   private int progress = 0;

   private int max = -1;

   private String info = null;

   private Font infoFont;

   private Font versionFont;

   private RenderingHints renderHints;

   private FlowframTkInvoker invoker;

   private final int progressHeight = 5;

   private final int margin = 12;

   private static final Color background = new Color(125, 244, 206);

   private static final int ICON_WIDTH=100, ICON_HEIGHT=100;

   private JDRResources resources;
}
