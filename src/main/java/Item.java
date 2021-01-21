import javafx.beans.InvalidationListener;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Observable;
import java.util.Random;

public class Item  implements PriceObservable {
    private ItemStack item;
    private ArrayList<PriceObserver> observers = new ArrayList<>();
    public int currentSellPrice;
    public int currentPurchasePrice;
    public int amountPerPurchase;
    public int amountPerSell;

    public Item(ItemStack itemStack, int baseSellPrice, int basePurchasePrice, int amountPerPurchase, int amountPerSell){
        this.item = itemStack;
        this.amountPerPurchase = amountPerPurchase;
        this.amountPerSell = amountPerSell;


        currentPurchasePrice = basePurchasePrice;
        currentSellPrice = baseSellPrice;
    }

    public ItemStack getItem(){
        return item;
    }



    @Override
    public void register(PriceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(PriceObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyChange() {
        for(PriceObserver observer : observers){
            observer.update(this);
        }
    }
}
