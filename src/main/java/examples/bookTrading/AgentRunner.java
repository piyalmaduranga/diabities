package examples.bookTrading;

import java.awt.Container;

import examples.IRAObjects.Human;
import examples.IRAObjects.Material;
import examples.IRAObjects.Space;
import examples.IRAObjects.Time;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class AgentRunner {
	
	private static AgentRunner agentRunner;
	private static AgentController taskNegotiator;
	
	public static AgentRunner getInstance() throws StaleProxyException{
		if(agentRunner==null){
			agentRunner = new AgentRunner();
			agentRunner.initAgents();
		}
		return agentRunner;
	}
	
	public static void main(String args[]){
		StartMainContainer mainContainer;
		try {
			mainContainer = StartMainContainer.getInstance();
				//Add all the resources
			Object [] spaces = new Object[]{new Space("L1H04", 160),new Space("L2H02", 120),new Space("L2H03", 60),new Space("L3H05", 200)};
			Object [] time = new Object[]{new Time("1"),new Time("2"),new Time("3"),new Time("4")};
			Object [] material = new Object[]{new Material("whiteboardpen"),new Material("projector"),new Material("dilini"),new Material("desk")};
			Object [] human = new Object[]{new Human("dilini"),new Human("locha"),new Human("karuna"),new Human("saminda")};
			
			//start resource agents
			mainContainer.startSpaceAgent("Space Agent", spaces);
			mainContainer.startTimeAgent("Time Agent", time);
			mainContainer.startHumanAgent("Human Agent", human);
			mainContainer.startMaterialAgent( "Material Agent", material);

			//Add task agents
			mainContainer.startTaskAgent( "L01English", new Object[]{new String("90"),new String("3"),new String("projector"),new String("dilini")});
			mainContainer.startTaskAgent("L03DataMining", new Object[]{new String("156"),new String("4"),new String("whiteboardpen"),new String("karuna")});
							
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	public StartMainContainer initAgents() throws StaleProxyException{
		StartMainContainer mainContainer= StartMainContainer.getInstance();
		try {
			
			//Add all the resources
			Object [] spaces = new Object[]{new Space("L1H04", 160),new Space("L2H02", 120),new Space("L2H03", 60),new Space("L3H05", 200)};
			Object [] time = new Object[]{new Time("1"),new Time("2"),new Time("3"),new Time("4")};
			Object [] material = new Object[]{new Material("whiteboardpen"),new Material("projector"),new Material("dilini"),new Material("desk")};
			Object [] human = new Object[]{new Human("dilini"),new Human("locha"),new Human("karuna"),new Human("saminda")};
			
			//start resource agents
			mainContainer.startSpaceAgent( "Space Agent", spaces);
			mainContainer.startTimeAgent("Time Agent", time);
			mainContainer.startHumanAgent( "Human Agent", human);
			mainContainer.startMaterialAgent( "Material Agent", material);
			setTaskNegotiator(mainContainer.startTaskNegotiatorAgent("Negotiator", null));
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		return mainContainer;
	}

	public static AgentController getTaskNegotiator() {
		return taskNegotiator;
	}

	public static void setTaskNegotiator(AgentController taskNegotiator) {
		AgentRunner.taskNegotiator = taskNegotiator;
	}

}
