
set RUN_TAR=%1
set RUN_TARGET=%2
set RUN_IN=%3
set RUN_SE=%4

cd %RUN_IN%

%RUN_TAR% cvf %RUN_SE%\%RUN_TARGET%.tar %RUN_TARGET%*

rmdir /Y  %RUN_TARGET%*

