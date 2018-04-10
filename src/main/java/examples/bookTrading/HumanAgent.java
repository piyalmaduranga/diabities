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
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Collections;

import examples.IRAObjects.Space;
import examples.IRAObjects.Human;

public class HumanAgent extends Agent {
	private static final long serialVersionUID = 1L;

	// The catalogue of books for sale (maps the title of a book to its price)
	private ArrayList<Human> catalogue;
	// The GUI by means of which the user can add books in the catalogue

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		Object[] o = getArguments();
		catalogue = new ArrayList<Human>();
		for (Object object : o) {
			Human Human = (Human) object;
			catalogue.add(Human);
		}
		// fill this catalogue through arguments
		// Create and show the GUI
		/*
		 * myGui = new BookSellerGui(this); myGui.showGui();
		 */

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Human-allocation");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from buyer agents
		addBehaviour(new HumanOfferRequestsServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new AllocateHumanServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Printout a dismissal message
		System.out.println("Human Agent " + getAID().getName() + " terminating.");
	}

	/**
	 * Inner class OfferRequestsServer. This is the behaviour used by
	 * Book-seller agents to serve incoming requests for offer from buyer
	 * agents. If the requested book is in the local catalogue the seller agent
	 * replies with a PROPOSE message specifying the price. Otherwise a REFUSE
	 * message is sent back.
	 */
	private class HumanOfferRequestsServer extends CyclicBehaviour { // Negotiation
																	// Logic
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				// Integer price = (Integer) catalogue.get(title);
				ArrayList<Human> proposals = new ArrayList<Human>();
				int index = 0;
				for (Human c : catalogue) {
					if (c.getLabel().equals(title)) {
						proposals.add(catalogue.get(index));
					}
					index++;
				}
				// Collections.sort(proposals);

				// Bargain Logic here
				if (proposals.size() != 0) {
					// The requested book is available for sale. Reply with the
					// price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(proposals.get(0).getLabel()));
				} else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer

	/**
	 * Inner class PurchaseOrdersServer. This is the behaviour used by
	 * Book-seller agents to serve incoming offer acceptances (i.e. purchase
	 * orders) from buyer agents. The seller agent removes the purchased book
	 * from its catalogue and replies with an INFORM message to notify the buyer
	 * that the purchase has been sucesfully completed.
	 */
	private class AllocateHumanServer extends CyclicBehaviour { // Acceptance
																// Criteria
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				Human removeObj = null;
				for (Human c : catalogue) {
					if (c.getLabel().equals(title)) {
						removeObj = c;

					}
				}
				catalogue.remove(removeObj);
				// Integer price = (Integer) catalogue.remove(title);
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("******************"+title + " allocated to task " + msg.getSender().getName());

				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer
}
