@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo ERRO: Java nao encontrado. Instale o Java 17 e tente novamente.
  pause
  exit /b 1
)
if not exist ".tools\gradle-8.1.1\bin\gradle.bat" (
  echo Baixando Gradle 8.1.1 pela primeira vez...
  if not exist ".tools" mkdir ".tools"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.1.1-bin.zip' -OutFile '.tools\gradle.zip'; Expand-Archive -Force '.tools\gradle.zip' '.tools'; Remove-Item '.tools\gradle.zip'"
  if errorlevel 1 (
    echo ERRO: nao foi possivel baixar o Gradle. Verifique a internet.
    pause
    exit /b 1
  )
)
echo Compilando Narutoto Ultimate RPG...
call ".tools\gradle-8.1.1\bin\gradle.bat" --no-daemon build
if errorlevel 1 (
  echo.
  echo A compilacao falhou. Envie o texto completo do erro para corrigirmos.
  pause
  exit /b 1
)
echo.
echo SUCESSO! O mod compilado esta em build\libs\
pause
