import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;




public class GreedyKempChain {

	ArrayList<ColorClass> color_classes;

	public class ColorClass{
		int my_color;

		TreeSet<Integer> my_nodes;
		TreeSet<Integer> my_neighbors;
		
		public String toFullString(ColorClass cc){
			String s = "";
			s += "color class "+my_color+" with "+my_nodes.size() +" nodes: ";
			for(Integer ii: my_nodes){
				s+=ii+" ";
			}
			s+= " and "+my_neighbors.size()+" neighbors of which " +and(my_neighbors,cc.my_nodes).size()+" are in color class "+cc.my_color+" : ";
			TreeSet<Integer> combo = and(my_neighbors,cc.my_nodes);
			for(Integer ii: combo){
				s+=ii+" ";
			}
			return s;
		}


		public ColorClass(int index, int nodes){
			my_color = index;
			my_nodes = new TreeSet<Integer>();
			my_neighbors = new TreeSet<Integer>();
		}

		public void addNode(int n, Graph g){
			my_nodes.add(n);
			for(int ii = 0; ii < g.getNode(n).edges.size(); ii++){
				my_neighbors.add(g.getNode(n).edges.get(ii).node_index);
			}
		}

		public void removeNode(int index, Graph g){
			my_nodes.remove(index);
			for(int ii = 0; ii < g.getNode(index).edges.size(); ii++){
				my_neighbors.remove(g.getNode(index).edges.get(ii).node_index);
			}
			for(Integer ii: my_nodes){
				for(int jj = 0; jj < g.getNode(ii).edges.size(); jj++){
					my_neighbors.add(g.getNode(ii).edges.get(jj).node_index);
				}
			}
		}

		/**
		 * returns true if the swap destroyed a color
		 * @param cc
		 * @param g
		 * @return
		 */
		public boolean swap(ArrayList<ColorClass> list, ColorClass cc,Graph g){
			TreeSet<Integer> mynodesANDccNeighbors = and(my_nodes,cc.my_neighbors);
			TreeSet<Integer> myneighborsANDccNodes = and(my_neighbors,cc.my_nodes);
			for(Integer ii: mynodesANDccNeighbors){
				cc.addNode(ii,g);
				this.removeNode(ii, g);
				g.getNode(ii).setColor(cc.my_color);
			}
			for(Integer ii: myneighborsANDccNodes){
				this.addNode(ii, g);
				cc.removeNode(ii, g);
				g.getNode(ii).setColor(this.my_color);
			}
			if(cc.my_nodes.size() == 0){
				System.out.println("here!!!!!!!!!!!!!!!!!");
				int index = cc.my_color;
				list.remove(index);
				for(int ii = 0; ii < list.size(); ii++){
					list.get(ii).changeColor(ii,g);
				}

				return true;
			}
			if(this.my_nodes.size() == 0){
				System.out.println("her2e!!!!!!!!!!!!!!!!!");
				int index = my_color;
				list.remove(index);
				for(int ii = 0; ii < list.size(); ii++){
					list.get(ii).changeColor(ii,g);
				}

				return true;
			}
			return false;
		}

		private TreeSet<Integer> and(TreeSet<Integer> s1,
				TreeSet<Integer> s2) {
			TreeSet<Integer> and = new TreeSet<Integer>();
			for(Integer ii: s1){
				for(Integer jj: s2){
					if(ii.intValue() == jj.intValue()){
						and.add(ii);
					}
				}
			}
			return and;
		}


		public boolean merge(ArrayList<ColorClass> list, ColorClass cc, Graph g){
			if(!isMergable(cc)){
				System.out.println("not mergible, stand off");
				return false;
			}
			this.swap(list,cc, g);
		

			return true;
		}

		private void changeColor(int color, Graph g) {
			my_color = color;
			for(Integer ii: my_nodes){
				g.getNode(ii).color = color;
			}

		}

		public int scoreSwap(ColorClass cc){
			if(and(my_nodes, cc.my_neighbors).size() == my_nodes.size() && and(cc.my_nodes,my_neighbors).size()==cc.my_nodes.size())return Integer.MIN_VALUE;
			if(this.my_nodes.size() > cc.my_nodes.size()){
				int score = my_nodes.size();
				score *= score;

				int newscore = my_nodes.size()-and(my_nodes,cc.my_neighbors).size()+and(my_neighbors,cc.my_nodes).size();
				newscore *= newscore;
				return newscore-score;
			}
			else if(this.my_nodes.size() < cc.my_nodes.size()){
				int score = cc.my_nodes.size();
				score *= score;

				int newscore = cc.my_nodes.size()-and(cc.my_nodes,my_neighbors).size()+and(cc.my_neighbors,my_nodes).size();
				newscore *= newscore;
				return newscore-score;
			}
			else return 0;
			
		}

