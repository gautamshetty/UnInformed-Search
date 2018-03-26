
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Program for finding the optimal route between two cities.
 * @author gautamshetty
 */
public class find_route {
	
	/**
	 * Data from input file containing all routes between cities.
	 */
	private Map data = null;
	
	/**
	 * Constructor.
	 * @param fileName
	 */
	public find_route(String fileName) {
		data = readData(fileName);
	}
	
	/**
	 * Creates and returns new Node object for a city. 
	 * @param cityName Name of city.
	 * @param stepCost Cost from parent city to this city. 
	 * @param parentNode Parent city object.
	 * @return
	 */
	private Node createNode(String cityName, long stepCost, Node parentNode) {
		return this.new Node(cityName, stepCost, parentNode);
	}
	
	/**
	 * Searches the optimal path, using Uniform Cost Search, between source city and destination city.
	 * Prints the optimal distance and intermediate cities.
	 * @param source Source city.
	 * @param destination Destination city.
	 */
	private void searchPath(String source, String destination) {
		
		Node sourceNode = createNode(source, 0, null);
		
		List frontier = new ArrayList();
		List exploredSet = new ArrayList();
		frontier.add(sourceNode);
		
		while (!frontier.isEmpty()) {
			
			Node node = getLowestCostNode(frontier);
			if (node.getCityName().equals(destination)) {	// Goal Test.
				
				System.out.println("distance: " + node.getPathCost() + " km");
				System.out.println("route:");
				printSolution(node);
				return;
			}
			exploredSet.add(node);
			
			List childNodes = getChildNodes(node);
			if (!childNodes.isEmpty()) {
				Iterator it = childNodes.iterator();
				while(it.hasNext()) {
					Node childNode = (Node) it.next();
					if (!frontier.contains(childNode) && !exploredSet.contains(childNode)) 
						frontier.add(childNode);
					else if (frontier.contains(childNode)) {
						Node prevChildNode = (Node) frontier.get(frontier.indexOf(childNode));
						if (childNode.getPathCost() < prevChildNode.getPathCost())
							frontier.set(frontier.indexOf(childNode), childNode);
					}
				}
				
				Collections.sort(frontier);
			}
		}
		
		System.out.println("distance: infinity");
		System.out.println("route: \nnone");
	}
	
	/**
	 * Returns list of all child nodes of cities from reachable from the parameter parent node. 
	 * @param node parent node.
	 * @return List of child city nodes.
	 */
	private List getChildNodes(Node node) {
		
		List childNodes = new ArrayList();
		
		List childList = (List) data.get(node.getCityName());
		if (childList != null) {
			Iterator it = childList.iterator();
			String [] strTokens = null;
			Node childNode = null;
			while (it.hasNext()) {
				strTokens = ((String) it.next()).split(" ");
				childNode = createNode(strTokens[0], Long.parseLong(strTokens[1]), node);
				childNodes.add(childNode);
			}
		}
		
		return childNodes;
	}
	
	/**
	 * Returns the city with lowest cost from the list, for a parent node and removes it from the list.
	 * @param frontier list of city nodes.
	 * @return Node object for lowest cost city.
	 */
	private Node getLowestCostNode(List frontier) {
		Iterator it = frontier.iterator();
		Node node = (Node) it.next();
		it.remove();
		return node;
	}
	
	/**
	 * Displays the path from source to destination cities with the intermediate cities and their distances.
	 * @param goalNode Destination city. 
	 */
	private void printSolution(Node goalNode) {
		
		if (goalNode.getParentNode() == null) 
			return;
		
		printSolution(goalNode.getParentNode());
		
		System.out.println(goalNode.getParentNode().getCityName() + " to " + goalNode.getCityName() 
								+ ", " + goalNode.getStepCost() + " km");
	}
	
	/**
	 * Reads the city route data from the file and returns a map data structure. 
	 * @param fileName Name of input file.
	 * @return Map of city routes from source to destination.
	 */
	private Map readData(String fileName) {
		
		Map data = new HashMap();
		
		BufferedReader bufReader = null;
		try {
			bufReader = new BufferedReader(new FileReader(fileName));
			
			String strLine = "";
			String [] strTokens = null;
			List destList = null;
			while ((strLine = bufReader.readLine()) != null && !strLine.trim().isEmpty() && !strLine.trim().equals("END OF INPUT")) {
				
				strTokens = strLine.split(" ");
				String srcCity = "", destCity = "";
				for (int i = 0; i < strTokens.length - 1; i++) {
					
					srcCity = strTokens[i];
					destCity = (i == 0 ? strTokens[1] : strTokens[0]);
					
					if (!data.containsKey(srcCity)) {
						destList = new ArrayList();
						data.put(srcCity, destList);
					} else 
						destList = (List) data.get(srcCity);
					
					destList.add(destCity+" "+strTokens[2]);
				}
			}
		} catch (FileNotFoundException fnfExp) {
			System.out.println("ERROR: File with name " + fileName + " not found.");
		} catch (Exception ioExp) {
			System.out.println("ERROR: Error reading file " + fileName);
			ioExp.printStackTrace();
		} finally {
			try {
				if (bufReader != null)
					bufReader.close();
			} catch(Exception exp){
				System.out.println("ERROR: Error closing the file " + fileName);
				exp.printStackTrace();
			}
		}
		
		return data;
	}
	
	public static void main(String[] args) {
		
		if (args.length != 3) {
			System.out.println("ERROR: Insufficient parameters.");
			System.out.println("SYNTAX: java find_route input_filename origin_city destination_city");
			System.exit(1);
		}
		
		String fileName = args[0];
		String source = args[1];
		String destination = args[2];
		
		find_route fr = new find_route(fileName);
		fr.searchPath(source, destination);
	}
	
	/**
	 * Node representing one city.
	 * Encapsulates details like name of city, distance from parent city, parent city 
	 * and total path cost from the source city.
	 * @author gautamshetty
	 */
	private class Node implements Comparable {
		
		private String cityName;
		
		private Long pathCost;
		
		private Long stepCost;
		
		private Node parentNode;
		
		public Node() {
		}
		
		public Node(String cityName, long stepCost, Node parentNode) {
			this.setCityName(cityName);
			this.setStepCost(stepCost);
			this.setPathCost((parentNode == null? stepCost : parentNode.getPathCost() + stepCost));
			this.setParentNode(parentNode);
		}
		
		public Long getStepCost() {
			return stepCost;
		}
		
		public void setStepCost(Long stepCost) {
			this.stepCost = stepCost;
		}
		
		public String getCityName() {
			return cityName;
		}
		
		public void setCityName(String cityName) {
			this.cityName = cityName;
		}
		
		public Long getPathCost() {
			return pathCost;
		}
		
		public void setPathCost(Long pathCost) {
			this.pathCost = pathCost;
		}
		
		public Node getParentNode() {
			return parentNode;
		}
		
		public void setParentNode(Node parentNode) {
			this.parentNode = parentNode;
		}
		
		@Override
		public int compareTo(Object obj) {
			Node n2 = (Node) obj;
			return this.getPathCost().compareTo(n2.getPathCost());
		}
		
		@Override
		public boolean equals(Object obj) {
			Node n2 = (Node) obj;
			return this.getCityName().equals(n2.getCityName());
		}
		
		@Override
		public int hashCode() {
			return this.getCityName().hashCode();
		}
	}
}