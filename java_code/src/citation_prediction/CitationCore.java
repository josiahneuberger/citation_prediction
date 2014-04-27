package citation_prediction;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.lang.Math;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;


/************************
* For additional information about this source code please visit:<br>
* 	http://josiahneuberger.github.io/citation_prediction/
*<br>
* Collaborative Research Project between University of Mary Washington and
*   the Dahlgren Naval Surface Warfare Center.
*<br>
* Dahlgren Naval Surface Warfare Center<br>
*       1. Dr. Jeff Solka <br>
*       2. Dr. Allen D. Parks <br>
*       3. Computer Scientist Kristen Ash<br>
*
*
* University of Mary Washington Faculty: <br>
*       1. Dr. Melody Denhere (Math) Supervisor Faculty <br>
*       2. Dr. Debra Hydorn (Math)<br>
*       3. Dr. Stephen Davies (Computer Science)<br>
* 
*       Student Researchers:<br>
*         1. William Etcho (Math)<br>
*         2. Josiah Neuberger (Computer Science)<br>
*
*
*
*<p>
* Brief Description:<br>
*   This project will explore the concept of predicting future citations based
*   on a citation history of a given paper. The project relies on the initial
*   research of Wang, Song, and Barabasi, which is outlined in their paper titled: 
*   "Quantifying Long-Term Scientific Impact" published in "Science 342, 127 (2013)".
*<br>
*   The prediction is based on three different qualities:
*     1. Preferred attachment
*     2. Age Decay
*     3. Fitness
*<p>
*   These three qualities are represented across three variables (The WSB Triple: Wang-Song-Barabási)
*<br>
*     1. (mu) - "indicates immediacy, governing the time for a paper to reach its citation peak (Science pg 128)"
*     2. (sigma) - "is longevity, capturing the decay rate (Science pg 128)"
*     3. (lambda) - relative fitness, which is related to '' the paper's fitness which "captures
*         the inherent differences between papers, accounting for the perceived novelty and 
*         importance of a discovery. (Science pg 128-129)"
*<p>
*   This Java program will solve the WSB triple by using the general math developed by Dr. Parks to implement 
*   the Newton-Raphson method of numeric analysis applied to the three non-linear equations found in the WSB
*   paper. The WSB Triple along with equation #3 on page 129 of the WSB paper will be used to predict future 
*   citations of a paper based on a segment of the paper's citation history.
*<p>
 * This class makes use of several packages of the Apache Commons Library (Apache License, V2.0):  http://commons.apache.org/
 *<br>
 *  Specifically:<br>
 *  	Math.Linear (RealMatrix, LUDecomposition, DecompositionSolver, MatrixUtils)
 *  	Math.Distribution (NormalDistributation: CDF and PDF functions)
 *  	CSV.CSVRecord
 *  	CSV.CSVParser
 *  	Note: the commons.csv package is part of the Sandbox package and therefore is considered in beta with no official releases.
 *  		this class uses the most recent build to extract data from a tabbed delimited csv file.
 *<br>  
 *  The graphs are all done using the jmathplot library (BSD 3-clause License), which can be found at:<br>
 *  	http://code.google.com/p/jmathplot/
 *  
 *  <br><br>
 *  License:<br>
 *  <br>
 *  Copyright 2014 Josiah Neuberger & William Etcho<br>
 *  Licensed under the Apache License, Version 2.0 (the "License");<br>
 *  you may not use this file except in compliance with the License.<br>
 *  You may obtain a copy of the License at<br>
 *<br>
 *  http://www.apache.org/licenses/LICENSE-2.0<br>
 *<br>
 * Unless required by applicable law or agreed to in writing, software<br>
 * distributed under the License is distributed on an "AS IS" BASIS,<br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.<br>
 * See the License for the specific language governing permissions and<br>
 * limitations under the License.<br>
 */

public class CitationCore {
	
	private NormalDistribution nd = new NormalDistribution();
	
	public static void main(String [] args) throws IOException {		
		
		CitationCore.CitationCoreTest cct = new CitationCore().new CitationCoreTest(new Scanner(System.in));
		cct.run_user_tests();
	}
	
	/**
	 * This is a basic test class that allows for the creation of various test, which can be
	 * added to the 'tests' variable structure for presentation to the user during execution.
	 * 
	 * 
	 *
	 */
	public class CitationCoreTest {
		
		public LinkedHashMap<Integer, TestCase> tests;
		private Scanner s;
		
