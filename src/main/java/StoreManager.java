import java.util.ArrayList;

public class StoreManager {
    private ArrayList<Category> categories = new ArrayList<>();

    public StoreManager(){
        loadCategories();
   }

   public void loadCategories(){
        if(EasyStore.EasyStore.getConfig().getConfigurationSection("category") != null){
            for(String categoryCode : EasyStore.EasyStore.getConfig().getConfigurationSection("category").getKeys(false)){
                Category category = new Category(categoryCode);
                categories.add(category);
            }
        }
   }
}
