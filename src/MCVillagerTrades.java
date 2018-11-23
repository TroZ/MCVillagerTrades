// Copyright (c) Brian Risinger 2018 All rights reserved.
// Licensed under the MIT license.

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

import javax.swing.*;

public class MCVillagerTrades {
	
	File srcFile, destFile;

	
	int itemCount = 0;
	int villagerCount = 0;
	int tradeCount = 0;
		
	/*TODO:
	
		re-write to use villagers to trade for items with custom tags
		
		Villager summon with trades (nitwit, named CustName, with a custom head, with two trades ):
		/summon villager ~ ~ ~ {Invulnerable:1b,CustomNameVisible:1b,Profession:5,Career:1,CareerLevel:1,CustomName:"{\"text\":\"CustName\"}",
		 	ArmorItems:[{},{},{},{id:"minecraft:player_head",Count:1b,tag:{SkullOwner:{Name:"MHF_Herobrine"}}}],
		 	Offers:{Recipes:[
		 		{rewardExp:0b,maxUses:2147483647,buy:{id:"minecraft:dirt",Count:1b},buyB:{id:"minecraft:stone",Count:1b},sell:{id:"minecraft:diamond_block",Count:1b,tag:{display:{Name:"{\"text\":\"Lucky Diamond\",\"color\":\"aqua\"}",Lore:["This diamond will bring you luck"]},Enchantments:[{id:"minecraft:unbreaking",lvl:5}],AttributeModifiers:[{AttributeName:"generic.luck",Name:"generic.luck",Amount:100,Operation:0,UUIDLeast:927405,UUIDMost:58386}]}}},
		 		{rewardExp:0b,maxUses:2147483647,buy:{id:"minecraft:player_head",Count:1b},sell:{id:"minecraft:player_head",Count:1b,tag:{SkullOwner:{Id:"c377aa0e-63e7-4ee5-b503-6d59512f1dc9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODE5Y2IxYmNlM2ZmMDk1MTMzMDUxODI4ODc4Yzk3YjZmY2VkMWM1ODgzNDQ2MjI3YmFkYjhiYmZkY2VmMSJ9fX0="}]}}}}}
		 	]}}
	
		note: profession is coat color, career defines default trades. career 1 is nitwit - no default trades. Profession 5 is nitwit - green coat, 1 is librarian - white coat.
		
	
		idea:
		main config specifies spawn direction and distance (direction = north, distance = 2), and command block direction (command_direction = north,south,east,west) 
		You have a config section that defines blocks, each with a name and 'definition' (name=def). The definition would be either id:"minecraft:dirt" (potentially with enchants / lore / attributes.  Technically these are all items, not actual blocks). Or you can supply a MCStacker.net /give command. Or you can supply a player head summon command (actually, this would just be a give command as well).
		Then you have a section for each villager that defines that villager's looks and list trades. name=, namecolor=. namestyle=, coat =, head =, particles =, invulnerable =, silent =, riding = none,boat,minecart
		Trades would be:
		[Block Name] <count> <,[Block Name] <count>> = [Block Name] <count>
		One or two block types traded for one block type, with up to 64 count of each type. unspecified count defaults to 1  
		
		borrow s2cb multi-command code. can probably just chain following blocks (chain command blocks)
		
		
		more up to date (more effects):
		/summon villager ~ ~ ~ {Silent:1b,Invulnerable:1b,CustomNameVisible:1b,PersistenceRequired:1b,CanPickUpLoot:0b,Profession:1,Career:1,CareerLevel:1,CustomName:"{\"text\":\"CustName\"}",ArmorItems:[{},{},{},{id:"minecraft:player_head",Count:1b,tag:{SkullOwner:{Name:"MHF_Herobrine"}}}],ActiveEffects:[{Id:2b,Amplifier:1b,Duration:2147483647}],Offers:{Recipes:[{rewardExp:0b,maxUses:2147483647,buy:{id:"minecraft:dirt",Count:1b},buyB:{id:"minecraft:stone",Count:1b},sell:{id:"minecraft:diamond_block",Count:1b,tag:{display:{Name:"{\"text\":\"Lucky Diamond\",\"color\":\"aqua\"}",Lore:["This diamond will bring you luck"]},Enchantments:[{id:"minecraft:unbreaking",lvl:5}],AttributeModifiers:[{AttributeName:"generic.luck",Name:"generic.luck",Amount:100,Operation:0,UUIDLeast:927405,UUIDMost:58386}]}}},{rewardExp:0b,maxUses:2147483647,buy:{id:"minecraft:player_head",Count:1b},sell:{id:"minecraft:player_head",Count:1b,tag:{display:{Name:"{\"text\":\"Xisuma\"}"},SkullOwner:{Id:"c377aa0e-63e7-4ee5-b503-6d59512f1dc9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODE5Y2IxYmNlM2ZmMDk1MTMzMDUxODI4ODc4Yzk3YjZmY2VkMWM1ODgzNDQ2MjI3YmFkYjhiYmZkY2VmMSJ9fX0="}]}}}}}]}}
	
	
	
		
		pack ideas:
		miniblocks
		
		uncraftable blocks:	coral crafter (fans + corals -> blocks), 
			Slabifier (Smooth Quartz, Smooth Red Sandstone, Smooth Sandstone, Smooth Stone, Petrified Oak Slab), 
			Rare Crafter (spiderweb, dead bush, fern (grass item + leaves?), Unstable TNT, tipped arrows (stack of arrows + potion = 64 tipped arrows), records (16 note block per?))
		
		spawner trader?
		
		
		optional multi-trade? any item that can be traded for x ( <= 27) other items, can instead trade item x27 + shulker box for shulker box with one of each item inside
		/give @p minecraft:red_shulker_box{display:{Name:"{\"text\":\"Set 1\"}"},BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:player_head",Count:1b,tag:{display:{Name:"{\"text\":\"Xisuma\"}"},SkullOwner:{Id:"c377aa0e-63e7-4ee5-b503-6d59512f1dc9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODE5Y2IxYmNlM2ZmMDk1MTMzMDUxODI4ODc4Yzk3YjZmY2VkMWM1ODgzNDQ2MjI3YmFkYjhiYmZkY2VmMSJ9fX0="}]}}}}]}} 1
		textWoodBox=/give @p minecraft:red_shulker_box{display:{Name:"{\"text\":\"Wood Letters\"}"},
		BlockEntityTag:{Items:[
		{Slot:0b,id:"minecraft:player_head",Count:1b,tag:{ }},
		]}} 1
		
		head give command:
		/give @p minecraft:player_head{display:{Name:"{\"text\":\"Xisuma\"}"},SkullOwner:{Id:"c377aa0e-63e7-4ee5-b503-6d59512f1dc9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODE5Y2IxYmNlM2ZmMDk1MTMzMDUxODI4ODc4Yzk3YjZmY2VkMWM1ODgzNDQ2MjI3YmFkYjhiYmZkY2VmMSJ9fX0="}]}}} 1
	
		minecart passengers:
		/summon minecart ~ ~ ~ {Passengers:[{id:"minecraft:villager",CareerLevel:1}]}
		/summon boat ~ ~ ~ {Type:"oak",Passengers:[{id:"minecraft:villager",CareerLevel:1}]}
	
	*/
	
