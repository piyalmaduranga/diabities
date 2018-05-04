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

public class TaskAgent extends Agent {
	private static final long serialVersionUID = 1L;

	// The title of the agent
	private String currentSugarLevel;
	private String preference;
	private String targetSugarLevel;
	
	private String exercise;
	// The list of known seller agents
	private AID exerciseAgent;
	private AID dietAgent;

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
			addBehaviour(new TickerBehaviour(this, 10000) {
				private static final long serialVersionUID = 1L;

				protected void onTick() {
					
					DFAgentDescription templateEx = new DFAgentDescription();
					ServiceDescription exd = new ServiceDescription();
					exd.setType("exercise-allocation");
					templateEx.addServices(exd);
					try {
						DFAgentDescription[] resultEx = DFService.search(myAgent, templateEx);
						exerciseAgent = resultEx[0].getName();
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					DFAgentDescription dietTemplate = new DFAgentDescription();
					ServiceDescription dietSd = new ServiceDescription();
					dietSd.setType("diet-allocation");
					dietTemplate.addServices(dietSd);
					try {
						DFAgentDescription[] dietResult = DFService.search(myAgent, dietTemplate);
						dietAgent = dietResult[0].getName();
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
		private String bestExercise; // The best offered price
		private String bestDiet;
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate exerciseMt; // The template to receive replies
		private MessageTemplate dietMt; // The template to receive replies
		
		private int step = 0;
		private boolean exerciserecieved;
		private boolean dietrecieved;

		public void action() {
			switch (step) {
			case 0:
				ACLMessage exerciseCfp, dietCfp;
				exerciseCfp = new ACLMessage(ACLMessage.CFP);
				dietCfp = new ACLMessage(ACLMessage.CFP);
				
				if(preference.equals("exercise")){
					// Send the cfp to all sellers
					//exerciseCfp = new ACLMessage(ACLMessage.CFP);
					exerciseCfp.addReceiver(exerciseAgent);
					exerciseCfp.setContent(currentSugarLevel);
					exerciseCfp.setConversationId("exercise-trade");
					exerciseCfp.setReplyWith("exerciseCfp" + System.currentTimeMillis()); // Unique
																					// value
					myAgent.send(exerciseCfp);
	
				}else if(preference.equals("diet")){
					
					//dietCfp = new ACLMessage(ACLMessage.CFP);
					dietCfp.addReceiver(dietAgent);
					dietCfp.setContent(currentSugarLevel);
				    dietCfp.setConversationId("diet-trade");
					dietCfp.setReplyWith("dietcfp" + System.currentTimeMillis()); // Unique
					
					// Send the cfp to all sellers																// value
					myAgent.send(dietCfp);
				}
								
				// Prepare the template to get proposals
				exerciseMt = MessageTemplate.and(MessageTemplate.MatchConversationId("exercise-trade"),
						MessageTemplate.MatchInReplyTo(exerciseCfp.getReplyWith()));
				dietMt = MessageTemplate.and(MessageTemplate.MatchConversationId("diet-trade"),
						MessageTemplate.MatchInReplyTo(dietCfp.getReplyWith()));
				step = 1;
				break;
				
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage exercisereply = myAgent.receive(exerciseMt);
				ACLMessage dietreply = myAgent.receive(dietMt);

				if (exercisereply != null) {
					// Reply received
					if (exercisereply.getPerformative() == ACLMessage.PROPOSE) {

						// This is an offer
						bestAgent = exercisereply.getSender();

						String exerciseName = exercisereply.getContent();
						bestExercise = exerciseName;
						exerciseAgent = bestAgent;
					}
					repliesCnt++;
				}
				if (dietreply != null) {
					// Reply received
					if (dietreply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						bestAgent = exercisereply.getSender();

						String dietName = exercisereply.getContent();
						bestDiet = dietName;
						dietAgent = bestAgent;
					}
					repliesCnt++;

				}
				if (repliesCnt >= 2) {

					// We received all replies
					step = 2;
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best
				// offer
				ACLMessage exProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				exProposal.addReceiver(exerciseAgent);
				exProposal.setContent(bestExercise);
				exProposal.setConversationId("exercise-trade");
				exProposal.setReplyWith("exproposal" + System.currentTimeMillis());
				myAgent.send(exProposal);

				ACLMessage dietProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				dietProposal.addReceiver(dietAgent);
				dietProposal.setContent(bestDiet);
				dietProposal.setConversationId("diet-trade");
				dietProposal.setReplyWith("dietproposal" + System.currentTimeMillis());
				myAgent.send(dietProposal);

				// Prepare the template to get the purchase order reply
				exerciseMt = MessageTemplate.and(MessageTemplate.MatchConversationId("exercise-trade"),
						MessageTemplate.MatchInReplyTo(exProposal.getReplyWith()));
				dietMt = MessageTemplate.and(MessageTemplate.MatchConversationId("time-trade"),
						MessageTemplate.MatchInReplyTo(dietProposal.getReplyWith()));
				step = 3;
				
				break;
			case 3:
				// Receive the purchase order reply
				exercisereply = myAgent.receive(exerciseMt);
				dietreply = myAgent.receive(dietMt);
				
				if (exercisereply != null) {
					// Purchase order reply received
					if (exercisereply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						exerciserecieved = true;
						System.out.println("Excerice Hours = " + bestExercise);
						
					} else {
						System.out.println("Attempt failed: requested space already allocated.");
						
					}
				}
				if (dietreply != null) {
					if (dietreply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						dietrecieved = true;
						System.out.println("Best Diet = " + bestDiet);
						//
					} else {
						System.out.println("Attempt failed: requested time already allocated.");
						
					}

				}

				if (dietrecieved && exerciserecieved ) { // resolved
					step = 4;
					myAgent.doDelete();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && exerciseAgent == null) {
				//call task negotiator
				
				// Data mining DB logic here
			} else if (step == 2 && dietAgent == null) {
				myAgent.addBehaviour(new NegotiateRequestsServer());
			
			}
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
