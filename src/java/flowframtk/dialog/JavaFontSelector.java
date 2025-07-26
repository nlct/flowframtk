// File          : JavaFontSelector.java
// Description   : Component for selecting Java font
// Creation Date : 2015-09-22
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2015-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.JLabelGroup;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;
import com.dickimawbooks.flowframtk.*;

/**
 * Component for selecting Font.
 */

public class JavaFontSelector extends JPanel
  implements ChangeListener,ActionListener
{
   public JavaFontSelector(FlowframTk application,
     JLabel nameLabel, String boldParentId, String italicParentId,
     String sizeId)
   {
      this(application, (String)null, boldParentId, italicParentId, sizeId);

      if (nameLabel != null)
      {
         this.nameLabel = nameLabel;
         nameLabel.setLabelFor(nameBox);
      }
   }

   public JavaFontSelector(FlowframTk application,
     String nameId, String boldId, String italicId,
     String sizeId)
   {
      super(new GridBagLayout());
      JDRResources resources = application.getResources();

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor=GridBagConstraints.BASELINE_LEADING;
      gbc.gridx=0;
      gbc.gridy=0;

      nameBox = new JComboBox<String>(application.getFontFamilies());
      nameBox.addActionListener(this);

      if (nameId != null)
      {
         nameLabel = resources.createAppLabel(nameId);
         nameLabel.setLabelFor(nameBox);
         add(nameLabel, gbc);
         gbc.gridx++;
      }

      add(nameBox, gbc);

      gbc.gridy++;

      JComponent attrComp = new JPanel(new FlowLayout(FlowLayout.LEADING));
      add(attrComp, gbc);

      if (boldId != null)
      {
         boldBox = resources.createDialogToggle(boldId, "bold", this);
         attrComp.add(boldBox);
      }

      if (italicId != null)
      {
         italicBox = resources.createDialogToggle(italicId, "italic", this);
         attrComp.add(italicBox);
      }

      sizeModel = new SpinnerNumberModel(10, 1, 100, 1);
      JSpinner sizeField = new JSpinner(sizeModel);
      sizeField.addChangeListener(this);

      if (sizeId != null)
      {
         JLabel sizeLabel = resources.createAppLabel(sizeId);
         sizeLabel.setLabelFor(sizeField);
         attrComp.add(sizeLabel);
      }

      attrComp.add(sizeField);

      sampleLabel = new JLabel(resources.getMessage("font.sample"));
      attrComp.add(sampleLabel);
   }

   public JavaFontSelector(FlowframTk application,
     JLabelGroup labelGrp,
     String nameId, String boldId, String italicId,
     String sizeId)
   {
      super(null);
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      JDRResources resources = application.getResources();

      JComponent row = new JPanel(new FlowLayout(FlowLayout.LEADING));
      add(row);

      nameBox = new JComboBox<String>(application.getFontFamilies());
      nameBox.addActionListener(this);
      nameBox.setMaximumSize(nameBox.getPreferredSize());

      if (nameId != null)
      {
         nameLabel = resources.createAppLabel(nameId);
         nameLabel.setLabelFor(nameBox);
         labelGrp.add(nameLabel);
         row.add(nameLabel);
      }

      row.add(nameBox);

      JComponent attrComp = new JPanel(new FlowLayout(FlowLayout.LEADING));
      add(attrComp);

      if (nameLabel != null)
      {
         attrComp.add(new JPanel()
          {
            @Override
            public Dimension getPreferredSize()
            {
               return nameLabel.getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize()
            {
               return nameLabel.getPreferredSize();
            }
          });
      }

      if (boldId != null)
      {
         boldBox = resources.createDialogToggle(boldId, "bold", this);
         attrComp.add(boldBox);
      }

      if (italicId != null)
      {
         italicBox = resources.createDialogToggle(italicId, "italic", this);
         attrComp.add(italicBox);
      }

      sizeModel = new SpinnerNumberModel(10, 1, 100, 1);
      JSpinner sizeField = new JSpinner(sizeModel);
      sizeField.addChangeListener(this);

      if (sizeId != null)
      {
         JLabel sizeLabel = resources.createAppLabel(sizeId);
         sizeLabel.setLabelFor(sizeField);
         attrComp.add(sizeLabel);
      }

      attrComp.add(sizeField);

      sampleLabel = new JLabel(resources.getMessage("font.sample"));
      attrComp.add(sampleLabel);

      Dimension dim = getMaximumSize();
      dim.height = getPreferredSize().height;
      setMaximumSize(dim);
   }

   public void setSelectedFont(Font f)
   {
      nameBox.setSelectedItem(f.getName());
      sizeModel.setValue(new Integer(f.getSize()));

      if (boldBox != null)
      {
         boldBox.setSelected(f.isBold());
      }

      if (italicBox != null)
      {
         italicBox.setSelected(f.isItalic());
      }

      sampleLabel.setFont(f);
   }

   public Font getSelectedFont()
   {
      int style = Font.PLAIN;

      if (boldBox != null && boldBox.isSelected())
      {
         style = style | Font.BOLD;
      }

      if (italicBox != null && italicBox.isSelected())
      {
         style = style | Font.ITALIC;
      }

      return new Font((String)nameBox.getSelectedItem(),
        style, sizeModel.getNumber().intValue());
   }

   public void stateChanged(ChangeEvent e)
   {
      sampleLabel.setFont(getSelectedFont());
   }

   public void actionPerformed(ActionEvent e)
   {
      sampleLabel.setFont(getSelectedFont());
   }

   private JComboBox<String> nameBox;
   private SpinnerNumberModel sizeModel;
   private JDRToggleButton boldBox=null, italicBox=null;
   private JLabel sampleLabel, nameLabel;
}