	static final String SUMMON_VILLAGER_BASIC = "summon villager ~{X} ~{Y} ~{Z} {Silent:1b,Invulnerable:1b,CustomNameVisible:{SHOWNAME}b,PersistenceRequired:1b,CanPickUpLoot:0b,Profession:{PROFESSION},Career:1,CareerLevel:100,CustomName:\"{\\\"text\\\":\\\"{NAME}\\\"}\",ArmorItems:[{},{},{},{{HEAD}}],Offers:{Recipes:[{TRADES}]}}";
	
	static final String TRADE_ONE_FOR_ONE = "{rewardExp:0b,maxUses:2147483647,buy:{id:\"{B-ONE}\",Count:{ONE-COUNT}b,tag:{{ONE-TAG}}},sell:{id:\"{SELL}\",Count:{SELL-COUNT}b,tag:{{SELL-TAG}}}}";
	static final String TRADE_TWO_FOR_ONE = "{rewardExp:0b,maxUses:2147483647,buy:{id:\"{B-ONE}\",Count:{ONE-COUNT}b,tag:{{ONE-TAG}}},buyB:{id:\"{B-TWO}\",Count:{TWO-COUNT}b,tag:{{TWO-TAG}}},sell:{id:\"{SELL}\",Count:{SELL-COUNT}b,tag:{{SELL-TAG}}}}";
	
