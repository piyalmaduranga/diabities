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

	// The title of the book to buy
	private String targetSpace;
	private String targetTime;
	private String targetMaterial;
	private String targetHuman;
	private String taskpriority;
	// The list of known seller agents
	private AID[] spaceAgents;
	private AID[] timeAgents;
	private AID[] materialAgents;
	private AID[] humanAgents;
	private AID[] negotiatorAgents;

	// Put agent initializations here
	protected void setup() {
		// Print a welcome message
		System.out.println("Hello! Task-agent " + getAID().getName() + " is ready.");

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetSpace = (String) args[0];
			targetTime = (String) args[1];
			targetMaterial = (String) args[2];
			targetHuman = (String) args[3];
			taskpriority = (String) args[5];
			System.out.println("Target space size is " + targetSpace);
			System.out.println("Target time is " + targetTime);
			System.out.println("Target material is " + targetMaterial);
			System.out.println("Target human is " + targetHuman);
			System.out.println("Target priority is " + taskpriority);
			/////////////////// For Task
			/////////////////// negotiator////////////////////////////////////
			DFAgentDescription tdfd = new DFAgentDescription();
			tdfd.setName(getAID());
			ServiceDescription std = new ServiceDescription();
			std.setType("negotiation-allocation");
			std.setName("JADE-book-trading");
			tdfd.addServices(std);
			try {
				DFService.register(this, tdfd);
			} catch (FIPAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Add a TickerBehaviour that schedules a request for seller agents
			// every minute
			addBehaviour(new TickerBehaviour(this, 10000) {
				private static final long serialVersionUID = 1L;

				protected void onTick() {
					System.out.println("Trying to allocate space for " + targetSpace);
					System.out.println("Trying to allocate timeslot " + targetTime);
					System.out.println("Trying to allocate materials " + targetMaterial);
					System.out.println("Trying to allocate human " + targetHuman);
					// Update the list of seller agents for space
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("space-allocation");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template);
						System.out.println("Found the following space offers:");
						spaceAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							spaceAgents[i] = result[i].getName();
							System.out.println(spaceAgents[i].getName());
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					/////////////////// For
					/////////////////// time////////////////////////////////////
					DFAgentDescription timeTemplate = new DFAgentDescription();
					ServiceDescription timeSd = new ServiceDescription();
					timeSd.setType("time-allocation");
					timeTemplate.addServices(timeSd);
					try {
						DFAgentDescription[] timeResult = DFService.search(myAgent, timeTemplate);
						System.out.println("Found the following time offers:");
						timeAgents = new AID[timeResult.length];
						for (int i = 0; i < timeResult.length; ++i) {
							timeAgents[i] = timeResult[i].getName();
							System.out.println(timeAgents[i].getName());
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}

					/////////////////// For
					/////////////////// material////////////////////////////////////
					DFAgentDescription materialTemplate = new DFAgentDescription();
					ServiceDescription materialSd = new ServiceDescription();
					materialSd.setType("material-allocation");
					materialTemplate.addServices(materialSd);
					try {
						DFAgentDescription[] materialResult = DFService.search(myAgent, materialTemplate);
						System.out.println("Found the following material offers:");
						materialAgents = new AID[materialResult.length];
						for (int i = 0; i < materialResult.length; ++i) {
							materialAgents[i] = materialResult[i].getName();
							System.out.println(materialAgents[i].getName());
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}

					/////////////////// For
					/////////////////// human////////////////////////////////////
					DFAgentDescription humanTemplate = new DFAgentDescription();
					ServiceDescription humanSd = new ServiceDescription();
					humanSd.setType("human-allocation");
					humanTemplate.addServices(humanSd);
					try {
						DFAgentDescription[] humanResult = DFService.search(myAgent, humanTemplate);
						System.out.println("Found the following human offers:");
						humanAgents = new AID[humanResult.length];
						for (int i = 0; i < humanResult.length; ++i) {
							humanAgents[i] = humanResult[i].getName();
							System.out.println(humanAgents[i].getName());
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
		private AID spaceAgent;
		private AID timeAgent;
		private AID materialAgent;
		private AID humanAgent;
		private String bestHall; // The best offered price
		private String bestTime;
		private String bestMaterial;
		private String bestHuman;
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate spaceMt; // The template to receive replies
		private MessageTemplate timeMt; // The template to receive replies
		private MessageTemplate humanMt; // The template to receive replies
		private MessageTemplate materialMt; // The template to receive replies

		private int step = 0;
		private boolean spacerecieved;
		private boolean timerecieved;
		private boolean materialrecieved;
		private boolean humanrecieved;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage spaceCfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < spaceAgents.length; ++i) {
					spaceCfp.addReceiver(spaceAgents[i]);
				}
				spaceCfp.setContent(targetSpace);
				spaceCfp.setConversationId("space-trade");
				spaceCfp.setReplyWith("spacecfp" + System.currentTimeMillis()); // Unique
																				// value
				myAgent.send(spaceCfp);
				// Send the cfp to all sellers
				ACLMessage timeCfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < timeAgents.length; ++i) {
					timeCfp.addReceiver(timeAgents[i]);
				}
				timeCfp.setContent(targetTime);
				timeCfp.setConversationId("time-trade");
				timeCfp.setReplyWith("timecfp" + System.currentTimeMillis()); // Unique
																				// value
				myAgent.send(timeCfp);
				// Send the cfp to all sellers
				ACLMessage materialCfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < materialAgents.length; ++i) {
					materialCfp.addReceiver(materialAgents[i]);
				}
				materialCfp.setContent(targetMaterial);
				materialCfp.setConversationId("material-trade");
				materialCfp.setReplyWith("materialcfp" + System.currentTimeMillis()); // Unique
																						// value
				myAgent.send(materialCfp);
				// Send the cfp to all sellers
				ACLMessage humanCfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < humanAgents.length; ++i) {
					humanCfp.addReceiver(humanAgents[i]);
				}
				humanCfp.setContent(targetHuman);
				humanCfp.setConversationId("human-trade");
				humanCfp.setReplyWith("humancfp" + System.currentTimeMillis()); // Unique
																				// value
				myAgent.send(humanCfp);
				// Prepare the template to get proposals
				spaceMt = MessageTemplate.and(MessageTemplate.MatchConversationId("space-trade"),
						MessageTemplate.MatchInReplyTo(spaceCfp.getReplyWith()));
				timeMt = MessageTemplate.and(MessageTemplate.MatchConversationId("time-trade"),
						MessageTemplate.MatchInReplyTo(timeCfp.getReplyWith()));
				materialMt = MessageTemplate.and(MessageTemplate.MatchConversationId("material-trade"),
						MessageTemplate.MatchInReplyTo(materialCfp.getReplyWith()));
				humanMt = MessageTemplate.and(MessageTemplate.MatchConversationId("human-trade"),
						MessageTemplate.MatchInReplyTo(humanCfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(spaceMt);
				ACLMessage timereply = myAgent.receive(timeMt);
				ACLMessage materialreply = myAgent.receive(materialMt);
				ACLMessage humanreply = myAgent.receive(humanMt);

				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {

						// This is an offer
						bestAgent = reply.getSender();

						String spaceHallName = reply.getContent();
						bestHall = spaceHallName;
						spaceAgent = bestAgent;
					}
					repliesCnt++;
				}
				if (timereply != null) {
					// Reply received
					if (timereply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						bestAgent = timereply.getSender();

						String timeProposal = timereply.getContent();
						if (timeProposal.equals(targetTime)) {
							bestTime = timeProposal;
							timeAgent = bestAgent;
						}
					}
					repliesCnt++;

				}
				if (materialreply != null) {
					// Reply received
					if (materialreply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						bestAgent = materialreply.getSender();
						String materialProposal = materialreply.getContent();
						if (materialProposal.equals(targetMaterial)) {
							bestMaterial = materialProposal;
							materialAgent = bestAgent;
						}
					}
					repliesCnt++;

				}
				if (humanreply != null) {
					// Reply received
					if (humanreply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						bestAgent = humanreply.getSender();

						String humanProposal = humanreply.getContent();
						if (humanProposal.equals(targetHuman)) {
							bestHuman = humanProposal;
							humanAgent = bestAgent;
						}
					}

					repliesCnt++;

				}
				if (repliesCnt >= (spaceAgents.length + timeAgents.length + materialAgents.length
						+ humanAgents.length)) {

					// We received all replies
					step = 2;
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best
				// offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(spaceAgent);
				order.setContent(bestHall);
				order.setConversationId("space-trade");
				order.setReplyWith("order" + System.currentTimeMillis());
				myAgent.send(order);

				ACLMessage order2 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order2.addReceiver(timeAgent);
				order2.setContent(bestTime);
				order2.setConversationId("time-trade");
				order2.setReplyWith("order2" + System.currentTimeMillis());
				myAgent.send(order2);

				ACLMessage materialorder = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				materialorder.addReceiver(materialAgent);
				materialorder.setContent(bestMaterial);
				materialorder.setConversationId("material-trade");
				materialorder.setReplyWith("materialorder" + System.currentTimeMillis());
				myAgent.send(materialorder);

				ACLMessage humanorder = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				humanorder.addReceiver(humanAgent);
				humanorder.setContent(bestHuman);
				humanorder.setConversationId("human-trade");
				humanorder.setReplyWith("humanorder" + System.currentTimeMillis());
				myAgent.send(humanorder);

				// Prepare the template to get the purchase order reply
				spaceMt = MessageTemplate.and(MessageTemplate.MatchConversationId("space-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				timeMt = MessageTemplate.and(MessageTemplate.MatchConversationId("time-trade"),
						MessageTemplate.MatchInReplyTo(order2.getReplyWith()));
				materialMt = MessageTemplate.and(MessageTemplate.MatchConversationId("material-trade"),
						MessageTemplate.MatchInReplyTo(materialorder.getReplyWith()));
				humanMt = MessageTemplate.and(MessageTemplate.MatchConversationId("human-trade"),
						MessageTemplate.MatchInReplyTo(humanorder.getReplyWith()));
				step = 3;
				break;
			case 3:
				// Receive the purchase order reply
				reply = myAgent.receive(spaceMt);
				timereply = myAgent.receive(timeMt);
				materialreply = myAgent.receive(materialMt);
				humanreply = myAgent.receive(humanMt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						spacerecieved = true;
						System.out.println(
								targetSpace + " successfully allocated from agent " + reply.getSender().getName());
						System.out.println("Hall = " + bestHall);
					} else {
						System.out.println("Attempt failed: requested space already allocated.");
						
					}
				}
				if (timereply != null) {
					if (timereply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						timerecieved = true;
						System.out.println(
								targetTime + " successfully allocated from agent " + timereply.getSender().getName());
						System.out.println("Time = " + bestTime);
						//
					} else {
						System.out.println("Attempt failed: requested time already allocated.");
						
					}

				}
				if (materialreply != null) {
					if (materialreply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						materialrecieved = true;
						System.out.println(targetMaterial + " successfully allocated from agent "
								+ materialreply.getSender().getName());
						System.out.println("material = " + bestMaterial);
						//
					} else {
						System.out.println("Attempt failed: requested material already allocated.");
					}

				}
				if (humanreply != null) {
					if (humanreply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						humanrecieved = true;
						System.out.println(
								targetHuman + " successfully allocated from agent " + humanreply.getSender().getName());
						System.out.println("human = " + bestHuman);
						//
					} else {
						System.out.println("Attempt failed: requested time already allocated.");
					}

				}

				if (spacerecieved && timerecieved && humanrecieved && materialrecieved) { // resolved
					step = 4;
					myAgent.doDelete();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && spaceAgent == null) {
				System.out.println(
						"Attempt failed: targetSpace " + targetSpace + " not available (Use data mining to suggest)");
				//call task negotiator
				
				// Data mining DB logic here
			} else if (step == 2 && timeAgent == null) {
				System.out.println(
						"Attempt failed: targetTime " + targetTime + " not available (Use data mining to suggest)");
				myAgent.addBehaviour(new NegotiateRequestsServer());
			
			} else if (step == 2 && humanAgent == null) {
				System.out.println(
						"Attempt failed: targetHuman " + targetHuman + " not available (Use data mining to suggest)");

			} else if (step == 2 && materialAgent == null) {
				System.out.println("Attempt failed: targetMaterial " + targetMaterial
						+ " not available (Use data mining to suggest)");

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
			reply.setContent(taskpriority);
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
