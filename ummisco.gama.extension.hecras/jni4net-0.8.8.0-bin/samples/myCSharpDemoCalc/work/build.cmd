@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "e:\git\gama.experimental\ummisco.gama.extension.hecras\jni4net-0.8.8.0-bin\lib\jni4net.j-0.8.8.0.jar"; "jvm\ras506\_HECRASController.java" "jvm\ras506\_HECRASController_.java" "jvm\ras506\__HECRASController_Event.java" "jvm\ras506\__HECRASController_Event_.java" "jvm\ras506\HECRASController.java" "jvm\ras506\HECRASController_.java" "jvm\ras506\__HECRASController.java" "jvm\ras506\__HECRASController_.java" "jvm\hecras_gama_coupling\HecRas_Data.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo HecRas_Gama.j4n.jar 
jar cvf HecRas_Gama.j4n.jar  -C target\classes "ras506\_HECRASController.class"  -C target\classes "ras506\_HECRASController_.class"  -C target\classes "ras506\___HECRASController.class"  -C target\classes "ras506\__HECRASController_Event.class"  -C target\classes "ras506\__HECRASController_Event_.class"  -C target\classes "ras506\____HECRASController_Event.class"  -C target\classes "ras506\HECRASController.class"  -C target\classes "ras506\HECRASController_.class"  -C target\classes "ras506\__HECRASController.class"  -C target\classes "ras506\__HECRASController_.class"  -C target\classes "ras506\____HECRASController.class"  -C target\classes "hecras_gama_coupling\HecRas_Data.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo HecRas_Gama.j4n.dll 
csc /nologo /warn:0 /t:library /out:HecRas_Gama.j4n.dll /recurse:clr\*.cs  /reference:"E:\git\gama.experimental\ummisco.gama.extension.hecras\jni4net-0.8.8.0-bin\samples\myCSharpDemoCalc\work\HecRas_Gama.dll" /reference:"E:\git\gama.experimental\ummisco.gama.extension.hecras\jni4net-0.8.8.0-bin\lib\jni4net.n-0.8.8.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