		CitationCoreTest(Scanner s) {
			tests = new LinkedHashMap<Integer, TestCase>();
			s = new Scanner(System.in);
			this.s = s;
			
			int testIndex = 0;
			
			//Add test here
			
			//uncomment this code so you can run test on many versions of 'm' one after the other.
			/*tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,2.9));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,20));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,30));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,50));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,70));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,100));*/
			
			
			double m = 30;
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"",1,m));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"(all papers with NO pause) ::",1, m, true, false));
			tests.put(++testIndex, new TestCase_AutoSearchData(s,"(all papers with pause) ::",1, m, true, true));			
		}
		
		
		/**
		 * Print a menu for a user to selet tests to run.
		 */
		public void run_user_tests() {
			
			
			boolean quit = false;
			int test_number = 0;
			
			do {
				System.out.println("***************CitationCore Menu of Test Cases********************");
				
				System.out.println("0: Quit");
			
				for (Entry<Integer, TestCase> e: this.tests.entrySet()) {
				
			       System.out.println(e.getKey() + ": " + e.getValue().name + " Test ");
				}
				
				System.out.println("******************************************************************");
				System.out.println("Please choose a menu item: ");
				test_number = s.nextInt();
				
				if (test_number != 0) {
					
					if (tests.containsKey(test_number)) { this.tests.get(test_number).run_test(); }
					else { System.err.println("ERROR: The number you inputed does not match a menu item.");	}
				}
				
			} while (test_number != 0);
			
			s.close();
				
		}
		
		
		/**
		 * Override this class to create a new testCase.
		 * You must implement the function run_test.
		 *
		 * @author Josiah Neuberger
		 */
		public abstract class TestCase {
			public String name;
			public String type;
			
			private CitationCore cc;
			private double[][] data;
			
			static final String TYPE_USER_DRIVEN = "user driven";
			static final String TYPE_STAND_ALONE = "stand alone";
			
			TestCase(String name, String type) {
				this.name = name;
				this.type = type;
				
				this.cc = new CitationCore();
			}
		
			/**
			 * This function is where your test code goes.
			 * @return Should return true if test passed and false otherwise.
			 */
			public abstract boolean run_test();
		}
		
		/**
		 * This class implements a test case which will allow a user to select paper(s) from
		 * a file to run the NRC Search algorithm on. The test will attempt to find WSB solutions
		 * for 5 year, 10 year, and all years of citation data available. A graph will be 
		 * presented to the user and saved for later reference.
		 * 
		 * 
		 * @author Josiah Neuberger
		 *
		 */
		private class TestCase_AutoSearchData extends TestCase {

			Scanner s;
			
			double step;
			double m;
			boolean runAllPapers;
			boolean pauseBetweenPapers;
			
			TestCase_AutoSearchData(Scanner s, String prependToTitle, double step, double m) {
				super(prependToTitle + " NRC Search with step=" + step + ", m=" + m, TestCase.TYPE_USER_DRIVEN);
				
				this.runAllPapers = false;
				this.pauseBetweenPapers = true;
				this.step = step;
				this.m = m;
				this.s = s;
				
			}
			
			TestCase_AutoSearchData(Scanner s, String prependToTitle, double step, double m, boolean runAllPapers, boolean pauseBetweenPapers) {
				super(prependToTitle + " NRC Search with step=" + step + ", m=" + m, TestCase.TYPE_USER_DRIVEN);
				
				this.runAllPapers = runAllPapers;
				this.pauseBetweenPapers = pauseBetweenPapers;
				this.step = step;
				this.m = m;
				this.s = s;
			}
			
			public boolean run_test() {
		
				String solutionString = "";
				String dirname = "papers/";
				String nl = System.getProperty("line.separator");
				File fdir = new File(dirname);
				ArrayList<String> filenames = new ArrayList<String>(Arrays.asList(fdir.list()));
				
				//Get available files to parse for paper's citation history
				int i=0;
				for (String name : filenames) { 
					System.out.println(i + ": " + name);
					i++;
				}
				System.out.println("Please choose a file to parse for papers: ");
				int filenumber_input = s.nextInt();

				assert( (filenumber_input>=0) && (filenumber_input<filenames.size()) );
				
				List<CSVRecord> papersfromfile =  CitationCore.getCSVData(dirname+filenames.get(filenumber_input), CSVFormat.DEFAULT, false);
				
				List <CSVRecord> papers;
				int pn;
				
				//Either run all the papers in the selected file or
				//	let the user choose a paper.
				if (!runAllPapers) {
					System.out.println("Please choose a paper #: ");
					int papernumber_input = s.nextInt();
					
					papers = papersfromfile.subList(papernumber_input-1, papernumber_input);
					pn = papernumber_input-1;
				
				} else {
					papers = papersfromfile;
					pn = 0;
				}
				
				//process papers for WSB solutions and present graphs
				for (int p=0; p<papers.size(); p++) {
					
					pn++; //The actual number of the paper from the file
					
					//Extract citation history and reformat for NRM for this paper
					String paper = "Filename: " + dirname+ filenames.get(filenumber_input) + " (papers indexed as 1,2,3....)";
					double[][] data5 = CitationCore.fixData(papers.get(p), 5);
					double[][] data10 = CitationCore.fixData(papers.get(p), 10);
					double[][] dataAll = CitationCore.fixData(papers.get(p), 0);
					
					//Find WSB solutions
					ArrayList<LinkedHashMap<String, Double>> solutions5 = CitationCore.newtonRaphson_ConvergenceTest(data5, step, m);
					ArrayList<LinkedHashMap<String, Double>> solutions10 = CitationCore.newtonRaphson_ConvergenceTest(data10, step, m);
					ArrayList<LinkedHashMap<String, Double>> solutionsAllData = CitationCore.newtonRaphson_ConvergenceTest(dataAll, step, m);
					
					solutionString += "P#" + pn + "(train=5):: " + solutions5.toString() + nl;
					solutionString += "P#" + pn + "(train=10):: " + solutions10.toString() + nl;
					solutionString += "P#" + pn + "(train=All):: " + solutionsAllData.toString() + nl;
					
					System.out.println("************************P#" + pn + " Solutions**************************");
					System.out.print("Solutions (5 years of training): " + solutions5.toString() + "\nSolutions (10 years of training):" + solutions10.toString() + "\nSolutions (all years of training): " + solutionsAllData.toString() + "\n");
					
					JFrame f = new JFrame();
					Plot2DPanel plot = super.cc.graphWSB(dataAll, m, null, "m=" + m + ", " + paper,"p#" + pn + ": (train=All)--->"+solutionsAllData.toString(), solutionsAllData, false, f);
					super.cc.graphWSB(dataAll, m, plot, "m=" + m + ", " + paper, "p#" + pn + ": (train=10)--->"+solutions10.toString(), solutions10, false, f);
					super.cc.graphWSB(dataAll, m, plot, "m=" + m + ", " + paper, "p#" + pn + ": (train=5)--->"+solutions5.toString(), solutions5, true, f);
					
					
					//Get screenshot to save graph for later reference
					Rectangle screenRect = new Rectangle(0,0,1000,800);
					try {
						System.err.println("WARNING: TAKING SCREEN CAPTURE. DON'T TOUCH ANYTHING");
						Thread.sleep(4000);
						BufferedImage plot_screenshot = new Robot().createScreenCapture(screenRect);
						System.err.println("Thanks, we're done now.");
						
						
						File dir = new File("saved_plots/" + filenames.get(filenumber_input));
						if (!dir.exists()) dir.mkdir();
					
						String fn = filenames.get(filenumber_input);
						String fnid = fn.substring(0,fn.indexOf("_"));
						
						ImageIO.write(plot_screenshot, "jpg", new File(dir + "/" + fnid + "_" + pn + "_m" + m + ".jpg"));
						//plot.toGraphicFile(new File(dir +"/" + pn + ".png"));
						if (!pauseBetweenPapers) { f.dispose();	}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if (pauseBetweenPapers) {
						System.out.println("Please press enter to move onto the next paper.");
						s.nextLine();
					}
				}
				
				try { //save the graph
					File dir = new File("saved_plots/" + filenames.get(filenumber_input));
					if (!dir.exists()) dir.mkdir();
					
					FileWriter bw = new FileWriter(dir + "/solutions.txt");
					bw.write(solutionString);
					bw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return true; //User driven so this test always returns true.
			}
		}
	}
	
	/**
	 * This function will take a list of WSB solutions and graph them. You can add solutions to the graph by saving
	 * 		the return of the function the first time and passing it back in on the next call as the 'plot'.
	 * 
	 * 
	 * @param data_in_days The citation data distributed mostly even in days (call fixdata function if data is in years).
	 * @param m The average number of new references in each new paper for a journal.
	 * @param plot The plot you would like the WSB solution plotted to.
	 * @param graphTitle The title to display on the JPanel.
	 * @param lineLegend The title of this curve.
	 * @param wsbSolutions The list of WSB solutions to graph.
	 * @param showGraph Display the graph to the user (generally called once all curves have been added.)
	 * @param frame The frame to display the graph in.
	 * @return A Panel containing the graph.
	 */
	public Plot2DPanel graphWSB(double[][] data_in_days, double m, Plot2DPanel plot, String graphTitle, String lineLegend, ArrayList<LinkedHashMap<String, Double>> wsbSolutions, boolean showGraph, JFrame frame) {
		
		//source of library: http://code.google.com/p/jmathplot/
		
		int plotLength = data_in_days.length + (365*3);
		double[][] data_in_years = new double[plotLength][2];
		
		//Translate the data into years instead of days
		for (int i=0; i<data_in_days.length; i++) {
			data_in_years[i][0] = data_in_days[i][0]/365;
			data_in_years[i][1] = data_in_days[i][1];
		}
		
		for (int i=data_in_days.length; i<plotLength; i++) {
			data_in_years[i][0] = data_in_years[i-1][0] + .025;
			data_in_years[i][1] = data_in_years[data_in_days.length-1][1];
		}

		if (plot == null) { 
			plot = new Plot2DPanel(); 

			plot.addScatterPlot("Actual Citations", data_in_years); //Plot the data
		}
		
		//Extract the timevalue column
		RealMatrix mdata = MatrixUtils.createRealMatrix(data_in_years);
		double[] tvalues = mdata.getColumn(0);
		double[] cvalues = new double[plotLength];
		
		for(LinkedHashMap<String, Double> s : wsbSolutions) {
			//calculate their fitted y values
			for (int i=0; i<plotLength; i++) {
				cvalues[i] = m * (Math.exp(s.get("lambda")*pnorm((Math.log(365*tvalues[i])-s.get("mu"))/s.get("sigma")))-1);
			}
			//Calculate the Ultimate Impact
			double c_impact = m * (Math.exp(s.get("lambda"))-1);
			//plot the fit
			plot.addLinePlot("Ultimate Impact=" + c_impact + " :: " + lineLegend, tvalues, cvalues);
		}
		
		// put the PlotPanel in a JFrame, as a JPanel
		if (showGraph) {
			plot.setAxisLabel(0, "Time in Years");
			plot.setAxisLabel(1, "Cumulative Citations");
			
			//Uncomment these if you wish to have fixed axis.
			//plot.setFixedBounds(1, 0, 500);
			//plot.setFixedBounds(0, 0, 60);
			
			// Add the file Title
            BaseLabel filetitle = new BaseLabel(graphTitle, Color.GRAY, 0.5, 1.1);
            filetitle.setFont(new Font("Courier", Font.BOLD, 13));
            plot.addPlotable(filetitle);
			
			plot.addLegend("SOUTH");
			//JFrame frame = new JFrame(graphTitle); 
			frame.setTitle(graphTitle);
			frame.setContentPane(plot);
			frame.setBounds(0, 0, 1000, 800);
			frame.setVisible(true);
			frame.repaint();
		}
		return plot;
	}
	
	/**
	 * This function calculates a bunch of values needed for each iteration of the Newton-Raphson numerical method for solving for a WSB solution.
	 * 
	 * @param data The citation data in days.
	 * @param mu The current iteration's mu value.
	 * @param sigma The current iteration's sigma value.
	 * @param m The constant value, which is determined by the average number of references in each new paper for a journal.
	 * @param t The last time value in the citation history.
	 * @param n The total number of citations for this paper.
	 * @param iteration The iteration number.
	 * @return A list containing all the calculated values for the Netwon-Raphson method for this iteration.
	 */
	private LinkedHashMap<String, Double> getIterationData(double[][] data, double mu, double sigma, double m, double t, double n, int iteration) {
		
		//Set variables to zero for each iteration that this function is called.
		double xt = 0;
		double xi = 0;
		double ti = 0;
		
		double mhat = m/n;
	  
		double s_ln_ti = 0;
		double s_ln_ti_sqrd = 0;
		double s_xi = 0;
		double s_xi_sqrd = 0;
		double s_pnorm_xi = 0;
		double s_dnorm_xi = 0;
  		double s_xi_dnorm_xi = 0;
  		double s_xi_sqrd_dnorm_xi = 0;
  		double s_xi_cubed_dnorm_xi = 0;
	  
		//Find X of T
		xt = (Math.log(t) - mu)/sigma;
		  
		//Sum the various values needed.
		for (int i=0; i<n; i++) {
	    
		  ti = data[i][0];
		  xi = (Math.log(ti) - mu)/sigma;
		    
		  s_ln_ti += Math.log(ti);
		  s_ln_ti_sqrd += Math.pow(Math.log(ti),2);
		    
		  s_xi += xi;
		  s_xi_sqrd += Math.pow(xi,2);
		    
		  s_pnorm_xi += pnorm(xi);
		  
		  s_dnorm_xi += dnorm(xi);
		  s_xi_dnorm_xi += xi * dnorm(xi);
		  s_xi_sqrd_dnorm_xi += Math.pow(xi,2) * dnorm(xi);
		  s_xi_cubed_dnorm_xi += Math.pow(xi,3) * dnorm(xi);
    
		}
		  
		//Divide by the total number of citations to get the expected values.
		s_ln_ti /= n;
		s_ln_ti_sqrd /= n;
		 
		s_xi /= n;
		s_xi_sqrd /= n;
		 
		s_pnorm_xi /=n;
		  
		s_dnorm_xi /= n;
		s_xi_dnorm_xi /= n;
		s_xi_sqrd_dnorm_xi /= n;
		s_xi_cubed_dnorm_xi /= n;
  
		LinkedHashMap<String, Double> r_list = new LinkedHashMap<String, Double>();
		
		//Add them to our list.
		r_list.put("iteration", (double) iteration);
		r_list.put("t", t);
		r_list.put("xt", xt);
		r_list.put("n", n);
		r_list.put("m", m);
		r_list.put("mhat", mhat);
		r_list.put("mu", mu);
		r_list.put("sigma", sigma);
		
		r_list.put("s_ln_ti", s_ln_ti);
		r_list.put("s_ln_ti_sqrd", s_ln_ti_sqrd);
		
		r_list.put("s_xi", s_xi);
		r_list.put("s_xi_sqrd", s_xi_sqrd);
		
		r_list.put("s_pnorm_xi", s_pnorm_xi);
		
		r_list.put("s_dnorm_xi", s_dnorm_xi);
		r_list.put("s_xi_dnorm_xi", s_xi_dnorm_xi);
		r_list.put("s_xi_sqrd_dnorm_xi", s_xi_sqrd_dnorm_xi);
		r_list.put("s_xi_cubed_dnorm_xi", s_xi_cubed_dnorm_xi);
		
		return r_list;
		  
	}

	/**
	 * This function calculates a bunch of values needed for each iteration of the Newton-Raphson numerical method for solving for a WSB solution.
	 * <br>
	 * Uses some default values:
	 * <br>
	 * t - extracted from the citation history in 'data'<br>
	 * n - extracted from the citation history in 'data'<br>
	 * 
	 * @param data The citation data in days.
	 * @param mu The current iteration's mu value.
	 * @param sigma The current iteration's sigma value.
	 * @param m A constant value, which is determined by the average number of references in each new paper for a journal.
	 * @return A list containing all the calculated values for the Netwon-Raphson method for this iteration.
	 */
	private LinkedHashMap<String, Double> getIterationData(double[][] data, double mu, double sigma, double m) {
		return getIterationData(data, mu, sigma, m, data[data.length-1][0], data[data.length-1][1], 0);
	}

	/**
	 * This function calculates the essential formulas for the Newton-Raphson function. The formulas used here
	 * were based on the math of Dr. Allen Parks.
	 * 
	 * @param l A list structure that will hold the calculated values.
	 * @return The list updated with the calculated values of the essential formulas.
	 */
	private LinkedHashMap<String, Double> getPartialsData(LinkedHashMap<String, Double> l) {
		
		double fn = ( ((1+l.get("mhat"))*pnorm(l.get("xt")) - l.get("s_pnorm_xi"))*l.get("s_xi") ) - 
			    l.get("s_dnorm_xi") + (1+l.get("mhat"))*dnorm(l.get("xt"));
		l.put("fn", fn);
		
		double gn = ( ((1+l.get("mhat"))*pnorm(l.get("xt")) - l.get("s_pnorm_xi"))*(l.get("s_xi_sqrd")-1) ) - 
			    l.get("s_xi_dnorm_xi") + ( (1+l.get("mhat"))*l.get("xt")*dnorm(l.get("xt")) );
		l.put("gn", gn);
		
		double df_dmu =  (    ( (1+l.get("mhat"))*((l.get("xt")-l.get("s_xi"))*dnorm(l.get("xt"))-pnorm(l.get("xt"))) ) + l.get("s_xi")*l.get("s_dnorm_xi")
	              - l.get("s_xi_dnorm_xi") + l.get("s_pnorm_xi")    ) /l.get("sigma");
		l.put("df_dmu", df_dmu);
		
		double df_dsigma = (   ( (1+l.get("mhat"))*((l.get("xt")-l.get("s_xi"))*l.get("xt")*dnorm(l.get("xt"))-l.get("s_xi")*pnorm(l.get("xt")))  ) 
                + l.get("s_xi")*(l.get("s_xi_dnorm_xi")+l.get("s_pnorm_xi")) - l.get("s_xi_sqrd_dnorm_xi")    ) /l.get("sigma");
		l.put("df_dsigma", df_dsigma);
		
		double dg_dmu = (   ( (1+l.get("mhat"))*(2*l.get("s_xi")*pnorm(l.get("xt")) + (l.get("s_xi_sqrd")-Math.pow(l.get("xt"),2))*dnorm(l.get("xt"))) ) 
	              - ( 2*l.get("s_xi")*l.get("s_pnorm_xi") + l.get("s_xi_sqrd")*l.get("s_dnorm_xi") - l.get("s_xi_sqrd_dnorm_xi") )    ) /(-l.get("sigma"));
		l.put("dg_dmu", dg_dmu);
		
		double dg_dsigma = (    ( (1+l.get("mhat"))*((Math.pow(l.get("xt"),3))*dnorm(l.get("xt")) - l.get("s_xi_sqrd")*l.get("xt")*dnorm(l.get("xt")) - 2*l.get("s_xi_sqrd")*pnorm(l.get("xt"))) )
                + 2*l.get("s_xi_sqrd")*l.get("s_pnorm_xi") + l.get("s_xi_sqrd")*l.get("s_xi_dnorm_xi") - l.get("s_xi_cubed_dnorm_xi")  ) /l.get("sigma");
		l.put("dg_dsigma", dg_dsigma);
		
		return l;
	}
	
	/**
	 * This function will print a list containing the calculated values for the Newton-Raphson method in
	 * a formated way that is easy to read.
	 * 
	 * @param l The list containing the Newton-Raphon calculated values.
	 */
	public void printList(LinkedHashMap<String, Double> l) {
		
		for (Entry<String, Double> e: l.entrySet()) {
				
	       System.out.println(e.getKey() + "=" + e.getValue());
		}
	}
	
	
	/**
	 * This function implements the algorithm designed by Josiah Neuberger and William Etcho used to solve for a WSB solution.
	 * The general math for the Newton-Raphson method was provided by Dr. Allen Parks and can be found in the function 'getPartialsData'.
	 * <br><br>
	 * This algorithm was created under the direction of Supervising University of Mary Washington faculty, Dr. Melody Denhere along with 
	 * consultation from Dr. Jeff Solka and Computer Scientists Kristin Ash with the Dahlgren Naval Surface Warfare Center.
	 * <br><br>
	 * If your interested in more information about the research behind this algorithm please visit:<br>
	 * http://josiahneuberger.github.io/citation_prediction/
	 * <br><br>
	 * 
	 * @param data The citation data in days.
	 * @param mu The initial mu guess to use in the Newton-Raphson method.
	 * @param sigma The initial sigma guess to use in the Newton-Raphson method.
	 * @param m The constant value, which is determined by the average number of references in each new paper for a journal.
	 * @param t The last time value in the paper's citation history.
	 * @param n The total number of citations for this paper.
	 * @param l A list structure to store values for each iteration (should be null to start).
	 * @param iteration The iteration (should be zero to start)
	 * @param max_iteration The maximum number of iterations to try before stopping.
	 * @param tolerance The tolerance level, which determines that a solution has been converged on.
	 * @return A list containing the WSB solution of (lambda, mu, sigma, iterations).
	 */
	private LinkedHashMap <String, Double> newtonRaphson(double[][] data, double mu, double sigma, double m, double t, double n, LinkedHashMap<String, Double> l, int iteration, int max_iteration, double tolerance) {
		
		double lambda;
		LinkedHashMap<String, Double> r = new LinkedHashMap<String, Double>();
		
		
		if (iteration > max_iteration) {
			System.out.println("Does not converge.");
			
			r.put("lambda", null);
			r.put("mu", null);
			r.put("sigma", null);
			r.put("iterations", null);
			
			return r;
		} else if (tolerance < 0.00000001) {
			System.out.println("Stopped due to tolerance.");
			
			r.put("lambda", getLambda(data, mu, sigma, m, t, n));
			r.put("mu", mu);
			r.put("sigma", sigma);
			r.put("iterations", (double) iteration);
			
			return r;
		} 
		else {
			
			l = getPartialsData(getIterationData(data, mu, sigma, m));
			
			double [] array_xn = { mu, sigma };
			double [] array_yn = { l.get("fn"), l.get("gn") };
			
			RealMatrix xn = MatrixUtils.createColumnRealMatrix(array_xn);
			RealMatrix yn = MatrixUtils.createColumnRealMatrix(array_yn);
			
			
			//http://commons.apache.org/proper/commons-math/userguide/linear.html
			
			double [][] array_jacobian = { {l.get("df_dmu"), l.get("df_dsigma")}, {l.get("dg_dmu"), l.get("dg_dsigma")} };
			RealMatrix jacobian = MatrixUtils.createRealMatrix(array_jacobian);
			LUDecomposition lud = new LUDecomposition(jacobian);
			DecompositionSolver decS = lud.getSolver();
			
			l.put("iteration", (double) (iteration + 1));
			//DEBUG: printList(l);
			
			if (!decS.isNonSingular()) {
				l.put("iteration", (double) (max_iteration+1));
				System.err.println("ERROR: Jacobian matrix was singular.");
			} else {
				
				RealMatrix solution = xn.subtract(decS.getInverse().multiply(yn));
				
				RealMatrix xndiff = solution.subtract(xn);
				tolerance = Math.sqrt(Math.pow(xndiff.getEntry(0, 0),2) + Math.pow(xndiff.getEntry(1,0),2));
			
				//update values
				l.put("mu", solution.getEntry(0, 0));
				l.put("sigma", solution.getEntry(1,0));
				
				//DEBUG: System.out.printf("\"%-20s=%25f\"\n", "NEW MU", l.get("mu"));
				//DEBUG: System.out.printf("\"%-20s=%25f\"\n",  "NEW SIGMA", l.get("sigma"));
			}
			//DEBUG: System.out.println("****************************************");
			
			return newtonRaphson(data, l.get("mu"), l.get("sigma"), m, l, (l.get("iteration").intValue()), tolerance);
		}
	}
	
	private LinkedHashMap <String, Double> newtonRaphson(double[][] data, double mu, double sigma, double m, LinkedHashMap<String, Double> l, int iteration, double tolerance) {
		return newtonRaphson(data, mu, sigma, m, data[data.length-1][0], data[data.length-1][1], l, iteration, 31, tolerance);
	}
	
	/**
	 * This function implements the algorithm designed by Josiah Neuberger and William Etcho used to solve for a WSB solution.
	 * The general math for the Newton-Raphson method was provided by Dr. Allen Parks and can be found in the function 'getPartialsData'.
	 * <br><br>
	 * This algorithm was created under the direction of Supervising University of Mary Washington faculty, Dr. Melody Denhere along with 
	 * consultation from Dr. Jeff Solka and Computer Scientists Kristin Ash with the Dahlgren Naval Surface Warfare Center.
	 * <br><br>
	 * If your interested in more information about the research behind this algorithm please visit:<br>
	 * http://josiahneuberger.github.io/citation_prediction/
	 * <br><br>
	 * This function uses the following default values:<br>
	 * Max Iterations = 31<br>
	 * 
	 * @param data The citation data in days.
	 * @param mu The initial mu guess to use in the Newton-Raphson method.
	 * @param sigma The initial sigma guess to use in the Newton-Raphson method.
	 * @param m The constant value, which is determined by the average number of references in each new paper for a journal.
	 * @return A list containing the WSB solution of (lambda, mu, sigma, iterations).
	 */
	public LinkedHashMap <String, Double> newtonRaphson(double[][] data, double mu, double sigma, double m) {
		return newtonRaphson(data, mu, sigma, m, data[data.length-1][0], data[data.length-1][1], null, 0, 31, .1);
	}
	

	/**
	 * This function runs the Newton-Raphson function on an interval from .1 to 10 returning a list of
	 * all the unique solutions.
	 * 
	 * @param data The citation data in days.
	 * @param step The step you would like to use to step through the interval of .1 to 10.
	 * @param m The average number of new references contained in each paper for a journal.
	 * @return A list containing the WSB solutions.
	 */
	public static ArrayList<LinkedHashMap<String, Double>> newtonRaphson_ConvergenceTest(double[][] data, double step, double m) {
		return newtonRaphson_ConvergenceTest(data, .1, 10, 10, step, m, false);
	}
	
	/**
	 * This function runs the Newton-Raphson function on an interval from .1 to 10 returning a list of
	 * all the unique solutions.
	 * 
	 * @param data The citation data in days.
	 * @param step The step you would like to use to step through the interval of .1 to 10.
	 * @param m The average number of new references contained in each paper for a journal.
	 * @return A list of list containing the WSB solutions.
	 */
	private static ArrayList<LinkedHashMap<String, Double>> newtonRaphson_ConvergenceTest(double[][] data, double start, double mu_guess, double sigma_guess, double step, double m, boolean wasAlreadyRun) {
		
		CitationCore cc = new CitationCore();
		
		String [] matrix_headers = { "mu0", "sigma0", "lambda", "mu", "sigma", "iteration" };
		ArrayList<ArrayList<Double>> matrix = new ArrayList<ArrayList<Double>>(100);
		ArrayList<LinkedHashMap<String, Double>> solutions = new ArrayList<LinkedHashMap<String, Double>>(100);
		ArrayList<Double> lambdas = new ArrayList<Double>();
		
		for (double mu0=start; mu0<(mu_guess+2); mu0+=step) {
			for (double sigma0=start; sigma0<(sigma_guess+2); sigma0+=step) {
				LinkedHashMap <String, Double> answer = cc.newtonRaphson(data,  mu0, sigma0, m);
				
				if (answer.get("lambda") != null) {
					ArrayList<Double> row = new ArrayList<Double>();
					
					row.add(mu0);
					row.add(sigma0);
					row.add(answer.get("lambda"));
					row.add(answer.get("mu"));
					row.add(answer.get("sigma"));					
					row.add(answer.get("iterations"));
					
					matrix.add(row);
					
					boolean isUnique = true;
					for (double l : lambdas) {
						if ((answer.get("lambda") < 0) || Math.abs(l - answer.get("lambda")) < 1e-2) {
							isUnique = false;
							break;
						}
					}
					if(isUnique) { 
						LinkedHashMap<String, Double> s = new LinkedHashMap<String, Double>();
						s.put("lambda", answer.get("lambda"));
						s.put("mu", answer.get("mu"));
						s.put("sigma", answer.get("sigma"));
						
						solutions.add(s); 
					}
					
					lambdas.add(answer.get("lambda"));
				}
			}
		}
		
		printMatrix(matrix, matrix_headers);
		System.out.println("Unique Solutions:");
		System.out.println(solutions.toString());
		
		if (!wasAlreadyRun && solutions.isEmpty()) return newtonRaphson_ConvergenceTest(data, start, mu_guess, sigma_guess, .1, m, true); 
		else return solutions;
	}
	
	/**
	 * Prints the citation history stored in 'l' in a formated way.
	 * 
	 * @param l Citation History.
	 * @param header Headers for columns.
	 */
	public static void printMatrix(ArrayList<ArrayList<Double>> l, String [] header) {
		
		int count = 0;
		
		System.out.print(count + ": ");
		for (Object h: header) {
			System.out.printf("%-20s", h);
		}
		System.out.println("");
		

		for (ArrayList<Double> row: l) {
			count++;
			
			System.out.print(count + ": ");
			for (double d: row) {
				System.out.printf("%-20f", d);
			}
			System.out.println("");
			
		}

	}
	
	/**
	 * Calculate lambda based on a solution found in the Newton-Raphson method for mu and sigma.
	 * 
	 * @param data The citation data in days.
	 * @param mu The solution for mu.
	 * @param sigma The solution for sigma.
	 * @param m The average number of references for new papers for a journal.
	 * @param t The last timestamp for the citation data.
	 * @param n The total number of citations from the citation history.
	 * @return The lambda value based on mu and sigma.
	 */
	public double getLambda(double[][] data, double mu, double sigma, double m, double t, double n) {
		
		double xt = (Math.log(t) - mu)/sigma;
		double mhat = m/n;
		
		
		double s_pnorm_xi = 0;
		
		for (int i=0; i<n; i++) {
			double ti = data[i][0];
			double xi = (Math.log(ti) - mu)/sigma;
			
			s_pnorm_xi += pnorm(xi);
		}
		s_pnorm_xi /= n;
		
		return 1/(   ((1+mhat)*pnorm(xt)) - (s_pnorm_xi)     );
	}
	
	
	private double pnorm(double x) {
		return nd.cumulativeProbability(x);
	}
	
	private double dnorm(double x) {
		return nd.density(x);
	}
	
	/**
	 * Get CSV citation history from a file.
	 * 
	 * @param filename The filename and path containing the citation data.
	 * @param format The format of the file.
	 * @param hasHeader Does the file have a line with headings?
	 * @return A record containing the csv information.
	 */
	private static List<CSVRecord> getCSVData(String filename, CSVFormat format, boolean hasHeader) {
		
		boolean error = true;
		List<CSVRecord> list_ourdata= null;
		
		 try {
			FileReader ourdata = new FileReader(filename);
			
			CSVParser data_parser = new CSVParser(ourdata, format);
	
			list_ourdata = data_parser.getRecords();
			
			if (hasHeader) { list_ourdata.remove(0); } //remove header file.
			
			Iterator<CSVRecord> list_iterator = list_ourdata.iterator();
			for (int rowIndex=0; rowIndex < list_ourdata.size(); rowIndex++) {
				CSVRecord record = list_iterator.next();
				
				System.out.println("#" + (rowIndex+1) + " " + record.toString());
				
			}
			
			data_parser.close();
			ourdata.close();
			
			error = false;
			
		 } catch(java.io.FileNotFoundException e) {
			 System.err.println("ERROR: There was an error opening, reading, or parsing the input file.");
			 System.err.println("ERROR:" + filename);
			 error = true;
		 } catch (java.io.IOException e) {
			 System.err.println("ERROR: Could not close the parser or the input file.");
			 error =  true;
		 } 
		 
		 if (error || list_ourdata == null) {
			 System.exit(1);
			 return null;
		 } else {
			 return list_ourdata;
		 }
	}

	/**
	 * Fix the citation data, which is in years by translating the timestamps and citations to be in days.
	 * 
	 * @param record The citation history in years.
	 * @param limitToRows Limit the rows being processed.
	 * @return The citation history in days.
	 */
	private static double[][] fixData(CSVRecord record, int limitToRows) {
		
		double[][] r = null;
		int citationCount = 0;
		int numberOfRowsToProcess = 0;
		
		Iterator<String> record_iterator = record.iterator();
		record_iterator.next(); //move pass paper id
		record_iterator.next(); //move pass paper publish year
		
		if (limitToRows!=0) { numberOfRowsToProcess = limitToRows; r = new double[numberOfRowsToProcess+1][2]; }
		else { numberOfRowsToProcess = record.size()-2; r = new double[record.size()-2][2]; }
		
		for (int rowIndex=0; record_iterator.hasNext() && rowIndex<numberOfRowsToProcess; rowIndex++) {
			
			String citations_forthis_year = record_iterator.next();
			
			r[rowIndex][0] = Double.valueOf(rowIndex); //timestamp
			r[rowIndex][1] = Double.valueOf(citations_forthis_year); //citation
			
			citationCount += r[rowIndex][1];
		}
		
		return fixData(r, citationCount);
		
	}

	/**
	 * Helper function to fix the citation data, which is in years by translating the timestamps and citations to be in days.
	 * 
	 * @param record The citation history in years.
	 * @param limitToRows Limit the rows being processed.
	 * @return The citation history in days.
	 */
	private static double[][] fixData(double [][] data, int citationCount) {
		
		double r[][] = new double[citationCount][2];
		int citations = 0;
		double timestamp = 0;
		
		for(int rowIndex=0; rowIndex<data.length; rowIndex++) { 
			for(int citationIndex=0; citationIndex<data[rowIndex][1]; citationIndex++) {
				citations += 1;
				timestamp = data[rowIndex][0] + (citationIndex+1)/data[rowIndex][1];
				
				//We use the citations to index in because this loop is creating a timestamp for each citation entry.
				r[citations-1][0] = 365 * timestamp; //convert to days
				r[citations-1][1] = citations;
			}
		}
		return r;
	}
	
	/**
	 * Convert citation history to a String object.
	 * 
	 * @param data The citation history.
	 * @return A string of the citation history.
	 */
	public static String toString(double [][] data) {
		String r = "[";
		
		for(int rowIndex=0; rowIndex<(data.length-1); rowIndex++) {
			r += "(" + (int) (Math.round(data[rowIndex][0])) + "," + data[rowIndex][1] + ")\n";
		}
		
		//take care of last entry without adding newline
		r += "(" + (int) (Math.round(data[data.length-1][0])) + "," + data[data.length-1][1] + ")]";
		
		return r;
	}
	
	
} 
