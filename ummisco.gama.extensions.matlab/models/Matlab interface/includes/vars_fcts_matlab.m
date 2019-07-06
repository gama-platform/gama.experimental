IC=[0;1e+7]; %(ug/m^3)
Tstart=0; %(s)
Tend=30*24*3600; %(s)
h=0.001; %(m/s)
K=1000; %(-)
n_renew=0.6*3600*100; %%(m^3/s) variable à récupérer sur GAMA
[T,y] = ode15s(@(T, y) myODE(T,y,n_renew,h,K),[Tstart Tend],IC);


function ypoint=myODE(T,y,n_renew,h,K)
    As=zeros(2);
    As(1,1)=-n_renew-h;
    As(1,2)=h/K;
    As(2,1)=h;
    As(2,2)=-h/K;
    ypoint=As*y;
end