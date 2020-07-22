% Created by FlowframTk version 0.8
% 20-Oct-2015 13:44:51
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{brochure}
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
\LoadClass[10pt]{article}
\RequirePackage[a4paper,portrait]{geometry}
\geometry{lmargin=66.0pt,rmargin=135.507874pt,tmargin=94.0pt,bmargin=191.04685pt}
\setlength{\evensidemargin}{\oddsidemargin}
\addtolength{\evensidemargin}{69.507874pt}
\RequirePackage{flowfram}
\renewcommand*{\@ff@pages@countreg}{\c@page}
\newflowframe[all]{396.008392pt}{560.007812pt}{-0.11631pt}{0.603851pt}[main]

\newdynamicframe[odd]{51.003754pt}{51.003784pt}{473.498138pt}{-186.501892pt}[footer]

% Border command for frame 'footer'
\expandafter\def\csname @flf@border@footer\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{101.003754pt}{101.003784pt}
\pgfputat{\pgfpoint{-40.0pt}{-10.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{100.501862pt}{0.501892pt}}
\pgfpathlineto{\pgfpoint{0.501862pt}{0.501892pt}}
\pgfpathcurveto{\pgfpoint{0.501862pt}{55.730347pt}}{\pgfpoint{45.273376pt}{100.501892pt}}{\pgfpoint{100.501862pt}{100.501892pt}}
\pgfpathlineto{\pgfpoint{100.501862pt}{0.501892pt}}
\pgfclosepath
\definecolor{fillpaint}{rgb}{0.68,0.36,1.0}\pgfsetfillcolor{fillpaint}
\pgfseteorule 
\pgfusepath{fill}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setdynamicframe*
{footer}{offset=0pt,border={@flf@border@footer}}

\setdynamicframe*{footer}{valign=c}
\setdynamiccontents*{footer}{\bfseries\color{white}\Huge }
\newdynamicframe[even]{51.003754pt}{51.003784pt}{-127.5056pt}{-186.501892pt}[evenfooter]

% Border command for frame 'evenfooter'
\expandafter\def\csname @flf@border@evenfooter\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{101.003754pt}{101.003784pt}
\pgfputat{\pgfpoint{-10.0pt}{-10.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{0.501877pt}{0.501892pt}}
\pgfpathlineto{\pgfpoint{100.501877pt}{0.501892pt}}
\pgfpathcurveto{\pgfpoint{100.501877pt}{55.730347pt}}{\pgfpoint{55.730362pt}{100.501892pt}}{\pgfpoint{0.501877pt}{100.501892pt}}
\pgfpathlineto{\pgfpoint{0.501877pt}{0.501892pt}}
\pgfclosepath
\definecolor{fillpaint}{rgb}{0.68,0.36,1.0}\pgfsetfillcolor{fillpaint}
\pgfseteorule 
\pgfusepath{fill}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setdynamicframe*
{evenfooter}{offset=0pt,border={@flf@border@evenfooter}}

\setdynamicframe*{evenfooter}{valign=c}
\setdynamiccontents*{evenfooter}{\bfseries\color{white}\Huge }
\newdynamicframe[odd]{231.713531pt}{31.003754pt}{292.788361pt}{613.498121pt}[header]

% Border command for frame 'header'
\expandafter\def\csname @flf@border@header\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{291.713531pt}{51.003754pt}
\pgfputat{\pgfpoint{-50.0pt}{-10.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{1.211639pt}{50.501879pt}}
\pgfpathlineto{\pgfpoint{51.211639pt}{0.501879pt}}
\pgfpathlineto{\pgfpoint{291.211639pt}{0.501879pt}}
\pgfpathlineto{\pgfpoint{291.211639pt}{50.501879pt}}
\pgfpathlineto{\pgfpoint{1.211639pt}{50.501879pt}}
\pgfclosepath
\definecolor{fillpaint}{rgb}{0.68,0.36,1.0}\pgfsetfillcolor{fillpaint}
\pgfseteorule 
\pgfusepath{fill}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setdynamicframe*
{header}{offset=0pt,border={@flf@border@header}}

\setdynamicframe*{header}{valign=c}
\setdynamiccontents*{header}{\bfseries\Large }
\newdynamicframe[even]{231.713501pt}{31.003754pt}{-128.925156pt}{613.498121pt}[evenheader]

% Border command for frame 'evenheader'
\expandafter\def\csname @flf@border@evenheader\endcsname#1{%
\begin{pgfpicture}{0pt}{0pt}{291.713501pt}{51.003754pt}
\pgfputat{\pgfpoint{-10.0pt}{-10.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{290.501877pt}{50.501879pt}}
\pgfpathlineto{\pgfpoint{240.501877pt}{0.501879pt}}
\pgfpathlineto{\pgfpoint{0.501877pt}{0.501879pt}}
\pgfpathlineto{\pgfpoint{0.501877pt}{50.501879pt}}
\pgfpathlineto{\pgfpoint{290.501877pt}{50.501879pt}}
\pgfclosepath
\definecolor{fillpaint}{rgb}{0.68,0.36,1.0}\pgfsetfillcolor{fillpaint}
\pgfseteorule 
\pgfusepath{fill}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setdynamicframe*
{evenheader}{offset=0pt,border={@flf@border@evenheader}}

\setdynamicframe*{evenheader}{valign=c}
\setdynamiccontents*{evenheader}{\bfseries\Large }
\renewcommand{\@dothehead}{}%
\renewcommand{\@dodynamicthehead}{%
 \setdynamiccontents*{header}{\@oddhead}%
 \setdynamiccontents*{evenheader}{\@evenhead}%
}%
\renewcommand{\@dothefoot}{}%
\renewcommand{\@dodynamicthefoot}{%
 \setdynamiccontents*{footer}{\@oddfoot}%
 \setdynamiccontents*{evenfooter}{\@evenfoot}%
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
 \setdynamicframe*{header,evenheader,footer,evenfooter}{hidethis=false}%
}
\newcommand*{\thisps@extra@empty}{%
 \setdynamicframe*{header,evenheader,footer,evenfooter}{hidethis=true}%
}
\newcommand*{\thisps@extra@plain}{%
  \setdynamicframe*{header,evenheader}{hidethis=true}%
  \setdynamicframe*{footer,evenfooter}{hidethis=false}%
}
  \newcommand*{\thisps@extra@headings}{%
    \setdynamicframe*{footer,evenfooter}{hidethis=true}%
    \setdynamicframe*{header,evenheader}{hidethis=false}%
  }%
  \newcommand*{\thisps@extra@myheadings}{%
    \setdynamicframe*{footer,evenfooter}{hidethis=true}%
    \setdynamicframe*{header,evenheader}{hidethis=false}%
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
 \setdynamicframe*{header,evenheader,footer,evenfooter}{hide=false}%
}
\newcommand*{\ps@extra@empty}{%
 \setdynamicframe*{header,evenheader,footer,evenfooter}{hide=true}%
}
\renewcommand*{\ps@plain}{%
  \let\@mkboth\@gobbletwo
  \let\@oddhead\@empty
  \let\@evenhead\@empty
  \def\@oddfoot{\flowframtkoddfooterfmt{\bfseries\color{white}\Huge \thepage}}%
  \def\@evenfoot{\flowframtkevenfooterfmt{\bfseries\color{white}\Huge \thepage}}%
}
\newcommand*{\ps@extra@plain}{%
  \setdynamicframe*{header,evenheader}{hide=true}%
  \setdynamicframe*{footer,evenfooter}{hide=false}%
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
    \def\@oddhead{\bfseries\Large {{\jdrheadingfmt\rightmark}\hfill\thepage}}%
    \def\@evenhead{\bfseries\Large {\thepage\hfill{\jdrheadingfmt\leftmark}}}%
    \let\@mkboth\markboth
    \def\sectionmark##1{%
      \markright{\jdrheadingcase{\ifnum\c@secnumdepth >\m@ne\thesection\quad\fi ##1}}}%
  }%
  \newcommand*{\ps@extra@headings}{%
    \setdynamicframe*{footer,evenfooter}{hide=true}%
    \setdynamicframe*{header,evenheader}{hide=false}%
  }%
  \renewcommand*{\ps@myheadings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{\bfseries\Large {{\jdrheadingfmt\rightmark}\hfill\thepage}}%
    \def\@evenhead{\bfseries\Large {\thepage\hfill{\jdrheadingfmt\leftmark}}}%
    \let\@mkboth\@gobbletwo
    \let\sectionmark\@gobble
    \let\subsectionmark\@gobble
  }%
  \newcommand*{\ps@extra@myheadings}{%
    \setdynamicframe*{footer,evenfooter}{hide=true}%
    \setdynamicframe*{header,evenheader}{hide=false}%
  }%
  \newcommand*{\ps@flowframtk}{%
    \def\@oddfoot{\flowframtkoddfooterfmt{\bfseries\color{white}\Huge \thepage}}%
    \def\@evenfoot{\flowframtkevenfooterfmt{\bfseries\color{white}\Huge \thepage}}%
    \def\@oddhead{\flowframtkoddheaderfmt{\bfseries\Large \rightmark}}%
    \def\@evenhead{\flowframtkevenheaderfmt{\bfseries\Large \leftmark}}%
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
    \def\@oddhead{\bfseries\Large {{\jdrheadingfmt\rightmark}\hfill\thepage}}%
    \def\@evenhead{\bfseries\Large {\thepage\hfill{\jdrheadingfmt\leftmark}}}%
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
    \setdynamicframe*{footer,evenfooter}{hide=true}%
    \setdynamicframe*{header,evenheader}{hide=false}%
  }%
  \renewcommand*{\ps@myheadings}{%
    \let\@oddfoot\@empty
    \let\@evenfoot\@empty
    \def\@oddhead{\bfseries\Large {{\jdrheadingfmt\rightmark}\hfill\thepage}}%
    \def\@evenhead{\bfseries\Large {\thepage\hfill{\jdrheadingfmt\leftmark}}}%
    \let\@mkboth\@gobbletwo
    \let\chaptermark\@gobble
    \let\sectionmark\@gobble
  }%
  \newcommand*{\ps@extra@myheadings}{%
    \setdynamicframe*{footer,evenfooter}{hide=true}%
    \setdynamicframe*{header,evenheader}{hide=false}%
  }%
  \newcommand*{\ps@flowframtk}{%
    \def\@oddfoot{\flowframtkoddfooterfmt{\bfseries\color{white}\Huge \thepage}}%
    \def\@evenfoot{\flowframtkevenfooterfmt{\bfseries\color{white}\Huge \thepage}}%
    \def\@oddhead{\flowframtkoddheaderfmt{\bfseries\Large \rightmark}}%
    \def\@evenhead{\flowframtkevenheaderfmt{\bfseries\Large \leftmark}}%
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
\pagestyle{flowframtk}
\endinput
