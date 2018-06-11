package examples.bookTrading;

import java.awt.Container;

import examples.IRAObjects.Proposal;
import examples.IRAObjects.Space;
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
						
			//start resource agents
			mainContainer.startHumanAgent("Human Agent", new String[]{"160","Exercise","110"});
			mainContainer.startSystemAgent( "System Agent1");
			mainContainer.startSystemAgent( "System Agent2");
			mainContainer.startSystemAgent( "System Agent3");
			mainContainer.startSystemAgent( "System Agent4");					
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	public StartMainContainer initAgents() throws StaleProxyException{
		StartMainContainer mainContainer= StartMainContainer.getInstance();
		try {
			
			//Add all the resources
			mainContainer.startHumanAgent("Human Agent", new String[]{"160","Exercise","110"});
			mainContainer.startSystemAgent( "System Agent1");
			mainContainer.startSystemAgent( "System Agent2");
			mainContainer.startSystemAgent( "System Agent3");
			mainContainer.startSystemAgent( "System Agent4");

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
