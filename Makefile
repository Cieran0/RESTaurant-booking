build:
	javac -d src src/*.java
	cd src; \
    jar cmvf ../MANIFEST.MF ../RESTaurantBooking.jar *.class

run: build
	java -jar RESTaurantBooking.jar
