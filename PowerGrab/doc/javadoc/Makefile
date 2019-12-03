javadoc: index.html

index.html:
	javadoc -sourcepath ../../src/main/java/ -d ./ -subpackages uk.ac.ed.inf.powergrab:org.paulgeorgiou -private -doctitle PowerGrab -windowtitle "PowerGrab documentation"

clean:
	cp ./Makefile ../MakefileJavadoc
	rm -r *
	cp ../MakefileJavadoc ./Makefile
	rm ../MakefileJavadoc
