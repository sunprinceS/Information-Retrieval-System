#!/bin/sh
java -cp classes:pdfbox -Xmx1g ir.Engine -d ./task2.3 -l ir18.jpg -p patterns.txt
