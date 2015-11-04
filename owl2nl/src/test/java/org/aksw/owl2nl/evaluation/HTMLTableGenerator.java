/**
 * 
 */
package org.aksw.owl2nl.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @author Lorenz Buehmann
 *
 */
public class HTMLTableGenerator {
	
	String style = 
			"<head>\n" + 
			"<style>\n" + 
			"table.sortable thead {\n" + 
			"    background-color:#eee;\n" + 
			"    color:#666666;\n" + 
			"    font-weight: bold;\n" + 
			"    cursor: pointer;\n" + 
			"}\n" + 
			"table.sortable tbody tr:nth-child(2n) td {\n" + 
			"    color: #000000;\n" + 
			"    background-color: #EAF2D3;\n" + 
			"}\n" + 
			"table.sortable tbody tr:nth-child(2n+1) td {\n" + 
			"  background: #ccfffff;\n" + 
			"}" + 
			"#benchmark {\n" + 
			"    font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" + 
			"    width: 100%;\n" + 
			"    border-collapse: collapse;\n" + 
			"}\n" + 
			"\n" + 
			"#benchmark td, #benchmark th {\n" + 
			"    font-size: 1em;\n" + 
			"    border: 1px solid #98bf21;\n" + 
			"    padding: 3px 7px 2px 7px;\n" + 
			"}\n" + 
			"\n" + 
			"#benchmark th {\n" + 
			"    font-size: 1.1em;\n" + 
			"    text-align: left;\n" + 
			"    padding-top: 5px;\n" + 
			"    padding-bottom: 4px;\n" + 
			"    background-color: #A7C942;\n" + 
			"    color: #ffffff;\n" + 
			"}\n"
			+ "#benchmark td.number {\n" + 
			"  text-align: right;\n" + 
			"}\n" + 
			"</style>\n"
			+ "<script type='text/javascript' src='http://www.kryogenix.org/code/browser/sorttable/sorttable.js'></script>" + 
			"</head>\n" + 
			"<body>";
	
	String style2 = 
			"<head>\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/twbs/bootstrap/master/dist/css/bootstrap.min.css\">\n" + 
			"<link rel=\"stylesheet\" href=\"https://rawgit.com/wenzhixin/bootstrap-table/1.8.0/dist/bootstrap-table.css\">\n" +
			"<style type=\"text/css\">\n" + 
			"   pre {\n" + 
			"	border: 0; \n" + 
			"	background-color: transparent\n"
			+ "font-family: monospace;" + 
			"	}\n"
			+ "table {\n" + 
			"    border-collapse: separate;\n" + 
			"    border-spacing: 0 5px;\n" + 
			"}\n" + 
			"\n" + 
			"thead th {\n" + 
			"    background-color: #006DCC;\n" + 
			"    color: white;\n" + 
			"}\n" + 
			"\n" + 
			"tbody td {\n" + 
			"    background-color: #EEEEEE;\n" + 
			"}\n" + 
			"\n" + 
			"tr td:first-child,\n" + 
			"tr th:first-child {\n" + 
			"    border-top-left-radius: 6px;\n" + 
			"    border-bottom-left-radius: 6px;\n" + 
			"}\n" + 
			"\n" + 
			"tr td:last-child,\n" + 
			"tr th:last-child {\n" + 
			"    border-top-right-radius: 6px;\n" + 
			"    border-bottom-right-radius: 6px;\n" + 
			"}\n" + 
			".fixed-table-container tbody td {\n" + 
			"    border: none;\n" + 
			"}\n" + 
			".fixed-table-container thead th {\n" + 
			"    border: none;\n" + 
			"}\n" + 
			"\n" + 
			".bootstrap-table .table {\n" + 
			"	border-collapse: inherit !important;\n" + 
			"}" + 
			"</style>\n" +
			"<script src=\"http://code.jquery.com/jquery-1.11.3.min.js\"></script>\n" + 
			"<script src=\"https://rawgit.com/twbs/bootstrap/master/dist/js/bootstrap.min.js\"></script>\n" + 
			"<script src=\"https://rawgit.com/wenzhixin/bootstrap-table/1.8.0/dist/bootstrap-table-all.min.js\"></script>\n" +
			"</head>\n";  
			
	
	private QueryExecutionFactory qef;

	public HTMLTableGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	private List<Query> loadQueries(File queriesFile) throws IOException {
		List<Query> queries = new ArrayList<Query>();
		
		for (String queryString : Files.readLines(queriesFile, Charsets.UTF_8)) {
			Query q = QueryFactory.create(queryString);
			queries.add(q);
		}
		return queries;
	}
	
	public void generateBenchmarkDescription(File benchmarkQueriesFile, File htmlOutputFile) throws Exception{
		List<Query> queries = loadQueries(benchmarkQueriesFile);
		
		Var var = Var.alloc("s");
		String html = "<html>\n";
		html += style2;
		html += "<body>\n";
		html += "<table data-toggle=\"table\" data-striped='true'>\n";
		// table header
		html += "<thead><tr>"
				+ "<th data-sortable=\"true\" data-valign='middle'>ID</th>"
				+ "<th data-sortable=\"true\" data-valign='middle'>Query</th>"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>Depth</th>"
				+ "<th data-align=\"right\" data-sortable=\"true\" data-valign='middle'>#Instances</th>"
				+ "</tr></thead>\n";
		
		html += "<tbody>\n";
		int id = 1;
		for (Query query : queries) {
			html += "<tr>\n";
			
			// 1. column: ID
			html += "<td>" + id++ + "</td>\n";
			
			// 2. column: SPARQL query
			html += "<td><pre>" + query.toString().replace("<", "&lt;").replace(">", "&gt;") + "</pre></td>\n";
			
			// 3. column: depth
			int depth = org.dllearner.utilities.QueryUtils.getSubjectObjectJoinDepth(query, var) + 1;
			html += "<td class='number'>" + depth + "</td>\n";
			
			// 4. column: #instances
			int nrOfInstances = 0;
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()) {
				rs.next();
				nrOfInstances++;
			}
			qe.close();
			html += "<td class='number'>" + nrOfInstances + "</td>\n";
			
			html += "</tr>\n";
		}
		html += "</tbody>\n";
		html += "</table>\n";
		html += "</body>\n";
		html += "</html>\n";
		
		try {
			Files.write(html, htmlOutputFile, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception{
		if(args.length < 4) {
			System.out.println("Usage: HTMLTableGenerator <source> <target> <endpointURL> <defaultGraphURI>");
			System.exit(0);
		}
		File source = new File(args[0]);
		File target = new File(args[1]);
		String endpointURL = args[2];
		String defaultGraph = args[3];
		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpointURL, defaultGraph);
		qef = new QueryExecutionFactoryPaginated(qef);
		HTMLTableGenerator generator = new HTMLTableGenerator(qef);
		generator.generateBenchmarkDescription(source, target);
	}

}
