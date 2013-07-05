import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Solver {



	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		//System.out.println(args[0]);
		solve(args);
	}

	private static void solve(String[] args) throws IOException {

		String fileName = null;

		// get the temp file name
		for(String arg : args){
			if(arg.startsWith("-file=")){
				fileName = arg.substring(6);
			} 
		}
		if(fileName == null)
			return;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String header = br.readLine();
		String[] line = header.split(" ");
		int nodes = Integer.parseInt(line[0]);
		int edges = Integer.parseInt(line[1]);
		String str;
		int[] sources = new int[edges];
		int[] terminals = new int[edges];
		int index = 0;
		while((str=br.readLine())!= null){
			line = str.split(" ");
			sources[index] = Integer.parseInt(line[0]);
			terminals[index++] = Integer.parseInt(line[1]);
			//System.out.println(sources[index-1]+" "+terminals[index-1]);
		}
		//System.out.println("\nSolution:");
		//FirstTry solver = new FirstTry(nodes,edges,sources,terminals);
		new GreedyKempChain(nodes,edges,sources,terminals);
	}

}