		public boolean isMergable(ColorClass cc){
			return(cc.my_nodes.size()-and(my_neighbors,cc.my_nodes).size()+and(my_nodes,cc.my_neighbors).size() == 0);
		}


	}


	public GreedyKempChain(int nodes, int edges, int[] sources, int[] terminals){
		color_classes = new ArrayList<ColorClass>();
		Graph graph = new Graph();
		for(int ii = 0; ii < nodes; ii++){
			graph.addNode(new Node(ii));
		}
		for(int ii = 0; ii < sources.length; ii++){

			graph.getNode(sources[ii]).addEdge(graph.getNode(terminals[ii]));
			graph.getNode(terminals[ii]).addEdge(graph.getNode(sources[ii]));
		}

		ArrayList<Node> copy = graph.copyOfNodes();
		Collections.sort(copy, new Comparator<Node>(){

			@Override
			public int compare(Node arg0, Node arg1) {

				return (4*arg1.ColoredNeighbors()+arg1.Neighbors())-(4*arg0.ColoredNeighbors()+arg0.Neighbors());
			}

		});
		int index = 0;
		while(!graph.isColored()){
			System.out.println(index++);

			int nodetochange = 0;
			for(int ii = 0; ii < nodes; ii++){
				if(!copy.get(ii).isColored()){
					nodetochange = ii;
					break;
				}
			}
			int colorclass = copy.get(nodetochange).setColorToMinAvailable();
			if(colorclass >= color_classes.size()){
				if(colorclass!=color_classes.size()){
					new Exception().printStackTrace();
				}
				color_classes.add(new ColorClass(colorclass,nodes));
			}
			color_classes.get(colorclass).addNode(copy.get(nodetochange).node_index,graph);
		}

		System.out.println("color classes "+color_classes.size());

		Graph bestSolution = graph.deepCopy();
		kempeChaining(graph);






		HashSet<Integer> colorset = new HashSet<Integer>();
		for(int ii = 0; ii < nodes; ii++){
			if(colorset.contains(graph.getNode(ii).color)){

			}
			else{
				colorset.add(graph.getNode(ii).color);
			}
		}
		System.out.println(colorset.size()+" "+ 0);
		for(int ii = 0; ii < nodes; ii++){
			System.out.print(graph.getNode(ii).color+" ");
		}


	}

	private boolean kempeChaining(Graph graph) {

		double temperature = 30;
		int index = 0;
		Random r = new Random(9);
		while(true){
			/**
			 * check for mergible
			 */
//			for(int ii = 0; ii < color_classes.size(); ii++){
//				for(int jj = 0; jj < color_classes.size();jj++){
//					if(ii == jj)continue;
//					//if(color_classes.get(ii).isMergable(color_classes.get(jj))){
//					boolean merge = color_classes.get(ii).isMergable(color_classes.get(jj));
//
//					if(merge){
//						System.out.println("\t\t\t\tmerging!");
//						color_classes.get(ii).merge(color_classes, color_classes.get(jj), graph);
//						System.out.println("Graph now colored with "+graph.numColors()+" colors");
//					}
//					//					int swapscore = color_classes.get(ii).scoreSwap(color_classes.get(jj));
//					//					System.out.println("swap score = "+swapscore);
//					//					if(swapscore < minSwapScore){
//					//						minSwapScore = swapscore;
//					//						swap1 = ii;
//					//						swap2 = jj;
//					//					}
//
//				}
//			}

			/**
			 * pick two random color classes to swap
			 */
			boolean change = false;
			while(!change){
				int first = r.nextInt(color_classes.size());
				int second = r.nextInt(color_classes.size());
				if(first == second)continue;
				int score = -1;
				boolean takeit = false;
				if((score = color_classes.get(first).scoreSwap(color_classes.get(second))) > 0){
					takeit = true;
				}
				else {
					
					double probability = Math.exp(score/temperature);
					//System.out.println("score of "+score+" probability "+probability);
					double threshold = r.nextDouble();
					if(probability > threshold){
						takeit = true;
					}
					
				}
				if(takeit){
					index++;
					change = true;
//					System.out.println("before swap");
//					System.out.println(color_classes.get(first).toFullString(color_classes.get(second)));
//					System.out.println(color_classes.get(second).toFullString(color_classes.get(first)));
					int size1 = color_classes.get(first).my_nodes.size();
					int size2 = color_classes.get(second).my_nodes.size();
					boolean merged = color_classes.get(first).swap(color_classes,color_classes.get(second),graph);
					int size3 = color_classes.get(first).my_nodes.size(); 
					int size4 = color_classes.get(second).my_nodes.size();
//					if(size1>size2){
//						System.out.println("color class "+first+" increased from "+size1+" to "+size3+" while color class "+second+" went from "+size2+" to "+size4);
//						
//					}
//					else{
//						System.out.println("color class "+second+" increased from "+size2+" to "+size4+" while color class "+first+" went from "+size1+" to "+size3);
//					}
//					System.out.println("after swap");
					if(index%100 == 0){
						System.out.println(objectiveFunction(color_classes)+" "+temperature+" min color size "+min(color_classes));
						temperature /= 1.002;
					}
//					System.out.println(color_classes.get(first).toFullString(color_classes.get(second)));
//					System.out.println(color_classes.get(second).toFullString(color_classes.get(first)));
//					System.out.println("swapping "+first+" with "+second+" with score "+score+(merged?" graph now smaller with"+color_classes.size()+" colors!":""));
//					System.exit(0);
					if(merged){
						System.out.println("reduced colors to "+color_classes.size()+"!");
					}
				}

			}


			

		}



	}

