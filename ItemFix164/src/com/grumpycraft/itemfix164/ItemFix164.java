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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileOutputStream;
import java.io.OutputStream;
public class ItemFix164 extends JavaPlugin {
	File configFile;
	FileConfiguration config;	
	public List<Integer> NotAllowedOutSideClaimWorld=new ArrayList<Integer>();
	public List<Integer> NotAllowedOutSideClaim=new ArrayList<Integer>();
	public List<Integer> NotAllowedOutSideClaimSplash=new ArrayList<Integer>();
	public List<Integer> DisableRightClick=new ArrayList<Integer>();
	public List<Integer> DisableLeftClick=new ArrayList<Integer>();
	public List<Integer> DontLookAtMeWithThat=new ArrayList<Integer>();
	public List<Integer> Mlist=new ArrayList<Integer>();
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
		NotAllowedOutSideClaimWorld=LoadCfg(config.getIntegerList("NotAllowedOutSideClaimWorld"),NotAllowedOutSideClaimWorld);
		NotAllowedOutSideClaim=LoadCfg(config.getIntegerList("NotAllowedOutSideClaim"),NotAllowedOutSideClaim);
		NotAllowedOutSideClaimSplash=LoadCfg(config.getIntegerList("NotAllowedOutSideClaimSplash"),NotAllowedOutSideClaimSplash);
		DisableRightClick=LoadCfg(config.getIntegerList("DisableRightClick"),DisableRightClick);
		DisableLeftClick=LoadCfg(config.getIntegerList("DisableLeftClick"),DisableLeftClick);
		DontLookAtMeWithThat=LoadCfg(config.getIntegerList("DontLookAtMeWithThat"),DontLookAtMeWithThat);
		Mlist.addAll(NotAllowedOutSideClaimWorld);Mlist.addAll(NotAllowedOutSideClaim);Mlist.addAll(DontLookAtMeWithThat);
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
		public void Break(BlockBreakEvent event){
			final Block block=event.getPlayer().getTargetBlock(null, 25);
			if(block!=null){ 
				if(((NotAllowedOutSideClaimWorld.contains(event.getPlayer().getItemInHand().getTypeId()) || (NotAllowedOutSideClaim.contains(event.getPlayer().getItemInHand().getTypeId()))) && InsideClaimNotOwnedByPlayer(block.getLocation(),event.getPlayer())==true) || IsSplashy(event.getPlayer(),2,block)==true){
					event.getPlayer().sendMessage(ChatColor.RED+"You cant not Break that!");
					CheckBar(event.getPlayer());
					event.setCancelled(true);
					return;
				}
			}
			return;
		}
		@EventHandler
		public void Move(PlayerMoveEvent event){
			Block block=event.getPlayer().getTargetBlock(null, 50);
			if(block!=null && !block.equals(Material.AIR)){
				if(NotAllowedOutSideClaimWorld.contains(event.getPlayer().getItemInHand().getTypeId()) || NotAllowedOutSideClaim.contains(event.getPlayer().getItemInHand().getTypeId())){
					CheckBar(event.getPlayer());
					event.getPlayer().sendMessage(ChatColor.RED+"You cant not Hold that here!");	
					return;
				}
			}
			return;
		}
		@EventHandler
		public void Target(EntityTargetEvent event){
			if((event.getEntity() instanceof Player) && (event.getTarget() instanceof Player)){
				Player player = (Player) event.getEntity();
				Player target = (Player) event.getTarget();
				if(DontLookAtMeWithThat.contains(player.getItemInHand().getTypeId())){
					player.getItemInHand().setType(Material.AIR);
					player.sendMessage(target+" Dont look at me with that!");
				}
			}
		}
	}
	@EventHandler
	public void InventoryClose(InventoryCloseEvent event) {
		HumanEntity human =  event.getView().getPlayer();
		if(human instanceof Player)
		{
			Player player = (Player)human;
			if(NotAllowedOutSideClaimWorld.contains(player.getItemInHand().getTypeId())){
				Block block=player.getTargetBlock(null, 50);
				if(block!=null){
					player.sendMessage(ChatColor.RED+"You cant not Hold that here!");					
					CheckBar(player);
					return;
				}
			}
			if(NotAllowedOutSideClaim.contains(player.getItemInHand().getTypeId())){
				Block block=player.getTargetBlock(null, 50);
				if(InsideClaimNotOwnedByPlayer(block.getLocation(),player)==true){
					player.sendMessage(ChatColor.RED+"You cant not Hold that here!");		
					CheckBar(player);
					return;
				}		
			}
			if(DontLookAtMeWithThat.contains(player.getItemInHand().getTypeId())){
				List<Entity> Near=player.getNearbyEntities(25, 25, 25);
				if(Near!=null){
					for(Entity N:Near){
						if(N.getType()==EntityType.PLAYER){
							if(!N.equals(player)){
								player.sendMessage("Dev-a");
								CheckBar(player);
								return;
							}
						}
					}
				}
			}
		}
	}	
	public boolean InsideClaimNotOwnedByPlayer(Location Loc, Player player){
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(Loc, true, null);
		if(claim==null)return false;
		if(claim!=null && !claim.getOwnerName().equalsIgnoreCase(player.getName()))return true;
		return false;
	}
	public boolean IsSplashy(Player player, int R, Block block){
		if(NotAllowedOutSideClaimSplash.contains(player.getItemInHand().getTypeId())){
			for(double x=block.getLocation().getX()-R;x<block.getLocation().getX()+R;x++){
				for(double y=block.getLocation().getY()-R;y<block.getLocation().getY()+R;y++){
					for(double z=block.getLocation().getZ()-R;z<block.getLocation().getZ()+R;z++){
						if(InsideClaimNotOwnedByPlayer(new Location(player.getWorld(),x,y,z),player)==true){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	@SuppressWarnings("deprecation")
	public void CheckBar(Player player){
		int pInv = 9;
		ItemStack item = new ItemStack(0);
		int Tally=0;
		int Slot=player.getInventory().getHeldItemSlot();
		if(Mlist.contains(player.getItemInHand().getTypeId())){
			for(int i=9;i<36;i++){
				if(player.getInventory().getItem(i)!=null){
					if(Mlist.contains(player.getInventory().getItem(i).getTypeId()))Tally++;
				}
			}
			if(Tally==27){
				if(player.getInventory().getItem(Slot)!=null){
					if(Mlist.contains(player.getInventory().getItem(Slot).getTypeId())){
						player.sendMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"AntiGrief"+ChatColor.RED+"] "+ChatColor.GREEN+"You don't have"+ChatColor.RED+"Inventory"+ChatColor.GREEN+"space for a restricted item, It has been mailed to you!"+ChatColor.GOLD+" If your mailbox has room"+ChatColor.GREEN+"!");							
						player.getInventory().setItem(Slot, item);
						player.updateInventory();
					}
				}	
				return;
			}
			if(player.getInventory().getItem(Slot)!=null && Mlist.contains(player.getInventory().getItem(Slot).getTypeId())){
				while(pInv < 36)
				{
					if((player.getInventory().getItem(Slot) != null && Mlist.contains(player.getInventory().getItem(Slot).getTypeId())) && (player.getInventory().getItem(pInv) == null || !Mlist.contains(player.getInventory().getItem(pInv).getTypeId())))
					{	
						item = player.getInventory().getItem(Slot);
						player.getInventory().setItem(Slot, player.getInventory().getItem(pInv));
						player.getInventory().setItem(pInv, item);
						Inventory I=player.getInventory();
						player.getInventory().setContents(I.getContents());						
						pInv=36;						
						return;						    
					}
					pInv++;
				}	
			}
		}
	}
}














