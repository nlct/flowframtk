PP_VERSION:=$(shell grep "public static final String APP_VERSION" java/flowframtk/FlowframTkInvoker.java | sed "s/public\sstatic\sfinal\sString\sAPP_VERSION=//" | tr -d "\"\; ")
IZPACKDIR=/usr/local/IzPack

all	: lib/flowframtk.jar lib/jdrview.jar lib/jdrinfo.jar \
	lib/ajr2ajr.jar \
	lib/ajr2jdr.jar \
	lib/ajr2tex.jar \
	lib/eps2jdr.jar \
	lib/jdr2ajr.jar \
	lib/jdr2eps.jar \
	lib/jdr2png.jar \
	lib/jdr2svg.jar \
	lib/jdr2tex.jar \
	lib/svg2jdr.jar

test	: lib/flowframtk.jar
	bin/flowframtk -debug

lib/jdr.jar	: classes/com/dickimawbooks/jdr/JDRObject.class
		cd classes ; \
		jar cvf ../lib/jdr.jar \
		com/dickimawbooks/jdr/*.class \
		com/dickimawbooks/jdr/io/*.class \
		com/dickimawbooks/jdr/io/eps/*.class \
		com/dickimawbooks/jdr/io/eps/operators/*.class \
		com/dickimawbooks/jdr/io/svg/*.class \
		com/dickimawbooks/jdr/exceptions/*.class \
		com/dickimawbooks/jdr/marker/*.class

classes/com/dickimawbooks/jdr/JDRObject.class	: classes \
	java/jdr/*.java \
	java/jdr/marker/*.java \
	java/jdr/exceptions/*.java \
	java/jdr/io/*.java \
	java/jdr/io/eps/*.java \
	java/jdr/io/eps/operators/*.java  \
	java/jdr/io/svg/*.java
	cd java/jdr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:unchecked -Xlint:deprecation \
	*.java */*.java */*/*.java */*/*/*.java

lib/jdrresources.jar      : lib/jdr.jar \
	classes/com/dickimawbooks/jdrresources/JDRResources.class \
	java/jdrresources/icons/*.png java/jdrresources/icons/*/*.png \
	classes/com/dickimawbooks/jdrresources/icons 
	cd classes ; \
	jar cvf ../lib/jdrresources.jar \
	com/dickimawbooks/jdrresources/*.class \
	com/dickimawbooks/jdrresources/filter/*.class \
	com/dickimawbooks/jdrresources/numfield/*.class \
	com/dickimawbooks/jdrresources/icons/*.png \
	com/dickimawbooks/jdrresources/icons/*/*.png

classes/com/dickimawbooks/jdrresources/icons :
	mkdir -p classes/com/dickimawbooks/jdrresources
	cd classes/com/dickimawbooks/jdrresources; ln -f -s ../../../../java/jdrresources/icons

classes/com/dickimawbooks/jdrresources/JDRResources.class : classes \
	java/jdrresources/*.java \
	java/jdrresources/*/*.java
	cd java/jdrresources ; \
	javac -target 1.7 -source 1.7 \
	  -bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	  -d ../../classes -Xlint:deprecation -Xlint:unchecked \
          -cp ../../lib/jdr.jar:../../lib/javahelp2.jar \
          *.java */*.java

classes	:
	mkdir -p classes

lib/flowframtk.jar	: java/flowframtk/Manifest.txt \
			lib/jdr.jar lib/jdrresources.jar \
			classes/com/dickimawbooks/flowframtk/FlowframTkInvoker.class \
			classes/splashimage.png \
			lib/resources/helpsets/flowframtk/en-GB/flowframtk.hs \
			lib/resources/helpsets/flowframtk/en-US/flowframtk.hs
	cd classes; \
	jar cmf ../java/flowframtk/Manifest.txt ../lib/flowframtk.jar \
	splashimage.png \
	com/dickimawbooks/flowframtk/*.class \
	com/dickimawbooks/flowframtk/*/*.class 

