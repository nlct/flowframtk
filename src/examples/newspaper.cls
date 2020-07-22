% Created by FlowframTk version 0.8
% 18-Oct-2015 21:57:28
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{newspaper}
\RequirePackage{pgf}
\RequirePackage{ifpdf}
\newcommand*{\jdroutline}[3]{%
  \GenericWarning{}{text outline can't be implemented}#3%
}%
\ifpdf
 \let\jdrorgmod\mod
 \InputIfFileExists{pdf-trans}%
 {%
   \renewcommand*{\jdroutline}[3]{%
     {\def\mod{\expandtwonumexprafter \modulo}%
     \setbox\@tempboxa\hbox{##3}%
     \boxgs{##1}{}\copy\@tempboxa
     }%
   }%
 }{}
 \let\mod\jdrorgmod
\else
 \IfFileExists{pst-char.sty}%
 {%
   \usepackage{pst-char}
   \renewcommand*{\jdroutline}[3]{%
     \begin{pspicture}(0,0)
     \pscharpath[##2]{##3}
     \end{pspicture}
   }
 }{}
\fi
\RequirePackage[utf8]{inputenc}
\RequirePackage[T1]{fontenc}
\RequirePackage{lmodern}

\DeclareOption{draft}{\PassOptionsToPackage{draft}{flowfram}}
\DeclareOption{final}{\PassOptionsToPackage{final}{flowfram}}
\DeclareOption{rotate}{\PassOptionsToPackage{rotate}{flowfram}}
\DeclareOption{norotate}{\PassOptionsToPackage{norotate}{flowfram}}
\DeclareOption{ttbtitle}{\PassOptionsToPackage{ttbtitle}{flowfram}}
\DeclareOption{ttbnotitle}{\PassOptionsToPackage{ttbnotitle}{flowfram}}
\DeclareOption{ttbnum}{\PassOptionsToPackage{ttbnum}{flowfram}}
\DeclareOption{ttbnonum}{\PassOptionsToPackage{ttbnonum}{flowfram}}
\DeclareOption{color}{\PassOptionsToPackage{color}{flowfram}}
\DeclareOption{nocolor}{\PassOptionsToPackage{nocolor}{flowfram}}
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}
\ProcessOptions
\LoadClass[12pt]{article}
\RequirePackage[a4paper,portrait]{geometry}
\geometry{lmargin=58.0pt,rmargin=58.0pt,tmargin=58.0pt,bmargin=58.0pt}
\setlength{\evensidemargin}{\oddsidemargin}
\addtolength{\evensidemargin}{54.0pt}
\RequirePackage{flowfram}

\renewcommand*{\@ff@pages@countreg}{\c@page}
\newstaticframe[all]{479.503784pt}{88.003754pt}{-0.501877pt}{641.544974pt}[title]

\setstaticframe*{title}{valign=c}
\setstaticcontents*{title}{\begin{center}
\bfseries\Huge
Fairy Tale Times
\end{center}
\hfill Issue 2. 7 December 2005.}
\newdynamicframe[all]{233.003738pt}{30.003754pt}{-0.501877pt}{598.044974pt}[leftheading]

\setdynamicframe*{leftheading}{valign=c}
\setdynamiccontents*{leftheading}{\section{Killer Wolf on the Loose}}
\newdynamicframe[all]{233.003754pt}{30.003754pt}{245.998138pt}{598.044974pt}[rightheading]

\setdynamicframe*{rightheading}{valign=c}
\setdynamiccontents*{rightheading}{\section{Tragic Wall Accident}}
\newdynamicframe[all]{479.503784pt}{305.503723pt}{-0.501877pt}{279.045004pt}[lefttext]

\setdynamicframe*{lefttext}{shape={\parshape=21
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt }}
\setdynamicframe*{lefttext}{valign=t}
\setdynamiccontents*{lefttext}{The authorities are warning of a killer wolf on the
loose. He has so far devoured an old grandmother and
two pig brothers. He is described as being furry with
big eyes and big teeth.

On Monday this week he broke into a house, and devoured
an old lady. He then disguised himself as the old lady
in order to deceive her granddaughter. Luckily for the little
girl a woodsman arrived in time to rescue her. Parents are
being cautioned not to let their children wander about on
their own, and to remind them not to talk to strangers.

The next day the wolf struck again, this time targeting two
pig brothers who had most incautiously made their dwellings
on the cheap using inadequate materials. The wolf also made
an attempt on the third pig brother, but was unable to break
into his house.

Police are appealing to the public for witnesses, and remind
people to keep their doors securely fastened at all times.

``Always ask to see identification,'' said one police advisor,
``and invest in improving the general security of your property.''
}
\newstaticframe[all]{71.266251pt}{76.285004pt}{414.8pt}{504.761847pt}[eggimage]

% Border command for frame 'eggimage'
\expandafter\def\csname @flf@border@eggimage\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{71.266251pt}{76.285004pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgftransformcm{1.0}{0.0}{0.0}{1.0}{\pgfpoint{0.0pt}{76.285004pt}}
\pgflowlevelsynccm
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[top,left]{\pgfimage[width=71.26625pt,height=76.285pt]{egg}}}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{eggimage}{offset=0pt,border={@flf@border@eggimage}}

\setstaticframe*{eggimage}{valign=c}
\newdynamicframe[all]{233.003754pt}{204.003738pt}{245.998138pt}{380.544989pt}[rightext]

\setdynamicframe*{rightext}{shape={\parshape=14
0.0pt 189.0pt 
0.0pt 174.0pt 
0.0pt 160.0pt 
0.0pt 160.0pt 
0.0pt 174.0pt 
0.0pt 174.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt }}
\setdynamicframe*{rightext}{valign=t}
\setdynamiccontents*{rightext}{An egg person tragically fell from a six foot wall yesterday
afternoon and was smashed to pieces. The king’s cavalry rushed
to the scene, but regretted that they were unable to help him.

Humpty Dumpty was believed to be sitting on the wall when he fell.
Police have ruled out foul play, but
are advising people not to play on high walls, particularly
those vulnerable members of the population suffering from
eggshell syndrome.

\small\em
Exclusive interview with one of the King's men on page 6.
}
\newstaticframe[all]{239.897797pt}{254.954056pt}{238.60733pt}{372.999182pt}[Lbar]

% Border command for frame 'Lbar'
\expandafter\def\csname @flf@border@Lbar\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{239.897797pt}{254.954056pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{0.50267pt}{254.451418pt}}
\pgfpathlineto{\pgfpoint{0.64267pt}{0.797668pt}}
\pgfpathlineto{\pgfpoint{239.39517pt}{0.502668pt}}
\definecolor{strokepaint}{rgb}{0.0,0.0,0.0}\pgfsetstrokecolor{strokepaint}
\pgfusepath{stroke}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{Lbar}{offset=0pt,border={@flf@border@Lbar}}

\setstaticframe*{Lbar}{valign=c}
\newstaticframe[all]{478.788757pt}{1.003784pt}{-0.284378pt}{271.621191pt}[hline]

% Border command for frame 'hline'
\expandafter\def\csname @flf@border@hline\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{478.788757pt}{1.003784pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{0.501878pt}{0.501909pt}}
\pgfpathlineto{\pgfpoint{478.286878pt}{0.501909pt}}
\definecolor{strokepaint}{rgb}{0.0,0.0,0.0}\pgfsetstrokecolor{strokepaint}
\pgfusepath{stroke}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{hline}{offset=0pt,border={@flf@border@hline}}

\setstaticframe*{hline}{valign=c}
\newdynamicframe[all]{479.503784pt}{30.003784pt}{-0.501877pt}{235.544958pt}[bottomheading]

\setdynamicframe*{bottomheading}{valign=t}
\setdynamiccontents*{bottomheading}{\section{Relief as Missing Sheep
Finally Return Home}}
\newflowframe[all]{233.003738pt}{218.503784pt}{-0.501877pt}{3.544958pt}[left]

\newflowframe[all]{233.003754pt}{218.503784pt}{245.998138pt}{3.544958pt}[right]

\newstaticframe[all]{58.217499pt}{46.172501pt}{-8.0pt}{184.87435pt}[sheepimage]

% Border command for frame 'sheepimage'
\expandafter\def\csname @flf@border@sheepimage\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{58.217499pt}{46.172501pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgftransformcm{1.0}{0.0}{0.0}{1.0}{\pgfpoint{0.0pt}{46.172501pt}}
\pgflowlevelsynccm
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[top,left]{\pgfimage[width=58.2175pt,height=46.1725pt]{sheep}}}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{sheepimage}{offset=0pt,border={@flf@border@sheepimage}}

\setstaticframe*{sheepimage}{valign=c}
\newdynamicframe[all]{30.003784pt}{30.003784pt}{492.498108pt}{-39.955042pt}[footer]

\setdynamicframe*
{footer}{evenx=-40.994018pt,eveny=-39.955042pt}

\setdynamicframe*{footer}{valign=c}
\setdynamiccontents*{footer}{\itshape}
\renewcommand{\@dothefoot}{}%
\renewcommand{\@dodynamicthefoot}{%
 \setdynamiccontents*{footer}{\@thefoot}%
}%
\renewcommand*{\thispagestyle}[1]{%
  \@ifundefined{ps@#1}\undefinedpagestyle
  {%
    \global\@specialpagetrue
    \gdef\@specialstyle {#1}%
    \@ifundefined{thisps@extra@#1}%
     {\@nameuse{thisps@extra@other}}%
     {\@nameuse{thisps@extra@#1}}%
  }%
}
\newcommand*{\thisps@extra@other}{%
 \setdynamicframe*{footer}{hidethis=false}%
}
\newcommand*{\thisps@extra@empty}{%
 \setdynamicframe*{footer}{hidethis=true}%
}
\newcommand*{\thisps@extra@plain}{%
  \setdynamicframe*{footer}{hidethis=false}%
}
  \newcommand*{\thisps@extra@headings}{%
    \setdynamicframe*{footer}{hidethis=true}%
  }%
  \newcommand*{\thisps@extra@myheadings}{%
    \setdynamicframe*{footer}{hidethis=true}%
  }%
\renewcommand*{\pagestyle}[1]{%
  \@ifundefined{ps@#1}\undefinedpagestyle
  {%
    \@nameuse{ps@#1}%
    \@ifundefined{ps@extra@#1}%
     {\@nameuse{ps@extra@other}}%
     {\@nameuse{ps@extra@#1}}%
  }%
}
\newcommand*{\ps@extra@other}{%
 \setdynamicframe*{footer}{hide=false}%
}
\newcommand*{\ps@extra@empty}{%
 \setdynamicframe*{footer}{hide=true}%
}
\renewcommand*{\ps@plain}{%
  \let\@mkboth\@gobbletwo
  \let\@oddhead\@empty
  \let\@evenhead\@empty
  \def\@oddfoot{\flowframtkoddfooterfmt{\itshape\thepage}}%
  \def\@evenfoot{\flowframtkevenfooterfmt{\itshape\thepage}}%
}
\newcommand*{\ps@extra@plain}{%
  \setdynamicframe*{footer}{hide=false}%
}
\providecommand*{\jdrheadingfmt}{\slshape}
\providecommand*{\jdrheadingcase}{\MakeUppercase}
\providecommand*{\flowframtkoddheaderfmt}[1]{\reset@font\hfill#1}
\providecommand*{\flowframtkevenheaderfmt}[1]{\reset@font#1\hfill}
\providecommand*{\flowframtkoddfooterfmt}[1]{\reset@font\hfil#1\hfil}
\providecommand*{\flowframtkevenfooterfmt}[1]{\reset@font\hfil#1\hfil}
\@ifundefined{chapter}
{%
  \renewcommand*{\ps@headings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{{\jdrheadingfmt\rightmark}\hfill\thepage}%
    \let\@evenhead\@oddhead
    \let\@mkboth\markboth
    \def\sectionmark##1{%
      \markright{\jdrheadingcase{\ifnum\c@secnumdepth >\m@ne\thesection\quad\fi ##1}}}%
  }%
  \newcommand*{\ps@extra@headings}{%
    \setdynamicframe*{footer}{hide=true}%
  }%
  \renewcommand*{\ps@myheadings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{{\jdrheadingfmt\rightmark}\hfill\thepage}%
    \def\@evenhead{\thepage\hfill{\jdrheadingfmt\leftmark}}%
    \let\@mkboth\@gobbletwo
    \let\sectionmark\@gobble
    \let\subsectionmark\@gobble
  }%
  \newcommand*{\ps@extra@myheadings}{%
    \setdynamicframe*{footer}{hide=true}%
  }%
  \newcommand*{\ps@flowframtk}{%
    \def\@oddfoot{\flowframtkoddfooterfmt{\itshape\thepage}}%
    \def\@evenfoot{\flowframtkevenfooterfmt{\itshape\thepage}}%
    \def\@oddhead{\flowframtkoddheaderfmt{\rightmark}}%
    \def\@evenhead{\flowframtkevenheaderfmt{\leftmark}}%
    \let\@mkboth\markboth
    \def\sectionmark##1{%
      \markboth{\jdrheadingcase{\ifnum\c@secnumdepth >\m@ne\thesection\quad\fi ##1}}{\jdrheadingcase{\ifnum\c@secnumdepth >\m@ne\thesection\quad\fi ##1}}}%
    \def\subsectionmark##1{%
      \markright{\jdrheadingcase{\ifnum\c@secnumdepth >\tw@\thesubsection\quad\fi ##1}}}%
  }%
}%
{%
  \renewcommand*{\ps@headings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{{\jdrheadingfmt\rightmark}\hfill\thepage}%
    \def\@evenhead{\thepage\hfill{\jdrheadingfmt\leftmark}}%
    \let\@mkboth\markboth
    \def\chaptermark##1{%
      \markboth
      {\jdrheadingcase{%
        \ifnum \c@secnumdepth >\m@ne
          \if@mainmatter
            \@chapapp\ \thechapter. \ 
          \fi
        \fi ##1%
      }}%
      {}%
    }%
    \def\sectionmark##1{%
      \markright{\jdrheadingcase{\ifnum\c@secnumdepth >\z@\thesection. \ \fi ##1}}}%
  }%
  \newcommand*{\ps@extra@headings}{%
    \setdynamicframe*{footer}{hide=true}%
  }%
  \renewcommand*{\ps@myheadings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{{\jdrheadingfmt\rightmark}\hfill\thepage}%
    \def\@evenhead{\thepage\hfill{\jdrheadingfmt\leftmark}}%
    \let\@mkboth\@gobbletwo
    \let\chaptermark\@gobble
    \let\sectionmark\@gobble
  }%
  \newcommand*{\ps@extra@myheadings}{%
    \setdynamicframe*{footer}{hide=true}%
  }%
  \newcommand*{\ps@flowframtk}{%
    \def\@oddfoot{\flowframtkoddfooterfmt{\itshape\thepage}}%
    \def\@evenfoot{\flowframtkevenfooterfmt{\itshape\thepage}}%
    \def\@oddhead{\flowframtkoddheaderfmt{\rightmark}}%
    \def\@evenhead{\flowframtkevenheaderfmt{\leftmark}}%
    \let\@mkboth\markboth
    \def\chaptermark##1{%
      \markboth
      {\jdrheadingcase{%
        \ifnum \c@secnumdepth >\m@ne
          \if@mainmatter
            \@chapapp\ \thechapter. \ 
          \fi
        \fi ##1%
      }}%
      {}%
    }%
    \def\sectionmark##1{%
      \markright{\jdrheadingcase{\ifnum\c@secnumdepth >\z@\thesection. \ \fi ##1}}}%
  }%
}
\@ifundefined{chapter}{\pagestyle{plain}}{\pagestyle{headings}}
% suppress section numbering
\setcounter{secnumdepth}{0}
% set the paragraph indentation for static
% and dynamic frames
\setlength{\sdfparindent}{\parindent}

\endinput
