# InstruAPK

InstruAPK is an open source tool for instrumenting APK Files. The instrumentation is done with the the purpose of collecting different coverage metrics when executing an APK under analysis. Current version allows only for method coverage instrumentation

# Compile
Download and compile InstruAPK with the following commands:
```
git clone https://github.com/TheSoftwareDesignLab/InstruAPK.git
cd InstruAPK
mvn clean
mvn package
```
The generated runnable jar can be found in: ``InstruAPK/target/InstruAPK-1.0.0.jar``

# Usage
To run InstruAPK use the following command, specifying the required arguments:
```
java -jar InstruAPK-1.0.0.jar <APKPath> <AppPackage> <Output> <ExtraComponentFolder> <InstrOperatorsDir> | tee instrumentationReport.txt
```
### Arguments
Provide the following list of required arguments when running MutAPK:
1. ``APK path``: relative path of the apk to mutate;
2. ``AppPackage``: App main package name;
3. ``Output``: relative path of the folder where the instrumented APK will be created;
4. ``ExtraCompFolder``:  relative path of the extra component folder (``InstruAPK/extra/``);
5. ``InstrOperatorsDir``: relative path to the folder containing the operators.properties files for selecting the instrumentation operators.
6. The last command is for storign the console output and use it in other tools. (It can be omitted)

### Example
```
cd InstruAPK
java -jar .\target\InstruAPK-1.0.0.jar .\foo.apk or.foo.app .\output\ .\extra\ .\
```

### Output
The output directory will contain a log file that summarise the statements added to the smali code during the instrumentation process and a folder for the instrumented apk. 
The instrumentations folders are named with the corresponding instrumenter ID (i.e., numerical ID). The log file contains information about the instrumentation process as well as the type and location of each mutant generated.

### Instrumentation
The instrumentations has the following format

``InstruAPK;;<methodIndex>;;<fileName>;;<methodName>;;<methodParameters>;;<callTimeInMillis>``

1. ``InstruAPK`` keyword that helps to identify all the output logcat lines related to the instrumentation.
2. ``methodIndex`` Unique identifier of the method (also corresponds to the mutation number).
3. ``fileName`` identifies the java class. As far as java files have the same names than the class defined inside them.
4. ``methodName`` method's name
5. ``methodParameters`` smali representation of the method's arguments. Some methods can be overloaded, and this helps developers to know the difference between those methods. The methodIndex can serves for the same purpose.
6. ``callTimeInMillis`` exact time, in milliseconds, when the method was called
