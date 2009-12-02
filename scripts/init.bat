:: ----------------------------------------------------------------------------
:: Copyright [2009] [NIC Labs]
:: 
:: Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
:: except in compliance with the License. You may obtain a copy of the 	License at
:: 
:: http://www.apache.org/licenses/LICENSE-2.0
:: 
:: Unless required by applicable law or 
:: agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
:: specific language governing permissions and limitations under the License.
:: 
:: ----------------------------------------------------------------------------

@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

for /F %%x in ('dir /s /b %0') do set str=%%x

set str=%str:scripts\init.bat=%

set JARS=%str%build\OpenLibsDte.jar
FOR %%j IN ("%str%lib\*.jar") DO SET JARS=!JARS!;%%j

set CLASSPATH=!JARS!:!CLASSPATH!
set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -cp "%CLASSPATH%"

goto end


:javahome
echo
echo La variable de ambiente JAVA_HOME debe ser asignada con la ubicacion
echo de una distribucion de Sun Java 1.6 (o superior) instalada en su sistema
echo Ejecute 
echo     set JAVA_HOME=<directorio donde está el JDK>"
goto end


:end
