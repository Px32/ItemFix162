package com.grumpycraft.itemfix164;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileOutputStream;
import java.io.OutputStream;
public class ItemFix164 extends JavaPlugin {
	File configFile;
	FileConfiguration config;	
	public List<Integer> DisableHoldingWorld=new ArrayList<Integer>();
	public List<Integer> DisableHoldingClaim=new ArrayList<Integer>();
	public List<Integer> DisableSplashNearClaim=new ArrayList<Integer>();
	public List<Integer> DisableRightClick=new ArrayList<Integer>();
	public List<Integer> DisableLeftClick=new ArrayList<Integer>();
	public List<Integer> DisableLookingAtPlayer=new ArrayList<Integer>();
	public List<Integer> MasterList=new ArrayList<Integer>();
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(sender.isOp() && commandLabel.equalsIgnoreCase("reloaditemfix")){
			Load();
		}
		return false;
	}
	private void firstRun() throws Exception {
		if(!configFile.exists()){                  
			configFile.getParentFile().mkdirs();   
			copy(getResource("config.yml"), configFile);
		}
	}
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void loadYamls() {
		try {
			config.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void saveYamls() {
		try {
			config.save(configFile); //saves the FileConfiguration to its File
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void Load(){
		System.out.println("Reloading ItemFix");
		loadYamls();
		DisableHoldingWorld=LoadCfg(config.getIntegerList("DisableHoldingWorld"),DisableHoldingWorld);
		DisableHoldingClaim=LoadCfg(config.getIntegerList("DisableHoldingClaim"),DisableHoldingClaim);
		DisableSplashNearClaim=LoadCfg(config.getIntegerList("DisableSplashNearClaim"),DisableSplashNearClaim);
		DisableRightClick=LoadCfg(config.getIntegerList("DisableRightClick"),DisableRightClick);
		DisableLeftClick=LoadCfg(config.getIntegerList("DisableLeftClick"),DisableLeftClick);
		DisableLookingAtPlayer=LoadCfg(config.getIntegerList("DisableLookingAtPlayer"),DisableLookingAtPlayer);
		MasterList.addAll(DisableHoldingWorld);MasterList.addAll(DisableHoldingClaim);MasterList.addAll(DisableLookingAtPlayer);
		saveYamls();
	}
	public List<Integer> LoadCfg(List<Integer> s,List<Integer> i){
		List<Integer> I=new ArrayList<Integer>();
		if(s!=null){
			for(int inc=0;inc<s.size();inc++){
				if(s.get(inc)!=0)I.add(new Integer(s.get(inc)));
			}
		}
		else I.add(99999);
		return(I);
	}
	@Override
	public void onEnable()
	{
		configFile = new File(getDataFolder(), "config.yml");
		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}
		config = new YamlConfiguration();
		loadYamls();
		Load();
		getServer().getPluginManager().registerEvents(new Fix(), this);
	}
	@Override
	public void onDisable() 
	{
		saveYamls();
		HandlerList.unregisterAll();
	}	
	public class Fix implements Listener {				
		@EventHandler
		public final void Break(BlockBreakEvent event){
			final Block block=event.getPlayer().getTargetBlock(null, 25);
			if(block!=null && !block.equals(Material.AIR)){
				if(DisableHoldingWorld.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(block.getLocation(),event.getPlayer())=="nc"){
					CheckBar(event.getPlayer(),ChatColor.RED+"You cant not Break that, "+ChatColor.DARK_RED+"Outside your claim!");
					event.setCancelled(true);
				}
				else if(DisableHoldingClaim.contains(event.getPlayer().getItemInHand().getTypeId()) &&  CheckClaim(block.getLocation(),event.getPlayer())=="nyc"){
					CheckBar(event.getPlayer(),ChatColor.RED+"You cant not Break that, "+ChatColor.DARK_RED+"That is not your claim!");
					event.setCancelled(true);
				}
				else if(IsSplashy(event.getPlayer(),2,block)==true){
					CheckBar(event.getPlayer(),ChatColor.RED+"You cant not Break that, "+ChatColor.DARK_RED+"Near this claim!");
					event.setCancelled(true);
				}
				else return;
			}
		}
		@EventHandler
		public final void Move(PlayerMoveEvent event){
			final Block block=event.getPlayer().getTargetBlock(null, 50);
			if(block!=null && !block.equals(Material.AIR)){
				if(DisableHoldingWorld.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(block.getLocation(),event.getPlayer())=="nc"){
					CheckBar(event.getPlayer(),ChatColor.RED+"You can only hold that, "+ChatColor.DARK_RED+"While in claim!");
					event.setCancelled(true);
				}
				else if(DisableHoldingClaim.contains(event.getPlayer().getItemInHand().getTypeId()) &&  CheckClaim(block.getLocation(),event.getPlayer())=="nyc"){
					CheckBar(event.getPlayer(),ChatColor.RED+"You can only hold that, "+ChatColor.DARK_RED+"While not in a claim that is not yours!");
					event.setCancelled(true);
				}
				else return;
			}
		}
		@EventHandler
		public void Interact(PlayerInteractEvent event){
			Action action=event.getAction();
			Block block=event.getPlayer().getTargetBlock(null, 50);
			if(block!=null && !block.equals(Material.AIR)){
				if(MasterList.contains(event.getPlayer().getItemInHand().getTypeId())){
					if(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK || action == Action.PHYSICAL || action==Action.LEFT_CLICK_AIR || action==Action.RIGHT_CLICK_AIR){
						if(DisableHoldingWorld.contains(event.getPlayer().getItemInHand().getTypeId())&& CheckClaim(block.getLocation(),event.getPlayer())=="nc"){
							CheckBar(event.getPlayer(),"dev-a");
							event.setCancelled(true);
						}
						else if(DisableHoldingClaim.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(block.getLocation(),event.getPlayer())=="nyc"){
							CheckBar(event.getPlayer(),"dev-b");
							event.setCancelled(true);
						}
						else if(IsSplashy(event.getPlayer(),2,block)==true){
							CheckBar(event.getPlayer(),"dev-c");						
							event.setCancelled(true);
						}
						else if(block.getTypeId()==2174 && (CheckClaim(block.getLocation(),event.getPlayer())=="nyc")){
							event.getPlayer().teleport(new Location(event.getPlayer().getWorld(),event.getPlayer().getLocation().getBlockX(),event.getPlayer().getLocation().getBlockY(),event.getPlayer().getLocation().getBlockZ(),event.getPlayer().getLocation().getYaw()*-1,event.getPlayer().getLocation().getPitch()));
							event.setCancelled(true);
						}
						else return;
					}
				}
				if(action==Action.LEFT_CLICK_BLOCK && DisableLeftClick.contains(event.getPlayer().getItemInHand().getTypeId()))event.setCancelled(true);
				else if(action==Action.RIGHT_CLICK_BLOCK && DisableRightClick.contains(event.getPlayer().getItemInHand().getTypeId()))event.setCancelled(true);
				else return;
			}
		}
		@EventHandler
		public final void Target(EntityTargetEvent event){
			Bukkit.getServer().broadcastMessage("T+"+event.getTarget().getType());
			Bukkit.getServer().broadcastMessage("P+"+event.getEntity().getType());
			if((event.getEntity() instanceof Player) && (event.getTarget() instanceof Player)){
				final Player player = (Player) event.getEntity();
				final Player target = (Player) event.getTarget();
				if(DisableLookingAtPlayer.contains(player.getItemInHand().getTypeId())){
					player.getItemInHand().setType(Material.AIR);
					CheckBar(player,ChatColor.RED+"Dont't loot at "+target.getName()+" with that!");
				}
			}
		}
		@EventHandler
		public final void HotBarSwap(PlayerItemHeldEvent event){
			if(DisableHoldingWorld.contains(event.getPlayer().getItemInHand().getTypeId())&& CheckClaim(event.getPlayer().getLocation(),event.getPlayer())=="nc"){
				CheckBar(event.getPlayer(),ChatColor.RED+"You can only swap that, "+ChatColor.DARK_RED+"While in claim!");
			}
			else if(DisableHoldingClaim.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(event.getPlayer().getLocation(),event.getPlayer())=="nyc"){
				CheckBar(event.getPlayer(),ChatColor.RED+"You can only swap that, "+ChatColor.DARK_RED+"While not in a claim that is not yours!");
			}
			else if(DisableLookingAtPlayer.contains(event.getPlayer().getItemInHand().getTypeId())){
				List<Entity> Near=event.getPlayer().getNearbyEntities(25, 25, 25);
				if(Near!=null){
					for(Entity N:Near){
						if(N.getType()==EntityType.PLAYER && !N.equals(event.getPlayer())){
							CheckBar(event.getPlayer(),"BetaTest1");
						}
					}
				}
			}
			else return;
		}
		@EventHandler
		public final void InventoryClose(InventoryCloseEvent event) {
			HumanEntity human =  event.getView().getPlayer();
			if(human instanceof Player)
			{
				Player player = (Player)human;
				Block block=event.getPlayer().getTargetBlock(null, 50);
				if((block!=null && !block.equals(Material.AIR))&&(DisableHoldingWorld.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(block.getLocation(),player)=="nc")){
					CheckBar(player,ChatColor.RED+"You can only switch that, "+ChatColor.DARK_RED+"While in claim!");
				}
				else if((block!=null && !block.equals(Material.AIR))&&(DisableHoldingClaim.contains(event.getPlayer().getItemInHand().getTypeId()) && CheckClaim(block.getLocation(),player)=="nyc")){
					CheckBar(player,ChatColor.RED+"You can only switch that, "+ChatColor.DARK_RED+"While not in a claim that is not yours!");
				}
				else if(DisableLookingAtPlayer.contains(player.getItemInHand().getTypeId())){
					List<Entity> Near=player.getNearbyEntities(25, 25, 25);
					if(Near!=null){
						for(Entity N:Near){
							if(N.getType()==EntityType.PLAYER && !N.equals(player)){
								CheckBar(player,"BetaTest2");
							}
						}
					}
				}
				else return;
			}
		}	
		@SuppressWarnings("deprecation")
		public final String CheckClaim(Location Loc, Player player){
			Claim claim = GriefPrevention.instance.dataStore.getClaimAt(Loc, true, null);
			if(claim==null)return "nc";
			else if(claim!=null && !claim.getOwnerName().equalsIgnoreCase(player.getName()))return "nyc";
			else if(claim!=null && claim.getOwnerName().equalsIgnoreCase(player.getName()))return "yc";
			ArrayList<String> builders = new ArrayList<String>();
			ArrayList<String> containers = new ArrayList<String>();
			ArrayList<String> accessors = new ArrayList<String>();
			ArrayList<String> managers = new ArrayList<String>();
			claim.getPermissions(builders, containers, accessors, managers);
			player.sendMessage("Explicit permissions here:");
			StringBuilder permissions = new StringBuilder();
			permissions.append(ChatColor.GOLD + "M: ");
			if(managers.size() > 0)
			{
				for(int i = 0; i < managers.size(); i++)
					permissions.append(managers.get(i) + " ");
			}

			player.sendMessage(permissions.toString());
			permissions = new StringBuilder();
			permissions.append(ChatColor.YELLOW + "B: ");

			if(builders.size() > 0)
			{                                
				for(int i = 0; i < builders.size(); i++)
					permissions.append(builders.get(i) + " ");                
			}

			player.sendMessage(permissions.toString());
			permissions = new StringBuilder();
			permissions.append(ChatColor.GREEN + "C: ");                                

			if(containers.size() > 0)
			{
				for(int i = 0; i < containers.size(); i++)
					permissions.append(containers.get(i) + " ");                
			}

			player.sendMessage(permissions.toString());
			permissions = new StringBuilder();
			permissions.append(ChatColor.BLUE + "A :");

			if(accessors.size() > 0)
			{
				for(int i = 0; i < accessors.size(); i++)
					permissions.append(accessors.get(i) + " ");                        
			}

			player.sendMessage(permissions.toString());

			player.sendMessage("(M-anager, B-builder, C-ontainers, A-ccess)");
			return "nc";

		}
		public final boolean IsSplashy(Player player, int R, Block block){
			if(DisableSplashNearClaim.contains(player.getItemInHand().getTypeId())){
				for(double x=block.getLocation().getX()-R;x<block.getLocation().getX()+R;x++){
					for(double y=block.getLocation().getY()-R;y<block.getLocation().getY()+R;y++){
						for(double z=block.getLocation().getZ()-R;z<block.getLocation().getZ()+R;z++){
							if(CheckClaim(new Location(player.getWorld(),x,y,z),player)=="nyc"){
								return true;
							}
						}
					}
				}
			}
			return false;
		}
		@SuppressWarnings("deprecation")
		public final void CheckBar(Player player, final String M){
			int pInv = 9,Tally=0,Slot=player.getInventory().getHeldItemSlot();
			ItemStack item = new ItemStack(0);
			if(MasterList.contains(player.getItemInHand().getTypeId())){
				for(int i=9;i<36;i++){
					if(player.getInventory().getItem(i)!=null){
						if(MasterList.contains(player.getInventory().getItem(i).getTypeId()))Tally++;
					}
				}
				if(Tally==27){
					if(player.getInventory().getItem(Slot)!=null){
						if(MasterList.contains(player.getInventory().getItem(Slot).getTypeId())){
							player.sendMessage(ChatColor.RED+"You had no free inventory space, for the item to be swapped."+ChatColor.DARK_RED+" It was deleted instead.");							
							player.getInventory().setItem(Slot, item);
							player.updateInventory();
						}
					}	
				}
				if(player.getInventory().getItem(Slot)!=null && MasterList.contains(player.getInventory().getItem(Slot).getTypeId())){
					while(pInv < 36)
					{
						if((player.getInventory().getItem(Slot) != null && MasterList.contains(player.getInventory().getItem(Slot).getTypeId())) && (player.getInventory().getItem(pInv) == null || !MasterList.contains(player.getInventory().getItem(pInv).getTypeId())))
						{	
							item = player.getInventory().getItem(Slot);
							player.getInventory().setItem(Slot, player.getInventory().getItem(pInv));
							player.getInventory().setItem(pInv, item);
							Inventory I=player.getInventory();
							player.getInventory().setContents(I.getContents());
							player.sendMessage(M);
							pInv=36;
							return;						    
						}
						pInv++;
					}	
				}
			}
		}
	}
}