	static final String SUMMON_BOAT = "summon boat ~{X} ~{Y} ~{Z} {Passengers:[{id:\"minecraft:villager\",{TAGS}}]}";
	static final String SUMMON_MINECART = "summon minecart ~{X} ~{Y} ~{Z} {Passengers:[{id:\"minecraft:villager\",{TAGS}}]}";
	
	
	static final String PACK_MCMETA = "{\r\n" + 
			"   \"pack\":{\r\n" + 
			"      \"pack_format\":3,\r\n" + 
			"      \"description\":\"Villager Trades - %DESC%\"\r\n" + 
			"   }\r\n" + 
			"}";
	
	
	private HashMap<String, Item> items = new HashMap<String,Item>();
	public HashSet<String> usedItems = new HashSet<String>();
	
	//main settings
	String packname = "";
	private int spawnXOffset = 0, spawnYOffset = 0, spawnZOffset = 0;
	private int commandXOffset = 0, commandYOffset = 0, commandZOffset = 0;
	//private int distance = 1;
	//private String commandDirection = "";
	//private int commandXOffset = 0, commandZOffset = 0;
	private InType in = InType.none;
	private boolean showName = true;
	private boolean outputDatapack = false;
	
	private ArrayList<String> commandList = new ArrayList<String>();

	public static void main(String[] args) {
		MCVillagerTrades mcvt = new MCVillagerTrades(); 
		mcvt.start();
	}
	
	public MCVillagerTrades() {
		
		String curdir = System.getProperty("user.dir");
		File dir = new File(curdir);
		
		System.out.println("Choose config file:");
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(dir);
		int result = jfc.showOpenDialog(null);
		if(result != JFileChooser.APPROVE_OPTION) {
			System.exit(0);
		}
		srcFile = jfc.getSelectedFile();
		
		System.out.println("Source file: "+ srcFile.getAbsolutePath());
		
		/*
		{
			//TODO , maybe have the user select the destination
			destFile = jfc.getCurrentDirectory();
			String name = File.separator + "recipes";
			destFile = new File(destFile,name);
			destFile.mkdirs();
			
			System.out.println("Destination Directory: "+destFile.getAbsolutePath());
		}
		*/
		
	}

