import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Category implements PriceObserver, Listener {
    private ArrayList<Item> items = new ArrayList<>();
    private String categoryCode;
    private ArrayList<Inventory> inventories = new ArrayList<>();
    private int itemPerPage = 45;

    public Category(String categoryCode){
        Bukkit.getPluginManager().registerEvents(this, EasyStore.EasyStore);
        this.categoryCode = categoryCode;
        loadItems();
        for(Item item : items){
            item.register(this);
        }
        setInventory();
    }

    public void loadItems(){
        FileConfiguration config = EasyStore.EasyStore.getConfig();

        for(String key : config.getConfigurationSection("category." + categoryCode).getKeys(false)){
            String materialName = config.getString("category." + categoryCode + "." + key + ".materialName");
            ItemStack itemStack = new ItemStack(Material.matchMaterial(materialName));
            int basePurchasePrice = config.getInt("category." + categoryCode + "." + key + ".basePurchasePrice");
            int baseSellPrice = config.getInt("category." + categoryCode + "." + key + ".baseSellPrice");
            int amountPerPurchase = config.getInt("category." + categoryCode + "." + key + ".amountPerPurchase");
            int amountPerSell = config.getInt("category." + categoryCode + "." + key + ".amountPerSell");

            Item item = new Item(itemStack,  baseSellPrice, basePurchasePrice, amountPerPurchase, amountPerSell);
            items.add(item);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)) return;
        if(e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) return;

        Player player = (Player) e.getWhoClicked();

        if(e.getInventory().getTitle().equalsIgnoreCase(categoryCode)){
            e.setCancelled(true);

            if(e.getRawSlot() >= itemPerPage){
                return;
            }

            if(e.getSlot() < itemPerPage){
                for(int i = 0; i < inventories.size(); i++){
                    if(e.getInventory().equals(inventories.get(i))){
                        int index = getItemIndexFromClickedSlot(i, e.getSlot());
                        Bukkit.getLogger().info(items.get(index).getItem().toString());
                        Item item = items.get(index);

                        if(e.getClick().isLeftClick()){
                            purchase(item, player);
                        } else if(e.getClick().isRightClick()){
                            sell(item, player);
                        }

                        break;
                    }
                }
            }
        }
    }

    public void purchase(Item item, Player player){
        if(EasyStore.EasyStore.getEconomy().getCashManager().hasBalance(player.getUniqueId(), item.currentPurchasePrice)){
            EasyStore.EasyStore.getEconomy().getCashManager().withdraw(player.getUniqueId(), item.currentPurchasePrice);
            ItemStack giveItem = item.getItem().clone();
            giveItem.setAmount(item.amountPerPurchase);
            player.getInventory().addItem(giveItem);
            player.sendMessage(item.getItem().toString() + "아이템을 " + item.currentPurchasePrice + "N에 구입하였습니다!");
        } else{
            player.sendMessage("돈이 부족합니다!");
        }
    }

    public void sell(Item item, Player player){
        if(isPlayerhasEnoughItem(player, item.getItem(), item.amountPerSell)){
            removePlayerItem(player, item.getItem(), item.amountPerSell);
            EasyStore.EasyStore.getEconomy().getCashManager().deposit(player.getUniqueId(), item.currentSellPrice);
            player.sendMessage(item.getItem().toString() + "아이템을 " + item.currentSellPrice + "N에 판매하였습니다!");
        } else{
            player.sendMessage("물건이 부족합니다!");
        }
    }

    private boolean isPlayerhasEnoughItem(Player player, ItemStack itemStack, int minimumCount){
        int count = 0;
        for(int i = 0; i < player.getInventory().getSize(); i++){
            ItemStack targetItem = player.getInventory().getItem(i);

            if(targetItem != null){
                if(targetItem.isSimilar(itemStack)){
                    count += targetItem.getAmount();
                }
            }
        }

        Bukkit.getLogger().info(count + "");
        return count >= minimumCount;
    }

    private void removePlayerItem(Player player, ItemStack itemStack, int count){
        int removedCount = 0;
        for(int i = 0; i< player.getInventory().getSize();i++){
            ItemStack targetItem = player.getInventory().getItem(i);

            if(targetItem != null){
                if(targetItem.isSimilar(itemStack)){

                        if(removedCount + targetItem.getAmount() < count){
                            removedCount += targetItem.getAmount();
                            player.getInventory().setItem(i, null);
                        } else if(removedCount + targetItem.getAmount() == count){
                            player.getInventory().setItem(i, null);
                            break;
                        } else{
                            Bukkit.getLogger().info("removed: " + removedCount);
                            Bukkit.getLogger().info("target amount: " + targetItem.getAmount());
                            Bukkit.getLogger().info("count: " + count);
                            targetItem.setAmount((removedCount + targetItem.getAmount()) - count);
                            break;
                        }

                }
            }
        }
    }

    public void setInventory(){
        int needPage = items.size() / itemPerPage + 1;
        for(int i = 0; i < needPage; i++){
            inventories.add(Bukkit.createInventory(null, 54, categoryCode));
        }

        int itemIndex = 0;
        for(int pageIndex = 0; pageIndex < inventories.size(); pageIndex++){
            for(int inventoryIndex = 0; inventoryIndex < itemPerPage; inventoryIndex++){
                if(itemIndex >= items.size()){
                    return;
                }

                Item item = items.get(itemIndex);
                ArrayList<String> lore = new ArrayList<>();
                lore.add("판매가: " + item.currentSellPrice + "/" + item.amountPerSell+"개");
                lore.add("구매가: " + item.currentPurchasePrice + "/" + item.amountPerPurchase+ "개");
                ItemMeta me = item.getItem().getItemMeta();
                me.setLore(lore);
                ItemStack exhibitItem = item.getItem().clone();
                exhibitItem.setItemMeta(me);
                inventories.get(pageIndex).setItem(inventoryIndex, exhibitItem);
                itemIndex++;
            }
        }
    }

    private int getItemIndexFromClickedSlot(int pageIndex, int itemIndex){
        return pageIndex * itemPerPage + itemIndex;
    }

    @Override
    public void update(Item item) {
        if(items.contains(item)){
            for(int i = 0; i < items.size(); i++){
                if(items.get(i).equals(item)){
                    for(int k = 1;; k++){
                        if(i - 45 * k < 0){
                            
                        }
                    }
                }
            }
        }
    }



    public void addItem(Item item){
        items.add(item);
        item.register(this);
    }

    public void removeItem(Item item){
        items.remove(item);
        item.unregister(this);
    }
}
