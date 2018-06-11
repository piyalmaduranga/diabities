package examples.bookTrading;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class StartMainContainer {
	private static StartMainContainer startMainContainer;
	private static AgentContainer agentContainer;
	private static String[] timeSlots;
	
	public static StartMainContainer getInstance() throws StaleProxyException{
		if(startMainContainer==null){
			startMainContainer = new StartMainContainer();
			startMainContainer.startContainer();
			timeSlots = new String[20];
		}
		return startMainContainer;
	}
	
	private jade.wrapper.AgentContainer startContainer() throws StaleProxyException{

		// Get a hold on JADE runtime
		jade.core.Runtime rt = jade.core.Runtime.instance();

		// Exit the JVM when there are no more containers around
		rt.setCloseVM(true);
		System.out.print("runtime created\n");

		// Create a default profile
		Profile profile = new ProfileImpl(null, 1200, null);
		profile.setParameter("host", "localhost");
		profile.setParameter("port", "12661");
		profile.setParameter("gui", "false");
		System.out.print("profile created\n");

		System.out.println("Launching a whole in-process platform..."+profile);
		jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(profile);

		// now set the default Profile to start a container
		ProfileImpl pContainer = new ProfileImpl("localhost", 12661, null);
		System.out.println("Launching the agent container ..."+pContainer);

		jade.wrapper.AgentContainer cont = rt.createAgentContainer(pContainer);
		System.out.println("Launching the agent container after ..."+pContainer);

		System.out.println("containers created");
		System.out.println("Launching the rma agent on the main container ...");
		agentContainer = mainContainer;
		return mainContainer;
		
	}
	
	
	public static AgentController startSystemAgent(String agentName) throws StaleProxyException{
		AgentController rma = agentContainer.createNewAgent(agentName,
				"examples.bookTrading.SystemAgent", null); 
		rma.start();
		return rma;		
	}
	
	public static AgentController startHumanAgent(String agentName, String [] args) throws StaleProxyException{
		AgentController rma = agentContainer.createNewAgent(agentName,
				"examples.bookTrading.HumanAgent",args); 
		rma.start();
		return rma;				
	}
	
	public static AgentController startTaskAgent(String agentName,Object[] resources) throws StaleProxyException{
		AgentController rma = agentContainer.createNewAgent(agentName,
				"examples.bookTrading.TaskAgent", resources); 
		rma.start();
		return rma;	
	
	}
	
	public static AgentController startTaskNegotiatorAgent(String agentName,Object[] resources) throws StaleProxyException{
		AgentController rma = agentContainer.createNewAgent(agentName,
				"examples.bookTrading.TaskNegotiator", resources); 
		rma.start();
		return rma;	
	
	}

	public static String[] getTimeSlots() {
		return timeSlots;
	}

	public static void setTimeSlots(int index,String slotName) {
		StartMainContainer.timeSlots[index] = slotName;
	}

}
