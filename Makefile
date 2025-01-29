# Path to the SQLite JDBC JAR file
SQLITE_JAR=sqlite-jdbc-3.48.0.0.jar

# Directory for compiled class files (target directory)
BUILD_DIR=build

# Default build target
build:
	mkdir -p $(BUILD_DIR)
	javac -cp $(SQLITE_JAR) -d $(BUILD_DIR) src/*.java 
	cd $(BUILD_DIR); \
    jar cmvf ../MANIFEST.MF ../RESTaurantBooking.jar *.class

# Run target
run: clean build
	java -cp $(SQLITE_JAR):$(BUILD_DIR) RESTaurantBooking

FINAL_JAR=RESTaurantBooking.jar

jar:
	mkdir -p $(BUILD_DIR)
	javac -cp $(SQLITE_JAR) -d $(BUILD_DIR) src/*.java
	
	unzip -o $(SQLITE_JAR) -d $(BUILD_DIR)
	
	jar cmvf MANIFEST.MF $(FINAL_JAR) -C $(BUILD_DIR) .

clean:
	rm -rf $(BUILD_DIR) $(FINAL_JAR)