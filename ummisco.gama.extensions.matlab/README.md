In the GAMA 1.9.0 version, it works with:
- MATLAB R2023a (and has been tested on Windows 64 bits)

To execute the connection to Matlab, theproduct / Gama.ini file should contain the following line in VM argument:
  * For Windows 64 bits: `-Djava.library.path="C:\Program Files\MATLAB\R2023a\bin\win64"`
  * For MacOS: `-Djava.library.path=/Applications/MATLAB_R2019a.app/bin/maci64/`




TODO :
- test with various data type
- manage the jar from Matlab (load at Runtime)
