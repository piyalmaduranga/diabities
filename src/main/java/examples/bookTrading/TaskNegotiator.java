package examples.bookTrading;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class TaskNegotiator extends Agent {
	private static final long serialVersionUID = 1L;

	// The list of known seller agents
	private AID[] negotiatorAgents;


	// Put agent initializations here
	protected void setup() {
		// Print a welcome message
		System.out.println("Hello! Task-agent "+getAID().getName()+" is ready.");

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (true) {

			// Add a TickerBehaviour that schedules a request for seller agents every minute
			addBehaviour(new TickerBehaviour(this, 5000) {
				private static final long serialVersionUID = 1L;

				protected void onTick() {

					///////////////////For Task negotiator////////////////////////////////////
					DFAgentDescription negotiatorTemplate = new DFAgentDescription();
					ServiceDescription negotiatorSd = new ServiceDescription();
					negotiatorSd.setType("negotiation-allocation");
					negotiatorTemplate.addServices(negotiatorSd);
					try {
						DFAgentDescription[] negotiatorResult = DFService.search(myAgent, negotiatorTemplate); 
						System.out.println("Found the following negotiation agents:");
						negotiatorAgents = new AID[negotiatorResult.length];
						for (int i = 0; i < negotiatorResult.length; ++i) {
							negotiatorAgents[i] = negotiatorResult[i].getName();
							System.out.println(negotiatorAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					

					// Perform the request
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {
			// Make the agent terminate
			System.out.println("No target book title specified");
			doDelete();
		}
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("negotiation-agent "+getAID().getName()+" terminating.");
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller 
	   agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;

		private AID bestAgent; // The agent who provides the best offer 
		private AID spaceAgent;
		private AID timeAgent;
		private AID materialAgent;
		private AID humanAgent;
		private AID taskAgent;
		private AID[] proposedAgents;
		private String bestHall;  // The best offered price
		private String bestTime;
		private String bestMaterial;
		private String bestHuman;
		private String bestTaskPriority;
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate spaceMt; // The template to receive replies
		private MessageTemplate timeMt; // The template to receive replies
		private MessageTemplate humanMt; // The template to receive replies
		private MessageTemplate materialMt; // The template to receive replies
		private MessageTemplate negotiationMt; // The template to receive replies
		
		private int step = 0;
		private boolean spacerecieved;
		private boolean timerecieved;
		private boolean materialrecieved;
		private boolean humanrecieved;

		public void action() {
			switch (step) {
			case 0:
				negotiationMt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(negotiationMt);
				
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						
						// This is an offer 
							bestAgent = reply.getSender();
							
						
							String taskDetails = reply.getContent();
							//send only task priority
							if (bestAgent == null || Integer.parseInt(bestTaskPriority) <=  Integer.parseInt(taskDetails)) {
								bestTaskPriority = taskDetails;
								taskAgent = bestAgent;
								
							}
					}
					repliesCnt++;
				}
				
				if (repliesCnt >= negotiatorAgents.length) {
					
					// We received all replies
					step = 2; 
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(taskAgent);
				order.setContent(bestTaskPriority);
				order.setConversationId("negotiation-trade");
				order.setReplyWith("negotiation"+System.currentTimeMillis());
				myAgent.send(order);
				
				ACLMessage order2 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				for (int i = 0; i < negotiatorAgents.length; ++i) {
					if(!negotiatorAgents[i].getName().equals(taskAgent))
					order2.addReceiver(negotiatorAgents[i]);
				}
				
				order2.setContent("0"); //Not Accepted
				order2.setConversationId("negotiation-trade");
				order2.setReplyWith("negotiation"+System.currentTimeMillis());
				myAgent.send(order2);


				// Prepare the template to get the purchase order reply
				negotiationMt = MessageTemplate.and(
						MessageTemplate.MatchConversationId("negotiation-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:    
				// Receive the purchase order reply
				reply = myAgent.receive(negotiationMt);

				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						spacerecieved = true;
						System.out.println(reply.getContent()
								+ " task successfully allocated to agent "
								+ reply.getSender().getName());
						//System.out.println("Hall = " + bestHall);
					}
					else {
						System.out.println("Attempt failed: requested space already allocated.");
					}
				}
				
				
				if(spacerecieved){ //resolved
					step = 4;
					myAgent.doDelete();
				}
				break;
			}        
		}

		public boolean done() {
				return step == 4;
			//Add data to db
		}
	}  // End of inner class RequestPerformer
}

