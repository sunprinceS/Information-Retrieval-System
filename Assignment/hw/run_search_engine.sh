#!/bin/sh
java -cp classes:pdfbox -Xmx1g ir.Engine -d ./davisWiki_small -l ir18.jpg -p patterns.txt
