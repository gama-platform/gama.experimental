model RJava

global skills:[RSkill]{
		file Rcode<-text_file("r.txt");
	init{
		write R_eval("x<-1");
		loop s over:Rcode.contents{
			unknown a<- R_eval(s);
			write "R>"+s;
			write a;
		}
	}
	
}
experiment RJava type:gui{
	output{
	}
}