ifeq ($(IZPACKDIR),)
IZPACKDIR:=/usr/local/IzPack
endif

# Java 8 extended support until December 2030
target_version := --release 8 -Xlint:-options

# Java 8 extended support until December 2030
target_version := --release 8 -Xlint:-options


all	: lib/flowframtk.jar lib/jdrview.jar lib/jdrinfo.jar lib/jdrconverter.jar 

test	: lib/flowframtk.jar
	bin/flowframtk -debug

lib/jdr.jar	: java/jdr/*.java \
	java/jdr/marker/*.java \
	java/jdr/exceptions/*.java \
	java/jdr/io/*.java \
	java/jdr/io/eps/*.java \
	java/jdr/io/eps/operators/*.java  \
	java/jdr/io/svg/*.java
	make -C java ../lib/jdr.jar

lib/jdrresources.jar      : lib/jdr.jar \
	java/jdrresources/*.java \
	java/jdrresources/*/*.java \
	java/jdrresources/icons/*.png \
	java/jdrresources/icons/*/*.png
	make -C java ../lib/jdrresources.jar

lib/flowframtk.jar	: java/flowframtk/Manifest.txt \
	lib/jdr.jar lib/jdrresources.jar \
	java/flowframtk/*.java \
	java/flowframtk/*/*.java
	make -C java ../lib/flowframtk.jar

lib/resources/helpsets/flowframtk/en-GB/index.xml	: doc/en/flowframtk-en-GB.tex \
			doc/en/flowframtk-main.tex \
			doc/en/flowframtk.bib \
			doc/en/images-en-GB/*.png \
			doc/sharedimages/*.png 
			make -C doc/en

lib/resources/helpsets/flowframtk/en/index.xml	: doc/en/flowframtk-en.tex \
			doc/en/flowframtk-main.tex \
			doc/en/flowframtk.bib \
			doc/en/images-en/*.png \
			doc/sharedimages/*.png 
			make -C doc/en

lib/jdrview.jar   : lib/jdr.jar lib/jdrresources.jar \
	java/jdrview/*.java
	make -C java ../lib/jdrview.jar

lib/jdrinfo.jar   : lib/jdr.jar java/jdrinfo/*.java
	make -C java ../lib/jdrinfo.jar

lib/jdrconverter.jar   : java/jdrconverter/Manifest.txt \
			lib/jdr.jar java/jdrconverter/*.java 
	make -C java ../lib/jdrconverter.jar

../unstable/flowframtk-installer.jar	: ../unstable \
	lib/flowframtk.jar lib/jdrview.jar lib/jdrinfo.jar lib/jdrconverter.jar \
        lib/resources/helpsets/flowframtk/en/index.xml \
        lib/resources/helpsets/flowframtk/en-GB/index.xml \
	installer/install.xml
	"$(IZPACKDIR)/bin/compile" installer/install.xml -b . \
	-h "$(IZPACKDIR)" \
	-o ../unstable/flowframtk-installer.jar -k standard

../unstable	:
	mkdir ../unstable

clean	:
	\rm -f lib/jdr.jar lib/jdrresources.jar
	\rm -f lib/flowframtk.jar lib/jdrview.jar lib/jdrinfo.jar
	make -C doc/en clean

squeaky	:
	make -C java clean