	public void start() {
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(srcFile.getAbsolutePath()), "UTF8"));
			
			int linecount = 0;
			//look for settings and then read them
			String line = br.readLine();
			linecount++;
			{
				boolean main = true;
				while(line!=null) {
					if(line.trim().length()>0) {
						char first = line.charAt(0);
						switch(first) {
							case '#':
								//comment - ignore
								break;
							case '[':
								if(!(line.trim()).equals("[main]")) {
									main = false;
								}
								break;
							default:
								int pos = line.indexOf('=');
								if(pos>0) {
									String name = line.substring(0, pos);
									String value = line.substring(pos+1);
									
									
									switch(name.trim().toLowerCase()) {
									case "packname":{
										packname = value;
										break;
									}
									/*
									case "direction":{
										value = value.toLowerCase();
										direction = value;
										if(value.contains("north")) {
											spawnZOffset = -1;
										}else if(value.contains("south")) {
											spawnZOffset = 1;
										}else if(value.contains("east")) {
											spawnXOffset = 1;
										}else if(value.contains("west")) {
											spawnXOffset = -1;
										}else {
											
										}
										break;
									}
									case "distance":{
										try {
											distance = Integer.parseInt(value);
										}catch(Exception e) {
											e.printStackTrace();
											showError(linecount,"Can't parse setting for distance ("+value+")");
											return;
										}
										break;
									}
									case "command_direction":{
										//these should be opposite of direction, as we need to move the spawning back
										value = value.toLowerCase();
										commandDirection = value;
										if(value.contains("north")) {
											commandZOffset = 1;
										}else if(value.contains("south")) {
											commandZOffset = -1;
										}else if(value.contains("east")) {
											commandXOffset = -1;
										}else if(value.contains("west")) {
											commandXOffset = 1;
										}
										break;
									}
									*/
									case "offset":{
										if(!parseTriplet(value,true)) {
											showError(linecount,"Can't parse setting for offset ("+value+")");
										}
										break;
									}
									case "offset_per_command":{
										if(!parseTriplet(value,false)) {
											showError(linecount,"Can't parse setting for offset_per_command ("+value+")");
										}
										break;
									}
									case "in":{
										try {
											in = InType.valueOf(value);
										}catch(Exception e) {
											System.out.println("Bad value for 'in' on line "+linecount+".  Assuming none.");
										}
										break;
									}
									case "showName":{
										try {
											showName = Boolean.parseBoolean(value);
										}catch(Exception e) {
											System.out.println("Bad value for showName on line "+linecount+".  Assuming true.");
										}
										break;
									}
									case "output":{
										if(value.equalsIgnoreCase("data")) {
											outputDatapack = true;
										}
										break;
									}
								}
							}
						}
					}
					
					if(!main)
						break;
					
					//get next line
					line = br.readLine();
					linecount++;
				}
			}
			
			if(packname.length()==0) {
				showError(linecount,"Invalid data file - missing packname");
				return;
			}
			
			
			//now make up the list of items for trade (buy / sell)
			{
				boolean itemssection = true;
				while(line!=null) {
					if(line.trim().length()>0) {
						char first = line.charAt(0);
						switch(first) {
						case '#':
							//comment - ignore
							break;
						case '[':
							if(!(line.trim()).equals("[items]")) {
								itemssection = false;
							}
							break;
						default:
							int pos = line.indexOf('=');
							if(pos>0) {
								String name = line.substring(0, pos);
								String value = line.substring(pos+1);
								
								if(name.indexOf(' ')>-1) {
									showError(linecount,"Item names cannot contain a space.");
									return;
								}
								
								if(items.containsKey(name)) {
									showError(linecount,"Duplicate item name found ("+name+")");
									return;
								}
								
								//TODO: parse item into usable format
								Item it = parseItem(value);
								if(it==null) {
									showError(linecount,"Unable to parse item ("+name+"="+value+")");
									return;
								}
								
								items.put(name, it);
								itemCount++;
							}
	
						}
					}
					
					if(!itemssection)
						break;
					
					//get next line
					line = br.readLine();
					linecount++;
				}
			}
			
			if(items.size()<1) {
				showError(linecount,"No items defined for trades!");
				return;
			}
			
			//villagers
			Villager v = null;
			boolean trades = false;
			while(line!=null) {
			
				if(line.trim().length()>0) {
					char first = line.charAt(0);
					switch(first) {
						case '#':
							//comment - ignore
							break;
						case '[':
							if(v!=null) {
								v.addCommand();
							}
							v = new Villager(villagerCount);
							villagerCount++;
							trades = false;
							break;
						default:

							if(!trades && line.trim().equalsIgnoreCase("trades:")) {
								trades = true;
								continue;
							}
							
							if(!trades) {
								int pos = line.indexOf('=');
								if(pos>0) {
									String name = line.substring(0, pos);
									String value = line.substring(pos+1);
									
									switch(name.trim().toLowerCase()) {
									case "name":
										v.name = value;
										break;
									case "head":
										v.head = parseHead(value);
										if(v.head == null) {
											showError(linecount,"Unable to parse head for villager "+villagerCount+" ("+value+")");
											return;
										}
										break;
									case "profession":
										switch(value.trim().toLowerCase()) {
										case "farmer":
											v.profession = 0;
											break;
										case "librarian":
											v.profession = 1;
											break;
										case "priest":
											v.profession = 2;
											break;
										case "blacksmith":
											v.profession = 3;
											break;
										case "butcher":
											v.profession = 4;
											break;
										case "nitwit":
											v.profession = 5;
											break;
										default:
											try {
												v.profession = Integer.parseInt(value);
											}catch(Exception e) {
												e.printStackTrace();
												showError(linecount,"Can't parse setting for profession ("+value+") for villager "+villagerCount);
												return;
											}
										}
										break;

									}
								}
							}else {
								//add trade to villager
								int pos = line.indexOf('=');
								if(pos>0) {
									String buy = line.substring(0, pos).trim();
									String buyB = null;
									String buyCount = null;
									String buyBCount = null;
									String sell = line.substring(pos+1).trim();
									String sellCount = null;
									pos = buy.indexOf(',');
									if(pos>0) {
										buyB =  buy.substring(pos+1).trim();
										buy = buy.substring(0, pos).trim();
										pos = buyB.indexOf(' ');
										if(pos>0) {
											buyBCount = buyB.substring(pos+1).trim();
											buyB = buyB.substring(0, pos).trim();
										}
									}
									pos = buy.indexOf(' ');
									if(pos>0) {
										buyCount = buy.substring(pos+1).trim();
										buy = buy.substring(0, pos).trim();
									}
									pos = sell.indexOf(' ');
									if(pos>0) {
										sellCount = sell.substring(pos+1).trim();
										sell = sell.substring(0, pos).trim();
									}
									
									Trade t = new Trade();
									Item i = items.get(buy);
									if(i==null) {
										showError(linecount,"Item "+buy+" not defined in items");
										return;
									}
									t.item1 = i.type;
									t.item1tag = i.tag;

									i = items.get(sell);
									if(i==null) {
										showError(linecount,"Item "+sell+" not defined in items");
										return;
									}
									t.sellitem = i.type;
									t.selltag = i.tag;
									
									usedItems.add(buy);
									usedItems.add(sell);
									
									if(buyB!=null) {
										i = items.get(buyB);
										if(i==null) {
											showError(linecount,"Item "+buyB+" not defined in items");
											return;
										}
										t.item2 = i.type;
										t.item2tag = i.tag;
										if(buyBCount!=null) {
											try {
												t.count2 = Integer.parseInt(buyBCount);
											}catch(Exception e) {
												showError(linecount,"Failed to parse second trade item count ("+buyBCount+")");
												return;
											}
										}
										usedItems.add(buyB);
									}
									if(buyCount!=null) {
										try {
											t.count1 = Integer.parseInt(buyCount);
										}catch(Exception e) {
											showError(linecount,"Failed to parse first trade item count ("+buyCount+")");
											return;
										}
									}
									if(sellCount!=null) {
										try {
											t.sellCount= Integer.parseInt(sellCount);
										}catch(Exception e) {
											showError(linecount,"Failed to parse sell item count ("+sellCount+")");
											return;
										}
									}
									
									v.trades.add(t);
									tradeCount++;
								}
							}
							break;
					}
				}
				
				//get next line
				line = br.readLine();
				linecount++;
			}
			
			//now add final villager
			if(v!=null) {
				v.addCommand();
			}
			
			//create destination file
			save(commandList);
			
		
		} catch ( IOException e) {
			System.out.println("Error opening "+srcFile.getAbsolutePath());
			e.printStackTrace();
		} finally {
			if(br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}		
		}
		
		System.out.println("\n\nDONE!\nItems: "+itemCount+"\nVillagers: "+villagerCount+"\nTrades: "+tradeCount);
		System.out.println("Wrote file: "+destFile.getAbsolutePath());
		
		ArrayList<String> unusedItems = new ArrayList<String>();
		for(String iName:items.keySet()) {
			if(!usedItems.contains(iName)) {
				unusedItems.add(iName);
			}
		}
		if(unusedItems.size()>0) {
			System.out.println("\n Unused items found:");
			boolean first = true;
			for(String s:unusedItems) {
				if(!first) {
					System.out.print(", ");
				}else {
					first = false;
				}
				System.out.print(s);
			}
		}
		
	}
	
	private boolean save(ArrayList<String> commandList) throws IOException {
		
		if(outputDatapack) {
			ZipOutputStream zos = null;
			try {
				destFile = new File(srcFile.getParentFile(),packname.toLowerCase()+".zip");
				if(destFile.exists()) {
					System.out.println("destination already exists - "+destFile.getAbsolutePath()+"\nEXITING!\n");
					return false;
				}
				zos = new ZipOutputStream(new FileOutputStream(destFile),StandardCharsets.UTF_8);
				zos.setMethod(ZipOutputStream.DEFLATED);
				zos.setLevel(9); //Max Compression
				writeFile(zos,"pack.mcmeta", PACK_MCMETA.replace("%DESC%", packname));
				
				//create directory structure
				String path = "data/villagertrades/functions/" + packname.toLowerCase();
				String regex = "/";
				String[] paths = path.split(regex);
				String fullpath = "";
				for(String p:paths) {
					if(fullpath.length()>0) {
						fullpath = fullpath + "/";
					}
					fullpath = fullpath + p;
					zos.putNextEntry(new ZipEntry(fullpath+"/"));
				}
				
			
				StringBuilder sb = new StringBuilder(65536);
				sb.append("#Function file to spawn villagers with custom trades for ");
				sb.append(packname);
				sb.append("\n");
				//sb.append("say Running...\n");
				for(String s : commandList) {
					sb.append(s);
					sb.append("\n");
				}
				//sb.append("say Done!\n");
				
				writeDataFile(zos,"spawn.mcfunction", sb.toString());
			
				//writeDataFile(zos,"test.mcfunction", "say \"TEST\"\n");
				
				zos.finish();
				
			}finally {
				if(zos!=null) {
					zos.close();
				}
			}
			
		}else {
			
			PrintWriter pw = null;
			try {
				destFile = new File(srcFile.getParentFile(),packname+".txt");
				if(destFile.exists()) {
					showError(0,"Destination already exists - "+destFile.getAbsolutePath());
					return false;
				}
				
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), StandardCharsets.UTF_8)));
				
				pw.println("Villager commands for pack "+packname);
				pw.println();
				pw.println(""+commandList.size()+" commands");
				pw.println();
				pw.println("Commands larger than 32500 characters won't work in command blocks.");
				pw.println();
				pw.println();
				
				for(String s : commandList) {
					pw.println(s);
				}
			}finally {
				if(pw!=null) {
					pw.close();
				}
			}
		}
		
		return true;
	}
	
	void writeDataFile(ZipOutputStream zos, String filename, String data) {
		String path = "data/villagertrades/functions/" + 
				packname.toLowerCase() + "/" +filename;
		writeFile(zos,path,data);
	}
	
	void writeFile(ZipOutputStream zos,String filename, String data) {

		try {
			zos.putNextEntry(new ZipEntry(filename));
			
			byte[] bytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void showError(int line, String message) {
		JOptionPane.showMessageDialog(null, "ERROR:\n"+message+"\non line "+line, "ERROR", JOptionPane.ERROR_MESSAGE);
		
	}
	
	public String parseHead(String str) {
		//TODO:  
		// we want: id:\"minecraft:player_head\",Count:1b,tag:{SkullOwner:{Name:\"MHF_Herobrine\"}}
		//from: /give @p minecraft:player_head{display:{Name:"{\"text\":\"Xisuma\"}"},SkullOwner:{Id:"c377aa0e-63e7-4ee5-b503-6d59512f1dc9",Properties:{textures:[{Value:"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NhODE5Y2IxYmNlM2ZmMDk1MTMzMDUxODI4ODc4Yzk3YjZmY2VkMWM1ODgzNDQ2MjI3YmFkYjhiYmZkY2VmMSJ9fX0="}]}}} 1
		
		//uses similar logic to parse item below, but we just really need the tag part
		
		if(!str.contains("player_head")) {
			return null;
		}
		
		if(str.startsWith("/give") || str.startsWith("give")) {
			//give command
			int pos = str.indexOf(' ',6);
			if(pos<0)
				return null;
			String istr = str.substring(pos);
			//check for tags
			int begin = istr.indexOf("{");
			if(begin>0) {
				pos = istr.lastIndexOf("}");
				return "id:\"minecraft:player_head\",Count:1b,tag:{"+istr.substring(begin+1, pos)+"}";
			}
		}
		
		return null;
	}
	
	public Item parseItem(String str) {
		//TODO: parse a simple item or a give command
		
		//simple:  dirt, minecraft:dirt  - no spaces
		//give simple:  /give @p minecraft:apple 1
		//give complex: /give @r[level=5..20,limit=2,sort=nearest,distance=3..10] minecraft:apple{display:{Name:"{\"text\":\"Magic Apple\"}",Lore:["I am an Apple"]},Enchantments:[{id:"minecraft:unbreaking",lvl:1}],AttributeModifiers:[{AttributeName:"generic.maxHealth",Name:"generic.maxHealth",Amount:30,Operation:0,UUIDLeast:617825,UUIDMost:715804}]} 32
		
		Item item = new Item();
		
		if(str.startsWith("/give") || str.startsWith("give")) {
			//give command
			int pos = str.indexOf(' ',6);
			if(pos<0)
				return null;
			String istr = str.substring(pos);
			//check for tags
			int begin = istr.indexOf("{");
			if(begin>0) {
				pos = istr.lastIndexOf("}");
				item.type = istr.substring(0,begin).trim();
				item.tag = istr.substring(begin+1, pos).trim();
			}else {
				item.type = str.trim();
				item.tag = "";
			}
		}else {
			//simple item
			if(str.contains(" "))
				return null;
			item.type = str.trim();
			item.tag = "";
		}
		
		
		return item;
	}
	
	private void addCommand(String command, int number) {
		
		if(!in.equals(InType.none)) {
			int x = spawnXOffset + commandXOffset * number;
			int y = spawnYOffset + commandYOffset * number;
			int z = spawnZOffset + commandZOffset * number;
			
			String cmd;
			if(in.equals(InType.boat)) {
				cmd=SUMMON_BOAT;
			}else {
				cmd=SUMMON_MINECART;
			}
			cmd = cmd.replace("{X}", ""+x).replace("{Y}", ""+y).replace("{Z}", ""+z);
			
			int start = command.indexOf('{');
			int end = command.lastIndexOf('}');
			String tags = command.substring(start+1, end);
			cmd = cmd.replace("{TAGS}", tags);
			
			command = cmd;
			
		}
		commandList.add(command);
	}
	
	private boolean parseTriplet(String value,boolean offset) {
		
		Pattern p = Pattern.compile("(\\d*)[, ]*(\\d*)[, ]*(\\d*)");
		Matcher m = p.matcher(value);
		if(!m.matches()) {
			return false;
		}
		int x = Integer.parseInt(m.group(1));
		int y = Integer.parseInt(m.group(2));
		int z = Integer.parseInt(m.group(3));
		
		if(offset) {
			spawnXOffset = x;
			spawnYOffset = y;
			spawnZOffset = z;
		}else {
			commandXOffset = x;
			commandYOffset = y;
			commandZOffset = z;
		}
		
		return true;
	}
	
	public class Villager {
		
		public int number;
		public String name;
		public int profession = 5;
		public String head;
		public ArrayList<Trade> trades = new ArrayList<Trade>();
		
		public Villager(int number) {
			this.number = number;
		}
		
		public void addCommand() {

			String command = SUMMON_VILLAGER_BASIC;
			int x = spawnXOffset + commandXOffset * number;
			int y = spawnYOffset + commandYOffset * number;
			int z = spawnZOffset + commandZOffset * number;
			command = command.replace("{X}", ""+x).replace("{Y}", ""+y).replace("{Z}", ""+z);
			if(name != null && showName == true) {
				command = command.replace("{SHOWNAME}", "1").replace("{NAME}", name);
			}else {
				command = command.replace("{SHOWNAME}", "0").replace("{NAME}", "");
			}
			command = command.replace("{PROFESSION}", ""+profession);
			if(head==null) {
				command = command.replace("{HEAD}", "");
			}else {
				String h = cleanHead(head);
				command = command.replace("{HEAD}", h);
			}
			
			StringBuilder allTrades = new StringBuilder();
			for(Trade t : trades) {
				String tstr;
				if(t.item2 !=null) {
					
					if(t.item2tag==null) {
						t.item2tag="";
					}
					
					tstr = TRADE_TWO_FOR_ONE;
					tstr = tstr.replace("{B-TWO}", t.item2).replace("{TWO-COUNT}", ""+t.count2).replace("{TWO-TAG}",""+t.item2tag);
				}else {
					tstr = TRADE_ONE_FOR_ONE;
				}
				
				if(t.item1tag==null) {
					t.item1tag="";
				}
				if(t.selltag==null) {
					t.selltag="";
				}
				
				tstr = tstr.replace("{B-ONE}", t.item1).replace("{ONE-COUNT}", ""+t.count1).replace("{ONE-TAG}",""+t.item1tag);
				tstr = tstr.replace("{SELL}", t.sellitem).replace("{SELL-COUNT}", ""+t.sellCount).replace("{SELL-TAG}",""+t.selltag);
				
				if(allTrades.length()>0) {
					allTrades.append(",");
				}
				allTrades.append(tstr);
			}
			command = command.replace("{TRADES}", allTrades.toString());
			MCVillagerTrades.this.addCommand(command,number);
		}
		
		/**
		 * this removes the name from a head to be placed on a villager as it will never be seen
		 * @param head - a head command string
		 * @return the string with the display tag removed
		 */
		public String cleanHead(String head) {
			
			return head.replaceAll("display:\\{.*?(?<!\\\\\\\")\\},", "");//that is a regex and a half! Due to \ being the escape character in both regexes and java
			
		}
	}
		
	public class Trade {
		String item1;
		String item1tag;
		int count1 = 1;
		String item2;
		String item2tag;
		int count2 = 1;
		String sellitem;
		String selltag;
		int sellCount = 1;
	}
	
	public class Item {
		String type;
		String tag;
	}
	
	public enum InType {
		none, boat, minecart
	}
}