	private int min(ArrayList<ColorClass> cc) {
		int min = Integer.MAX_VALUE;
		for(int ii = 0; ii < cc.size(); ii++){
			if(cc.get(ii).my_nodes.size() < min){
				min = cc.get(ii).my_nodes.size();
			}
		}
		return min;
	}

	private int objectiveFunction(ArrayList<ColorClass> cc) {
		int score = 0;
		for(int ii = 0; ii < cc.size(); ii++){
			score += cc.get(ii).my_nodes.size()*cc.get(ii).my_nodes.size();
		}
		return score;
	}

	public class Graph{

		ArrayList<Node> nodes;
		public Graph(){
			nodes = new ArrayList<Node>();
		}


		public int numColors() {
			HashSet<Integer> colors = new HashSet<Integer>();
			for(int ii = 0; ii < nodes.size(); ii++){
				colors.add(nodes.get(ii).color);
			}
			return colors.size();
		}


		public Graph deepCopy() {
			Graph graph = new Graph();
			for(int ii = 0; ii < nodes.size(); ii++){
				Node n = new Node(nodes.get(ii).node_index);
				n.color = nodes.get(ii).color;
				graph.addNode(n);
			}
			for(int ii = 0; ii < nodes.size(); ii++){
				for(int jj = 0; jj < nodes.get(ii).edges.size(); jj++){
					graph.getNode(ii).addEdge(graph.getNode(nodes.get(ii).edges.get(jj).node_index));
				}
			}
			return graph;
		}


		public boolean isColored() {

			for(int ii = 0; ii < nodes.size(); ii++){
				if(!nodes.get(ii).isColored()){
					return false;
				}
			}
			return true;
		}
		public void addNode(Node n){
			nodes.add(n);
		}

		public Node getNode(int index){
			return nodes.get(index);
		}


		public ArrayList<Node> getNodes(){
			return nodes;
		}

		/**
		 * copies the array list, not the nodes
		 * @return
		 */
		public ArrayList<Node> copyOfNodes(){
			ArrayList<Node> copy = new ArrayList<Node>();
			for(int ii = 0; ii < nodes.size(); ii++){
				copy.add(nodes.get(ii));
			}
			return copy;
		}

	}

	public class Node{

		ArrayList<Node> edges;
		int node_index;
		int color;

		public Node(int nodeindex){
			node_index = nodeindex;
			edges = new ArrayList<Node>();
			color = -1;
		}



		public int setColorToMinAvailable() {
			int trycolor = 0;
			boolean free = false;
			while(!free){
				free = true;
				for(int ii = 0; ii < edges.size(); ii++){
					//System.out.println(edges.get(ii).getColor()+" trycolor "+trycolor);
					if(edges.get(ii).getColor() == trycolor){
						free = false;
						break;
					}
				}
				if(!free)
					trycolor++;
			}
			System.out.println("Setting color to "+trycolor+" edges "+edges.size()+" colored neighbors "+ColoredNeighbors());
			this.color = trycolor;
			return this.color;
		}

		private int getColor() {
			return this.color;
		}

		public int Neighbors(){
			return edges.size();
		}

		public int ColoredNeighbors() {
			int coloredneighbors = 0;
			for(int ii = 0; ii < edges.size(); ii++){
				coloredneighbors += edges.get(ii).isColored()?1:0;
			}
			return coloredneighbors;
		}

		public void setColor(int color) {
			this.color = color;
		}

		public void addEdge(Node e){
			edges.add(e);
		}

		public boolean isColored(){
			return (color!=-1);
		}


	}

	public class Edge{
		int node_1;
		int node_2;
		public Edge(int n1, int n2){
			node_1 = n1;
			node_2 = n2;
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
