// File          : TeXEditorListener.java
// Description   : Listener for typing TeX code
// Creation Date : 2014-05-15
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

package com.dickimawbooks.flowframtk.dialog;

import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.text.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public interface TeXEditorListener
{
   public void addEdit(UndoableEdit edit);
}
