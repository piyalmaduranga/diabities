/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package examples.bookTrading;

import jade.core.Agent;
import java.util.ArrayList;
import java.util.Collections;
import examples.IRAObjects.Space;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class HumanAgent extends Agent {
	private static final long serialVersionUID = 1L;

	// The title of the agent
	private String currentSugarLevel;
	private String preference;
	private String targetSugarLevel;
	
	private String exercise;
	// The list of known seller agents
	private ArrayList<AID> systemAgents =  new ArrayList<AID>();


	// Put agent initializations here
	protected void setup() {
		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			
			currentSugarLevel = (String) args[0];
			preference = (String) args[1];
			targetSugarLevel = (String) args[2];
			
			/////////////////// negotiator////////////////////////////////////
			DFAgentDescription tdfd = new DFAgentDescription();
			tdfd.setName(getAID());
			ServiceDescription std = new ServiceDescription();
			std.setType("negotiation-allocation");
			std.setName("diabities-check");
			tdfd.addServices(std);
			try {
				DFService.register(this, tdfd);
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Checks whether there are newly registered agents in the agent registry
			// every 10 secs
			addBehaviour(new TickerBehaviour(this, 100000) {
				private static final long serialVersionUID = 1L;

				protected void onTick() {
					
					DFAgentDescription templateEx = new DFAgentDescription();
					ServiceDescription exd = new ServiceDescription();
					exd.setType("proposal-allocation");
					templateEx.addServices(exd);
					systemAgents.clear();
					try {
						DFAgentDescription[] resultEx = DFService.search(myAgent, templateEx);
						for(DFAgentDescription dfa: resultEx){
							systemAgents.add(dfa.getName());
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					// Perform the request
					myAgent.addBehaviour(new RequestPerformer());
				}
			});
		} else {
			// Make the agent terminate
			System.out.println("No target book title specified");
			doDelete();
		}
	} 

	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Task-agent " + getAID().getName() + " terminating.");
	}

	/**
	 * Inner class RequestPerformer. This is the behaviour used by Book-buyer
	 * agents to request seller agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;

		private AID bestAgent; // The agent who provides the best offer
		private AID exerciseAgent;
		private AID dietAgent;
		private String bestProposal; // The best offered price
		private String bestDiet;
		private int repliesCnt = 0; // The counter of replies from seller agents
		private ArrayList<MessageTemplate> systemMts = new ArrayList<MessageTemplate>(); // The template to receive replies
		private MessageTemplate systemAcceptMt;
		private int step = 0;
		private boolean systemacceptresponserecieved;

		public void action() {
			switch (step) {
			case 0:
				for (AID systemAgent : systemAgents){
					System.out.println("check no of");
					ACLMessage systemCfp;
					systemCfp = new ACLMessage(ACLMessage.CFP);
					systemCfp.addReceiver(systemAgent);
					systemCfp.setContent(currentSugarLevel);
					systemCfp.setConversationId("proposal-trade");
					systemCfp.setReplyWith("systemCFP" + System.currentTimeMillis()); // Unique
																					// value
					myAgent.send(systemCfp);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					for(MessageTemplate aid : systemMts){
//						systemMts.remove(aid);
//					}
					systemMts.clear();
					
					systemMts.add(MessageTemplate.and(MessageTemplate.MatchConversationId("proposal-trade"),
							MessageTemplate.MatchInReplyTo(systemCfp.getReplyWith())));
				}
				// Prepare the template to get proposals

				step = 1;
				break;
				
			case 1:
				// Receive all proposals/refusals from seller agents
				ArrayList<ACLMessage> systemreply = new ArrayList<ACLMessage>();
				System.out.println("*******" + systemMts.size());
				for(MessageTemplate systemmts: systemMts){
					System.out.println("*******" + systemmts);
					ACLMessage amessage = myAgent.receive(systemmts);
					System.out.println("*******" + amessage);
					systemreply.add(amessage);
				}
				for (ACLMessage systemMessage : systemreply) {
					// Reply received
					
					if (systemMessage.getPerformative() == ACLMessage.PROPOSE) {

						//piyal Accept scenario check blood level and preference count
						bestAgent = systemMessage.getSender();
						String exerciseName = systemMessage.getContent();
						// logic goes here
						// acceptence criteria
						bestProposal = exerciseName;
						exerciseAgent = bestAgent; // bestAgent = bestAgent
						System.out.println("$$$$$$$$$"+exerciseName+"$$$$$$$$$");
					}
					repliesCnt++;
				}
			
				if (bestAgent == null) {
					System.out.println("$$$$$$$$$$$$$$$$$$");
					System.out.println("Best agent is NOT NULL");
					// We received all replies
					step = 0;
				} else {
					step = 2;
				}
				break;
			case 2:
				System.out.println("************************");
				System.out.println("CASE 02");
				// Notify system agent
				// offer
				ACLMessage exProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				exProposal.addReceiver(bestAgent);
				exProposal.setContent(bestProposal);
				exProposal.setConversationId("propsal-trade");
				exProposal.setReplyWith("exproposal" + System.currentTimeMillis());
				myAgent.send(exProposal);

				// Prepare the template to get the purchase order reply
				systemAcceptMt = MessageTemplate.and(MessageTemplate.MatchConversationId("propsal-trade"),
						MessageTemplate.MatchInReplyTo(exProposal.getReplyWith()));
				step = 3;
				
				break;
			case 3:
				// Receive the purchase order reply
				ACLMessage systemacceptreply = myAgent.receive(systemAcceptMt);
								
				if (systemacceptreply != null) {
					
					// Purchase order reply received
					if (systemacceptreply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						systemacceptresponserecieved = true;
						
						
						//Piyal print output
						System.out.println("Excerice Hours = " + bestProposal);
					} else {
						System.out.println("Attempt failed: requested space already allocated.");
						
					}
				}
				if (systemacceptresponserecieved ) { // resolved
					System.out.println("CASE 03 TO DELETE");
					step = 4;
					myAgent.doDelete();
				}
				break;
			}
		}

		public boolean done() {
/*			if (step == 2 && exerciseAgent == null) {
				//call task negotiator
				
				// Data mining DB logic here
			} else if (step == 2 && dietAgent == null) {
				myAgent.addBehaviour(new NegotiateRequestsServer());
			
			}*/
			return false;
			// Add data to db
		}
	} // End of inner class RequestPerformer

	/**
	 * Inner class OfferRequestsServer. This is the behaviour used by
	 * Book-seller agents to serve incoming requests for offer from buyer
	 * agents. If the requested book is in the local catalogue the seller agent
	 * replies with a PROPOSE message specifying the price. Otherwise a REFUSE
	 * message is sent back.
	 */
	private class NegotiateRequestsServer extends CyclicBehaviour { // Negotiation
																	// Logic
		private static final long serialVersionUID = 1L;

		public void action() {

			ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("negotiation-allocation");
			//template.addServices(sd);
			
				DFAgentDescription[] result = null;
				try {
					result = DFService.search(myAgent, template);
					System.out.println("Size of results "+result[1].getName());
				} catch (FIPAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				

			// The requested book is available for sale. Reply with the price
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent(preference);
			for(DFAgentDescription aid:result){
				if(aid.getName().toString().contains("Negotiator")){
					System.out.println("Found the following negoatiation offers:");
					reply.addReceiver(aid.getName());
				}
			}
			reply.setConversationId("negotiation-allocation");
			reply.setReplyWith("negotiationcfp" + System.currentTimeMillis());

			myAgent.send(reply);
		}
	} // End of inner class OfferRequestsServer

	/**
	 * Inner class PurchaseOrdersServer. This is the behaviour used by
	 * Book-seller agents to serve incoming offer acceptances (i.e. purchase
	 * orders) from buyer agents. The seller agent removes the purchased book
	 * from its catalogue and replies with an INFORM message to notify the buyer
	 * that the purchase has been sucesfully completed.
	 */
	private class RecieveAllocationServer extends CyclicBehaviour { // Acceptance
																	// Criteria
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				if(!title.equals("0")){
					
				}
				// Integer price = (Integer) catalogue.remove(title);
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("******************" + title + " allocated to task " + msg.getSender().getName());

				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer
}
