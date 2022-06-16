import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Parser {	
	final public static String TAG_STRING = "#$%";
	
	static public String returnStyleDetailsFromName(List<Style> styleList, String name) {	
		//get the styling details based on the name from the html
		for(Style style : styleList) {
			if(style.getName().equals(name)) {
				return style.getDetails();
			}
		}
		//return nothing if there isn't a match
		return "";
	}
	
	static public String returnAllStyleDetails(List<Style> styleList, String section) {
		//run returnStyleDetailsFromName multiple times to get all styling details
		String details = "";
		for(Style style : styleList) {
			if(section.contains(style.getName())) {
				details += style.getDetails();
			}
		}
		return details;
	}
	
	static public String eraseTagFromHeaders(String section) {
		//get rid of the markers for which is headers and which are not
		int start = section.indexOf(TAG_STRING)+(TAG_STRING+"</span>").length();
		section = section.substring(start);
		
		//also get rid of anything between < and /> tags
		start = section.indexOf(">");
		int end = section.indexOf("</");
		if(end > start) {
			section = section.substring(start+1, section.indexOf("</"));	
		} else {
			section = section.substring(start);
		}
		
		return section;
	}
	
	static public String checkAndAddBold(String line, String style) {
		//add bold tags if it was in styling
		if(style.contains("font-weight:bold")) {
			return "<b>"+line+"</b>";
		}
		return line;
	}
	
	static public String checkAndAddItalics(String line, String style) {
		//add italics tags if it was in styling
		if(style.contains("font-style:italic")) {
			return "<i>"+line+"</i>";
		}
		return line;
	}
	
	static public String eraseParagraphTags(String section, List<Style> styleList) {
		//minimize html documents
		String finalSection = "";
		String[] sectionArray = section.split("<p ");
		for(String part : sectionArray){
			String[] spanArray = part.split("<span ");
			
			for(String spanPart : spanArray) {
				String styleDesc = returnAllStyleDetails(styleList, spanPart);
				String addPart = spanPart;
				if(styleDesc.contains(""))
				if(spanPart.length() > 5) {
					String checkString = spanPart.substring(0, 5);
					if(checkString.equals("class")) {
						int end = spanPart.indexOf('>');
						finalSection += checkAndAddItalics(
								checkAndAddBold(
										spanPart.substring(end+1), 
										styleDesc
										), 
								styleDesc);
					} else {
						finalSection += checkAndAddItalics( 
								checkAndAddBold(spanPart, styleDesc) 
								, styleDesc);
					}	
				} else {
					finalSection += checkAndAddItalics(
							checkAndAddBold(spanPart, styleDesc), 
							styleDesc);
				}				
			}
			finalSection += "<br />";
		}
		finalSection = finalSection.replaceAll("<span>","").replaceAll("</span>","")
				.replaceAll("<div>","").replaceAll("</div>","")
				.replaceAll("<p>","").replaceAll("</p>","&nbsp;");
		return finalSection;
	}
	
	public static List<File> getAllFilesFromFolder(List<File> fileList, File folder) {
		//get all files form a folder so this can run more than one file at a time
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.isDirectory()) {
				fileList = getAllFilesFromFolder(fileList, fileEntry);
			} else {
				if(fileEntry.getName()
						.substring(fileEntry.getName().length() - 5).equals(".html")) {
					fileList.add(fileEntry);
				}
			}
		}
		return fileList;
	}
	
	public static String eraseClasses(String s) {
		boolean classExists = true;
		while(classExists) {
			int start = s.indexOf("class");
			if(start == -1) {
				classExists = false;
				return s;
			}
			int end = s.indexOf(">");
			s = s.substring(end+ 1).replace("</span>", "").replace("</p>", "");
		}
		return s;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//get files from folder
		
		//get the file paths from the config path (in src folder)
		//format is 
		//CSV=<absolute path>
		//HTML=<absolute path>
		File pathFile = new File("src\\paths.config");
		String inputPath = "";
		String outputPath = "";
		
		//for keeping track in console
		System.out.println("Starting conversion...");
		
		try {
			//get the config file path
			FileReader pathReader = new FileReader(pathFile.getAbsolutePath());
			BufferedReader pathBuffer = new BufferedReader(pathReader);
			String line = "";
			String reportName = "";
			
			try {
				line = pathBuffer.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			while(line != null) {
				//get the path for the html documents and where we want to put
				//the csv documents
				String[] lineArray = line.split("=");
				if(lineArray[0].equals("HTML")) {
					inputPath = lineArray[1];
				} else if(lineArray[0].equals("CSV")) {
					outputPath = lineArray[1];
				}
				line = pathBuffer.readLine();
			}
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<File> fileList = new ArrayList<File>();
		File folder = new File(inputPath);
		fileList = getAllFilesFromFolder(fileList, folder);
		
		//run for each file
		for(File f : fileList) {
			//get the path and start
			String filename = f.getAbsolutePath();
			System.out.println("Starting "+filename);
			FileReader file;
			try {
				file = new FileReader(filename);
				
				BufferedReader reader = new BufferedReader(file);
				String line = "";
				boolean start = false;
				boolean inStyle = false;
				
				//set up variables that need to be printed out
				//order in csv is header, table, paragraph, domain,
				//tableName, sendFileName, and report
				String header = "";
				String table = "";
				String paragraph = "";
				String report = "";

				String domain = "";
				String tableName = "";
				
				String sendFileName = f.getName().replace(".html", "");
				
				//use these for find the domain and tableName variables
				boolean foundEWCP = false;
				boolean findTables = false;
				int EWCPtrCount = 0;
				int EWCPtdCount = 0;
				
				//list for what will end up in db
				//and for styling in case we need it
				List<DBItem> dbList = new ArrayList<DBItem>();
				List<Style> styleList = new ArrayList<Style>();
				
	
				try {
					//get the line
					line = reader.readLine();
					while(line != null) {
						
						//check if there is styling that needs to be handled
						if(line.contains("style")) {
							inStyle = true;
						}
						
						//get all the documents in the styling and
						//add to style list
						if(inStyle) {
							String [] lineSplit = line.split("}");
							for(String section : lineSplit) {
								if(section.contains("{")) {
									String[] sp = section.trim().split("\\{");
									
									if(sp.length > 1) {
										styleList.add(new Style(sp[0].replace(".", ""), sp[1]));	
									}
								}
							}
						}
						
						//stop checking for styling if it is marked in html
						if(line.contains("/style")) {
							inStyle = false;
						} 
						
						//get all style details if this is a table name of a header
						//(need it to check for relevant details)
						String stylingDetails = "";
						if(!start && styleList.size() > 0 && line.contains(TAG_STRING)) {
							stylingDetails = returnAllStyleDetails(styleList, line);
						}
						
						//if we find the first table header then we begin parsing
						if(!start && ( stylingDetails.contains("text-align:center")) && line.contains(TAG_STRING)) {
							start = true;
						}
						
						//only run when the first table header is found
						if(start) {
							//split up line according to paragraphs
							String [] lineSplit = line.trim().split("</p>");
							
							//handle each paragraph seperately
							for(String section : lineSplit) {
								section = section.replaceAll("\"", "\\\"")+"</p>";
								//check for EAW table name (which marks that we should start searching for
								//the domain and second table name
								if(section.contains("EAW table name")) {
									foundEWCP = true;
								}
								
								//if still in the right header then we search
								//NOTE: Depends on trs being on different lines at the moment
								if(foundEWCP && header.contains("EWCP Measure(s) subcategory (add additional rows for each EWCP measure included)")) {									
									//split according to tr
									//the first column of the should go to domain
									//the fifth will be for the table name
									String trSection = section;
									if(section.contains("<tr")) {										
										findTables = true;
										//find the second tr for the domain (first is header)
										//after that, we don't need this
										//could go through one section or multiple
										//sections
										int EWCPStart = trSection.indexOf("<tr");
										while(EWCPtrCount < 1 && EWCPStart < trSection.length() && EWCPStart > -1) {
											EWCPtrCount++;
											trSection = trSection.substring(EWCPStart+3);
											EWCPStart = trSection.indexOf("<tr");
										}
									}
									
									//Once done with tr, then we go through the tds
									//could be on the same line as the trs or different line as the trs
									//Once domain is found, only run once another tr is found
									if(EWCPtrCount == 1) {
										String tdSection = trSection;
										
										if(tdSection.contains("<td")) {
											int EWCPStart = tdSection.indexOf("<td");

											//find the first and fifth cell of each row (for domain and tables)
											//could go through one section or multiple
											//sections
											while(EWCPtdCount < 5 && EWCPStart < tdSection.length() && EWCPStart > -1 && findTables) {
												EWCPtdCount++;
												tdSection = tdSection.substring(EWCPStart+3);
												EWCPStart = tdSection.indexOf("<td");
												
												if(EWCPtdCount == 1) {
													int end = tdSection.indexOf("</td");
													if(end > -1) {
														domain += eraseClasses(tdSection.substring(0, end))+"|";												
													} else {
														domain += eraseClasses(tdSection)+"|";
													}
												} else if(EWCPtdCount == 5 && findTables) {
													int end = tdSection.indexOf("</td");
													if(end > -1) {
														tableName += eraseClasses(tdSection.substring(0, end))+"|";												
													} else {
														tableName += eraseClasses(tdSection)+"|";
													}
													
													EWCPtdCount = 0;
													findTables = false;
												}
											}
										}
									}
									
								}
								
								//get the styling for the current section
								String sectionStyling = returnAllStyleDetails(styleList, section);
								
								//if some of the code is colored gray and has the tag marking headers, then it is a header
								if(section.contains(TAG_STRING) && sectionStyling.contains("color:#808080") ) {	
									//erase the tags
									section = eraseTagFromHeaders(section);
									//if section is centered, then it is the table, otherwise is a header
									if(sectionStyling.contains("text-align:center")) {
										//if header exists, then add new row to the list for db
										if(!header.isEmpty()) {
											dbList.add(new DBItem(table, header, paragraph, sendFileName, report));											
											header = "";
										}
										table = section;
									} else {
										//again, if header exists then add a new row to the list
										if(!header.isEmpty()) {
											//the title in Administrative Category also doubles as the report name
											if(table.equals("Administrative Category") && header.equals("Title")) {
												report = paragraph;
											}
											dbList.add(new DBItem(table, header, paragraph, sendFileName, report));	
										}
										paragraph = "";	
										header = section;
									} 
								} else {	
									//anything that isn't a header or title becomes part of the value field
									paragraph += eraseParagraphTags(section, styleList);
								}							
							}
						}
						line = reader.readLine();
					}
					dbList.add(new DBItem(table, header, paragraph, sendFileName, report));
					reader.close();
					
					//get the file for output
					String outputfilename = outputPath+f.getName().replace(".html", "")+".csv";
					File outputFile = new File(outputfilename);
					outputFile.createNewFile();
					PrintWriter writer = new PrintWriter(outputfilename);
					
					writer.println("Report,Report,"+report);
					writer.println("Report,FileName,"+sendFileName);
					if(!domain.isEmpty()) {
						writer.println("Report,Domain,"+domain.substring(0, domain.length()-2));						
					} else {
						writer.println("Report,Domain,"+domain.substring(0, domain.length()));
					}
					if(!tableName.isEmpty()) {
						writer.println("Report,Table,"+tableName.substring(0, tableName.length()-2));						
					} else {
						writer.println("Report,Table,"+tableName.substring(0, tableName.length()));
					}
										
					//write the finalized list to the csv files
					for(DBItem item : dbList) {
						writer.println(item.toString());
					}
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("File "+filename+" not found");
			}			
			System.out.println("Ending "+filename);
		}
		System.out.println("Ending conversion...");
	}

}
