import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import JaCoP.constraints.Alldifferent;
import JaCoP.constraints.Max;
import JaCoP.constraints.XeqY;
import JaCoP.constraints.XneqY;
import JaCoP.constraints.XplusClteqZ;
import JaCoP.core.IntVar;
import JaCoP.core.Store;
import JaCoP.search.DepthFirstSearch;
import JaCoP.search.IndomainMin;
import JaCoP.search.InputOrderSelect;
import JaCoP.search.Search;
import JaCoP.search.SelectChoicePoint;


public class FirstTry {

	Node[] the_nodes;
	Edge[] the_edges;


	public FirstTry(int nodes, int edges, int[] sources, int[] terminals){

		int size = nodes; 


		/**
		 * set up JGraphT and find maximal cliques 
		 */
		String[] vertices = new String[nodes];
		for(int ii = 0; ii < nodes; ii++){
			vertices[ii] = String.valueOf(ii);
		}
		SimpleGraph<String,DefaultEdge> graph = new SimpleGraph<String,DefaultEdge>(DefaultEdge.class);
		for(int ii = 0; ii < nodes; ii++){
			graph.addVertex(vertices[ii]);
		}
		for(int ii = 0; ii < sources.length; ii++){
			graph.addEdge(vertices[sources[ii]], vertices[terminals[ii]]);
		}
		
		int[] degree = new int[nodes];
		int smallestDegree = Integer.MAX_VALUE;
		int largestDegree = Integer.MIN_VALUE;
		for(int ii = 0; ii < nodes; ii++){
			
			degree[ii] = graph.edgesOf(vertices[ii]).size();
			if(degree[ii] > largestDegree){
				largestDegree = degree[ii];
			}
			if(degree[ii] < smallestDegree){
				smallestDegree = degree[ii];
			}
		
		}
		System.out.println("graph has minimum degree of "+smallestDegree+" and maximum degree of "+largestDegree);

//		BronKerboschCliqueFinder<String, DefaultEdge> cliquer = new BronKerboschCliqueFinder<String, DefaultEdge>(graph);
//		Collection<Set<String>> cliques = cliquer.getBiggestMaximalCliques();
//		int lowerbound = Integer.MIN_VALUE;
//
//		for(Set clique: cliques){
//			if(lowerbound < clique.size()){
//				lowerbound = clique.size();
//			}
//		}
//		System.out.println(cliques.size()+" cliques found, the biggest of which contains "+lowerbound);
//		System.out.println("Lower Bound "+lowerbound);

		int lowerbound = 5;

		/**
		 * find node pairs that have all the same connections but not connections to each other
		 */
		ArrayList<Integer> nodepair1 = new ArrayList<Integer>();
		ArrayList<Integer> nodepair2 = new ArrayList<Integer>();
		for(int ii = 0; ii < nodes; ii++){

			for(int jj = ii+1; jj < nodes; jj++){
				Set<DefaultEdge> edges1 = graph.edgesOf(String.valueOf(ii));
				Set<DefaultEdge> edges2 = graph.edgesOf(String.valueOf(jj));


				//are the edges of ii a subset of jj?
				boolean subsetiofj = true;
				for(DefaultEdge e: edges1){
					String terminal = (graph.getEdgeSource(e).equals(vertices[ii])?graph.getEdgeTarget(e):graph.getEdgeSource(e));
					if(graph.getEdge(vertices[jj], terminal) == null && graph.getEdge(terminal, vertices[jj]) == null){
						subsetiofj = false;
						break;
					}
				}
				if(subsetiofj){
					if(graph.getEdge(vertices[ii], vertices[jj]) != null || graph.getEdge(vertices[jj], vertices[ii]) != null){

					}
					else{
						nodepair1.add(ii);
						nodepair2.add(jj);
					}
				}
				else{
					boolean subsetjofi = true;
					//are the edges of jj a subset of ii?
					for(DefaultEdge e: edges2){
						String terminal = (graph.getEdgeSource(e).equals(vertices[jj])?graph.getEdgeTarget(e):graph.getEdgeSource(e));
						if(graph.getEdge(vertices[ii], terminal) == null && graph.getEdge(terminal, vertices[ii]) == null){
							subsetjofi = false;
							break;
						}
					}
					if(graph.getEdge(vertices[ii], vertices[jj]) != null || graph.getEdge(vertices[jj], vertices[ii]) != null){

					}
					else if(subsetjofi){
						nodepair1.add(ii);
						nodepair2.add(jj);
					}
				}
			}
		}


		System.out.println("node equalities or subsets found = "+nodepair1.size());


		System.out.println("starting constraint programming");

		int maxcolors = 127;

		for(int colors = maxcolors; colors >= lowerbound; colors--){
			Store store = new Store();  // define FD store 
			// define finite domain variables 
			IntVar[] v = new IntVar[size]; 
			for (int ii=0; ii<size; ii++) {
				v[ii] = new IntVar(store, "v"+ii, 0, Math.min(ii,colors-1)); 
			}
			/**
			 *  define constraints 
			 */
			//basic coloring constraints
			for(int ii = 0; ii < sources.length; ii++){
				store.impose(new XneqY(v[sources[ii]],v[terminals[ii]]));
			}


			// color symmetry constraints
			for(int ii = 1; ii < nodes; ii++){

				IntVar[] varsSoFar = new IntVar[ii];
				for(int jj = 0; jj < ii; jj++){
					varsSoFar[jj] = v[jj];
				}
				IntVar max = new IntVar(store,"sym"+ii,0,ii);
				Max m = new Max(varsSoFar,max);

				store.impose(m);

				//now we have the max domain value of 0..ii-1
				//how do we impose a constraint that the domain of ii is less than or equal to that +1
				store.impose(new XplusClteqZ(v[ii],-1,m.max)); // I think this does it?
			}


			// set clique alldifferent constraints
//			for(Set<String> clique: cliques){
//				if(clique.size()>2){
//					IntVar[] cliquevars = new IntVar[clique.size()];
//					int index = 0;
//					//System.out.print("clique: ");
//					for(String vertex: clique){
//						//System.out.print(vertex+" ");
//						cliquevars[index++]=v[Integer.parseInt(vertex)];
//					}
//					//System.out.println();
//					store.impose(new Alldifferent(cliquevars));
//				}
//			}


			//node pair constraints
			for(int ii = 0; ii < nodepair1.size(); ii++){
				store.impose(new XeqY(v[nodepair1.get(ii)],v[nodepair2.get(ii)]));
			}



			System.out.println("starting solver");
			// search for a solution and print results 
			Search<IntVar> search = new DepthFirstSearch<IntVar>(); 
			SelectChoicePoint<IntVar> select = 
					new InputOrderSelect<IntVar>(store, v, 
							new IndomainMin<IntVar>()); 
			boolean result = search.labeling(store, select); 

			if ( result ) {
				HashSet<Integer> uniqueColors = new HashSet<Integer>();
				for(int ii = 0; ii < v.length; ii++){
					if(uniqueColors.contains(v[ii])){
						//do nothing
					}
					else{
						uniqueColors.add(v[ii].value());
						colors = uniqueColors.size();
					}
				}
				System.out.print("Solution: "+(colors)+" colors = ");
				for(int ii = 0; ii < v.length; ii++){
					System.out.print(v[ii]+", ");
				}
				System.out.println();
				//            System.out.println("Solution: " + v[0]+", "+v[1] +", "+ 
				//                                              v[2] +", "+v[3]); 
				//break;
			}
			else 
				System.out.println(colors+" colors... No");
		}
	}


	public class Node{

		ArrayList<Edge> edges;
		int node_index;
		BitSet domain;

		public Node(int nodeindex){
			node_index = nodeindex;
			edges = new ArrayList<Edge>();
		}

		public void setColor(int color) {
			domain.clear();
			domain.set(color);

		}

		public void addEdge(Edge e){
			edges.add(e);
		}

		public void setDomainSize(int colors){
			domain = new BitSet(colors);
			for(int ii = 0; ii < domain.size(); ii++){
				domain.set(ii);
			}
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
