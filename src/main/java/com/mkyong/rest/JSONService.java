package com.mkyong.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import examples.bookTrading.AgentRunner;
import examples.bookTrading.StartMainContainer;
import jade.wrapper.StaleProxyException;

@Path("/task")
public class JSONService {

	@GET
	@Path("/getSlot")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrackInJSON(@QueryParam("name") String name,
								@QueryParam("time") String time,
								@QueryParam("human") String human,
								@QueryParam("space") String space,
								@QueryParam("material") String material,
								@QueryParam("priority") String priority) throws StaleProxyException {

		AgentRunner.getInstance();
		String[] timeslots = new String[20];
		
		StartMainContainer.startTaskAgent( name, new Object[]{new String(space),new String(time),new String(material),new String(human),timeslots,priority});
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Gson gson = new Gson();
		return gson.toJson(StartMainContainer.getTimeSlots());
	}
}