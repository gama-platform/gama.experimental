@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "c:\git\bachunghai_irrigation\aaa\jni4net-0.8.8.0-bin\lib\jni4net.j-0.8.8.0.jar"; "jvm\mike_gama_coupling\Mike_Data.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo Mike_Gama.j4n.jar 
jar cvf Mike_Gama.j4n.jar  -C target\classes "mike_gama_coupling\Mike_Data.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo Mike_Gama.j4n.dll 
csc /nologo /warn:0 /t:library /out:Mike_Gama.j4n.dll /recurse:clr\*.cs  /reference:"C:\git\BacHungHai_Irrigation\AAA\jni4net-0.8.8.0-bin\samples\myCSharpDemoCalc\work\Mike_Gama.dll" /reference:"C:\git\BacHungHai_Irrigation\AAA\jni4net-0.8.8.0-bin\lib\jni4net.n-0.8.8.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
