/**
* Name: MovingAgent
* Based on the internal empty template. 
* Author: lucas
* Tags: 
*/


model MovingAgent

species movingAgent skills:[moving, ProxySkill]
{	
	rgb col <- #red;
	point target <- any_location_in(world);
	bool display_true <- false;
	
	init
	{
		
	}
	
	aspect classic
	{		
		draw line(location, target) color: col;
		if(display_true)
		{
			draw circle(1) color: #blue;
		}else
		{
			draw circle(1) color: col;
		}
		
		draw name color: #black;
	}
	
	reflex move when: target != location
	{
		do goto speed: speed target:target;
	}
	
	reflex target when: target = location
	{
		target <- any_location_in(world.shape);
	}
	
	list<agent> insert_self_into_list(list<agent> li)
	{
		if(li != nil)
		{
			add self to: li;
		}else
		{
			li <- [self];
		}
		return li;
	}
}

species followingAgent parent: movingAgent
{	
	movingAgent targetAgent;
	
	init
	{
		col <- #black;
		speed <- speed - (speed / 20);
		
		if(targetAgent = nil)
		{
			targetAgent <- one_of(movingAgent);
			target <- targetAgent.location;
		}
	}
	
	aspect classic
	{		
		draw line(location, target) color: col;
		draw circle(2) color: col;
	}
	reflex move when: target != location
	{
		target <- targetAgent.location;
		do goto speed: speed target:target;
	}
	
	reflex target when: target = location
	{
		target <- targetAgent.location;
	}
}

species standingAgent skills:[ProxySkill]
{
	int data; 
	rgb col;
	bool fake <- false;
	int index;
	string syncmode <- "";
	
	init
	{
		col <- #blue;
		data <- 15;
	}

	reflex move
	{
	}
	
	reflex updateData
	{
		data <- data + 1;
		write("MY data " + data);
	}
	
	aspect classic
	{		
		if(!fake)
		{
			
			draw circle(1.5) color: col;
			draw name at: location color: #black font: font('Default', 9, #bold);
		}else
		{
			draw circle(0.5) color: rgb(col,0.5);
			draw name+"_COPY" at: location color: #black font: font('Default', 9, #bold);
			//draw polyline([standingAgent[index].position, self.position]);
			//draw line(location, 50) color: col;
		}
		
		
		//draw string(data) color: #purple font: font('Default', 50, #bold);
	}
}

species interactingAgent skills:[ProxySkill]
{
	rgb col;
	standingAgent target;	
	
	init
	{
		col <- #red;
	}
	
	reflex acquire_target
	{
		if(target = nil)
		{
			target <- one_of(standingAgent);
			write("new target " + target);
			write("target data " + target.data);
		}
	}
	
	reflex move
	{
		if(target != nil)
		{		
			write("I SEE THIS STANDING AGENT : " + target.data);
			target.data <- target.data + 1;
		}
	}
	
	aspect classic
	{		
		draw circle(1.5) color: col;
		draw name at: location color: #black font: font('Default', 20, #bold);
		if(target != nil)
		{		
			draw line(location, target) color: col;
			draw string(target.data) color: #purple font: font('Default', 50, #bold);
		}
	}
}