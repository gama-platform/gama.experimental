# Requierement and installation 

In the GAMA 1.9.0 version, the connection with Matlab works with:
- **MATLAB R2023a** on **Windows 64 bits**

In the following, we make the hypothesis that Matlab is installed on the folder: `C:\Program Files\MATLAB\R2023a\`.

To execute the connection to Matlab, the following line should be added to the launching file in VM arguments (this should be added to the `gama.product`file when using the GAMA Git version or to the `Gama.ini` file when using the GAMA release version):
  * For Windows 64 bits: `-Djava.library.path="C:\Program Files\MATLAB\R2023a\bin\win64"`

In addition, the `PATH` environment variable should contain the path to Matlab. 
  * In the system environment variables, **add** to the PATH variable the following:
    ``C:\Program Files\MATLAB\R2023a\extern\bin\win64;C:\Program Files\MATLAB\R2023a\bin\win64;``

# Using another version of Matlab

If you are using another version of Matlab (or another OS than MacOS), it is highly recommend to use the Git version of GAMA.

You will have to:
  * replace the `engine.jar` file (in `libs` folder of the ummisco.gama.extensions.matlab project) by the file `engine.jar` that you can find in your Matlab. It is located in the folder: `extern/engines/java/jar`.
  *  Modify the `-Djava.library.path` variable in the `gama.product` or in `gama.ini`
  *  Set the proper environment variables (depending on your OS). Check on the Matlab website to know the proper variable to set.
