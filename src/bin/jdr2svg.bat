@echo off
java -jar "%~dp0\..\lib\jdrconverter.jar" --from JDR --to SVG %*
