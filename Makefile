build:
	mvn clean package install dependency:copy-dependencies -DskipTests
	mkdir -p bin/
	cp -f target/jdbc-export.jar bin/
	cp -fr target/dependency/* bin/
	cp -f src/main/resources/jdbc-export.* bin/

clean:
	rm -f bin/*

build-graal: build
	cd bin && native-image -jar jdbc-export.jar --no-fallback