classes/com/dickimawbooks/flowframtk/FlowframTkInvoker.class : \
	lib/jdr.jar lib/jdrresources.jar \
	java/flowframtk/*.java \
	java/flowframtk/*/*.java
	cd java/flowframtk ; \
	javac -target 1.7 -source 1.7 \
	  -bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	  -d ../../classes -Xlint:unchecked -Xlint:deprecation \
          -cp ../../lib/javahelp2.jar:../../lib/jdr.jar:../../lib/jdrresources.jar \
          *.java */*.java

classes/splashimage.png	: classes images/splashimage.png
	cp -u images/splashimage.png classes/

lib/resources/helpsets/flowframtk/en-GB/flowframtk.hs	: doc/flowframtk-en.xml \
			doc/createflowframtkdocs \
			doc/images-en-GB/*.png \
			doc/sharedimages/*.png 
			make -C doc

lib/resources/helpsets/flowframtk/en-US/flowframtk.hs	: doc/flowframtk-en.xml \
			doc/createflowframtkdocs \
			doc/images-en-US/*.png \
			doc/sharedimages/*.png 
			make -C doc

lib/jdrview.jar   : lib/jdr.jar lib/jdrresources.jar \
	classes/com/dickimawbooks/jdrview/JDRView.class \
	java/jdrview/Manifest.txt 
	cd classes ; \
	jar cmf ../java/jdrview/Manifest.txt \
	   ../lib/jdrview.jar \
	   com/dickimawbooks/jdrview/*.class

classes/com/dickimawbooks/jdrview/JDRView.class : classes \
	lib/jdr.jar lib/jdrresources.jar \
	java/jdrview/*.java
	cd java/jdrview ; \
	javac -target 1.7 -source 1.7 \
	  -bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	  -d ../../classes -Xlint:unchecked  -Xlint:deprecation \
	  -cp ../../lib/javahelp2.jar:../../lib/jdr.jar:../../lib/jdrresources.jar \
	  *.java

lib/jdrinfo.jar   : classes/com/dickimawbooks/jdrinfo/JdrInfo.class \
	java/jdrinfo/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdrinfo/Manifest.txt \
	../lib/jdrinfo.jar \
	com/dickimawbooks/jdrinfo/*.class

classes/com/dickimawbooks/jdrinfo/JdrInfo.class : lib/jdr.jar java/jdrinfo/*.java
	cd java/jdrinfo ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/ajr2ajr.jar   : classes/com/dickimawbooks/ajr2ajr/Ajr2Ajr.class \
	java/ajr2ajr/Manifest.txt
	cd classes ; \
	jar cmf ../java/ajr2ajr/Manifest.txt \
	  ../lib/ajr2ajr.jar \
	  com/dickimawbooks/ajr2ajr/*.class

classes/com/dickimawbooks/ajr2ajr/Ajr2Ajr.class : lib/jdr.jar java/ajr2ajr/*.java
	cd java/ajr2ajr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/ajr2jdr.jar   : classes/com/dickimawbooks/ajr2jdr/Ajr2Jdr.class \
	java/ajr2jdr/Manifest.txt
	cd classes ; \
	jar cmf ../java/ajr2jdr/Manifest.txt \
	  ../lib/ajr2jdr.jar \
	  com/dickimawbooks/ajr2jdr/*.class

classes/com/dickimawbooks/ajr2jdr/Ajr2Jdr.class : lib/jdr.jar java/ajr2jdr/*.java
	cd java/ajr2jdr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/ajr2tex.jar   : classes/com/dickimawbooks/ajr2tex/Ajr2Tex.class \
	java/ajr2tex/Manifest.txt
	cd classes ; \
	jar cmf ../java/ajr2tex/Manifest.txt \
	  ../lib/ajr2tex.jar \
	  com/dickimawbooks/ajr2tex/*.class

classes/com/dickimawbooks/ajr2tex/Ajr2Tex.class : lib/jdr.jar java/ajr2tex/*.java
	cd java/ajr2tex ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/eps2jdr.jar   : classes/com/dickimawbooks/eps2jdr/Eps2Jdr.class \
	java/eps2jdr/Manifest.txt
	cd classes ; \
	jar cmf ../java/eps2jdr/Manifest.txt \
	  ../lib/eps2jdr.jar ../lib/jdrresources.jar \
	  com/dickimawbooks/eps2jdr/*.class

classes/com/dickimawbooks/eps2jdr/Eps2Jdr.class : lib/jdr.jar lib/jdrresources.jar java/eps2jdr/*.java
	cd java/eps2jdr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar:../../lib/jdrresources.jar \
	*.java

lib/jdr2ajr.jar   : classes/com/dickimawbooks/jdr2ajr/Jdr2Ajr.class \
	java/jdr2ajr/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdr2ajr/Manifest.txt \
	  ../lib/jdr2ajr.jar \
	  com/dickimawbooks/jdr2ajr/*.class

classes/com/dickimawbooks/jdr2ajr/Jdr2Ajr.class : lib/jdr.jar java/jdr2ajr/*.java
	cd java/jdr2ajr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/jdr2eps.jar   : classes/com/dickimawbooks/jdr2eps/Jdr2Eps.class \
	java/jdr2eps/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdr2eps/Manifest.txt \
	  ../lib/jdr2eps.jar \
	  com/dickimawbooks/jdr2eps/*.class

classes/com/dickimawbooks/jdr2eps/Jdr2Eps.class : lib/jdr.jar java/jdr2eps/*.java
	cd java/jdr2eps ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/jdr2png.jar   : classes/com/dickimawbooks/jdr2png/Jdr2Png.class \
	java/jdr2png/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdr2png/Manifest.txt \
	  ../lib/jdr2png.jar \
	  com/dickimawbooks/jdr2png/*.class

classes/com/dickimawbooks/jdr2png/Jdr2Png.class : lib/jdr.jar java/jdr2png/*.java
	cd java/jdr2png ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/jdr2svg.jar   : classes/com/dickimawbooks/jdr2svg/Jdr2Svg.class \
	java/jdr2svg/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdr2svg/Manifest.txt \
	  ../lib/jdr2svg.jar \
	  com/dickimawbooks/jdr2svg/*.class

classes/com/dickimawbooks/jdr2svg/Jdr2Svg.class : lib/jdr.jar java/jdr2svg/*.java
	cd java/jdr2svg ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/jdr2tex.jar   : classes/com/dickimawbooks/jdr2tex/Jdr2Tex.class \
	java/jdr2tex/Manifest.txt
	cd classes ; \
	jar cmf ../java/jdr2tex/Manifest.txt \
	  ../lib/jdr2tex.jar \
	  com/dickimawbooks/jdr2tex/*.class

classes/com/dickimawbooks/jdr2tex/Jdr2Tex.class : lib/jdr.jar java/jdr2tex/*.java
	cd java/jdr2tex ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

lib/svg2jdr.jar   : classes/com/dickimawbooks/svg2jdr/Svg2Jdr.class \
	java/svg2jdr/Manifest.txt
	cd classes ; \
	jar cmf ../java/svg2jdr/Manifest.txt \
	  ../lib/svg2jdr.jar \
	  com/dickimawbooks/svg2jdr/*.class

classes/com/dickimawbooks/svg2jdr/Svg2Jdr.class : lib/jdr.jar java/svg2jdr/*.java
	cd java/svg2jdr ; \
	javac -target 1.7 -source 1.7 \
	-bootclasspath $(JAVA7BOOTCLASSLIB)/rt.jar \
	-d ../../classes -Xlint:deprecation -Xlint:unchecked \
	-cp ../../lib/jdr.jar \
	*.java

clean	:
	\rm -r -f classes
	\rm -f lib/jdr.jar lib/jdrresources.jar
	\rm -f lib/flowframtk.jar lib/jdrview.jar lib/jdrinfo.jar
	\rm -f lib/ajr2ajr.jar
	\rm -f lib/ajr2jdr.jar
	\rm -f lib/ajr2tex.jar
	\rm -f lib/eps2jdr.jar
	\rm -f lib/jdr2ajr.jar
	\rm -f lib/jdr2eps.jar
	\rm -f lib/jdr2png.jar
	\rm -f lib/jdr2svg.jar
	\rm -f lib/jdr2tex.jar
	\rm -f lib/svg2jdr.jar
	make -C doc squeaky clean
