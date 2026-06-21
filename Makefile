build:
	mvn clean package install dependency:copy-dependencies -DskipTests
	mkdir -p bin/
	cp -f target/jdbc-export.jar bin/
	cp -fr target/dependency/* bin/
	cp -f src/main/resources/jdbc-export.cmd bin/
	cp -f src/main/resources/jdbc-export bin/

fatjar:
	# TODO: Implemeent
	mvn clean package

test:
	mvn clean test

clean:
	mvn clean
	rm -f bin/*

# TODO: Not tested yet
build-graal: build jdbc-export-linux jdbc-export.exe

jdbc-export-linux:
	native-image -jar jdbc-export.jar --no-fallback

jdbc-export.exe:
	native-image -jar jdbc-export.jar --no-fallback